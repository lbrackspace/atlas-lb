package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.auth.AuthService;
import org.openstack.atlas.cloudfiles.CloudFilesDao;
import org.openstack.atlas.constants.Constants;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.logs.entities.NameVal;
import org.openstack.atlas.service.domain.logs.entities.State;
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

        Map<String, List> filenames = null;
        try {
            filenames = getLocalInputFiles(cacheLocation);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        if (filenames.isEmpty()) {
            throw new IllegalArgumentException("No filename provided in the job data");
        }

        for (String fileName : filenames.keySet()) {
            State state = null;

            try {
                state = createJob(NameVal.ARCHIVE, runner.getInputString() + ":" + fileName);

                List<String> accountLogFiles = filenames.get(fileName);
                for (String accountLogFile : accountLogFiles) {
                    String absoluteFileName = fileName + "/" + accountLogFile;
                    try {

                        String account = getAccount(fileName);

                        LOG.info("Preparing to upload LogFile to Cloud Files: " + absoluteFileName);

                        String loadBalancerId = getLoadBalancerIdFromFileName(accountLogFile);

                        LoadBalancer lb = loadBalancerRepository.getByIdAndAccountId(Integer.parseInt(loadBalancerId), Integer.parseInt(account));

                        String containername = getContainerName(loadBalancerId, lb.getName(), runner.getRawlogsFileDate());

                        String remoteFileName = getRemoteFileName(loadBalancerId, lb.getName(), runner.getRawlogsFileDate());

                        dao.uploadLocalFile(authService.getUser(account), containername, absoluteFileName, remoteFileName);

                        deleteLocalFile(absoluteFileName);

                        LOG.info("Upload complete for LogFile: " + absoluteFileName);
                    } catch (Exception e) {
                        LOG.error("Error trying to upload to CloudFiles: " + absoluteFileName, e);
                    }

                }
                deleteLocalFile(fileName);

                finishJob(state);
            } catch (Exception e) {
                if (state != null) {
                    failJob(state);
                }
                LOG.error("JOB " + runner.getInputString() + "failed to upload to Cloud Files. Please have a developer look into it.", e);
                //throw new ExecutionException("Could no upload to cloudfiles: ", e);
            }
        }

        File folder = new File(cacheLocation);
        for (File runtimeFolder : folder.listFiles()) {
            deleteLocalFile(runtimeFolder.getAbsolutePath());
        }
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

    private String getTotalTimeTaken(HadoopRunner runner) {
        String timeTaken = "";
        Date startDate = getStartDate(runner);

        Date now = Calendar.getInstance().getTime();
        long diff = now.getTime() - startDate.getTime();
        timeTaken = Long.toString((diff/1000));
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

    private String getContainerName(String lbId, String lbName, String rawlogsFileDate) {
        String monthYear = getMonthYear(rawlogsFileDate);
        StringBuilder sb = new StringBuilder();
        sb.append("lb ");
        sb.append(lbId).append(" ");
        sb.append(lbName).append(" ");
        sb.append(monthYear);
        return getFormattedName(sb.toString());
    }

    private String getRemoteFileName(String lbId, String lbName, String rawlogsFileDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("lb ");
        sb.append(lbId).append(" ");
        sb.append(lbName).append(" ");
        sb.append(rawlogsFileDate);
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

    private String getAccount(String absoluteFileName) {
        ///var/log/zxtm/hadoop/cache/386085
        return absoluteFileName.substring(absoluteFileName.lastIndexOf("/") + 1, absoluteFileName.length());
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
