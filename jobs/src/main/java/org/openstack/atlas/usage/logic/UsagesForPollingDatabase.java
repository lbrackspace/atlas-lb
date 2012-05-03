package org.openstack.atlas.usage.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.usage.helpers.LoadBalancerNameMap;

import java.util.*;

public class UsagesForPollingDatabase {
    private final Log LOG = LogFactory.getLog(UsagesForPollingDatabase.class);
    private LoadBalancerRepository loadBalancerRepository;
    private Collection<LoadBalancerNameMap> loadBalancerNameMaps;
    private Map<String, Long> bytesInMap;
    private Map<String, Long> bytesOutMap;
    private Map<String, Integer> currentConnectionsMap;
    private Map<String, Long> bytesInMapSsl;
    private Map<String, Long> bytesOutMapSsl;
    private Map<String, Integer> currentConnectionsMapSsl;
    private Calendar pollTime;
    private Map<Integer, LoadBalancerUsage> usagesAsMap;
    private List<LoadBalancerUsage> recordsToInsert;
    private List<LoadBalancerUsage> recordsToUpdate;

    public UsagesForPollingDatabase(LoadBalancerRepository loadBalancerRepository, Collection<LoadBalancerNameMap> loadBalancerNameMaps, Map<String, Long> bytesInMap, Map<String, Long> bytesOutMap, Map<String, Integer> currentConnectionsMap, Map<String, Long> bytesInMapSsl, Map<String, Long> bytesOutMapSsl, Map<String, Integer> currentConnectionsMapSsl, Calendar pollTime, Map<Integer, LoadBalancerUsage> usagesAsMap) {
        this.loadBalancerRepository = loadBalancerRepository;
        this.loadBalancerNameMaps = loadBalancerNameMaps;
        this.bytesInMap = bytesInMap;
        this.bytesOutMap = bytesOutMap;
        this.currentConnectionsMap = currentConnectionsMap;
        this.bytesInMapSsl = bytesInMapSsl;
        this.bytesOutMapSsl = bytesOutMapSsl;
        this.currentConnectionsMapSsl = currentConnectionsMapSsl;
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

        for (LoadBalancerNameMap loadBalancerNameMap : loadBalancerNameMaps) {
            try {
                Integer accountId = loadBalancerNameMap.getAccountId();
                Integer lbId = loadBalancerNameMap.getLoadBalancerId();

                if (!usagesAsMap.containsKey(lbId)) {
                    // Case 1: No record exists at all. Create one.
                    LoadBalancerUsage newRecord = createNewUsageRecord(loadBalancerNameMap, accountId, lbId);
                    recordsToInsert.add(newRecord);
                } else {
                    LoadBalancerUsage currentRecord = usagesAsMap.get(lbId);
                    if (currentRecord.getEndTime().get(Calendar.HOUR_OF_DAY) != pollTime.get(Calendar.HOUR_OF_DAY) && pollTime.after(currentRecord.getEndTime())) {
                        // Case 2: A record exists but need to create new one because current hour is later than endTime hour.
                        LoadBalancerUsage newRecord = createNewUsageRecord(loadBalancerNameMap, accountId, lbId);
                        // Copy over tags from last record.
                        newRecord.setNumVips(currentRecord.getNumVips());
                        newRecord.setTags(currentRecord.getTags());
                        recordsToInsert.add(newRecord);
                        // Add bandwidth that occurred between currentRecord endTime and newRecord startTime to currentRecord
                        currentRecord.setCumulativeBandwidthBytesIn(calculateCumBandwidthBytesIn(currentRecord, bytesInMap.get(loadBalancerNameMap.getNonSslVirtualServerName())));
                        currentRecord.setCumulativeBandwidthBytesOut(calculateCumBandwidthBytesOut(currentRecord, bytesOutMap.get(loadBalancerNameMap.getNonSslVirtualServerName())));
                        currentRecord.setCumulativeBandwidthBytesInSsl(calculateCumBandwidthBytesInSsl(currentRecord, bytesInMapSsl.get(loadBalancerNameMap.getSslVirtualServerName())));
                        currentRecord.setCumulativeBandwidthBytesOutSsl(calculateCumBandwidthBytesOutSsl(currentRecord, bytesOutMapSsl.get(loadBalancerNameMap.getSslVirtualServerName())));
                        recordsToUpdate.add(currentRecord);
                    } else {
                        // Case 3: A record exists and we need to update because current day is the same as endTime day.
                        updateCurrentRecord(loadBalancerNameMap, currentRecord);
                        recordsToUpdate.add(currentRecord);
                    }
                }
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%d'. Ignoring usage record...", loadBalancerNameMap.getLoadBalancerId()));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%d'. Ignoring usage record...", loadBalancerNameMap.getLoadBalancerId()));
            }
        }
        return this;
    }

