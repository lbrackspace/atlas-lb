package org.openstack.atlas.scheduler.execution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cloudfiles.CloudFilesUtils;
import org.openstack.atlas.cloudfiles.objs.AuthToken;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;
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
import java.util.Collections;
import java.util.List;
import org.openstack.atlas.cloudfiles.CloudFilesConfig;
import org.openstack.atlas.cloudfiles.SegmentMd5Thread;


import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.scheduler.execution.threads.CloudFilesLzoUploadThread;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.debug.Debug;

public class FileWatchdogJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final Log LOG = LogFactory.getLog(FileWatchdogJobExecution.class);
    private static final VerboseLogger vlog = new VerboseLogger(FileWatchdogJobExecution.class);

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        // This may be the first time the app is run so attempt to copy JobsJar over
        // By configureing the app to attempt to copy the Jar before THE_ONE_TO_RULE_THEM_ALL
        // is checked this will allow all DCs to copy their jars before the LZO is uploaded and a job
        // is run.

        try {
            HadoopLogsConfigs.copyJobsJar();
        } catch (IOException ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Unable to copy JobsJar: %s", excMsg), ex);
        }
        if (!jobStateRepository.isJobReadyToGo()) {
            LOG.warn(String.format("THE_ONE_TO_RULE_THEM_ALL jobstate is not set to \"GO\". Not running log processing yet"));
            return;
        }
        try {
            uploadLzoToCloudFilesIfNeeded();
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Unable to check and upload LZOs to cloudFiles: %s", excMsg), ex);
        }
        List<String> localInputFiles = hdfsUtils.getLocalInputFiles(HadoopLogsConfigs.getFileSystemRootDir());
        List<String> scheduledFilesToRun = new ArrayList<String>();
        for (String inputFile : localInputFiles) {
            vlog.log(String.format("Searching for %s", "%:" + inputFile));
            List<JobState> states = jobStateRepository.getEntriesLike(JobName.FILECOPY, "%:" + inputFile);
            if (states.isEmpty()) {
                try {
                    StaticFileUtils.getDateStringFromFileName(inputFile);
                    scheduledFilesToRun.add(inputFile);
                } catch (IllegalArgumentException ae) {
                    vlog.log(String.format("Not adding file %s as its not a valid log file\n"));
                    continue;
                }

                vlog.log(String.format("Scheduling new inputFile %s\n", inputFile));
            } else {
                vlog.log(String.format("Skipping inputFile %s as it was already in the state table: %s", inputFile, StaticStringUtils.<JobState>collectionToString(states, ",")));
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

        vlog.log(String.format("created WatchDog Job %s: for scheduledJob: %s", state, schedulerConfigs));

        try {
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMMddHH:mm:ss");
            String dateNow = formatter.format(currentDate.getTime());

            String jobName = "fileMove:" + dateNow + schedulerConfigs.getInputString();
            vlog.log(String.format("Calling FileMoveJob with jobName %s and schedulerConfigs=%s", jobName, schedulerConfigs));
            scheduler.scheduleJob(jobName, FileMoveJob.class, schedulerConfigs);
        } catch (SchedulingException e) {
            LOG.error(e);
            state.setState(JobStateVal.FAILED);
            jobStateRepository.update(state);
            throw new ExecutionException(e);
        }
        finishJob(state);
    }

    private void uploadLzoToCloudFilesIfNeeded() {
        CloudFilesUtils cfu = new CloudFilesUtils();
        int flags = 0;
        String excMsg;
        String localLzoDir = HadoopLogsConfigs.getFileSystemRootDir();
        List<Integer> foundHours = new ArrayList<Integer>();
        try {
            foundHours = cfUtils.listLzoHourKeysFromLocal(localLzoDir);
        } catch (Exception ex) {
            excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Error coulden't search for local LZO files in %s: %s", localLzoDir, excMsg), ex);
            return;
        }
        Collections.sort(foundHours);
        for (Integer hourKey : foundHours) {
            String lzoName = CloudFilesUtils.hourKeyToFileName(hourKey);
            String joinPath = StaticFileUtils.joinPath(localLzoDir, lzoName);
            String expandedPath = StaticFileUtils.expandUser(joinPath);
            long fileSize = StaticFileUtils.fileSize(expandedPath);
            flags = lzoService.newHdfsLzo(hourKey, fileSize);
            if (flags < 0) {
                spawnCloudFilesUploadThread(hourKey, expandedPath);
            } else if ((lzoService.getStateFlags(hourKey) & HdfsLzo.NEEDS_MASK) == 0) {
                // Files are ready to be deleted
                LOG.info(String.format("deleting local file: %s as its uploaded to CloudFiles and Hdfs.\n", expandedPath));
                File doomedFile = new File(expandedPath);
                doomedFile.delete();
            }
        }
    }

    private void spawnCloudFilesUploadThread(int hourKey, String lzoPath) {
        String fmt = "Spawning new thread for uploading %s LZO hourKey %d to cloudFiles nThreads running=%d totalRuns=%d\n";
        vlog.printf(fmt, lzoPath, hourKey, CloudFilesLzoUploadThread.getNRunning(), CloudFilesLzoUploadThread.getTotalRuns());
        String lzoAppend = HadoopLogsConfigs.getLzoNameAppend();
        String cntName = StaticFileUtils.stripDirectoryFromFileName(lzoPath) + "." + lzoAppend;
        CloudFilesLzoUploadThread thread = new CloudFilesLzoUploadThread(lzoService, cfUtils, hourKey, lzoPath, cntName);
        thread.start();
    }
}
