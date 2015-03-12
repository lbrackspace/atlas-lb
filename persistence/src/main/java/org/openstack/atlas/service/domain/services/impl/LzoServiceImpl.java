package org.openstack.atlas.service.domain.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LzoService;
import org.openstack.atlas.service.domain.services.helpers.LzoState;
import org.openstack.atlas.util.common.CloudFilesSegment;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.common.comparators.CloudFilesSegmentComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LzoServiceImpl extends BaseService implements LzoService {

    private static final CloudFilesSegmentComparator segCmp;

    static {
        segCmp = new CloudFilesSegmentComparator();
    }

    @Override
    public HdfsLzo createHdfsLzo(int hour) {
        HdfsLzo lzo = new HdfsLzo(hour);
        lzoRepository.persist(lzo);
        return lzo;
    }

    @Override
    public CloudFilesLzo createCloudFilesLzo(int hour, int frag, String md5Sum) {
        CloudFilesLzo lzo = new CloudFilesLzo(hour, frag, md5Sum);
        lzoRepository.persist(lzo);
        return lzo;
    }

    @Override
    public LzoState getHdfsLzoState(int hour_key) {
        HdfsLzo lzo;
        try {
            lzo = lzoRepository.getHdfsLzo(hour_key);
        } catch (EntityNotFoundException ex) {
            return LzoState.EMPTY_ENTRY;
        }
        if (lzo.isFinished()) {
            return LzoState.FINISHED;
        }
        return LzoState.SENDING;

    }

    @Override
    public LzoState getCloudFilesLzoState(int hour_key, int frag) {
        CloudFilesLzo lzo;
        try {
            lzo = lzoRepository.getCloudFilesLzo(hour_key, frag);
        } catch (EntityNotFoundException ex) {
            return LzoState.EMPTY_ENTRY;
        }
        if (lzo.isFinished()) {
            return LzoState.FINISHED;
        }
        return LzoState.SENDING;
    }

    @Override
    public HdfsLzo getHdfsLzo(int hour_key) throws EntityNotFoundException {
        return lzoRepository.getHdfsLzo(hour_key);
    }

    @Override
    public CloudFilesLzo getCloudFilesLzo(int hour_key, int frag) throws EntityNotFoundException {
        return lzoRepository.getCloudFilesLzo(hour_key, frag);
    }

    @Override
    public List<CloudFilesLzo> getCloudFilesLzo(int hour_key) {
        return lzoRepository.getCloudFilesLzo(hour_key);
    }

    @Override
    public boolean setFinishedHdfsLzo(int hour_key) {
        HdfsLzo lzo;
        try {
            lzo = getHdfsLzo(hour_key);
        } catch (EntityNotFoundException ex) {
            lzo = new HdfsLzo(hour_key);
            lzo.setFinished(true);
            lzoRepository.persist(lzo);
            return false;
        }
        if (lzo.isFinished()) {
            return true;
        }
        lzo.setFinished(true);
        lzoRepository.<HdfsLzo>merge(lzo);
        return true;
    }

    @Override
    public boolean setFinishedCloudFilesLzo(int hour_key, int frag) {
        CloudFilesLzo lzo;
        try {
            lzo = getCloudFilesLzo(hour_key, frag);
        } catch (EntityNotFoundException ex) {
            lzo = new CloudFilesLzo(hour_key, frag, "FILE MARKED FINISHED BEFORE BEING SENT ERROR");
            lzo.setFinished(true);
            lzoRepository.persist(lzo);
            return false;
        }
        if (lzo.isFinished()) {
            return true;
        }
        lzo.setFinished(true);
        lzoRepository.merge(lzo);
        return true;
    }

    @Override
    public void createLzoPairs(int hour_key, CloudFilesSegmentContainer cnt) {
        List<CloudFilesSegment> segs = cnt.getSegments();
        if (segs.size() <= 0) {
            return;
        }
        Collections.sort(segs,segCmp);
        HdfsLzo hdfsLzo = new HdfsLzo(hour_key);
        for(CloudFilesSegment seg : segs){
            String md5Sum = seg.getMd5sum();
            int frag = seg.getFragNumber();
            CloudFilesLzo cloudFilesLzo = new CloudFilesLzo(hour_key, frag, md5Sum);
            lzoRepository.persist(cloudFilesLzo);
        }
    }

    @Override
    public boolean isLzoFinished(int hour_key) {
        HdfsLzo hdfsLzo;
        try {
            hdfsLzo = lzoRepository.getHdfsLzo(hour_key);
        } catch (EntityNotFoundException ex) {
            return false;
        }
        List<CloudFilesLzo> cloudLzos = lzoRepository.getCloudFilesLzo(hour_key);

        for(CloudFilesLzo cloudLzo : cloudLzos){
            if (!cloudLzo.isFinished()){
                return false;
            }
        }
        return true;
    }
}