    public void updateCurrentRecord(LoadBalancerNameMap loadBalancerNameMap, LoadBalancerUsage currentRecord) {

        Integer oldNumPolls = currentRecord.getNumberOfPolls();
        Integer newNumPolls = oldNumPolls + 1;
        currentRecord.setEndTime(pollTime);
        currentRecord.setNumberOfPolls(newNumPolls);

        if (oldNumPolls == 0) { // polls will equal 0 only when an event occurs, thus we act as if avg ccs are new
            if (currentConnectionsMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) currentRecord.setAverageConcurrentConnections(currentConnectionsMap.get(loadBalancerNameMap.getNonSslVirtualServerName()).doubleValue());
            if (currentConnectionsMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) currentRecord.setAverageConcurrentConnectionsSsl(currentConnectionsMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()).doubleValue());
        } else {
            if (currentConnectionsMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) currentRecord.setAverageConcurrentConnections(UsageCalculator.calculateNewAverage(currentRecord.getAverageConcurrentConnections(), oldNumPolls, currentConnectionsMap.get(loadBalancerNameMap.getNonSslVirtualServerName())));
            if (currentConnectionsMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) currentRecord.setAverageConcurrentConnectionsSsl(UsageCalculator.calculateNewAverage(currentRecord.getAverageConcurrentConnectionsSsl(), oldNumPolls, currentConnectionsMapSsl.get(loadBalancerNameMap.getSslVirtualServerName())));
            else currentRecord.setAverageConcurrentConnectionsSsl(UsageCalculator.calculateNewAverage(currentRecord.getAverageConcurrentConnectionsSsl(), oldNumPolls, 0));
        }

        currentRecord.setCumulativeBandwidthBytesIn(calculateCumBandwidthBytesIn(currentRecord, bytesInMap.get(loadBalancerNameMap.getNonSslVirtualServerName())));
        currentRecord.setCumulativeBandwidthBytesInSsl(calculateCumBandwidthBytesInSsl(currentRecord, bytesInMapSsl.get(loadBalancerNameMap.getSslVirtualServerName())));
        currentRecord.setCumulativeBandwidthBytesOut(calculateCumBandwidthBytesOut(currentRecord, bytesOutMap.get(loadBalancerNameMap.getNonSslVirtualServerName())));
        currentRecord.setCumulativeBandwidthBytesOutSsl(calculateCumBandwidthBytesOutSsl(currentRecord, bytesOutMapSsl.get(loadBalancerNameMap.getSslVirtualServerName())));

