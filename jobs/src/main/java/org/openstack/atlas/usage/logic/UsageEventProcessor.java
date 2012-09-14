package org.openstack.atlas.usage.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;
import org.openstack.atlas.usage.helpers.LoadBalancerNameMap;

import java.util.*;

public class UsageEventProcessor {
    private final Log LOG = LogFactory.getLog(UsageEventProcessor.class);
    private final int BATCH_SIZE = 100;

    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private HostRepository hostRepository;
    private LoadBalancerUsageRepository hourlyUsageRepository;
    private UsageRepository rollupUsageRepository;
    private LoadBalancerUsageEventRepository usageEventRepository;

    private LoadBalancerRepository loadBalancerRepository;
    private Collection<LoadBalancerNameMap> loadBalancerNameMaps;
    private Map<String, Long> bytesInMap;
    private Map<String, Long> bytesOutMap;
    private Map<String, Integer> currentConnectionsMap;
    private Map<String, Long> bytesInMapSsl;
    private Map<String, Long> bytesOutMapSsl;
    private Map<String, Integer> currentConnectionsMapSsl;
    private Calendar pollTime;
    private Map<Integer, LoadBalancerUsage> hourlyUsagesAsMap;
    private Map<Integer, Usage> rollupUsagesAsMap;
    private List<LoadBalancerUsage> usagesToCreate;
    private List<LoadBalancerUsage> usagesToUpdate;
    private List<LoadBalancerUsageEvent> inOrderUsageEventEntries;

    public UsageEventProcessor(List<LoadBalancerUsageEvent> inOrderUsageEventEntries, LoadBalancerUsageRepository hourlyUsageRepository, LoadBalancerRepository loadBalancerRepository) {
        this.inOrderUsageEventEntries = inOrderUsageEventEntries;
        this.hourlyUsageRepository = hourlyUsageRepository;
        this.loadBalancerRepository = loadBalancerRepository;
        this.usagesToCreate = new ArrayList<LoadBalancerUsage>();
        this.usagesToUpdate = new ArrayList<LoadBalancerUsage>();
    }

    public List<LoadBalancerUsage> getUsagesToCreate() {
        return this.usagesToCreate;
    }

    public List<LoadBalancerUsage> getUsagesToUpdate() {
        return this.usagesToUpdate;
    }

