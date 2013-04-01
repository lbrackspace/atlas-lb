package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.usage.execution.UsageToAtomHopperRetryExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class AtomHopperUsageRetryJob extends Job {
    private final Log LOG = LogFactory.getLog(AtomHopperUsageRetryJob.class);
    private UsageToAtomHopperRetryExecution atomHopperUsageJobRetryExecution;

    @Required
    public void setAtomHopperUsageJobExecution(UsageToAtomHopperRetryExecution atomHopperUsageJobRetryExecution) {
        this.atomHopperUsageJobRetryExecution = atomHopperUsageJobRetryExecution;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
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

}
