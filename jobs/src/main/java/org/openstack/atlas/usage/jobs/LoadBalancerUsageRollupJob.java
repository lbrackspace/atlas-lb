package org.openstack.atlas.usage.jobs;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.usage.helpers.ConfigurationKeys;
import org.openstack.atlas.usage.helpers.EsbConfiguration;
import org.openstack.atlas.usage.helpers.TimeZoneHelper;
import org.openstack.atlas.usage.logic.UsagesForDay;
import org.openstack.atlas.usage.logic.UsageRollupMerger;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.*;

public class LoadBalancerUsageRollupJob extends QuartzJobBean {
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
        LOG.info("Usage Rollup Job started at " + Calendar.getInstance().getTime());
        Calendar rollupTimeMarker = Calendar.getInstance();
        rollupTimeMarker.add(Calendar.HOUR_OF_DAY, -1); // Leaves at least one hour of data in the database. Ensures bitmask/numVips gets copied over
        LOG.info("Retrieving usage entries to process from polling DB...");
        List<LoadBalancerUsage> pollingUsages = pollingUsageRepository.getAllRecordsBefore(rollupTimeMarker);
        Map<Integer, List<UsagesForDay>> lbIdUsageMap = generateLbIdUsagesMap(pollingUsages);
        LOG.info("Retrieving most recent usage entries from main DB...");
        List<Usage> rollUpUsages = rollUpUsageRepository.getMostRecentUsageForLoadBalancers(lbIdUsageMap.keySet());
        Map<Integer, Usage> lbIdRollupUsageMap = generateLbIdUsageMap(rollUpUsages);
        LOG.info("Processing usage entries...");
        UsageRollupMerger usagesForDatabase = new UsageRollupMerger(lbIdUsageMap, lbIdRollupUsageMap).invoke();
        if (!usagesForDatabase.getUsagesToUpdate().isEmpty())
            rollUpUsageRepository.batchUpdate(usagesForDatabase.getUsagesToUpdate());
        if (!usagesForDatabase.getUsagesToInsert().isEmpty())
            rollUpUsageRepository.batchCreate(usagesForDatabase.getUsagesToInsert());
        LOG.info("Deleting processed usage entries...");
        pollingUsageRepository.deleteAllRecordsBefore(rollupTimeMarker);
        LOG.info("Usage Rollup Job completed at " + Calendar.getInstance().getTime());
    }

    private Map<Integer, Usage> generateLbIdUsageMap(List<Usage> rollUpUsages) {
        Map<Integer, Usage> lbIdRollupUsageMap = new HashMap<Integer, Usage>();

        for (Usage rollUpUsage : rollUpUsages) {
            lbIdRollupUsageMap.put(rollUpUsage.getLoadbalancer().getId(), rollUpUsage);
        }

        return lbIdRollupUsageMap;
    }

    /*
     *  Returns A hashmap with the key being the loadbalancer id and the value being a list of
     *  usages arranged by day.
     */
    private Map<Integer, List<UsagesForDay>> generateLbIdUsagesMap(List<LoadBalancerUsage> pollingUsages) {
        Map<Integer, List<UsagesForDay>> lbIdUsageMap = new HashMap<Integer, List<UsagesForDay>>();

        for (LoadBalancerUsage pollingUsage : pollingUsages) {
            Integer lbId = pollingUsage.getLoadbalancerId();
            String timeZoneCode = configuration.getString(ConfigurationKeys.usage_timezone_code);
            Calendar endTimeForTimeZone = TimeZoneHelper.getCalendarForTimeZone(pollingUsage.getEndTime(), TimeZone.getTimeZone(timeZoneCode));
            int dayOfYear = endTimeForTimeZone.get(Calendar.DAY_OF_YEAR);

            if (lbIdUsageMap.containsKey(lbId)) {
                boolean addedUsageRecord = false;
                for (UsagesForDay usagesForDay : lbIdUsageMap.get(lbId)) {
                    if (usagesForDay.getDayOfYear() == dayOfYear) {
                        usagesForDay.getUsages().add(pollingUsage);
                        addedUsageRecord = true;
                    }
                }

                if (!addedUsageRecord) {
                    UsagesForDay usagesForDay = new UsagesForDay();
                    usagesForDay.setDayOfYear(dayOfYear);
                    usagesForDay.getUsages().add(pollingUsage);
                    lbIdUsageMap.get(lbId).add(usagesForDay);
                }

            } else {
                List<UsagesForDay> usagesForDayList = new ArrayList<UsagesForDay>();
                UsagesForDay usagesForDay = new UsagesForDay();
                usagesForDay.setDayOfYear(dayOfYear);
                usagesForDay.getUsages().add(pollingUsage);
                usagesForDayList.add(usagesForDay);
                lbIdUsageMap.put(lbId, usagesForDayList);
            }
        }

        return lbIdUsageMap;
    }

}
