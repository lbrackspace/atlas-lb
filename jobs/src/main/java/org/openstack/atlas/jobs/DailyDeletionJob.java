package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;

public class DailyDeletionJob extends Job {
    private final Log LOG = LogFactory.getLog(DailyDeletionJob.class);
    private LoadBalancerUsageRepository usageRepository;
    private HostUsageRepository hostUsageRepository;

    @Required
    public void setUsageRepository(LoadBalancerUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Required
    public void setHostUsageRepository(HostUsageRepository hostUsageRepository) {
        this.hostUsageRepository = hostUsageRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Daily deletion job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.DAILY_DELETION_JOB, JobStateVal.IN_PROGRESS);

        try {
            deleteLoadBalancerUsageRecords();
            deleteHostUsageRecords();
        } catch (Exception e) {
            jobStateService.updateJobState(JobName.DAILY_DELETION_JOB, JobStateVal.FAILED);
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.DAILY_DELETION_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Daily deletion job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void deleteLoadBalancerUsageRecords() {
        LOG.info("Deleting old loadbalancer usage records...");
        usageRepository.deleteOldRecords();
        LOG.info("Completed deleting old loadbalancer usage records.");
    }

    private void deleteHostUsageRecords() {
        LOG.info("Deleting old host usage records...");
        hostUsageRepository.deleteOldRecords();
        LOG.info("Completed deleting old host usage records.");
    }
}
