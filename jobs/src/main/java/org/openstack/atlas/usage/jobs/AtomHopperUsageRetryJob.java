package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.usage.execution.UsageAtomHopperRetryExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class AtomHopperUsageRetryJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(AtomHopperUsageRetryJob.class);
    private UsageAtomHopperRetryExecution atomHopperUsageJobRetryExecution;

    @Required
    public void setAtomHopperUsageJobExecution(UsageAtomHopperRetryExecution atomHopperUsageJobRetryExecution) {
        this.atomHopperUsageJobRetryExecution = atomHopperUsageJobRetryExecution;
    }

    private void startPoller() throws JobExecutionException {
        LOG.info("Starting Atom Hopper Usage Retry Job");
        try {
            atomHopperUsageJobRetryExecution.execute();
        } catch (Exception e) {
            LOG.error("Atom Hopper Usage Retry Job Failed to start: " + e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public Log getLogger() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JobName getJobName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void run() throws Exception {
        startPoller();
    }

    @Override
    public void cleanup() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
