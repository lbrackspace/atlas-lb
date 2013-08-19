package org.openstack.atlas.scheduler.execution;

import com.rackspacecloud.client.cloudfiles.FilesException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.auth.AuthService;
import org.openstack.atlas.cloudfiles.CloudFilesDao;
import org.openstack.atlas.config.CloudFilesZipInfo;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.exception.AuthException;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.logs.hadoop.util.LogFileNameBuilder;
import org.openstack.atlas.logs.hadoop.util.StaticLogUtils;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.*;

public class ArchiveLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final Log LOG = LogFactory.getLog(ArchiveLoadBalancerLogsJobExecution.class);
    private static final VerboseLogger vlog = new VerboseLogger(ArchiveLoadBalancerLogsJobExecution.class);
    private LoadBalancerRepository loadBalancerRepository;
    private CloudFilesDao dao;
    private JobScheduler jobScheduler;
    private AuthService authService;

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        // Build a quick map so we don't have to send thousands of seperate queries to our database
        Map<LoadBalancerIdAndName, String> lbId2NameMap = createLbIdAndNameMap(loadBalancerRepository.getActiveLoadbalancerIdsAndNames());

        String fileHour = schedulerConfigs.getInputString();
        vlog.printf("SchedulerConfigs = %s", schedulerConfigs.toString());
        List<CloudFilesZipInfo> zipInfoList = schedulerConfigs.getCloudFilesZipInfoList();
        Collections.sort(zipInfoList); // Sort by accountId and loadbalancerId for Saner debuging

        Set<String> failedUploads = new HashSet<String>();
        Set<String> allFiles = new HashSet<String>();
        Set<String> accountDirs = new HashSet<String>();
        JobState state = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString());

        int totalFilesToUpload = 0;
        for (CloudFilesZipInfo zipInfo : schedulerConfigs.getCloudFilesZipInfoList()) {
            totalFilesToUpload++;
            int accountId = zipInfo.getAccountId();
            int loadbalancerId = zipInfo.getLoadbalancerId();
            String accountIdStr = Integer.toString(accountId);
            String loadBalancerIdStr = Integer.toString(loadbalancerId);
            String localCacheFile = zipInfo.getCacheFile();
            allFiles.add(localCacheFile); // Keep track of all files
            accountDirs.add(StaticFileUtils.stripDirectoryFromFileName(localCacheFile));
            try {
                LoadBalancerIdAndName currentLbIds = new LoadBalancerIdAndName();
                currentLbIds.setAccountId(accountId);
                currentLbIds.setLoadbalancerId(loadbalancerId);
                if (!lbId2NameMap.containsKey(currentLbIds)) {
                    throw new EntityNotFoundException(String.format("(accoundId=%d, loadbalancerId=%d) not found", accountId, loadbalancerId));
                }

                String lbName = lbId2NameMap.get(currentLbIds);
                vlog.printf("LoadBalancer Name: %s", lbName); //TODO: remove
                String containerName = LogFileNameBuilder.getContainerName(loadBalancerIdStr, lbName, fileHour);
                String remoteFileName = LogFileNameBuilder.getRemoteFileName(loadBalancerIdStr, lbName, fileHour);
                vlog.printf("Attempting to send file=%s -> containerName=%s remoteFileName=%s", localCacheFile, containerName, remoteFileName);

                dao.uploadLocalFile(authService.getUser(accountIdStr), containerName, localCacheFile, remoteFileName);
                LOG.info("Uploaded logFile: " + localCacheFile + "  to cloudfile as " + containerName + "/" + remoteFileName);
            } catch (EntityNotFoundException e) {
                JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + localCacheFile);
                failJob(individualState);
                failedUploads.add(localCacheFile);
                LOG.error("Error trying to upload to CloudFiles for loadbalancer that doesn't exist: " + localCacheFile, e);
            } catch (FilesException e) {
                JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + localCacheFile);
                failJob(individualState);
                failedUploads.add(localCacheFile);
                LOG.error("Error trying to upload to CloudFiles: " + localCacheFile, e);
            } catch (AuthException e) {
                JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + localCacheFile);
                failJob(individualState);
                failedUploads.add(localCacheFile);
                LOG.error("Error trying to upload to CloudFiles: " + localCacheFile, e);
            } catch (Exception e) {
                JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + localCacheFile);
                failJob(individualState);
                failedUploads.add(localCacheFile);
                LOG.error("Unexpected Error trying to upload to CloudFiles: " + localCacheFile, e);
            }
        }

        // Delete the non failed upload jobs
        Set<String> doomedFiles = new HashSet<String>(allFiles);
        doomedFiles.removeAll(failedUploads);
        for (String doomedFile : doomedFiles) {
            StaticFileUtils.deleteLocalFile(doomedFile);
        }

        // Attempt to delete any empty account Directories
        for (String doomedAccountDir : accountDirs) {
            StaticFileUtils.deleteLocalFile(doomedAccountDir);
        }

        // Attempt to delete the date dir
        String dateDir = StaticFileUtils.joinPath(HadoopLogsConfigs.getCacheDir(), fileHour);
        StaticFileUtils.deleteLocalFile(dateDir);


        finishJob(state);
        LOG.info("JOB COMPLETED. Total Time Taken for job " + schedulerConfigs.getInputString() + " to complete : " + StaticFileUtils.getTotalTimeTaken(schedulerConfigs.getRunTime()) + " seconds");
        LOG.info("Failed to upload " + failedUploads.size() + " files out of " + totalFilesToUpload + " files");
    }

    @Deprecated
    public void executeDeprecated(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        jobScheduler = scheduler;

        String cacheLocation = HadoopLogsConfigs.getCacheDir();

        Map<String, List> accountFilesMap = null;
        try {
            accountFilesMap = StaticFileUtils.getLocalCachedFiles(cacheLocation);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        if (accountFilesMap.isEmpty()) {
            throw new IllegalArgumentException("No filename provided in the job data");
        }
        List<String> failed = new ArrayList<String>();
        int total = 0;

        JobState state = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString());
        for (String accountDirectory : accountFilesMap.keySet()) {
            try {
                List<String> accountLogFiles = accountFilesMap.get(accountDirectory);
                for (String absoluteFileName : accountLogFiles) {
                    try {
                        total++;
                        String accountId = StaticLogUtils.getAccountId(absoluteFileName);
                        String loadBalancerId = StaticLogUtils.getLoadBalancerId(absoluteFileName);

                        String logFileTime = StaticFileUtils.getLogFileTime(absoluteFileName);

                        LoadBalancer lb = loadBalancerRepository.getByIdAndAccountId(Integer.parseInt(loadBalancerId), Integer.parseInt(accountId));

                        //accountId = "563374";
                        //LoadBalancer lb = new LoadBalancer();
                        //lb.setName("My lb name");


                        String containername = LogFileNameBuilder.getContainerName(loadBalancerId, lb.getName(), logFileTime);
                        String remoteFileName = LogFileNameBuilder.getRemoteFileName(loadBalancerId, lb.getName(), logFileTime);

                        dao.uploadLocalFile(authService.getUser(accountId), containername, absoluteFileName, remoteFileName);

                        StaticFileUtils.deleteLocalFile(absoluteFileName);
                        LOG.info("Uploaded logFile: " + absoluteFileName + "  to cloudfile as " + containername + "/" + remoteFileName);

                        //We will log each individual upload event only if it fails. No need to track those that succeeded.
                    } catch (EntityNotFoundException e) {
                        JobState individualState = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles for loadbalancer that doesn't exist: " + absoluteFileName, e);
                    } catch (FilesException e) {
                        JobState individualState = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch (AuthException e) {
                        JobState individualState = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch (Exception e) {
                        // Usually its caused by SSL Exception due to some weird staging & test accounts. So ignoring for now.
                        // Catch all so we can proceed.
                        JobState individualState = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Unexpected Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    }
                }

                StaticFileUtils.deleteLocalFile(accountDirectory);
            } catch (Exception e) {
                LOG.error("JOB " + schedulerConfigs.getInputString() + "failed to upload to Cloud Files. Please have a developer look into it.", e);
            }
        }
        File folder = new File(cacheLocation);
        for (File runtimeFolder : folder.listFiles()) {
            StaticFileUtils.deleteLocalFile(runtimeFolder.getAbsolutePath());
        }
        finishJob(state);
        LOG.info("JOB COMPLETED. Total Time Taken for job " + schedulerConfigs.getInputString() + " to complete : " + StaticFileUtils.getTotalTimeTaken(schedulerConfigs.getRunTime()) + " seconds");
        LOG.info("Failed to upload " + failed.size() + " files out of " + total + " files");
    }

    // Build AccountId/LbId to name map
    public Map<LoadBalancerIdAndName, String> createLbIdAndNameMap(List<LoadBalancerIdAndName> dbResults) {
        Map<LoadBalancerIdAndName, String> nameMap = new HashMap<LoadBalancerIdAndName, String>();
        for (LoadBalancerIdAndName lbId : dbResults) {
            LoadBalancerIdAndName key = new LoadBalancerIdAndName();
            key.setAccountId(lbId.getAccountId());
            key.setLoadbalancerId(lbId.getLoadbalancerId());
            String val = lbId.getName();
            nameMap.put(key, val);
        }
        return nameMap;
    }

    @Required
    public void setCloudFilesDao(CloudFilesDao cloudFilesDao) {
        this.dao = cloudFilesDao;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