    public UsageEventProcessor process() {
        LOG.info("Processing usage events...");

        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = createEventUsageMap();
        Map<Integer, LoadBalancerUsage> recentUsageMap = createRecentUsageMap(newEventUsageMap.keySet());

        updateTimestampsAndTagsForUsages(newEventUsageMap, recentUsageMap);


        //
        //
        //
        //



/*        for (LoadBalancerUsageEvent usageEventEntry : inOrderUsageEventEntries) {
            UsageEvent usageEvent = UsageEvent.valueOf(usageEventEntry.getEventType());
            LoadBalancerUsage previousHourlyUsageRecord = getPreviousHourlyUsageRecord(usageEventEntry, newEventUsageMap);

            int updatedTags = getTags(usageEventEntry.getAccountId(), usageEventEntry.getLoadbalancerId(), usageEvent, previousHourlyUsageRecord);

            Calendar eventTime;
            if (previousHourlyUsageRecord != null && previousHourlyUsageRecord.getEndTime().after(usageEventEntry.getStartTime())) {
                eventTime = Calendar.getInstance();
            } else {
                eventTime = usageEventEntry.getStartTime();
            }

            LoadBalancerUsage newUsage = new LoadBalancerUsage();
            newUsage.setAccountId(usageEventEntry.getAccountId());
            newUsage.setLoadbalancerId(usageEventEntry.getLoadbalancerId());
            newUsage.setNumVips(usageEventEntry.getNumVips());
            newUsage.setStartTime(eventTime);
            newUsage.setEndTime(eventTime);
            newUsage.setNumberOfPolls(0);
            newUsage.setTags(updatedTags);
            newUsage.setEventType(usageEventEntry.getEventType());

            if (previousHourlyUsageRecord != null) {
                Integer oldNumPolls = previousHourlyUsageRecord.getNumberOfPolls();
                Integer newNumPolls = (previousHourlyUsageRecord.getId() != null) ? oldNumPolls + 1 : 1; // If it hasn't been created then its only 1 poll

                if (usageEventEntry.getLastBandwidthBytesIn() != null) {
                    previousHourlyUsageRecord.setCumulativeBandwidthBytesIn(UsageCalculator.calculateCumBandwidthBytesIn(previousHourlyUsageRecord, usageEventEntry.getLastBandwidthBytesIn()));
                    previousHourlyUsageRecord.setLastBandwidthBytesIn(usageEventEntry.getLastBandwidthBytesIn());
                    previousHourlyUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastBandwidthBytesOut() != null) {
                    previousHourlyUsageRecord.setCumulativeBandwidthBytesOut(UsageCalculator.calculateCumBandwidthBytesOut(previousHourlyUsageRecord, usageEventEntry.getLastBandwidthBytesOut()));
                    previousHourlyUsageRecord.setLastBandwidthBytesOut(usageEventEntry.getLastBandwidthBytesOut());
                    previousHourlyUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastBandwidthBytesInSsl() != null) {
                    previousHourlyUsageRecord.setCumulativeBandwidthBytesInSsl(UsageCalculator.calculateCumBandwidthBytesInSsl(previousHourlyUsageRecord, usageEventEntry.getLastBandwidthBytesInSsl()));
                    previousHourlyUsageRecord.setLastBandwidthBytesInSsl(usageEventEntry.getLastBandwidthBytesInSsl());
                    previousHourlyUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastBandwidthBytesOutSsl() != null) {
                    previousHourlyUsageRecord.setCumulativeBandwidthBytesOutSsl(UsageCalculator.calculateCumBandwidthBytesOutSsl(previousHourlyUsageRecord, usageEventEntry.getLastBandwidthBytesOutSsl()));
                    previousHourlyUsageRecord.setLastBandwidthBytesOutSsl(usageEventEntry.getLastBandwidthBytesOutSsl());
                    previousHourlyUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastConcurrentConnections() != null) {
                    if(UsageEvent.SSL_ONLY_ON.name().equals(previousHourlyUsageRecord.getEventType())) {
                        previousHourlyUsageRecord.setAverageConcurrentConnections(0.0);
                    } else {
                        previousHourlyUsageRecord.setAverageConcurrentConnections(UsageCalculator.calculateNewAverage(previousHourlyUsageRecord.getAverageConcurrentConnections(), oldNumPolls, usageEventEntry.getLastConcurrentConnections()));
                    }
                    previousHourlyUsageRecord.setNumberOfPolls(newNumPolls);
                    previousHourlyUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastConcurrentConnectionsSsl() != null) {
                    if(UsageEvent.SSL_OFF.name().equals(previousHourlyUsageRecord.getEventType())) {
                        previousHourlyUsageRecord.setAverageConcurrentConnectionsSsl(0.0);
                    } else {
                        previousHourlyUsageRecord.setAverageConcurrentConnectionsSsl(UsageCalculator.calculateNewAverage(previousHourlyUsageRecord.getAverageConcurrentConnectionsSsl(), oldNumPolls, usageEventEntry.getLastConcurrentConnectionsSsl()));
                    }
                    previousHourlyUsageRecord.setNumberOfPolls(newNumPolls);
                    previousHourlyUsageRecord.setEndTime(eventTime);
                }

                if (previousHourlyUsageRecord.getId() != null) {
                    usagesToUpdate.add(previousHourlyUsageRecord);
                }

                newUsage.setLastBandwidthBytesIn(previousHourlyUsageRecord.getLastBandwidthBytesIn());
                newUsage.setLastBandwidthBytesInSsl(previousHourlyUsageRecord.getLastBandwidthBytesInSsl());
                newUsage.setLastBandwidthBytesOut(previousHourlyUsageRecord.getLastBandwidthBytesOut());
                newUsage.setLastBandwidthBytesOutSsl(previousHourlyUsageRecord.getLastBandwidthBytesOutSsl());
            }

            if (newEventUsageMap.containsKey(newUsage.getLoadbalancerId())) {
                newEventUsageMap.get(newUsage.getLoadbalancerId()).add(newUsage);
            } else {
                List<LoadBalancerUsage> recentUsagesForLb = new ArrayList<LoadBalancerUsage>();
                recentUsagesForLb.add(newUsage);
                newEventUsageMap.put(newUsage.getLoadbalancerId(), recentUsagesForLb);
            }
        }

        // Move usages over to array
        for (Integer lbId : newEventUsageMap.keySet()) {
            for (LoadBalancerUsage loadBalancerUsage : newEventUsageMap.get(lbId)) {
                usagesToCreate.add(loadBalancerUsage);
            }
        }

        if (!usagesToCreate.isEmpty()) hourlyUsageRepository.batchCreate(usagesToCreate);
        if (!usagesToUpdate.isEmpty()) hourlyUsageRepository.batchUpdate(usagesToUpdate);

        try {
            BatchAction<LoadBalancerUsageEvent> deleteEventUsagesAction = new BatchAction<LoadBalancerUsageEvent>() {
                public void execute(Collection<LoadBalancerUsageEvent> usageEventEntries) throws Exception {
                    usageEventRepository.batchDelete(usageEventEntries);
                }
            };
            ExecutionUtilities.executeInBatches(inOrderUsageEventEntries, BATCH_SIZE, deleteEventUsagesAction);
        } catch (Exception e) {
            LOG.error("Exception occurred while deleting usage event entries.", e);
        }

        LOG.info(String.format("%d usage events processed.", usagesToCreate.size()));*/

        return this;
    }

