package org.openstack.atlas.scheduler;

import org.openstack.atlas.scheduler.execution.FileMoveJobExecution;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.common.VerboseLogger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

public class FileMoveJob extends BaseMapreduceJob implements StatefulJob {
    private static final Log LOG = LogFactory.getLog(FileMoveJob.class);
    private static final VerboseLogger vlog = new VerboseLogger(FileMoveJob.class);

    private QuartzExecutable fileMoveJobExecution;

    @Required
    public void setFileMoveJobExecution(FileMoveJobExecution fileMoveJobExecution) {
        this.fileMoveJobExecution = fileMoveJobExecution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        QuartzSchedulerConfigs configScheduler = getSchedulerConfigs(context);
        LOG.info("running " + getClass() + " on " + configScheduler.getRunTime() + " for logFileDate: " + configScheduler.getRawlogsFileTime());

        try {
            fileMoveJobExecution.execute(createSchedulerInstance(context), configScheduler);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }

    }
}
