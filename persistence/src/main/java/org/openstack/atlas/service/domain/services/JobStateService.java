package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;

public interface JobStateService {

    public JobState updateJobState(JobName jobName, JobStateVal jobStateVal);
}
