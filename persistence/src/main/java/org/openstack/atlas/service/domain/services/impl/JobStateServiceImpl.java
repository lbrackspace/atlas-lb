package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class JobStateServiceImpl extends BaseService implements JobStateService {

    @Override
    /* Creates an entry in the database if it doesn't exist */
    public JobState updateJobState(JobName jobName, JobStateVal jobStateVal) {
        JobState jobState;

        try {
            jobState = jobStateRepository.getByName(jobName);
        } catch (EntityNotFoundException e) {
            jobState = jobStateRepository.create(jobName);
        }

        jobState.setState(jobStateVal);
        if (jobStateVal.equals(JobStateVal.IN_PROGRESS)) jobState.setStartTime(Calendar.getInstance());
        if (jobStateVal.equals(JobStateVal.FINISHED)) jobState.setEndTime(Calendar.getInstance());
        jobStateRepository.update(jobState);
        return jobState;
    }
}
