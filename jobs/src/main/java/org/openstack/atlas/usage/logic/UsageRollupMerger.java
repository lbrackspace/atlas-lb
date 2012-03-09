package org.openstack.atlas.usage.logic;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.usage.helpers.ConfigurationKeys;
import org.openstack.atlas.usage.helpers.EsbConfiguration;
import org.openstack.atlas.usage.helpers.TimeZoneHelper;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.usage.Tier;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

import java.util.*;

public class UsageRollupMerger {
    private Map<Integer, List<UsagesForDay>> lbIdUsageMap;
    private Map<Integer, Usage> lbIdRollupUsageMap;
    private List<Usage> usagesToInsert;
    private List<Usage> usagesToUpdate;
    private Configuration configuration = new EsbConfiguration();

    public UsageRollupMerger(Map<Integer, List<UsagesForDay>> lbIdUsageMap, Map<Integer, Usage> lbIdRollupUsageMap) {
        this.lbIdUsageMap = lbIdUsageMap;
        this.lbIdRollupUsageMap = lbIdRollupUsageMap;
    }

    public List<Usage> getUsagesToInsert() {
        return usagesToInsert;
    }

    public List<Usage> getUsagesToUpdate() {
        return usagesToUpdate;
    }

    public UsageRollupMerger invoke() {
        usagesToInsert = new ArrayList<Usage>();
        usagesToUpdate = new ArrayList<Usage>();

        for (Integer lbId : lbIdUsageMap.keySet()) {
            Usage rollUpUsage = lbIdRollupUsageMap.get(lbId);

            for (UsagesForDay usagesForDay : lbIdUsageMap.get(lbId)) {
                List<Usage> mergedUsageRecords;

                String timeZoneCode = configuration.getString(ConfigurationKeys.usage_timezone_code);
                if (rollUpUsage != null && usagesForDay.getDayOfYear() == TimeZoneHelper.getCalendarForTimeZone(rollUpUsage.getEndTime(), TimeZone.getTimeZone(timeZoneCode)).get(Calendar.DAY_OF_YEAR)) {
                    mergedUsageRecords = mergeUsageRecords(usagesForDay.getUsages(), rollUpUsage);
                } else {
                    mergedUsageRecords = mergeUsageRecords(usagesForDay.getUsages(), null);
                }

                for (Usage mergedUsageRecord : mergedUsageRecords) {
                    if (mergedUsageRecord.getId() == null) {
                        usagesToInsert.add(mergedUsageRecord);
                    } else {
                        usagesToUpdate.add(mergedUsageRecord);
                    }
                }
            }
        }
        return this;
    }

