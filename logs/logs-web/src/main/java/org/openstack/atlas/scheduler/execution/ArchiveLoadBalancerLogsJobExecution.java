package org.openstack.atlas.scheduler.execution;

import com.rackspacecloud.client.cloudfiles.FilesException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.auth.AuthService;
import org.openstack.atlas.cloudfiles.CloudFilesDao;
import org.openstack.atlas.exception.AuthException;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.openstack.atlas.util.LogFileNameBuilder;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openstack.atlas.util.HadoopLogsConfigs;
import org.openstack.atlas.util.StaticFileUtils;
import org.openstack.atlas.util.StaticLogUtils;

public class ArchiveLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(ArchiveLoadBalancerLogsJobExecution.class);

    private LoadBalancerRepository loadBalancerRepository;
    private CloudFilesDao dao;
    private JobScheduler jobScheduler;
    private AuthService authService;

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
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
                        LOG.info("Uploaded logFile: " + absoluteFileName + "  to cloudfile as " + containername + "/" + remoteFileName );

                    //We will log each individual upload event only if it fails. No need to track those that succeeded.
                    } catch (EntityNotFoundException e) {
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles for loadbalancer that doesn't exist: " + absoluteFileName, e);
                    } catch(FilesException e){
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch(AuthException e){
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch(Exception e){
                        // Usually its caused by SSL Exception due to some weird staging & test accounts. So ignoring for now.
                        // Catch all so we can proceed.
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, schedulerConfigs.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Unexpected Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    }
                }

                StaticFileUtils.deleteLocalFile(accountDirectory);
            } catch(Exception e){
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
