package org.openstack.atlas.scheduler;

import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.scheduler.execution.SplitLoadBalancerLogsJobExecution;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

public class SplitLoadBalancerLogsJob extends BaseMapreduceJob {

    private static Log log = LogFactory.getLog(SplitLoadBalancerLogsJob.class);

    private QuartzExecutable execution;

    @Required
    public void setSplitLoadBalancerLogsJobExecution(SplitLoadBalancerLogsJobExecution execution) {
        this.execution = execution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HadoopRunner runner = getRunner(context);
        log.info("setting up SplitLoadBalancerLogsJob run up for " + runner.getInputString());

        try {
            execution.execute(createSchedulerInstance(context), runner);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }
    }
}
