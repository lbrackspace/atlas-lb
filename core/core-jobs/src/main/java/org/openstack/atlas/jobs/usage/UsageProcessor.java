package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.batch.BatchAction;
import org.openstack.atlas.jobs.logic.UsageCalculator;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.*;

public class UsageProcessor implements BatchAction<LoadBalancer> {
    private final Log LOG = LogFactory.getLog(UsageProcessor.class);
    private UsageRepository usageRepository;
    private Map<Integer, Long> bytesInMap;
    private Map<Integer, Long> bytesOutMap;
    private Calendar pollTime;
    private List<UsageRecord> recordsToInsert;
    private List<UsageRecord> recordsToUpdate;

    public UsageProcessor(UsageRepository usageRepository, Map<Integer, Long> bytesInMap, Map<Integer, Long> bytesOutMap) {
        this.usageRepository = usageRepository;
        this.bytesInMap = bytesInMap;
        this.bytesOutMap = bytesOutMap;
    }

    public List<UsageRecord> getRecordsToInsert() {
        return recordsToInsert;
    }

    public List<UsageRecord> getRecordsToUpdate() {
        return recordsToUpdate;
    }

    public void execute(List<LoadBalancer> loadBalancers) {
        pollTime = Calendar.getInstance();
        Map<Integer, Integer> lbIdAccountIdMap = generateLbIdAccountIdMap(loadBalancers);
        List<UsageRecord> usages = usageRepository.getMostRecentUsageForLoadBalancers(lbIdAccountIdMap.keySet());
        Map<Integer, UsageRecord> usagesAsMap = convertUsagesToMap(usages);
        recordsToInsert = new ArrayList<UsageRecord>();
        recordsToUpdate = new ArrayList<UsageRecord>();

        for (LoadBalancer loadBalancer : loadBalancers) {
            try {
                if (!usagesAsMap.containsKey(loadBalancer.getId()) && (bytesInMap.containsKey(loadBalancer.getId()) || bytesOutMap.containsKey(loadBalancer.getId()))) {
                    // Case 1: No record exists at all and inbound or outbound bandwidth exists. Create one.
                    UsageRecord newRecord = createNewUsageRecord(loadBalancer);
                    recordsToInsert.add(newRecord);
                } else if (bytesInMap.containsKey(loadBalancer.getId()) || bytesOutMap.containsKey(loadBalancer.getId())) {
                    UsageRecord mostRecentRecord = usagesAsMap.get(loadBalancer.getId());
                    if (mostRecentRecord.getEndTime().get(Calendar.HOUR_OF_DAY) != pollTime.get(Calendar.HOUR_OF_DAY) && pollTime.after(mostRecentRecord.getEndTime())) {
                        // Case 2: A record exists but need to create new one because current hour is later than endTime hour.
                        UsageRecord newRecord = createNewUsageRecord(loadBalancer);
                        recordsToInsert.add(newRecord);
                        // Add bandwidth that occurred between mostRecentRecord endTime and newRecord startTime to mostRecentRecord
                        mostRecentRecord.setTransferBytesIn(calculateCumBandwidthBytesIn(mostRecentRecord, bytesInMap.get(loadBalancer.getId())));
                        mostRecentRecord.setTransferBytesOut(calculateCumBandwidthBytesOut(mostRecentRecord, bytesOutMap.get(loadBalancer.getId())));
                        recordsToUpdate.add(mostRecentRecord);
                    } else {
                        // Case 3: A record exists and we need to update because current day is the same as endTime day.
                        updateMostRecentRecord(loadBalancer, mostRecentRecord);
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

    public void updateMostRecentRecord(LoadBalancer loadBalancer, UsageRecord mostRecentRecord) {
        Long bytesInValue = bytesInMap.get(loadBalancer.getId());
        Long bytesOutValue = bytesOutMap.get(loadBalancer.getId());
        mostRecentRecord.setEndTime(pollTime);

        if (bytesInValue != null) {
            mostRecentRecord.setTransferBytesIn(calculateCumBandwidthBytesIn(mostRecentRecord, bytesInValue));
            mostRecentRecord.setLastBytesInCount(bytesInValue);
        }

        if (bytesOutValue != null) {
            mostRecentRecord.setTransferBytesOut(calculateCumBandwidthBytesOut(mostRecentRecord, bytesOutValue));
            mostRecentRecord.setLastBytesOutCount(bytesOutValue);
        }
    }

    private UsageRecord createNewUsageRecord(LoadBalancer loadBalancer) {
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

    private Long calculateCumBandwidthBytesIn(UsageRecord currentRecord, Long currentSnapshotValue) {
        return UsageCalculator.calculateCumulativeBytes(currentRecord.getTransferBytesIn(), currentRecord.getLastBytesInCount(), currentSnapshotValue);
    }

    private Long calculateCumBandwidthBytesOut(UsageRecord currentRecord, Long currentSnapshotValue) {
        return UsageCalculator.calculateCumulativeBytes(currentRecord.getTransferBytesOut(), currentRecord.getLastBytesOutCount(), currentSnapshotValue);
    }

    private Map<Integer, Integer> generateLbIdAccountIdMap(List<LoadBalancer> loadBalancers) {
        Map<Integer, Integer> lbIdAccountIdMap = new HashMap<Integer, Integer>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            lbIdAccountIdMap.put(loadBalancer.getId(), loadBalancer.getAccountId());
        }
        return lbIdAccountIdMap;
    }

    private Map<Integer, UsageRecord> convertUsagesToMap(List<UsageRecord> usages) {
        Map<Integer, UsageRecord> usagesAsMap = new HashMap<Integer, UsageRecord>();
        for (UsageRecord usage : usages) {
            usagesAsMap.put(usage.getLoadBalancer().getId(), usage);
        }
        return usagesAsMap;
    }
}
