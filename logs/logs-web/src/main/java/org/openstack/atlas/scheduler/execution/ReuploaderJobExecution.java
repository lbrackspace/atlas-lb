package org.openstack.atlas.scheduler.execution;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.logs.common.util.ReuploaderUtils;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.openstack.atlas.util.common.VerboseLogger;
import org.springframework.beans.factory.annotation.Required;
import org.openstack.atlas.exception.AuthException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class ReuploaderJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final Log LOG = LogFactory.getLog(ReuploaderJobExecution.class);
    private static final VerboseLogger vlog = new VerboseLogger(ReuploaderJobExecution.class);
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        vlog.printf("ReuploaderJob starting: ");

        JobState state = createJob(JobName.ARCHIVE, schedulerConfigs.getInputString());

        try {
            Map<Integer, LoadBalancerIdAndName> lbId2NameMap = createLbIdAndNameMap(loadBalancerRepository.getAllLoadbalancerIdsAndNames());

            String dir = HadoopLogsConfigs.getCacheDir();
            vlog.printf("cache dir: %s", dir);
            ReuploaderUtils reup = new ReuploaderUtils(dir, lbId2NameMap);

            reup.reuploadFiles();
            reup.clearDirs(3);
        } catch (AuthException e) {
            JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, e.getMessage());
            failJob(individualState);
            LOG.error("Error trying to upload to CloudFiles: ", e);
        } catch (Exception e) {
            JobState individualState = createJob(JobName.LOG_FILE_CF_UPLOAD, e.getMessage());
            failJob(individualState);
            LOG.error("Unexpected Error trying to upload to CloudFiles: ", e);
        }

        finishJob(state);
        LOG.info("JOB COMPLETED. Total Time Taken for job " + schedulerConfigs.getInputString() + " to complete : " + StaticFileUtils.getTotalTimeTaken(schedulerConfigs.getRunTime()) + " seconds");
    }

    // Build AccountId/LbId to name map
    public Map<Integer, LoadBalancerIdAndName> createLbIdAndNameMap(List<LoadBalancerIdAndName> dbResults) {
        Map<Integer, LoadBalancerIdAndName> nameMap = new HashMap<Integer, LoadBalancerIdAndName>();
        for (LoadBalancerIdAndName lbId : dbResults) {
            int key = lbId.getLoadbalancerId();
            nameMap.put(key, lbId);
        }
        return nameMap;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }
}
