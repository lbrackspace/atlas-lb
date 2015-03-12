package org.openstack.atlas.service.domain.services;

import java.util.List;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.helpers.LzoState;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;

public interface LzoService {

    public void createLzoPairs(int hour_key, CloudFilesSegmentContainer cnt);

    public HdfsLzo createHdfsLzo(int hour_key);

    public CloudFilesLzo createCloudFilesLzo(int hour, int frag, String md5Sum);

    public LzoState getHdfsLzoState(int hour_key);

    public LzoState getCloudFilesLzoState(int hour_key, int frag);

    public HdfsLzo getHdfsLzo(int hour_key) throws EntityNotFoundException;

    public CloudFilesLzo getCloudFilesLzo(int hour_key, int frag) throws EntityNotFoundException;

    public List<CloudFilesLzo> getCloudFilesLzo(int hour_key);

    public boolean setFinishedHdfsLzo(int hour_key);

    public boolean setFinishedCloudFilesLzo(int hour_key, int frag);

    public boolean isLzoFinished(int hour_key);
}
