package org.openstack.atlas.scheduler.execution;

import com.rackspacecloud.client.cloudfiles.FilesException;
import org.openstack.atlas.auth.AuthService;
import org.openstack.atlas.cloudfiles.CloudFilesDao;
import org.openstack.atlas.constants.Constants;
import org.openstack.atlas.exception.AuthException;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArchiveLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(ArchiveLoadBalancerLogsJobExecution.class);

    private LoadBalancerRepository loadBalancerRepository;
    private CloudFilesDao dao;
    private JobScheduler jobScheduler;
    private AuthService authService;

    public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {


        // stupid manual set, this has to be done. a circular dep because of how
        // quartz must init its scheduler factory crap. currently u cannot have
        // a bean that has a dependency on a bean that is in the
        // schedulerFactoryBean#schedulerContextAsMap
        jobScheduler = scheduler;

        String cacheLocation = utils.getCacheDir();

        Map<String, List> accountDirsMap = null;
        try {
            accountDirsMap = getLocalInputFiles(cacheLocation);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        if (accountDirsMap.isEmpty()) {
            throw new IllegalArgumentException("No filename provided in the job data");
        }

        JobState state = createJob(JobName.ARCHIVE, runner.getInputString());
        for (String accountDir : accountDirsMap.keySet()) {
            try {
                List<String> accountLogFiles = accountDirsMap.get(accountDir);
                for (String accountLogFile : accountLogFiles) {
                    String absoluteFileName = accountDir + "/" + accountLogFile;
                    try {
                        String account = getAccount(accountDir);
                        String runTimeForFile = getRuntimeForFile(accountDir);

                        LOG.info("Preparing to upload LogFile to Cloud Files: " + absoluteFileName);

                        File file = new File(absoluteFileName);
                        if(!file.exists()) {
                            LOG.info("Couldn't find the file anymore. Probably uploaded by another thread already. So ignoring file: " + absoluteFileName);
                            continue;
                        }

                        String loadBalancerId = getLoadBalancerIdFromFileName(accountLogFile);
                        LoadBalancer lb = loadBalancerRepository.getByIdAndAccountId(Integer.parseInt(loadBalancerId), Integer.parseInt(account));

                        String containername = getContainerName(loadBalancerId, lb.getName(), runTimeForFile);
                        String remoteFileName = getRemoteFileName(loadBalancerId, lb.getName(), runTimeForFile);

                        dao.uploadLocalFile(authService.getUser(account), containername, absoluteFileName, remoteFileName);

                        deleteLocalFile(absoluteFileName);
                        LOG.info("Upload complete for LogFile: " + absoluteFileName);

                    //We will log each individual upload event only if it fails. No need to track those that succeeded.
                    } catch (EntityNotFoundException e) {
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, runner.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        LOG.error("Error trying to upload to CloudFiles for loadbalancer that doesn't exist: " + absoluteFileName, e);
                    } catch(FilesException e){
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, runner.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch(AuthException e){
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, runner.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    } catch(Exception e){
                        // Usually its caused by SSL Exception due to some weird staging & test accounts. So ignoring for now.
                        // Catch all so we can proceed.
                        JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, runner.getInputString() + ":" + absoluteFileName);
                        failJob(individualState);
                        LOG.error("Unexpected Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    }
                }

                deleteLocalFile(accountDir);
            } catch(Exception e){
                LOG.error("JOB " + runner.getInputString() + "failed to upload to Cloud Files. Please have a developer look into it.", e);
            }
        }
        File folder = new File(cacheLocation);
        for (File runtimeFolder : folder.listFiles()) {
            deleteLocalFile(runtimeFolder.getAbsolutePath());
        }
        finishJob(state);
        LOG.info("JOB COMPLETED. Total Time Taken for job " + runner.getInputString() + " to complete : " + getTotalTimeTaken(runner) + " seconds");
    }

    private Date getStartDate(HadoopRunner runner) {
        Date startDate = new Date();
        try {
            String inputDate = runner.getInputString(); //20110215-130916
            DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
            startDate = df.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }

    private Date getDate(String runTimeForFile) {
        Date startDate = new Date();
        try {
            //20110215-130916
            DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
            startDate = df.parse(runTimeForFile);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }

    private String getTotalTimeTaken(HadoopRunner runner) {
        String timeTaken = "";
        Date startDate = getStartDate(runner);

        Date now = Calendar.getInstance().getTime();
        long diff = now.getTime() - startDate.getTime();
        timeTaken = Long.toString((diff / 1000));
        return timeTaken;
    }

    private void deleteLocalFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.isDirectory() && file.listFiles().length == 0) {
                file.delete();
                LOG.info("Deleted file after uploading to CloudFiles: " + fileName);
            } else if (file.isFile()) {
                file.delete();
                LOG.info("Deleted file after uploading to CloudFiles: " + fileName);
            } else if (file.isDirectory() && file.listFiles().length > 0) {
                LOG.info(file.listFiles().length + " existing files inside " + fileName + ". Hence unable to clean up dir.");
            }
        } catch (Exception e) {
            LOG.error("Error deleting file after uploading to CloudFiles: " + fileName, e);
        }
    }

    private String getLoadBalancerIdFromFileName(String localileName) {
        return localileName.split("_")[2];
    }

    private String getContainerName(String lbId, String lbName, String runTimeForFile) {
        String rawLogsFileDate = Constants.Rawlogs.RAWLOGS_FORMAT.format(getDate(runTimeForFile));
        String monthYear = getMonthYear(rawLogsFileDate);
        StringBuilder sb = new StringBuilder();
        sb.append("lb ");
        sb.append(lbId).append(" ");
        sb.append(lbName).append(" ");
        sb.append(monthYear);
        return getFormattedName(sb.toString());
    }

    private String getRemoteFileName(String lbId, String lbName, String runTimeForFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("lb ");
        sb.append(lbId).append(" ");
        sb.append(lbName).append(" ");
        sb.append(Constants.Rawlogs.RAWLOGS_FORMAT.format(getDate(runTimeForFile)));
        sb.append(".zip");
        return getFormattedName(sb.toString());
    }

    private String getFormattedName(String name) {
        return name.replaceAll(" ", "_");

    }

    private Map<String, List> getLocalInputFiles(String cacheLocation) {
        Map<String, List> map = new HashMap<String, List>();

        File folder = new File(cacheLocation);
        File[] files = folder.listFiles();

        for (File runtime : files) {
            if (runtime.isDirectory()) {
                File[] accountFolders = runtime.listFiles();
                for (File parent : accountFolders) {
                    if (parent.isDirectory()) {
                        File[] zippedFiles = parent.listFiles();
                        List<String> filesLog = new ArrayList<String>();
                        for (File log : zippedFiles) {
                            if (log.getName().endsWith(".zip")) {
                                filesLog.add(log.getName());

                            }
                        }
                        map.put(parent.getAbsolutePath(), filesLog);
                    }
                }
            }
        }
        return map;
    }

    private String getMonthYear(String rawLogsFileDate) {
        String monthYear = "";
        try {
            Date date = Constants.Rawlogs.RAWLOGS_FORMAT.parse(rawLogsFileDate);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int year = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);

            String month = "invalid";
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getShortMonths();
            if (m >= 0 && m <= 11) {
                month = months[m];
            }
            monthYear = month + "_" + year;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return monthYear;
    }

    private String getAccount(String accountDir) {
        //var/log/zxtm/hadoop/cache/20110823-161700/10
        return accountDir.substring(accountDir.lastIndexOf("/") + 1, accountDir.length());
    }

    private String getRuntimeForFile(String accountDir) {
        //var/log/zxtm/hadoop/cache/20110823-161700/10
        String runTimeDir = accountDir.substring(0, accountDir.lastIndexOf("/"));
        return runTimeDir.substring(runTimeDir.lastIndexOf("/")+1, runTimeDir.length());
    }

    public void setCloudFilesDao(CloudFilesDao cloudFilesDao) {
        this.dao = cloudFilesDao;
    }

    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
