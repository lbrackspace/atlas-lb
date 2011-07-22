package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.scheduler.FileMoveJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

public class FileAssembleJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(FileAssembleJobExecution.class);

    private JobScheduler jobScheduler;

    public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {
        // stupid manual set, this has to be done. a circular dep because of how
        // quartz must init its scheduler factory crap. currently u cannot have
        // a bean that has a dependency on a bean that is in the
        // schedulerFactoryBean#schedulerContextAsMap
        setJobScheduler(scheduler);


        String runTime = runner.getInputString();
        JobState allJobState = createJob(JobName.FILEASSEMBLE, runTime);
        try {
//            runner.setOldestDate(getOldestLoglineSpeculatively(runner));
//            runner.setCacheIPs(createCacheIPs());
            scheduleMoveToDFS(runner);
//            scheduleMoveToCloudFiles(data);
//            scheduleComputeCyclesNormalizeJob(data, runTime);
        } catch (Exception e) {
            LOG.error(e);
            failJob(allJobState);
            throw new ExecutionException(e);
        }

        finishJob(allJobState);
    }

    @Required
    private void setJobScheduler(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    private void scheduleMoveToDFS(HadoopRunner runner) throws SchedulingException {
        String jobName = "fileMove" + runner.getInputString();
        jobScheduler.scheduleJob(jobName, FileMoveJob.class, runner);
    }
}
