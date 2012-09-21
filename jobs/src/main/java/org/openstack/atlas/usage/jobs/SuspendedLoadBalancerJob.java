package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.usage.helpers.ConfigurationKeys;
import org.openstack.atlas.usage.helpers.EsbConfiguration;
import org.openstack.atlas.usage.helpers.TimeZoneHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/*
 *  Creates a suspended lb_usage record for every suspended load balancer. This is suppose to run once per day.
 */
// TODO: Create tests for this class
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
            List<Usage> usagesToUpdate = new ArrayList<Usage>();
            List<Usage> usagesToCreate = new ArrayList<Usage>();
            final Calendar now = Calendar.getInstance();
            final String timeZoneCode = configuration.getString(ConfigurationKeys.usage_timezone_code);

            for (LoadBalancer suspendedLoadBalancer : suspendedLoadBalancers) {
                Usage recentUsage = rollUpUsageRepository.getMostRecentUsageForLoadBalancer(suspendedLoadBalancer.getId());

                // if day is the same update endTime for current record
                if (recentUsage != null && TimeZoneHelper.getCalendarForTimeZone(now, TimeZone.getTimeZone(timeZoneCode)).get(Calendar.DAY_OF_YEAR) == TimeZoneHelper.getCalendarForTimeZone(recentUsage.getEndTime(), TimeZone.getTimeZone(timeZoneCode)).get(Calendar.DAY_OF_YEAR)) {
                    recentUsage.setEndTime(now);
                    usagesToUpdate.add(recentUsage);
                    // if day is the same max out endTime for current record and create new record
                } else if(recentUsage != null && TimeZoneHelper.getCalendarForTimeZone(now, TimeZone.getTimeZone(timeZoneCode)).get(Calendar.DAY_OF_YEAR) != TimeZoneHelper.getCalendarForTimeZone(recentUsage.getEndTime(), TimeZone.getTimeZone(timeZoneCode)).get(Calendar.DAY_OF_YEAR)) {
                    recentUsage.getEndTime().set(Calendar.HOUR_OF_DAY, 23);
                    recentUsage.getEndTime().set(Calendar.MINUTE, 59);
                    recentUsage.getEndTime().set(Calendar.SECOND, 59);
                    recentUsage.getEndTime().set(Calendar.MILLISECOND, 999);
                    usagesToUpdate.add(recentUsage);

                    Usage usage  = new Usage();
                    usage.setEventType(UsageEvent.SUSPENDED_LOADBALANCER.name());
                    usage.setLoadbalancer(suspendedLoadBalancer);
                    usage.setAccountId(suspendedLoadBalancer.getAccountId());
                    usage.setTags(recentUsage.getTags());
                    usage.setNumVips(recentUsage.getNumVips());
                    usage.setStartTime(now);
                    usage.setEndTime(now);
                    usagesToCreate.add(usage);
                } else {
                    Usage usage  = new Usage();
                    usage.setEventType(UsageEvent.SUSPENDED_LOADBALANCER.name());
                    usage.setLoadbalancer(suspendedLoadBalancer);
                    usage.setAccountId(suspendedLoadBalancer.getAccountId());
                    usage.setStartTime(now);
                    usage.setEndTime(now);
                    usagesToCreate.add(usage);
                }
            }

            if (!usagesToUpdate.isEmpty()) rollUpUsageRepository.batchUpdate(usagesToCreate);
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
