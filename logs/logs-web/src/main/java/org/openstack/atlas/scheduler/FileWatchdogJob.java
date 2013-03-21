package org.openstack.atlas.scheduler;

import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.net.URL;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.util.common.VerboseLogger;

public class FileWatchdogJob extends BaseMapreduceJob implements StatefulJob {

    private static final Log LOG = LogFactory.getLog(FileWatchdogJob.class);
    private static final VerboseLogger vlog = new VerboseLogger(FileWatchdogJob.class);
    private QuartzExecutable execution;

    @Required
    public void setFileWatchdogJobExecution(QuartzExecutable execution) {
        this.execution = execution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        QuartzSchedulerConfigs schedulerConfigs = getSchedulerConfigs(context);
        LOG.info("running " + getClass() + " on " + schedulerConfigs.getRunTime());

        vlog.log(String.format("SchedulerConfig = %s", schedulerConfigs.toString()));
        schedulerConfigs.setJobJarPath(HadoopLogsConfigs.getLocalJobsJarPath());
        try {
            execution.execute(createSchedulerInstance(context), schedulerConfigs);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }
    }

    public static String findPathJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        return jarPath.substring("file:".length(), jarPath.lastIndexOf("!"));

    }
}
