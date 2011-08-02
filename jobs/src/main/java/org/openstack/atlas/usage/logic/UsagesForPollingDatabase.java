package org.openstack.atlas.usage.logic;

import org.openstack.atlas.usage.helpers.AdapterNameHelper;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UsagesForPollingDatabase {
    private final Log LOG = LogFactory.getLog(UsagesForPollingDatabase.class);
    private List<String> loadBalancerNames;
    private Map<String, Long> bytesInMap;
    private Map<String, Long> bytesOutMap;
    private Map<String, Integer> currentConnectionsMap;
    private Calendar pollTime;
    private Map<Integer, LoadBalancerUsage> usagesAsMap;
    private List<LoadBalancerUsage> recordsToInsert;
    private List<LoadBalancerUsage> recordsToUpdate;

    public UsagesForPollingDatabase(List<String> loadBalancerNames, Map<String, Long> bytesInMap, Map<String, Long> bytesOutMap, Map<String, Integer> currentConnectionsMap, Calendar pollTime, Map<Integer, LoadBalancerUsage> usagesAsMap) {
        this.loadBalancerNames = loadBalancerNames;
        this.bytesInMap = bytesInMap;
        this.bytesOutMap = bytesOutMap;
        this.currentConnectionsMap = currentConnectionsMap;
        this.pollTime = pollTime;
        this.usagesAsMap = usagesAsMap;
    }

    public List<LoadBalancerUsage> getRecordsToInsert() {
        return recordsToInsert;
    }

    public List<LoadBalancerUsage> getRecordsToUpdate() {
        return recordsToUpdate;
    }

    public UsagesForPollingDatabase invoke() {
        recordsToInsert = new ArrayList<LoadBalancerUsage>();
        recordsToUpdate = new ArrayList<LoadBalancerUsage>();

        for (String name : loadBalancerNames) {
            try {
                Integer accountId = AdapterNameHelper.stripAccountIdFromName(name);
                Integer lbId = AdapterNameHelper.stripLbIdFromName(name);

                if (!usagesAsMap.containsKey(lbId)) {
                    // Case 1: No record exists at all. Create one.
                    LoadBalancerUsage newRecord = createNewUsageRecord(name, accountId, lbId);
                    recordsToInsert.add(newRecord);
                } else {
                    LoadBalancerUsage currentRecord = usagesAsMap.get(lbId);
                    if (currentRecord.getEndTime().get(Calendar.HOUR_OF_DAY) != pollTime.get(Calendar.HOUR_OF_DAY) && pollTime.after(currentRecord.getEndTime())) {
                        // Case 2: A record exists but need to create new one because current hour is later than endTime hour.
                        LoadBalancerUsage newRecord = createNewUsageRecord(name, accountId, lbId);
                        // Copy over tags from last record.
                        newRecord.setNumVips(currentRecord.getNumVips());
                        newRecord.setTags(currentRecord.getTags());
                        recordsToInsert.add(newRecord);
                        // Add bandwidth that occurred between currentRecord endTime and newRecord startTime to currentRecord
                        currentRecord.setCumulativeBandwidthBytesIn(calculateCumBandwidthBytesIn(currentRecord, bytesInMap.get(name)));
                        currentRecord.setCumulativeBandwidthBytesOut(calculateCumBandwidthBytesOut(currentRecord, bytesOutMap.get(name)));
                        recordsToUpdate.add(currentRecord);
                    } else {
                        // Case 3: A record exists and we need to update because current day is the same as endTime day.
                        updateCurrentRecord(name, currentRecord);
                        recordsToUpdate.add(currentRecord);
                    }
                }
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Ignoring usage record...", name));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Ignoring usage record...", name));
            }
        }
        return this;
    }

    public void updateCurrentRecord(String name, LoadBalancerUsage currentRecord) {

        Integer oldNumPolls = currentRecord.getNumberOfPolls();
        Integer newNumPolls = oldNumPolls + 1;
        currentRecord.setEndTime(pollTime);
        currentRecord.setNumberOfPolls(newNumPolls);

        if (oldNumPolls == 0) { // polls will equal 0 only when an event occurs, thus we act as if usage data is brand new
            currentRecord.setAverageConcurrentConnections(currentConnectionsMap.get(name).doubleValue());
            currentRecord.setCumulativeBandwidthBytesIn(0l);
            currentRecord.setCumulativeBandwidthBytesOut(0l);
        } else {
            currentRecord.setAverageConcurrentConnections(UsageCalculator.calculateNewAverage(currentRecord.getAverageConcurrentConnections(), oldNumPolls, currentConnectionsMap.get(name)));
            currentRecord.setCumulativeBandwidthBytesIn(calculateCumBandwidthBytesIn(currentRecord, bytesInMap.get(name)));
            currentRecord.setCumulativeBandwidthBytesOut(calculateCumBandwidthBytesOut(currentRecord, bytesOutMap.get(name)));
        }
        
        currentRecord.setLastBandwidthBytesIn(bytesInMap.get(name));
        currentRecord.setLastBandwidthBytesOut(bytesOutMap.get(name));
    }

    private LoadBalancerUsage createNewUsageRecord(String name, Integer accountId, Integer lbId) {
        LoadBalancerUsage newRecord = new LoadBalancerUsage();
        newRecord.setAccountId(accountId);
        newRecord.setLoadbalancerId(lbId);
        newRecord.setAverageConcurrentConnections(currentConnectionsMap.get(name).doubleValue());
        newRecord.setCumulativeBandwidthBytesIn(0l);
        newRecord.setCumulativeBandwidthBytesOut(0l);
        newRecord.setLastBandwidthBytesIn(bytesInMap.get(name));
        newRecord.setLastBandwidthBytesOut(bytesOutMap.get(name));
        newRecord.setStartTime(pollTime);
        newRecord.setEndTime(pollTime);
        newRecord.setNumberOfPolls(1);
        // TODO: Query main DB for vip and tags info
        newRecord.setNumVips(1);
        newRecord.setTags(0);
        newRecord.setEventType(null);
        return newRecord;
    }

    public Long calculateCumBandwidthBytesIn(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesIn()) {
            return currentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue - currentRecord.getLastBandwidthBytesIn();
        } else {
            return currentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue;
        }
    }

    public Long calculateCumBandwidthBytesOut(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesOut()) {
            return currentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue - currentRecord.getLastBandwidthBytesOut();
        } else {
            return currentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue;
        }
    }
}
