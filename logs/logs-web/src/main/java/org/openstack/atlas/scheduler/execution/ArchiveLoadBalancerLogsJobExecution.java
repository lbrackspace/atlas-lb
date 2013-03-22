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
import org.openstack.atlas.tools.HadoopRunner;
import org.openstack.atlas.util.LogFileNameBuilder;
import org.openstack.atlas.util.LogFileUtil;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArchiveLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(ArchiveLoadBalancerLogsJobExecution.class);

    private LoadBalancerRepository loadBalancerRepository;
    private CloudFilesDao dao;
    private JobScheduler jobScheduler;
    private AuthService authService;

    public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {
        jobScheduler = scheduler;

        String cacheLocation = utils.getCacheDir();

        Map<String, List> accountFilesMap = null;
        try {
            accountFilesMap = LogFileUtil.getLocalCachedFiles(cacheLocation);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        if (accountFilesMap.isEmpty()) {
            throw new IllegalArgumentException("No filename provided in the job data");
        }
        List<String> failed = new ArrayList<String>();
        int total = 0;

        String timestamp = StaticDateTimeUtils.nowDateTime(false).toString();

        JobState state = createJob(JobName.ARCHIVE, timestamp);
        for (String accountDirectory : accountFilesMap.keySet()) {
            try {
                List<String> accountLogFiles = accountFilesMap.get(accountDirectory);
                for (String absoluteFileName : accountLogFiles) {
                    try {
                        total++;
                        String accountId = LogFileUtil.getAccountId(absoluteFileName);
                        String loadBalancerId = LogFileUtil.getLoadBalancerId(absoluteFileName);

                        String logFileTime = LogFileUtil.getLogFileTime(absoluteFileName);

                        LoadBalancer lb = loadBalancerRepository.getByIdAndAccountId(Integer.parseInt(loadBalancerId), Integer.parseInt(accountId));

                        //accountId = "563374";
                        //LoadBalancer lb = new LoadBalancer();
                        //lb.setName("My lb name");


                        String containername = LogFileNameBuilder.getContainerName(loadBalancerId, lb.getName(), logFileTime);
                        String remoteFileName = LogFileNameBuilder.getRemoteFileName(loadBalancerId, lb.getName(), logFileTime);

                        dao.uploadLocalFile(authService.getUser(accountId), containername, absoluteFileName, remoteFileName);

                        LogFileUtil.deleteLocalFile(absoluteFileName);
                        LOG.info("Uploaded logFile: " + absoluteFileName + "  to cloudfile as " + containername + "/" + remoteFileName );

                    //We will log each individual upload event only if it fails. No need to track those that succeeded.
                    } catch (EntityNotFoundException e) {
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, timestamp + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles for loadbalancer that doesn't exist: " + absoluteFileName, e);
                    } catch(FilesException e){
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, timestamp + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch(AuthException e){
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, timestamp + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch(Exception e){
                        // Usually its caused by SSL Exception due to some weird staging & test accounts. So ignoring for now.
                        // Catch all so we can proceed.
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, timestamp + ":" + absoluteFileName);
                        failJob(individualState);
                        failed.add("absoluteFileName");
                        LOG.error("Unexpected Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    }
                }

                LogFileUtil.deleteLocalFile(accountDirectory);
            } catch(Exception e){
                LOG.error("JOB " + timestamp + "failed to upload to Cloud Files. Please have a developer look into it.", e);
            }
        }
        File folder = new File(cacheLocation);
        for (File runtimeFolder : folder.listFiles()) {
            LogFileUtil.deleteLocalFile(runtimeFolder.getAbsolutePath());
        }
        finishJob(state);

        LOG.info("JOB COMPLETED. Total Time Taken for job " + timestamp + " to complete : " + "UNKNOWN" + " seconds");
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
