package org.openstack.atlas.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.scheduler.execution.ReuploaderJobExecution;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class ReuploaderJob extends BaseMapreduceJob {

    private static final Log LOG = LogFactory.getLog(ReuploaderJob.class);

    private QuartzExecutable execution;

    @Required
    public void setArchiveLoadBalancerLogsJobExecution(ReuploaderJobExecution reuploaderJobExecution) {
        this.execution = reuploaderJobExecution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        QuartzSchedulerConfigs schedulerConfigs = getSchedulerConfigs(context);
        LOG.info("running " + getClass() + " on " + schedulerConfigs.getRunTime() + " for logFileDate: " + schedulerConfigs.getRawlogsFileTime());
        try {
            execution.execute(createSchedulerInstance(context), schedulerConfigs);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }
    }

}