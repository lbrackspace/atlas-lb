package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.scheduler.FileAssembleJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.logs.entities.NameVal;
import org.openstack.atlas.service.domain.logs.entities.State;
import org.openstack.atlas.service.domain.logs.entities.StateVal;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class FileWatchdogJobExecution extends LoggableJobExecution implements QuartzExecutable {
     private static final Log LOG = LogFactory.getLog(FileWatchdogJobExecution.class);

    public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {
        LOG.info("Job started at " + Calendar.getInstance().getTime());
        List<String> localInputFiles = utils.getLocalInputFiles();
        List<String> scheduledFilesToRun = new LinkedList<String>();

        // Check to see if any filemoves have been issued for these files.
        LOG.debug("checking for files " + localInputFiles);

        for (String inputFile : localInputFiles) {
            List states = stateDao.getStateLike(NameVal.FILECOPY, inputFile);
            if (states.isEmpty()) {
                // it does not exist, so schedule it.
                scheduledFilesToRun.add(inputFile);
            }
        }

        if (scheduledFilesToRun.isEmpty()) {
            // assume that we have no scheduled files to move.
            LOG.info("Could not find any files that are not already scheduled. returning.");
            return;
        } else {
            State state = stateDao.addState(NameVal.WATCHDOG, runner.getInputString());

            // now that we have a list of files, schedule them.
            String jobName = "fileAssemble" + runner.getInputString();
            runner.setInputForMultiPathJobs(scheduledFilesToRun);

            try {
                scheduler.scheduleJob(FileAssembleJob.class, runner);
            } catch (SchedulingException e) {
                LOG.error(e);
                state.setState(StateVal.FAILED);
                stateDao.update(state);
                throw new ExecutionException(e);
            }

            finishJob(state);
        }

    }
}
