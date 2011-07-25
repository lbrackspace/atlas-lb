package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import java.util.List;

public interface JobStateService {

    public JobState getById(Integer id) throws EntityNotFoundException;

    public List<JobState> getAll(Integer... p);

    public List<JobState> getByState(String state, Integer... p);

    public JobState updateJobState(JobName jobName, JobStateVal jobStateVal);
}
