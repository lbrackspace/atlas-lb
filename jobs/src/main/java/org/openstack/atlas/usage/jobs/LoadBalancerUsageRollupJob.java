package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.helpers.EsbConfiguration;
import org.openstack.atlas.usage.logic.UsageRollupProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.PersistenceException;
import java.util.*;

public class LoadBalancerUsageRollupJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsageRollupJob.class);

    private UsageRepository rollUpUsageRepository;
    private LoadBalancerUsageRepository pollingUsageRepository;
    private Configuration configuration = new EsbConfiguration();

    @Required
    public void setRollUpUsageRepository(UsageRepository rollUpUsageRepository) {
        this.rollUpUsageRepository = rollUpUsageRepository;
    }

    @Required
    public void setPollingUsageRepository(LoadBalancerUsageRepository pollingUsageRepository) {
        this.pollingUsageRepository = pollingUsageRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Usage rollup job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.IN_PROGRESS);

        try {
            Calendar rollupTimeMarker = Calendar.getInstance();
            rollupTimeMarker.add(Calendar.HOUR_OF_DAY, -1); // Leaves at least one hour of data in the database. Ensures bitmask/numVips gets copied over

            LOG.info("Retrieving usage entries to process from polling DB...");
            List<LoadBalancerUsage> pollingUsages = pollingUsageRepository.getAllRecordsBeforeTimeInOrder(rollupTimeMarker);

            LOG.info("Processing usage entries...");
            UsageRollupProcessor usagesForDatabase = new UsageRollupProcessor(pollingUsages, rollUpUsageRepository).process();

            int retries = 3;
            while (retries > 0) {
                if (!usagesForDatabase.getUsagesToUpdate().isEmpty()) {
                    try {
                        rollUpUsageRepository.batchUpdate(usagesForDatabase.getUsagesToUpdate());
                        retries = 0;
                    } catch (PersistenceException e) {
                        LOG.warn("Deleted load balancer(s) detected! Finding and removing from batch...", e);
                        deleteBadEntries(usagesForDatabase.getUsagesToUpdate());
                        retries--;
                        LOG.warn(String.format("%d retries left.", retries));
                    }
                } else {
                    break;
                }
            }

            retries = 3;
            while (retries > 0) {
                if (!usagesForDatabase.getUsagesToCreate().isEmpty()) {
                    try {
                        rollUpUsageRepository.batchCreate(usagesForDatabase.getUsagesToCreate());
                        retries = 0;
                    } catch (PersistenceException e) {
                        LOG.warn("Deleted load balancer(s) detected! Finding and removing from batch...", e);
                        deleteBadEntries(usagesForDatabase.getUsagesToCreate());
                        retries--;
                        LOG.warn(String.format("%d retries left.", retries));
                    }
                } else {
                    break;
                }
            }
            LOG.info("Deleting processed usage entries...");
            pollingUsageRepository.deleteAllRecordsBefore(rollupTimeMarker);
        } catch (Exception e) {
            LOG.error("Usage rollup job failed!", e);
            jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.FAILED);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.FINISHED);
        LOG.info(String.format("Usage rollup job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void deleteBadEntries(List<Usage> usagesWithBadEntries) {
        List<Integer> loadBalancerIdsWithBadId = new ArrayList<Integer>();
        for (Usage usage : usagesWithBadEntries) {
            loadBalancerIdsWithBadId.add(usage.getLoadbalancer().getId());
        }

        List<Integer> loadBalancersFromDatabase = rollUpUsageRepository.getLoadBalancerIdsIn(loadBalancerIdsWithBadId);
        loadBalancerIdsWithBadId.removeAll(loadBalancersFromDatabase); // Remove valid ids from list

        for (Integer loadBalancerId : loadBalancerIdsWithBadId) {
            pollingUsageRepository.deleteAllRecordsForLoadBalancer(loadBalancerId);
        }

        List<Usage> usageItemsToDelete = new ArrayList<Usage>();

        for (Usage usageItem : usagesWithBadEntries) {
            if (loadBalancerIdsWithBadId.contains(usageItem.getLoadbalancer().getId())) {
                usageItemsToDelete.add(usageItem);
            }
        }

        usagesWithBadEntries.removeAll(usageItemsToDelete);
    }

}
