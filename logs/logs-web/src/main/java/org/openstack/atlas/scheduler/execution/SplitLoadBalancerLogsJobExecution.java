package org.openstack.atlas.scheduler.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.openstack.atlas.config.CloudFilesZipInfo;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.io.FileBytesWritable;
import org.openstack.atlas.logs.hadoop.sequencefiles.SequenceFileReaderException;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import org.openstack.atlas.scheduler.ArchiveLoadBalancerLogsJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.openstack.atlas.util.HdfsUtils;
import org.openstack.atlas.util.StaticFileUtils;
import org.openstack.atlas.util.StaticStringUtils;
import org.openstack.atlas.util.VerboseLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SplitLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final VerboseLogger vlog = new VerboseLogger(SplitLoadBalancerLogsJobExecution.class, VerboseLogger.LogLevel.INFO);
    private static final Log LOG = LogFactory.getLog(SplitLoadBalancerLogsJobExecution.class);
    private DirectoryTool tool;

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        // Get reducer Directory
        tool = null;
        String fileHour = schedulerConfigs.getInputString();
        JobState state = createJob(JobName.FILES_SPLIT, fileHour);
        List<LogReducerOutputValue> zipFileInfoList;
        List<String> reducerOutdirComponents = new ArrayList<String>();
        reducerOutdirComponents.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
        reducerOutdirComponents.add("lb_logs_split");
        reducerOutdirComponents.add(fileHour);
        String hdfsReducerOutputDirectory = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(reducerOutdirComponents));
        String localCacheDir = HadoopLogsConfigs.getCacheDir();
        try {
            zipFileInfoList = hdfsUtils.getZipFileInfoList(hdfsReducerOutputDirectory);
        } catch (SequenceFileReaderException ex) {
            String excMsg = StaticStringUtils.getExtendedStackTrace(ex);
            LOG.error(String.format("Could not list sequenceFiles for directory %s: %s", hdfsReducerOutputDirectory, excMsg));
            failJob(state);
            throw new ExecutionException(ex);
        }

        for (LogReducerOutputValue zipFileInfo : zipFileInfoList) {
            int accountId = zipFileInfo.getAccountId();
            int loadbalancerId = zipFileInfo.getLoadbalancerId();
            long crc = zipFileInfo.getCrc();
            long uncompressedSize = zipFileInfo.getFileSize();
            int nLines = zipFileInfo.getnLines();
            String hdfsZipFilePath = zipFileInfo.getLogFile();
            String zipFileNameNoDir = StaticFileUtils.stripDirectoryFromFileName(hdfsZipFilePath);

            // Build the local cache file Name
            List<String> localZipPathComponents = new ArrayList<String>();
            localZipPathComponents.add(localCacheDir);
            localZipPathComponents.add(fileHour);
            localZipPathComponents.add(Integer.toString(accountId));
            localZipPathComponents.add(zipFileNameNoDir);
            List<String> mergedCacheZipPathComponents = StaticFileUtils.joinPath(localZipPathComponents);
            String fullCacheZipPath = StaticFileUtils.splitPathToString(mergedCacheZipPathComponents);

            // Download The zip file from Hdfs
            FSDataInputStream zipFileInputStream;
            FSDataOutputStream zipfileCacheOutputStream;
            vlog.printf("Downloading hdfs %s ->%s", hdfsZipFilePath, fullCacheZipPath);
            try {
                zipFileInputStream = hdfsUtils.openHdfsInputFile(hdfsZipFilePath, false);
            } catch (IOException ex) {
                String msg = String.format("Error opening hdfsZip file %s for reading. Skipping this entry", hdfsZipFilePath);
                String excMsg = StaticStringUtils.getExtendedStackTrace(ex);
                LOG.error(String.format("%s:%s", msg, excMsg), ex);
                continue;
            }
            try {
                zipfileCacheOutputStream = hdfsUtils.openHdfsOutputFile(fullCacheZipPath, true, true);
            } catch (IOException ex) {
                String msg = String.format("Error opening cacheZipFile %s for writing. Skipping this entry", fullCacheZipPath);
                String excMsg = StaticStringUtils.getExtendedStackTrace(ex);
                LOG.error(String.format("%s:%s", msg, excMsg), ex);
                StaticFileUtils.close(zipFileInputStream);
                continue;
            }
            try {
                StaticFileUtils.copyStreams(zipFileInputStream, zipfileCacheOutputStream, null, hdfsUtils.getBufferSize());
            } catch (IOException ex) {
                String msg = String.format("Error opening writing data from  %s -> %s", hdfsZipFilePath, fullCacheZipPath);
                String excMsg = StaticStringUtils.getExtendedStackTrace(ex);
                LOG.error(String.format("%s:%s", msg, excMsg), ex);
                StaticFileUtils.close(zipFileInputStream);
                StaticFileUtils.close(zipfileCacheOutputStream);
                continue;
            }


            // Build the CloudFilesZipInfo entry and put it on the schedulerConfigs list for the ArchiveLoadbalancerLogsJob
            CloudFilesZipInfo cloudFileZipEntry = new CloudFilesZipInfo();
            cloudFileZipEntry.setAccountId(accountId);
            cloudFileZipEntry.setLoadbalancerId(loadbalancerId);
            cloudFileZipEntry.setCrc(crc);
            cloudFileZipEntry.setUncompressedSize(uncompressedSize);
            cloudFileZipEntry.setnLines(nLines);
            cloudFileZipEntry.setHdfsFile(hdfsZipFilePath);
            cloudFileZipEntry.setCacheFile(fullCacheZipPath);
            cloudFileZipEntry.setLocalCacheDir(localCacheDir);
            schedulerConfigs.getCloudFilesZipInfoList().add(cloudFileZipEntry);
            vlog.printf("Added %s", cloudFileZipEntry.toString());
            StaticFileUtils.close(zipFileInputStream);
            StaticFileUtils.close(zipfileCacheOutputStream);
        }
        try {
            scheduleArchiveLoadBalancerLogsJob(scheduler, schedulerConfigs);
        } catch (SchedulingException ex) {
            String msg = "Error attempting to schedule Archive Job. This job shall be marked as a failure";
            String excMsg = StaticStringUtils.getExtendedStackTrace(ex);
            LOG.error(String.format("%s:%s", msg, excMsg), ex);
            failJob(state);
            throw new ExecutionException(msg, ex);
        }
        finishJob(state);
    }

    @Deprecated
    public void executeDeprecated(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {

        JobState state = createJob(JobName.FILES_SPLIT, schedulerConfigs.getInputString());

        try {

            tool = null;
            //tool = createTool(LbStatsTool.class);
            //tool.setupHadoopRun(schedulerConfigs);
            //String directory = tool.getOutputDirectory();

            FileStatus[] files = new FileStatus[0];
            //files = hdfsUtils.listStatuses(tool.getOutputDirectory(), false);
            //LOG.info("No of files in " + directory + " is: " + files.length);

            for (int i = 0; i < files.length; i++) {
                if (files[i].getPath().getName().startsWith("part-")) {

                    Path local = hdfsUtils.moveToLocalCacheDir(files[i].getPath());
                    SequenceFile.Reader reader = hdfsUtils.getReader(local, true);

                    Text key = new Text();
                    int seqCount = 0;
                    String currentKey = null;

                    while (reader.next(key)) {
                        if (key.toString().equals(currentKey)) {
                            seqCount++;
                        } else {
                            currentKey = key.toString();
                            seqCount = 0;
                        }

                        String accountId = getAccount(key.toString());
                        String loadbalancerId = getLoadBalancerId(key.toString());

                        String cacheLocation = HadoopLogsConfigs.getCacheDir() + "/" + schedulerConfigs.getRawlogsFileTime() + "/" + accountId;
                        new File(cacheLocation).mkdirs();
                        String filename = getFileName(loadbalancerId, schedulerConfigs.getRawlogsFileTime());

                        String cacheLocationAndFile = cacheLocation + "/" + filename;

                        File file = new File(cacheLocationAndFile);
                        if (file.exists()) {
                            LOG.warn("A file with name " + cacheLocationAndFile + " already exists. This can lead to missing log files as the old files can be overwritten.");
                            cacheLocationAndFile = cacheLocationAndFile + "_" + i;
                        }

                        cacheLocationAndFile = cacheLocationAndFile + ".zip";

                        FileBytesWritable val = new FileBytesWritable();
                        val.setOrder(seqCount);
                        val.setFileName(cacheLocationAndFile);
                        reader.getCurrentValue(val);
                        LOG.info("File Written to: " + cacheLocationAndFile);
                    }

                    reader.close();
                    HdfsUtils.deleteLocalFile(local);
                }

            }

            scheduleArchiveLoadBalancerLogsJob(scheduler, schedulerConfigs);
        } catch (IOException e) {
            e.printStackTrace();
            failJob(state);
            throw new ExecutionException(e);
        } catch (Exception e) {
            e.printStackTrace();
            failJob(state);
            throw new ExecutionException(e);
        }

        finishJob(state);
    }

    private String getFileName(String lbId, String rawlogsFileDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("access log ");
        sb.append(lbId).append(" ");
        sb.append(rawlogsFileDate);
        return getFormattedName(sb.toString());
    }

    private String getFormattedName(String name) {
        return name.replaceAll(" ", "_");

    }

    private String getAccount(String key) {
        return key.split(":")[0];
    }

    private String getLoadBalancerId(String key) {
        return key.split(":")[1];
    }

    private void scheduleArchiveLoadBalancerLogsJob(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws SchedulingException {
        scheduler.scheduleJob(ArchiveLoadBalancerLogsJob.class, schedulerConfigs);
    }
}
