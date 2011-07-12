package org.openstack.atlas.scheduler;

import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.tools.HadoopRunner;
import org.openstack.atlas.util.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

import java.net.URL;

/**
 * Watchdog to check if any new files have hit the filesystem. This checks for
 * any new runs. It requires no input params, but it can optionally set the
 * RUNTIME which sets it for all the subsequent jobs in the chain.
 *
 *
 */
public class FileWatchdogJob extends BaseMapreduceJob {

    private static final Log LOG = LogFactory.getLog(FileWatchdogJob.class);

    private FileSystemUtils fileSystemUtils;
    private QuartzExecutable execution;

    @Required
    public void setFileSystemUtils(FileSystemUtils fileSystemUtils) {
        this.fileSystemUtils = fileSystemUtils;
    }

    @Required
    public void setFileWatchdogJobExecution(QuartzExecutable execution) {
        this.execution = execution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        HadoopRunner runner = getRunner(context);
        LOG.info("setting up FileWatchdogJob for " + runner.getInputString());

        String jarPath = findPathJar(DirectoryTool.class);
        LOG.info("Hadoop Jar path resolved at runtime is: " + jarPath);
        if(jarPath == null) {
            throw new IllegalArgumentException("Couldn't resolve the Hadoop Jar path in runtime. Aborting the job now!");
        }
        try {
            execution.execute(createSchedulerInstance(context), runner);
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
