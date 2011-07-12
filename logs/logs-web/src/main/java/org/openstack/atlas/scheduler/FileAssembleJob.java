package org.openstack.atlas.scheduler;

import org.openstack.atlas.scheduler.execution.FileAssembleJobExecution;
import org.openstack.atlas.scheduler.execution.QuartzExecutable;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * Moves a single file or a set of files onto the DFS and schedules a run based
 * on the inputDir timestamp.
 * <p/>
 * Data needed in jobMap is one of * COPY_ALL_FILES - List of String names *
 * COPY_SINGLE_FILE - single String name
 *
 *
 */
public class FileAssembleJob extends BaseMapreduceJob implements StatefulJob {
    private static final Log LOG = LogFactory.getLog(FileAssembleJob.class);

    private QuartzExecutable fileAssembleJobExecution;

    @Required
    public void setFileAssembleJobExecution(FileAssembleJobExecution fileAssembleJobExecution) {
        this.fileAssembleJobExecution = fileAssembleJobExecution;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String runTime = getRuntime(context);
        LOG.info("running " + getClass() + " on " + runTime);

        Map data = context.getJobDetail().getJobDataMap();
        HadoopRunner runner = HadoopRunner.createRunnerFromValues(data);
        try {
            fileAssembleJobExecution.execute(createSchedulerInstance(context), runner);
        } catch (ExecutionException e) {
            throw new JobExecutionException(e);
        }

    }
}
