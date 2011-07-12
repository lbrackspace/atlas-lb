package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.service.domain.logs.entities.NameVal;
import org.openstack.atlas.service.domain.logs.entities.State;
import org.openstack.atlas.service.domain.logs.entities.StateVal;
import org.openstack.atlas.service.domain.logs.repository.StateRepository;
import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.util.DateTime;
import org.openstack.atlas.util.FileSystemUtils;

public class LoggableJobExecution {
    protected StateRepository stateDao;
    protected FileSystemUtils utils;
    protected org.openstack.atlas.cfg.Configuration conf;

    protected State createJob(NameVal val, String jobInput) {
        return stateDao.addState(val, jobInput);
    }

    protected void setJobState(StateVal val, State state) {
        state.setState(val);
        stateDao.update(state);
    }

    protected void finishJob(State state) {
        state.setState(StateVal.FINISHED);
        state.setEndTime(new DateTime().getCalendar());
        stateDao.update(state);
    }

    protected void deleteJob(State state) {
        state.setState(StateVal.DELETED);
        stateDao.update(state);
    }

    protected void failJob(State state) {
        state.setState(StateVal.FAILED);
        state.setEndTime(new DateTime().getCalendar());
        stateDao.update(state);
    }

    public final void setStateDao(StateRepository stateDao) {
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