        if (bytesInMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) currentRecord.setLastBandwidthBytesIn(bytesInMap.get(loadBalancerNameMap.getNonSslVirtualServerName()));
        if (bytesInMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) currentRecord.setLastBandwidthBytesInSsl(bytesInMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()));
        if (bytesOutMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) currentRecord.setLastBandwidthBytesOut(bytesOutMap.get(loadBalancerNameMap.getNonSslVirtualServerName()));
        if (bytesOutMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) currentRecord.setLastBandwidthBytesOutSsl(bytesOutMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()));
    }

    private LoadBalancerUsage createNewUsageRecord(LoadBalancerNameMap loadBalancerNameMap, Integer accountId, Integer lbId) {
        LoadBalancerUsage newRecord = new LoadBalancerUsage();
        newRecord.setAccountId(accountId);
        newRecord.setLoadbalancerId(lbId);
        if (currentConnectionsMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) newRecord.setAverageConcurrentConnections(currentConnectionsMap.get(loadBalancerNameMap.getNonSslVirtualServerName()).doubleValue());
        if (currentConnectionsMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) newRecord.setAverageConcurrentConnectionsSsl(currentConnectionsMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()).doubleValue());
        newRecord.setCumulativeBandwidthBytesIn(0l);
        newRecord.setCumulativeBandwidthBytesOut(0l);
        newRecord.setCumulativeBandwidthBytesInSsl(0l);
        newRecord.setCumulativeBandwidthBytesOutSsl(0l);
        if (bytesInMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) newRecord.setLastBandwidthBytesIn(bytesInMap.get(loadBalancerNameMap.getNonSslVirtualServerName()));
        if (bytesInMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) newRecord.setLastBandwidthBytesInSsl(bytesInMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()));
        if (bytesOutMap.get(loadBalancerNameMap.getNonSslVirtualServerName()) != null) newRecord.setLastBandwidthBytesOut(bytesOutMap.get(loadBalancerNameMap.getNonSslVirtualServerName()));
        if (bytesOutMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()) != null) newRecord.setLastBandwidthBytesOutSsl(bytesOutMapSsl.get(loadBalancerNameMap.getSslVirtualServerName()));
        newRecord.setStartTime(pollTime);
        newRecord.setEndTime(pollTime);
        newRecord.setNumberOfPolls(1);
        newRecord.setNumVips(1); // TODO: Query main DB for vip info
        newRecord.setTags(0);
        newRecord.setEventType(null);

        if (isServiceNetLoadBalancer(accountId, lbId)) {
            BitTags bitTags = new BitTags();
            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            newRecord.setTags(bitTags.getBitTags());
        }

        return newRecord;
    }

    public Long calculateCumBandwidthBytesIn(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesIn();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesIn()) {
            return currentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue - currentRecord.getLastBandwidthBytesIn();
        } else {
            return currentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue;
        }
    }

    public Long calculateCumBandwidthBytesInSsl(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesInSsl();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesInSsl()) {
            return currentRecord.getCumulativeBandwidthBytesInSsl() + currentSnapshotValue - currentRecord.getLastBandwidthBytesInSsl();
        } else {
            return currentRecord.getCumulativeBandwidthBytesInSsl() + currentSnapshotValue;
        }
    }

    public Long calculateCumBandwidthBytesOut(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesOut();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesOut()) {
            return currentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue - currentRecord.getLastBandwidthBytesOut();
        } else {
            return currentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue;
        }
    }

    public Long calculateCumBandwidthBytesOutSsl(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesOutSsl();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesOutSsl()) {
            return currentRecord.getCumulativeBandwidthBytesOutSsl() + currentSnapshotValue - currentRecord.getLastBandwidthBytesOutSsl();
        } else {
            return currentRecord.getCumulativeBandwidthBytesOutSsl() + currentSnapshotValue;
        }
    }

    private boolean isServiceNetLoadBalancer(Integer accountId, Integer lbId) {
        try {
            final Set<VirtualIp> vipsByAccountIdLoadBalancerId = loadBalancerRepository.getVipsByAccountIdLoadBalancerId(accountId, lbId);

            for (VirtualIp virtualIp : vipsByAccountIdLoadBalancerId) {
                if (virtualIp.getVipType().equals(VirtualIpType.SERVICENET)) return true;
            }

        } catch (EntityNotFoundException e) {
            return false;
        } catch (DeletedStatusException e) {
            return false;
        }

        return false;
    }
}
