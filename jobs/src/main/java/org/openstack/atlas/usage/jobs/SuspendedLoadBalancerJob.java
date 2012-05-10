package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.usage.helpers.EsbConfiguration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 *  Creates a suspended lb_usage record for every suspended load balancer. This is suppose to run once per day.
 */
public class SuspendedLoadBalancerJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(SuspendedLoadBalancerJob.class);

    private LoadBalancerRepository loadBalancerRepository;
    private UsageRepository rollUpUsageRepository;
    private Configuration configuration = new EsbConfiguration();

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setRollUpUsageRepository(UsageRepository rollUpUsageRepository) {
        this.rollUpUsageRepository = rollUpUsageRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Suspended load balancer job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.IN_PROGRESS);

        try {
            List<LoadBalancer> suspendedLoadBalancers = loadBalancerRepository.getLoadBalancersWithStatus(LoadBalancerStatus.SUSPENDED);
            List<Usage> usagesToCreate = new ArrayList<Usage>();
            Calendar now = Calendar.getInstance();

            for (LoadBalancer suspendedLoadBalancer : suspendedLoadBalancers) {
                Usage recentUsage = rollUpUsageRepository.getMostRecentUsageForLoadBalancer(suspendedLoadBalancer.getId());
                Usage usage  = new Usage();
                usage.setEventType(LoadBalancerStatus.SUSPENDED.name());
                usage.setLoadbalancer(suspendedLoadBalancer);
                usage.setAccountId(suspendedLoadBalancer.getAccountId());
                if (recentUsage != null) usage.setTags(recentUsage.getTags());
                if (recentUsage != null) usage.setNumVips(recentUsage.getNumVips());
                usage.setStartTime(now);
                usage.setEndTime(now);
                usagesToCreate.add(usage);
            }

            if (!usagesToCreate.isEmpty()) rollUpUsageRepository.batchCreate(usagesToCreate);
        } catch (Exception e) {
            LOG.error("Suspended load balancer job failed!", e);
            jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.FAILED);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Suspended load balancer job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