    private void updateTimestampsAndTagsForUsages(Map<Integer, List<LoadBalancerUsage>> newEventUsageMap, Map<Integer, LoadBalancerUsage> recentUsageMap) {
        for (Integer lbId : newEventUsageMap.keySet()) {
            List<LoadBalancerUsage> loadBalancerUsages = newEventUsageMap.get(lbId);

            if (recentUsageMap.containsKey(lbId)) {
                final LoadBalancerUsage recentUsage = recentUsageMap.get(lbId);

                // Update recent usage end time
                Calendar newEndTimeForRecentUsage = calculateEndTime(recentUsage.getEndTime(), loadBalancerUsages.get(0).getStartTime());
                recentUsage.setEndTime(newEndTimeForRecentUsage);
                usagesToUpdate.add(recentUsage);

                // New records may be needed if we are near the hour mark or if poller goes down.
                List<LoadBalancerUsage> bufferRecords = createBufferRecordsIfNeeded(recentUsage, loadBalancerUsages.get(0));
                if (!bufferRecords.isEmpty()) usagesToCreate.addAll(bufferRecords);
            }

            for (int i=0; i < loadBalancerUsages.size(); i++) {
                if (i < loadBalancerUsages.size()-1) {
                    LoadBalancerUsage firstUsage = loadBalancerUsages.get(i);
                    LoadBalancerUsage secondUsage = loadBalancerUsages.get(i+1);

                    // Update firstUsage end time add it to the create list
                    Calendar newEndTimeForRecentUsage = calculateEndTime(firstUsage.getEndTime(), secondUsage.getStartTime());
                    firstUsage.setEndTime(newEndTimeForRecentUsage);
                    usagesToCreate.add(firstUsage);

                    List<LoadBalancerUsage> bufferRecords = createBufferRecordsIfNeeded(firstUsage, secondUsage);
                    if (!bufferRecords.isEmpty()) usagesToCreate.addAll(bufferRecords);
                } else {
                    // Add last record whose timestamps are the same.
                    usagesToCreate.add(loadBalancerUsages.get(i));
                }
            }
        }
    }

