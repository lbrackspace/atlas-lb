package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.constants.Constants;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.mapreduce.LbStatsTool;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.scheduler.SplitLoadBalancerLogsJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.tools.HadoopRunner;
import org.openstack.atlas.tools.HadoopTool;
import org.openstack.atlas.util.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;

public class OrderLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG =
            LogFactory.getLog(OrderLoadBalancerLogsJobExecution.class);

    private HadoopTool tool;
    private FileSystemUtils utils;

    public void setLbStatsTool(LbStatsTool tool) {
        this.tool = tool;
    }

    public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {

        JobState state = createJob(JobName.MAPREDUCE, runner.getInputString());

        runner.setRawlogsFileDate(Constants.Rawlogs.RAWLOGS_FORMAT.format(Calendar.getInstance().getTime()));

        tool.setupHadoopRun(runner);

        try {
            tool.executeHadoopRun();

            scheduleFileCreateJoinJob(scheduler, runner);
        } catch (Exception e) {
            LOG.error(e);
            failJob(state);
            throw new ExecutionException(e);
        }

        finishJob(state);
    }

    private void scheduleFileCreateJoinJob(JobScheduler scheduler, HadoopRunner runner) throws SchedulingException {
        scheduler.scheduleJob(SplitLoadBalancerLogsJob.class, runner);
    }
}

