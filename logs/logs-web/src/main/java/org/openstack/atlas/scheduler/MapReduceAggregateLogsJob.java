package org.openstack.atlas.scheduler;

import org.openstack.atlas.scheduler.execution.MapReduceAggregateLogsJobExecution;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class MapReduceAggregateLogsJob extends BaseMapreduceJob {

    private static Log LOG = LogFactory.getLog(MapReduceAggregateLogsJob.class);

    private QuartzExecutable execution;

    @Required
    public void setOrderLoadBalancerLogsJobExecution(MapReduceAggregateLogsJobExecution execution) {
        this.execution = execution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HadoopRunner runner = getRunner(context);
        LOG.info("running " + getClass() + " on " + runner.getRunTime() + " for logFileDate: " + runner.getRawlogsFileTime());

        try {
            execution.execute(createSchedulerInstance(context), runner);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }
    }
}
