package org.openstack.atlas.scheduler.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.mapreduce.LbStatsTool;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.scheduler.SplitLoadBalancerLogsJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.openstack.atlas.tools.HadoopTool;
import org.springframework.beans.factory.annotation.Required;

public class MapReduceAggregateLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(MapReduceAggregateLogsJobExecution.class);

    private HadoopTool tool;

    @Required
    public void setLbStatsTool(LbStatsTool tool) {
        this.tool = tool;
    }

    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs runner) throws ExecutionException {
        JobState state = createJob(JobName.MAPREDUCE, runner.getInputString());
        tool.setupHadoopRun(runner);

        try {
            tool.executeHadoopRun();
            scheduleSplitLoadBalancerLogsJob(scheduler, runner);
        } catch (Exception e) {
            LOG.error(e);
            failJob(state);
            throw new ExecutionException(e);
        }

        finishJob(state);
    }

    private void scheduleSplitLoadBalancerLogsJob(JobScheduler scheduler, QuartzSchedulerConfigs runner) throws SchedulingException {
        scheduler.scheduleJob(SplitLoadBalancerLogsJob.class, runner);
    }
}

