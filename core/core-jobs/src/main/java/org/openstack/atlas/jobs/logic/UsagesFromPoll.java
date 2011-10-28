package org.openstack.atlas.jobs.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UsagesFromPoll {
    private final Log LOG = LogFactory.getLog(UsagesFromPoll.class);
    private List<LoadBalancer> loadBalancers;
    private Map<Integer, Long> bytesInMap;
    private Map<Integer, Long> bytesOutMap;
    private Calendar pollTime;
    private Map<Integer, UsageRecord> usagesAsMap;
    private List<UsageRecord> recordsToInsert;
    private List<UsageRecord> recordsToUpdate;

    public UsagesFromPoll(List<LoadBalancer> loadBalancers, Map<Integer, Long> bytesInMap, Map<Integer, Long> bytesOutMap, Calendar pollTime, Map<Integer, UsageRecord> usagesAsMap) {
        this.loadBalancers = loadBalancers;
        this.bytesInMap = bytesInMap;
        this.bytesOutMap = bytesOutMap;
        this.pollTime = pollTime;
        this.usagesAsMap = usagesAsMap;
    }

    public List<UsageRecord> getRecordsToInsert() {
        return recordsToInsert;
    }

    public List<UsageRecord> getRecordsToUpdate() {
        return recordsToUpdate;
    }

    public UsagesFromPoll invoke() {
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
        return this;
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
}
