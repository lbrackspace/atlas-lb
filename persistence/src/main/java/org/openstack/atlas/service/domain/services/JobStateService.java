package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import java.util.List;

public interface JobStateService {

    JobState getById(Integer id) throws EntityNotFoundException;

    JobState getByName(JobName jobName);

    List<JobState> getAll(Integer offset, Integer limit, Integer marker);

    List<JobState> getByState(String state, Integer... p);

    JobState updateJobState(JobName jobName, JobStateVal jobStateVal);

    JobState updateJobState(JobName jobName, JobStateVal jobStateVal, String inputPath);

    void deleteOldLoggingStates();

}
