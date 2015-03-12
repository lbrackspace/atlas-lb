package org.openstack.atlas.service.domain.services;

import java.util.List;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;

public interface LzoService {

    public HdfsLzo createHdfsLzo(int hour_key, long fileSize);

    public CloudFilesLzo createCloudFilesLzo(int hour, int frag, String md5Sum);

    public HdfsLzo getHdfsLzo(int hour_key);

    public CloudFilesLzo getCloudFilesLzo(int hour_key, int frag);

    public List<CloudFilesLzo> getCloudFilesLzo(int hour_key);

    public int newHdfsLzo(int hour_key, long fileSize);

    public boolean setStateFlagsFalse(int hourKey,int flags);

    public boolean setStateFlagsTrue(int hourKey, int flags);

    public int getStateFlags(int hourKey);

    public void finishCloudFilesLzo(int hourKey, CloudFilesSegmentContainer sc);

    public List<CloudFilesLzo> newCloudFilesLzo(int hourKey, CloudFilesSegmentContainer sc);
}
