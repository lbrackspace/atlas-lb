package org.openstack.atlas.service.domain.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LzoService;
import org.openstack.atlas.service.domain.services.helpers.CloudFilesLzoKey;
import org.openstack.atlas.util.common.CloudFilesSegment;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.common.comparators.CloudFilesSegmentComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openstack.atlas.service.domain.services.helpers.LzoServiceMutex;

@Service
public class LzoServiceImpl extends BaseService implements LzoService {

    private static final CloudFilesSegmentComparator segCmp;

    static {
        segCmp = new CloudFilesSegmentComparator();
    }

    @Override
    public HdfsLzo createHdfsLzo(int hour, long fileSize) {
        HdfsLzo lzo = new HdfsLzo(hour, fileSize);
        lzoRepository.persist(lzo);
        return lzo;
    }

    @Override
    public CloudFilesLzo createCloudFilesLzo(int hour, int frag, String md5Sum) {
        CloudFilesLzo lzo = new CloudFilesLzo(hour, frag, md5Sum);
        lzoRepository.persist(lzo);
        return lzo;
    }

    @Transactional
    @Override
    public void finishCloudFilesLzo(int hourKey, CloudFilesSegmentContainer sc) {
        Map<CloudFilesLzoKey, CloudFilesLzo> lzoMap = new HashMap<CloudFilesLzoKey, CloudFilesLzo>();
        List<CloudFilesLzo> dbLzos = lzoRepository.getCloudFilesLzo(hourKey);
        for (CloudFilesLzo dbLzo : dbLzos) {
            CloudFilesLzoKey key = new CloudFilesLzoKey(hourKey, dbLzo.getFrag());
            lzoMap.put(key, dbLzo);
        }
        for (CloudFilesSegment segment : sc.getSegments()) {
            CloudFilesLzoKey key = new CloudFilesLzoKey(hourKey, segment.getFragNumber());
            if (lzoMap.containsKey(key)) {
                CloudFilesLzo lzo = lzoMap.get(key);

                lzo.setFinished(true);
                lzoRepository.merge(lzo);
            }
        }
    }

    @Transactional
    @Override
    public List<CloudFilesLzo> newCloudFilesLzo(int hourKey, CloudFilesSegmentContainer sc) {
        Map<CloudFilesLzoKey, CloudFilesLzo> lzoMap = new HashMap<CloudFilesLzoKey, CloudFilesLzo>();
        List<CloudFilesLzo> out = new ArrayList<CloudFilesLzo>();
        List<CloudFilesLzo> dbLzos = lzoRepository.getCloudFilesLzo(hourKey);
        for (CloudFilesLzo dbLzo : dbLzos) {
            CloudFilesLzoKey key = new CloudFilesLzoKey(hourKey, dbLzo.getFrag());
            lzoMap.put(key, dbLzo);
        }
        for (CloudFilesSegment segment : sc.getSegments()) {
            int fragNumber = segment.getFragNumber();
            CloudFilesLzoKey key = new CloudFilesLzoKey(hourKey, fragNumber);
            String md5Sum = segment.getMd5sum();
            CloudFilesLzo lzo;
            if (lzoMap.containsKey(key)) {
                lzo = lzoMap.get(key);
            } else {
                lzo = new CloudFilesLzo(hourKey, fragNumber, md5Sum);
            }
            lzo.setMd5(md5Sum);
            lzo.bumpStartTime();
            lzo.setHourKey(hourKey);
            lzo.setFinished(false);
            lzo.bumpStartTime();
            lzoRepository.merge(lzo);
            out.add(lzo);
        }
        return out;
    }

    @Override
    public HdfsLzo getHdfsLzo(int hour_key) {
        return lzoRepository.getHdfsLzo(hour_key);
    }

    @Override
    public CloudFilesLzo getCloudFilesLzo(int hour_key, int frag) {
        return lzoRepository.getCloudFilesLzo(hour_key, frag);
    }

    @Override
    public List<CloudFilesLzo> getCloudFilesLzo(int hour_key) {
        return lzoRepository.getCloudFilesLzo(hour_key);
    }

    @Override
    public int newHdfsLzo(int hourKey, long fileSize) {
        synchronized (LzoServiceMutex.class) {
            HdfsLzo lzo = lzoRepository.getHdfsLzo(hourKey);
            if (lzo == null) {
                lzo = new HdfsLzo(hourKey, fileSize);
                lzoRepository.persist(lzo);
                return -1;
            } else {
                if (lzo.isReuploadNeeded()) {
                    lzo.setReuploadNeeded(false);
                    lzo.setCfNeeded(true);
                    lzo.setMd5Needed(true);
                    lzoRepository.merge(lzo);
                    return -1;
                }
                return getFlags(lzo);
            }
        }
    }

    @Override
    public boolean setStateFlagsFalse(int hourKey, int flags) {
        synchronized (LzoServiceMutex.class) {
            HdfsLzo lzo = lzoRepository.getHdfsLzo(hourKey);
            if (lzo == null) {
                return false;
            }
            lzo = setFlags(lzo, (HdfsLzo.NEEDS_MASK ^ flags) & getFlags(lzo));
            lzoRepository.merge(lzo);
        }
        return true;
    }

    @Override
    public boolean setStateFlagsTrue(int hourKey, int flags) {
        synchronized (LzoServiceMutex.class) {
            HdfsLzo lzo = lzoRepository.getHdfsLzo(hourKey);
            if (lzo == null) {
                return false;
            }
            setFlags(lzo, getFlags(lzo) | flags);
            lzoRepository.merge(lzo);
            return true;
        }
    }

    @Override
    public int getStateFlags(int hourKey) {
        synchronized (LzoServiceMutex.class) {
            HdfsLzo lzo = lzoRepository.getHdfsLzo(hourKey);
            if (lzo == null) {
                return -1;
            }
            return getFlags(lzo);
        }
    }

    private int getFlags(HdfsLzo lzo) {
        int flags = 0;
        if (lzo.isReuploadNeeded()) {
            flags |= HdfsLzo.NEEDS_REUPLOAD;
        }
        if (lzo.isMd5Needed()) {
            flags |= HdfsLzo.NEEDS_MD5;
        }
        if (lzo.isCfNeeded()) {
            flags |= HdfsLzo.NEEDS_CF;
        }
        if (lzo.isHdfsNeeded()) {
            flags |= HdfsLzo.NEEDS_HDFS;
        }
        return flags;
    }

    private HdfsLzo setFlags(HdfsLzo lzo, int flags) {
        if ((flags & HdfsLzo.NEEDS_REUPLOAD) > 0) {
            lzo.setReuploadNeeded(true);
        } else {
            lzo.setReuploadNeeded(false);
        }
        if ((flags & HdfsLzo.NEEDS_MD5) > 0) {
            lzo.setMd5Needed(true);
        } else {
            lzo.setMd5Needed(false);
        }
        if ((flags & HdfsLzo.NEEDS_CF) > 0) {
            lzo.setCfNeeded(true);
        } else {
            lzo.setCfNeeded(false);
        }
        if ((flags & HdfsLzo.NEEDS_HDFS) > 0) {
            lzo.setHdfsNeeded(true);
        } else {
            lzo.setHdfsNeeded(false);
        }
        return lzo;
    }
}
