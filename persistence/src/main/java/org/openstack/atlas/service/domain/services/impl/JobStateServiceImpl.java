package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class JobStateServiceImpl extends BaseService implements JobStateService {

    @Override
    public JobState getById(Integer id) throws EntityNotFoundException {
        return jobStateRepository.getById(id);
    }

    @Override
    public JobState getByName(JobName jobName) {
        JobState jobState;
        try {
            jobState = jobStateRepository.getByName(jobName);
        } catch (EntityNotFoundException e) {
            jobState = updateJobState(jobName, JobStateVal.CREATED);
        }

        return jobState;
    }

    @Override
    public List<JobState> getAll(Integer offset, Integer limit, Integer marker) {
        return jobStateRepository.getAll(offset, limit, marker);
    }

    @Override
    public List<JobState> getByState(String state, Integer... p) {
        return jobStateRepository.getByState(state, p);
    }

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

    @Override
    /* Creates an entry in the database if it doesn't exist */
    public JobState updateInputPath(JobName jobName, String inputPath) {
        JobState jobState;

        try {
            jobState = jobStateRepository.getByName(jobName);
        } catch (EntityNotFoundException e) {
            jobState = jobStateRepository.create(jobName, inputPath);
        }

        jobState.setInputPath(inputPath);
        jobStateRepository.update(jobState);
        return jobState;
    }

    public void deleteOldLoggingStates() {
        List<JobName> jobNames = new ArrayList<JobName>();
        jobNames.add(JobName.WATCHDOG);
        jobNames.add(JobName.FILEASSEMBLE);
        jobNames.add(JobName.FILECOPY_PARENT);
        jobNames.add(JobName.FILECOPY);
        jobNames.add(JobName.MAPREDUCE);
        jobNames.add(JobName.FILES_SPLIT);
        jobNames.add(JobName.LOG_FILE_CF_UPLOAD);
        jobNames.add(JobName.ARCHIVE);

        jobStateRepository.deleteByNamesOlderThanNDays(jobNames, 30);
    }

    @Override
    public boolean isJobReadyToGo() {
        return jobStateRepository.isJobReadyToGo();
    }
}