    private List<Usage> mergeUsageRecords(List<LoadBalancerUsage> usages, Usage rollUpUsage) {
        List<Usage> mergedUsageRecords = new ArrayList<Usage>();
        boolean canAddCurrUsageRecord = false;
        Usage latestCustomerFacingRecord;

        if (rollUpUsage == null) latestCustomerFacingRecord = createNullUsageRecord();
        else latestCustomerFacingRecord = rollUpUsage;

        // TODO: This list needs to be sorted by timestamps in order to work. Verify
        for (LoadBalancerUsage usageRecordToMerge : usages) {
            Tier.Level usageToMergeTier = Tier.calculateTierLevel(usageRecordToMerge.getAverageConcurrentConnections()); // Only usage non-ssl traffic for tier calculation
            Tier.Level currUsageTier = Tier.calculateTierLevel(latestCustomerFacingRecord.getAverageConcurrentConnections());

            if (((latestCustomerFacingRecord.getEventType() == null && usageRecordToMerge.getEventType() == null)
                  || (latestCustomerFacingRecord.getEventType() != null && usageRecordToMerge.getEventType() == null))
                  && usageRecordToMerge.getTags().equals(latestCustomerFacingRecord.getTags())
                  && usageRecordToMerge.getNumVips().equals(latestCustomerFacingRecord.getNumVips())
                  && usageToMergeTier.equals(currUsageTier)) {
                // Merge records if tags, vips and tier are the same and eventsType are both null or first is not null and second is
                Double currUsageCcs = latestCustomerFacingRecord.getAverageConcurrentConnections();
                Double currUsageCcsSsl = latestCustomerFacingRecord.getAverageConcurrentConnectionsSsl();
                Integer currUsagePolls = latestCustomerFacingRecord.getNumberOfPolls();
                Double usageToMergeCcs = usageRecordToMerge.getAverageConcurrentConnections();
                Double usageToMergeCcsSsl = usageRecordToMerge.getAverageConcurrentConnectionsSsl();
                Integer usageToMergePolls = usageRecordToMerge.getNumberOfPolls();

                if ((usageToMergePolls + currUsagePolls) > 0.0) {
                    latestCustomerFacingRecord.setAverageConcurrentConnections((currUsageCcs * currUsagePolls + usageToMergeCcs * usageToMergePolls) / (usageToMergePolls + currUsagePolls));
                    latestCustomerFacingRecord.setAverageConcurrentConnectionsSsl((currUsageCcsSsl * currUsagePolls + usageToMergeCcsSsl * usageToMergePolls) / (usageToMergePolls + currUsagePolls));
                } else {
                    latestCustomerFacingRecord.setAverageConcurrentConnections(0.0);
                    latestCustomerFacingRecord.setAverageConcurrentConnectionsSsl(0.0);
                }
                latestCustomerFacingRecord.setIncomingTransfer(latestCustomerFacingRecord.getIncomingTransfer() + usageRecordToMerge.getCumulativeBandwidthBytesIn());
                latestCustomerFacingRecord.setIncomingTransferSsl(latestCustomerFacingRecord.getIncomingTransferSsl() + usageRecordToMerge.getCumulativeBandwidthBytesInSsl());
                latestCustomerFacingRecord.setOutgoingTransfer(latestCustomerFacingRecord.getOutgoingTransfer() + usageRecordToMerge.getCumulativeBandwidthBytesOut());
                latestCustomerFacingRecord.setOutgoingTransferSsl(latestCustomerFacingRecord.getOutgoingTransferSsl() + usageRecordToMerge.getCumulativeBandwidthBytesOutSsl());
                latestCustomerFacingRecord.setNumberOfPolls(usageToMergePolls + currUsagePolls);
                latestCustomerFacingRecord.setEndTime(usageRecordToMerge.getEndTime());
            } else {
                // Else create a new record
                if (canAddCurrUsageRecord) mergedUsageRecords.add(latestCustomerFacingRecord);
                latestCustomerFacingRecord = new Usage();

                LoadBalancer lb = new LoadBalancer();
                lb.setId(usageRecordToMerge.getLoadbalancerId());
                lb.setAccountId(usageRecordToMerge.getAccountId());
                latestCustomerFacingRecord.setLoadbalancer(lb);
                latestCustomerFacingRecord.setAccountId(usageRecordToMerge.getAccountId());
                latestCustomerFacingRecord.setAverageConcurrentConnections(usageRecordToMerge.getAverageConcurrentConnections());
                latestCustomerFacingRecord.setAverageConcurrentConnectionsSsl(usageRecordToMerge.getAverageConcurrentConnectionsSsl());
                latestCustomerFacingRecord.setIncomingTransfer(usageRecordToMerge.getCumulativeBandwidthBytesIn());
                latestCustomerFacingRecord.setIncomingTransferSsl(usageRecordToMerge.getCumulativeBandwidthBytesInSsl());
                latestCustomerFacingRecord.setOutgoingTransfer(usageRecordToMerge.getCumulativeBandwidthBytesOut());
                latestCustomerFacingRecord.setOutgoingTransferSsl(usageRecordToMerge.getCumulativeBandwidthBytesOutSsl());
                latestCustomerFacingRecord.setStartTime(usageRecordToMerge.getStartTime());
                latestCustomerFacingRecord.setEndTime(usageRecordToMerge.getEndTime());
                latestCustomerFacingRecord.setNumberOfPolls(usageRecordToMerge.getNumberOfPolls());
                latestCustomerFacingRecord.setNumVips(usageRecordToMerge.getNumVips());
                latestCustomerFacingRecord.setTags(usageRecordToMerge.getTags());
                latestCustomerFacingRecord.setEventType(usageRecordToMerge.getEventType());
            }

            canAddCurrUsageRecord = true; // Can't add until we've gone through one loop
        }

        if (canAddCurrUsageRecord) mergedUsageRecords.add(latestCustomerFacingRecord);
        return mergedUsageRecords;
    }

    private Usage createNullUsageRecord() {
        Usage currUsageRecord = new Usage();
        currUsageRecord.setAccountId(null);
        currUsageRecord.setAverageConcurrentConnections(null);
        currUsageRecord.setAverageConcurrentConnectionsSsl(null);
        currUsageRecord.setIncomingTransfer(null);
        currUsageRecord.setIncomingTransferSsl(null);
        currUsageRecord.setOutgoingTransfer(null);
        currUsageRecord.setOutgoingTransferSsl(null);
        currUsageRecord.setNumberOfPolls(null);
        currUsageRecord.setNumVips(null);
        currUsageRecord.setTags(null);
        currUsageRecord.setEventType(null);
        return currUsageRecord;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
