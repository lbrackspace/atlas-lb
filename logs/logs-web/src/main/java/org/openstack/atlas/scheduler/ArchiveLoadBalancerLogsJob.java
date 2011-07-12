package org.openstack.atlas.scheduler;

import org.openstack.atlas.scheduler.execution.ArchiveLoadBalancerLogsJobExecution;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class ArchiveLoadBalancerLogsJob extends BaseMapreduceJob {

    private static final Log LOG = LogFactory.getLog(ArchiveLoadBalancerLogsJob.class);

    private QuartzExecutable execution;

    @Required
    public void setArchiveLoadBalancerLogsJobExecution(ArchiveLoadBalancerLogsJobExecution archiveLoadBalancerLogsJobExecution) {
        this.execution = archiveLoadBalancerLogsJobExecution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            HadoopRunner runner = HadoopRunner.createRunnerFromValues(context.getJobDetail().getJobDataMap());
            LOG.info("setting up ArchiveLoadBalancerLogsJob for " + runner.getInputString());
            execution.execute(createSchedulerInstance(context), runner);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }
    }

}