    public static List<LoadBalancerUsage> createBufferRecordsIfNeeded(LoadBalancerUsage recentUsage, LoadBalancerUsage nextUsage) {
        assert(nextUsage.getStartTime().after(recentUsage.getEndTime())); // TODO: Figure out how to handle this error case.

        List<LoadBalancerUsage> bufferRecords = new ArrayList<LoadBalancerUsage>();

        Calendar previousRecordsEndTime = (Calendar) recentUsage.getEndTime().clone();
        Calendar nextUsagesStartTime = (Calendar) nextUsage.getStartTime().clone();

        while(previousRecordsEndTime.before(nextUsagesStartTime)) {
            if(isEndOfHour(previousRecordsEndTime)) {
                // Move previousRecordsEndTime to next hour since we don't need a buffer for this hour.
                previousRecordsEndTime.add(Calendar.MILLISECOND, 1);

                if(previousRecordsEndTime.before(nextUsagesStartTime)
                        && previousRecordsEndTime.get(Calendar.HOUR_OF_DAY) == nextUsagesStartTime.get(Calendar.HOUR_OF_DAY)
                        && previousRecordsEndTime.get(Calendar.DAY_OF_MONTH) == nextUsagesStartTime.get(Calendar.DAY_OF_MONTH)
                        && previousRecordsEndTime.get(Calendar.YEAR) == nextUsagesStartTime.get(Calendar.YEAR)
                        ) {
                    // We need a buffer record for the beginning of the hour.
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(recentUsage, previousRecordsEndTime, nextUsagesStartTime);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) nextUsagesStartTime.clone();
                } else if(previousRecordsEndTime.getTimeInMillis() != nextUsagesStartTime.getTimeInMillis()) {
                    // We need a buffer record for the whole hour.
                    Calendar newEndTimeForBufferRecord = calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(recentUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
                }
            } else {
                // We need a buffer record for the end of the hour.
                Calendar newEndTimeForBufferRecord = calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(recentUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                bufferRecords.add(newBufferRecord);
                previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
            }
        }
        
        return bufferRecords;
    }

    private static LoadBalancerUsage instantiateAndPopulateBufferRecord(LoadBalancerUsage recentUsage, Calendar previousRecordsEndTime, Calendar newEndTimeForBufferRecord) {
        LoadBalancerUsage newBufferRecord = new LoadBalancerUsage();
        newBufferRecord.setAccountId(recentUsage.getAccountId());
        newBufferRecord.setLoadbalancerId(recentUsage.getLoadbalancerId());
        newBufferRecord.setLastBandwidthBytesIn(recentUsage.getLastBandwidthBytesIn());
        newBufferRecord.setLastBandwidthBytesInSsl(recentUsage.getLastBandwidthBytesInSsl());
        newBufferRecord.setLastBandwidthBytesOut(recentUsage.getLastBandwidthBytesOut());
        newBufferRecord.setLastBandwidthBytesOutSsl(recentUsage.getLastBandwidthBytesOutSsl());
        newBufferRecord.setTags(recentUsage.getTags());
        newBufferRecord.setNumVips(recentUsage.getNumVips());
        newBufferRecord.setStartTime((Calendar) previousRecordsEndTime.clone());
        newBufferRecord.setEndTime((Calendar) newEndTimeForBufferRecord.clone());
        return newBufferRecord;
    }

    private static boolean isEndOfHour(Calendar calendar) {
        return calendar.get(Calendar.MINUTE) == 59 && calendar.get(Calendar.SECOND) == 59 && calendar.get(Calendar.MILLISECOND) == 999;
    }

    public static Calendar calculateEndTime(Calendar recentUsageEndTime, Calendar nextUsageStartTime) {
        assert(nextUsageStartTime.after(recentUsageEndTime)); // TODO: Figure out how to handle this error case.

        if (recentUsageEndTime.get(Calendar.HOUR_OF_DAY) == nextUsageStartTime.get(Calendar.HOUR_OF_DAY)
                && recentUsageEndTime.get(Calendar.DAY_OF_MONTH) == nextUsageStartTime.get(Calendar.DAY_OF_MONTH)
                && recentUsageEndTime.get(Calendar.MONTH) == nextUsageStartTime.get(Calendar.MONTH)
                && recentUsageEndTime.get(Calendar.YEAR) == nextUsageStartTime.get(Calendar.YEAR)) {
            return nextUsageStartTime;
        }
        
        // Return a new end time that reaches the very end of the hour
        Calendar newEndTime = Calendar.getInstance();
        newEndTime.setTime(recentUsageEndTime.getTime());
        newEndTime.set(Calendar.MINUTE, 59);
        newEndTime.set(Calendar.SECOND, 59);
        newEndTime.set(Calendar.MILLISECOND, 999);
        return newEndTime;
    }

    public Map<Integer, List<LoadBalancerUsage>> createEventUsageMap() {
        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = new HashMap<Integer, List<LoadBalancerUsage>>();

        for (LoadBalancerUsageEvent inOrderUsageEventEntry : inOrderUsageEventEntries) {
            Integer key = inOrderUsageEventEntry.getLoadbalancerId();
            if(newEventUsageMap.containsKey(key)) {
                LoadBalancerUsage newUsage = new LoadBalancerUsage();
                newUsage.setAccountId(inOrderUsageEventEntry.getAccountId());
                newUsage.setLoadbalancerId(inOrderUsageEventEntry.getLoadbalancerId());
                newUsage.setNumVips(inOrderUsageEventEntry.getNumVips());
                newUsage.setStartTime(inOrderUsageEventEntry.getStartTime());
                newUsage.setEndTime(inOrderUsageEventEntry.getStartTime()); // Will most likely change in 2nd pass
                newUsage.setNumberOfPolls(0);
                newUsage.setTags(0); // Will most likely change in 2nd pass
                newUsage.setEventType(inOrderUsageEventEntry.getEventType());

                newEventUsageMap.get(key).add(newUsage);
            } else {
                List<LoadBalancerUsage> usages = new ArrayList<LoadBalancerUsage>();
                LoadBalancerUsage newUsage = new LoadBalancerUsage();
                newUsage.setAccountId(inOrderUsageEventEntry.getAccountId());
                newUsage.setLoadbalancerId(inOrderUsageEventEntry.getLoadbalancerId());
                newUsage.setNumVips(inOrderUsageEventEntry.getNumVips());
                newUsage.setStartTime(inOrderUsageEventEntry.getStartTime());
                newUsage.setEndTime(inOrderUsageEventEntry.getStartTime()); // Will most likely change in 2nd pass
                newUsage.setNumberOfPolls(0);
                newUsage.setTags(0); // Will most likely change in 2nd pass
                newUsage.setEventType(inOrderUsageEventEntry.getEventType());
                usages.add(newUsage);
                newEventUsageMap.put(key, usages);
            }
        }

        return newEventUsageMap;
    }

    public Map<Integer, LoadBalancerUsage> createRecentUsageMap(Set<Integer> loadBalancerIds) {
        Map<Integer, LoadBalancerUsage> recentUsageMap = new HashMap<Integer, LoadBalancerUsage>();

        for (Integer loadBalancerId : loadBalancerIds) {
            LoadBalancerUsage mostRecentUsageForLoadBalancer = hourlyUsageRepository.getMostRecentUsageForLoadBalancer(loadBalancerId);
            recentUsageMap.put(loadBalancerId, mostRecentUsageForLoadBalancer);
        }

        return recentUsageMap;
    }

    private LoadBalancerUsage getPreviousHourlyUsageRecord(LoadBalancerUsageEvent usageEventEntry, Map<Integer, List<LoadBalancerUsage>> newEventUsageMap) {
        // return previous event that has yet to be created if it exists
        if (newEventUsageMap.containsKey(usageEventEntry.getLoadbalancerId())) {
            List<LoadBalancerUsage> loadBalancerUsagesForLb = newEventUsageMap.get(usageEventEntry.getLoadbalancerId());
            return (LoadBalancerUsage) loadBalancerUsagesForLb.toArray()[loadBalancerUsagesForLb.size()-1];
        }

        // will return null if it doesn't exist
        return hourlyUsageRepository.getMostRecentUsageForLoadBalancer(usageEventEntry.getLoadbalancerId());
    }

    public int getTags(Integer accountId, Integer lbId, UsageEvent usageEvent, LoadBalancerUsage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTags());
        } else {
            tags = new BitTags();
        }

        switch (usageEvent) {
            case CREATE_LOADBALANCER:
                tags.flipAllTagsOff();
                break;
            case DELETE_LOADBALANCER:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_OFF:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_ONLY_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_MIXED_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOn(BitTag.SSL_MIXED_MODE);
                break;
            default:
        }

        if (isServiceNetLoadBalancer(accountId, lbId)) {
            tags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return tags.getBitTags();
    }

    public boolean isServiceNetLoadBalancer(Integer accountId, Integer lbId) {
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
