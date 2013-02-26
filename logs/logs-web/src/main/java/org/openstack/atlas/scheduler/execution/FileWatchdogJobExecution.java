package org.openstack.atlas.scheduler.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.scheduler.FileMoveJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.apache.hadoop.tools.rumen.HadoopLogsAnalyzer;
import org.openstack.atlas.tools.HadoopConfiguration;
import org.openstack.atlas.util.HadoopLogsConfigs;
import org.openstack.atlas.util.HdfsUtils;
import org.openstack.atlas.util.StaticFileUtils;

public class FileWatchdogJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(FileWatchdogJobExecution.class);
    private HdfsUtils hdfsUtils = new HdfsUtils();

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        List<String> localInputFiles = hdfsUtils.getLocalInputFiles(HadoopLogsConfigs.getFileSystemRootDir());

        List<String> scheduledFilesToRun = new ArrayList<String>();
        for (String inputFile : localInputFiles) {
            List states = jobStateRepository.getEntriesLike(JobName.FILECOPY, inputFile);
            if (states.isEmpty()) {
                scheduledFilesToRun.add(inputFile);
            }
        }

        if (scheduledFilesToRun.size() == 1) {
            //eg. /var/log/zxtm/rotated/2012021017-access_log.aggregated
            String logFileDate = StaticFileUtils.getDateStringFromFileName(scheduledFilesToRun.get(0));
            schedulerConfigs.setRawlogsFileTime(logFileDate);
            schedulerConfigs.setInputString(schedulerConfigs.getRawlogsFileTime());
        } else if (scheduledFilesToRun.size() >= 1) {
            String newestFile = StaticFileUtils.getNewestFile(scheduledFilesToRun);
            String logFileDate = StaticFileUtils.getDateStringFromFileName(newestFile);
            schedulerConfigs.setRawlogsFileTime(logFileDate);
            schedulerConfigs.setInputString(schedulerConfigs.getRawlogsFileTime());
            scheduledFilesToRun.clear();
            scheduledFilesToRun.add(newestFile);
        } else if (scheduledFilesToRun.isEmpty()) {
            LOG.info("Could not find any files that are not already scheduled. returning.");
            return;
        }

        String inputString = schedulerConfigs.getInputString();
        JobState state = createJob(JobName.WATCHDOG, inputString);
        schedulerConfigs.setInputForMultiPathJobs(scheduledFilesToRun);

        try {
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMMddHH:mm:ss");
            String dateNow = formatter.format(currentDate.getTime());

            String jobName = "fileMove:" +  dateNow + schedulerConfigs.getInputString();
            scheduler.scheduleJob(jobName, FileMoveJob.class, schedulerConfigs);
        } catch (SchedulingException e) {
            LOG.error(e);
            state.setState(JobStateVal.FAILED);
            jobStateRepository.update(state);
            throw new ExecutionException(e);
        }

        finishJob(state);
    }
}
