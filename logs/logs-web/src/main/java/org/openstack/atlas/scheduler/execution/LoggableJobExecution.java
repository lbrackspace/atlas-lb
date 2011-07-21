package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.logs.entities.JobName;
import org.openstack.atlas.service.domain.logs.entities.JobStateVal;
import org.openstack.atlas.service.domain.logs.repository.JobStateRepository;
import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.util.DateTime;
import org.openstack.atlas.util.FileSystemUtils;

public class LoggableJobExecution {
    protected JobStateRepository stateDao;
    protected FileSystemUtils utils;
    protected org.openstack.atlas.cfg.Configuration conf;

    protected JobState createJob(JobName val, String jobInput) {
        return stateDao.addEntry(val, jobInput);
    }

    protected void setJobState(JobStateVal val, JobState state) {
        state.setState(val);
        stateDao.update(state);
    }

    protected void finishJob(JobState state) {
        state.setState(JobStateVal.FINISHED);
        state.setEndTime(new DateTime().getCalendar());
        stateDao.update(state);
    }

    protected void deleteJob(JobState state) {
        state.setState(JobStateVal.DELETED);
        stateDao.update(state);
    }

    protected void failJob(JobState state) {
        state.setState(JobStateVal.FAILED);
        state.setEndTime(new DateTime().getCalendar());
        stateDao.update(state);
    }

    public final void setStateDao(JobStateRepository stateDao) {
        this.stateDao = stateDao;
    }

    public DirectoryTool createTool(Class jobclass) throws Exception {
        DirectoryTool d = (DirectoryTool) jobclass.newInstance();
        d.setConf(conf);
        d.setFileSystemUtils(utils);
        return d;
    }

    public final void setConf(org.openstack.atlas.cfg.Configuration conf) {
        this.conf = conf;
    }

    public final void setFileSystemUtils(FileSystemUtils fileSystemUtils) {
        this.utils = fileSystemUtils;
    }
}
