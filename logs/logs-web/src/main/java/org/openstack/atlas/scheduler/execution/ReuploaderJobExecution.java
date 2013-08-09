package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;

public class ReuploaderJobExecution extends LoggableJobExecution implements QuartzExecutable {

//    private static final Log LOG = LogFactory.getLog(ReuploaderJobExecution.class);
//    private static final VerboseLogger vlog = new VerboseLogger(ReuploaderJobExecution.class);
//    private LoadBalancerRepository loadBalancerRepository;
//
//    @Override
//    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
//        vlog.printf("ReuploaderJob starting: ");
//
//        JobState state = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString());
//
//        try {
//            Map<Integer, LoadBalancerIdAndName> lbId2NameMap = createLbIdAndNameMap(loadBalancerRepository.getAllLoadbalancerIdsAndNames());
//
//            String dir = conf.getString(LbLogsConfigurationKeys.rawlogs_cache_dir);
//            vlog.printf("cache dir: %s", dir);
//            ReuploaderUtils reup = new ReuploaderUtils(dir, lbId2NameMap);
//
//            reup.reuploadFiles();
//            reup.clearDirs(3);
//        } catch (AuthException e) {
//            JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, e.getMessage());
//            failJob(individualState);
//            LOG.error("Error trying to upload to CloudFiles: ", e);
//        } catch (Exception e) {
//            JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, e.getMessage());
//            failJob(individualState);
//            LOG.error("Unexpected Error trying to upload to CloudFiles: ", e);
//        }
//
//        finishJob(state);
//        LOG.info("JOB COMPLETED. Total Time Taken for job " + schedulerConfigs.getInputString() + " to complete : " + StaticFileUtils.getTotalTimeTaken(schedulerConfigs.getRunTime()) + " seconds");
//    }
//
//    // Build AccountId/LbId to name map
//    public Map<Integer, LoadBalancerIdAndName> createLbIdAndNameMap(List<LoadBalancerIdAndName> dbResults) {
//        Map<Integer, LoadBalancerIdAndName> nameMap = new HashMap<Integer, LoadBalancerIdAndName>();
//        for (LoadBalancerIdAndName lbId : dbResults) {
//            int key = lbId.getLoadbalancerId();
//            nameMap.put(key, lbId);
//        }
//        return nameMap;
//    }
//
//    @Required
//    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
//        this.loadBalancerRepository = loadBalancerRepository;
//    }

    @Override public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
