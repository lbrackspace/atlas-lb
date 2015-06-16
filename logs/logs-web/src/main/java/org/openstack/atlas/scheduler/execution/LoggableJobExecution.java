package org.openstack.atlas.scheduler.execution;

import java.io.IOException;
import org.openstack.atlas.hadoop.deprecated.DateTime;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.repository.JobStateRepository;

import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.springframework.beans.factory.annotation.Required;

public class LoggableJobExecution {
    protected JobStateRepository jobStateRepository;
    protected org.openstack.atlas.cfg.Configuration conf;
    protected HdfsUtils hdfsUtils = HadoopLogsConfigs.getHdfsUtils();

    protected JobState createJob(JobName val, String jobInput) {
        return jobStateRepository.create(val, jobInput);
    }

    protected void setJobState(JobStateVal val, JobState state) {
        state.setState(val);
        jobStateRepository.update(state);
    }

    protected void finishJob(JobState state) {
        state.setState(JobStateVal.FINISHED);
        state.setEndTime(new DateTime().getCalendar());
        jobStateRepository.update(state);
    }

    protected void deleteJob(JobState state) {
        state.setState(JobStateVal.DELETED);
        jobStateRepository.update(state);
    }

    protected void failJob(JobState state) {
        state.setState(JobStateVal.FAILED);
        state.setEndTime(new DateTime().getCalendar());
        jobStateRepository.update(state);
    }
    
    @Required
    public void setJobStateRepository(JobStateRepository jobStateRepository) {
        this.jobStateRepository = jobStateRepository;
    }

    @Required
    public void setConf(org.openstack.atlas.cfg.Configuration conf) {
        this.conf = conf;
    }
}
