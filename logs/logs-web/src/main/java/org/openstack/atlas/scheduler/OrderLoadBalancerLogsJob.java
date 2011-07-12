package org.openstack.atlas.scheduler;

import org.openstack.atlas.scheduler.execution.OrderLoadBalancerLogsJobExecution;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class OrderLoadBalancerLogsJob extends BaseMapreduceJob {

    private static Log log = LogFactory.getLog(OrderLoadBalancerLogsJob.class);

    private QuartzExecutable execution;

    @Required
    public void setOrderLoadBalancerLogsJobExecution(OrderLoadBalancerLogsJobExecution execution) {
        this.execution = execution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HadoopRunner runner = getRunner(context);
        log.info("setting up OrderLoadBalancerLogsJob  for " + runner.getInputString());

        try {
            execution.execute(createSchedulerInstance(context), runner);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }
    }
}
