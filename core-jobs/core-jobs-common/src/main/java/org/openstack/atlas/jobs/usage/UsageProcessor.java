package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.batch.BatchAction;
import org.openstack.atlas.jobs.usage.logic.UsageCalculator;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.*;

public class UsageProcessor implements BatchAction<LoadBalancer> {
    private final Log LOG = LogFactory.getLog(UsageProcessor.class);
    protected UsageRepository usageRepository;
    protected Map<Integer, Long> bytesInMap;
    protected Map<Integer, Long> bytesOutMap;
    protected Calendar pollTime;
    protected List<UsageRecord> recordsToInsert;
    protected List<UsageRecord> recordsToUpdate;

    public UsageProcessor(UsageRepository usageRepository, Map<Integer, Long> bytesInMap, Map<Integer, Long> bytesOutMap) {
        this.usageRepository = usageRepository;
        this.bytesInMap = bytesInMap;
        this.bytesOutMap = bytesOutMap;
    }

    public List<UsageRecord> getRecordsToInsert() {
        if (recordsToInsert == null) throw new RuntimeException("Please call execute first before retrieving data.");
        return recordsToInsert;
    }

    public List<UsageRecord> getRecordsToUpdate() {
        if (recordsToUpdate == null) throw new RuntimeException("Please call execute first before retrieving data.");
        return recordsToUpdate;
    }

    public void execute(List<LoadBalancer> loadBalancers) {
        pollTime = Calendar.getInstance();
        recordsToInsert = recordsToInsert == null ? new ArrayList<UsageRecord>() : recordsToInsert;
        recordsToUpdate = recordsToUpdate == null ? new ArrayList<UsageRecord>() : recordsToUpdate;
        Map<Integer, Integer> lbIdAccountIdMap = generateLbIdAccountIdMap(loadBalancers);
        List<UsageRecord> usages = usageRepository.getMostRecentUsageRecordsForLoadBalancers(lbIdAccountIdMap.keySet());
        Map<Integer, UsageRecord> usagesAsMap = convertUsagesToMap(usages);

        for (LoadBalancer loadBalancer : loadBalancers) {
            try {
                if (!usagesAsMap.containsKey(loadBalancer.getId()) && (bytesInMap.containsKey(loadBalancer.getId()) || bytesOutMap.containsKey(loadBalancer.getId()))) {
                    // Case 1: No record exists at all and inbound or outbound bandwidth exists. Create one.
                    recordsToInsert.add(createNewUsageRecord(loadBalancer));
                } else if (bytesInMap.containsKey(loadBalancer.getId()) || bytesOutMap.containsKey(loadBalancer.getId())) {
                    // Case 2: A record exists and inbound or outbound bandwidth exists.
                    UsageRecord mostRecentRecord = usagesAsMap.get(loadBalancer.getId());

                    if (mostRecentRecord.getEndTime().get(Calendar.DAY_OF_YEAR) != pollTime.get(Calendar.DAY_OF_YEAR) && pollTime.after(mostRecentRecord.getEndTime())) {
                        // Case 2a: A record exists but need to create new one because current day is later than endTime day.
                        // Also, Add bandwidth that occurred between mostRecentRecord endTime and newRecord startTime to
                        // newRecord so that we don't lose it.
                        UsageRecord newRecord = createNewUsageRecord(loadBalancer);
                        newRecord.setTransferBytesIn(calcCumBandwidthBytesInForSingleRecord(mostRecentRecord, bytesInMap.get(loadBalancer.getId())) - mostRecentRecord.getTransferBytesIn());
                        newRecord.setTransferBytesOut(calcCumBandwidthBytesOutForSingleRecord(mostRecentRecord, bytesOutMap.get(loadBalancer.getId()))  - mostRecentRecord.getTransferBytesOut());
                        recordsToInsert.add(newRecord);
                    } else {
                        // Case 2b: A record exists and we need to update because current day is the same as endTime day.
                        updateMostRecentRecord(mostRecentRecord);
                        recordsToUpdate.add(mostRecentRecord);
                    }
                }
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Ignoring usage record...", loadBalancer));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Ignoring usage record...", loadBalancer));
            }
        }
    }

    public void updateMostRecentRecord(UsageRecord mostRecentRecord) {
        Long bytesInValue = bytesInMap.get(mostRecentRecord.getLoadBalancer().getId());
        Long bytesOutValue = bytesOutMap.get(mostRecentRecord.getLoadBalancer().getId());
        mostRecentRecord.setEndTime(pollTime);

        if (bytesInValue != null) {
            mostRecentRecord.setTransferBytesIn(calcCumBandwidthBytesInForSingleRecord(mostRecentRecord, bytesInValue));
            mostRecentRecord.setLastBytesInCount(bytesInValue);
        }

        if (bytesOutValue != null) {
            mostRecentRecord.setTransferBytesOut(calcCumBandwidthBytesOutForSingleRecord(mostRecentRecord, bytesOutValue));
            mostRecentRecord.setLastBytesOutCount(bytesOutValue);
        }
    }

    protected UsageRecord createNewUsageRecord(LoadBalancer loadBalancer) {
        Long bytesInValue = bytesInMap.get(loadBalancer.getId());
        Long bytesOutValue = bytesOutMap.get(loadBalancer.getId());

        if (bytesInValue == null) bytesInValue = 0l;
        if (bytesOutValue == null) bytesOutValue = 0l;

        UsageRecord newRecord = new UsageRecord();
        newRecord.setLoadBalancer(loadBalancer);
        newRecord.setTransferBytesIn(0l);
        newRecord.setTransferBytesOut(0l);
        newRecord.setLastBytesInCount(bytesInValue);
        newRecord.setLastBytesOutCount(bytesOutValue);
        newRecord.setStartTime(pollTime);
        newRecord.setEndTime(pollTime);
        return newRecord;
    }

    protected Long calcCumBandwidthBytesInForSingleRecord(UsageRecord currentRecord, Long currentSnapshotValue) {
        return UsageCalculator.calculateCumulativeBytes(currentRecord.getTransferBytesIn(), currentRecord.getLastBytesInCount(), currentSnapshotValue);
    }

    protected Long calcCumBandwidthBytesOutForSingleRecord(UsageRecord currentRecord, Long currentSnapshotValue) {
        return UsageCalculator.calculateCumulativeBytes(currentRecord.getTransferBytesOut(), currentRecord.getLastBytesOutCount(), currentSnapshotValue);
    }

    protected Map<Integer, Integer> generateLbIdAccountIdMap(List<LoadBalancer> loadBalancers) {
        Map<Integer, Integer> lbIdAccountIdMap = new HashMap<Integer, Integer>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            lbIdAccountIdMap.put(loadBalancer.getId(), loadBalancer.getAccountId());
        }
        return lbIdAccountIdMap;
    }

    protected Map<Integer, UsageRecord> convertUsagesToMap(List<UsageRecord> usages) {
        Map<Integer, UsageRecord> usagesAsMap = new HashMap<Integer, UsageRecord>();
        for (UsageRecord usage : usages) {
            usagesAsMap.put(usage.getLoadBalancer().getId(), usage);
        }
        return usagesAsMap;
    }
}
