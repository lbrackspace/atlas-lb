package org.openstack.atlas.usage.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;

import java.util.*;

public class UsageEventProcessor {
    private static final Log LOG = LogFactory.getLog(UsageEventProcessor.class);

    private LoadBalancerUsageRepository hourlyUsageRepository;
    private LoadBalancerRepository loadBalancerRepository;
    private UsageRepository rollupUsageRepository;

    private List<LoadBalancerUsage> usagesToCreate;
    private List<LoadBalancerUsage> usagesToUpdate;
    private List<LoadBalancerUsageEvent> inOrderUsageEventEntries;

    public UsageEventProcessor(List<LoadBalancerUsageEvent> inOrderUsageEventEntries, LoadBalancerUsageRepository hourlyUsageRepository, UsageRepository rollupUsageRepository, LoadBalancerRepository loadBalancerRepository) {
        this.inOrderUsageEventEntries = inOrderUsageEventEntries;
        this.hourlyUsageRepository = hourlyUsageRepository;
        this.rollupUsageRepository = rollupUsageRepository;
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
        LOG.info(String.format("Processing %d usage events...", inOrderUsageEventEntries.size()));

        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = createEventUsageMap();
        Map<Integer, LoadBalancerUsage> recentUsageMap = createRecentUsageMap(newEventUsageMap.keySet());

        updateTimestampsAndTagsForUsages(newEventUsageMap, recentUsageMap);

        LOG.info(String.format("%d usage events processed.", inOrderUsageEventEntries.size()));
        return this;
    }

    private void updateTimestampsAndTagsForUsages(Map<Integer, List<LoadBalancerUsage>> newEventUsageMap, Map<Integer, LoadBalancerUsage> recentUsageMap) {
        for (Integer lbId : newEventUsageMap.keySet()) {
            List<LoadBalancerUsage> loadBalancerUsages = newEventUsageMap.get(lbId);

            if (recentUsageMap.containsKey(lbId)) {
                final LoadBalancerUsage recentUsage = recentUsageMap.get(lbId);
                final LoadBalancerUsage firstNewUsage = loadBalancerUsages.get(0);

                // Update recent usage end time
                Calendar newEndTimeForRecentUsage = calculateEndTime(recentUsage.getEndTime(), firstNewUsage.getStartTime());
                if(!recentUsage.getEndTime().equals(newEndTimeForRecentUsage)) {
                    recentUsage.setEndTime(newEndTimeForRecentUsage);
                    usagesToUpdate.add(recentUsage);
                }

                // New records may be needed if we are near the hour mark or if poller goes down.
                List<LoadBalancerUsage> bufferRecords = createBufferRecordsIfNeeded(recentUsage, firstNewUsage);
                mutateCumulativeFields(recentUsage, bufferRecords, firstNewUsage);
                if (!bufferRecords.isEmpty()) usagesToCreate.addAll(bufferRecords);

                // Update the tags to the proper tags.
                UsageEvent usageEvent = UsageEvent.valueOf(firstNewUsage.getEventType());
                int updatedTags = calculateTags(recentUsage.getAccountId(), lbId, usageEvent, recentUsage);
                firstNewUsage.setTags(updatedTags);
            } else {
                final Usage recentUsage = rollupUsageRepository.getMostRecentUsageForLoadBalancer(lbId);
                if (recentUsage != null) {
                    final LoadBalancerUsage firstNewUsage = loadBalancerUsages.get(0);

                    // Update the tags to the proper tags.
                    UsageEvent usageEvent = UsageEvent.valueOf(firstNewUsage.getEventType());
                    int updatedTags = calculateTags(recentUsage.getAccountId(), lbId, usageEvent, recentUsage);
                    firstNewUsage.setTags(updatedTags);
                }
            }

            for (int i = 0; i < loadBalancerUsages.size(); i++) {
                if (i < loadBalancerUsages.size() - 1) {
                    LoadBalancerUsage firstUsage = loadBalancerUsages.get(i);
                    LoadBalancerUsage secondUsage = loadBalancerUsages.get(i + 1);

                    // Update firstUsage end time add it to the create list
                    Calendar newEndTimeForRecentUsage = calculateEndTime(firstUsage.getEndTime(), secondUsage.getStartTime());
                    firstUsage.setEndTime(newEndTimeForRecentUsage);
                    usagesToCreate.add(firstUsage);

                    // New records may be needed if we are near the hour mark or if poller goes down.
                    List<LoadBalancerUsage> bufferRecords = createBufferRecordsIfNeeded(firstUsage, secondUsage);
                    mutateCumulativeFields(firstUsage, bufferRecords, secondUsage);
                    if (!bufferRecords.isEmpty()) usagesToCreate.addAll(bufferRecords);

                    // Update the tags to the proper tags.
                    UsageEvent usageEvent = UsageEvent.valueOf(secondUsage.getEventType());
                    int updatedTags = calculateTags(firstUsage.getAccountId(), lbId, usageEvent, firstUsage);
                    secondUsage.setTags(updatedTags);
                } else {
                    // Add last record whose timestamps are the same.
                    usagesToCreate.add(loadBalancerUsages.get(i));
                }
            }
        }
    }

    public static void mutateCumulativeFields(LoadBalancerUsage previousUsage, List<LoadBalancerUsage> bufferRecords, LoadBalancerUsage nextUsage) {
        final int MILLISECONDS_PER_HOUR = 60000;
        // Update cumulative fields if the previousUsage and nextUsage are less than an hour apart or if there are no buffer records.
        // This prevents usage records from having a ton of usage if we weren't polling for an extended period of time, but ensures
        // that we properly calculate cumulative fields.
        if (bufferRecords.isEmpty() || Math.abs(nextUsage.getStartTime().getTimeInMillis() - previousUsage.getEndTime().getTimeInMillis()) < MILLISECONDS_PER_HOUR) {
            if (previousUsage.getLastBandwidthBytesIn() != null && nextUsage.getLastBandwidthBytesIn() != null) {
                final Long updatedCumBandwidthBytesIn = UsageCalculator.calculateCumBandwidthBytesIn(previousUsage, nextUsage.getLastBandwidthBytesIn());
                previousUsage.setCumulativeBandwidthBytesIn(updatedCumBandwidthBytesIn);
            }

            if (previousUsage.getLastBandwidthBytesInSsl() != null && nextUsage.getLastBandwidthBytesInSsl() != null) {
                final Long updatedCumBandwidthBytesInSsl = UsageCalculator.calculateCumBandwidthBytesInSsl(previousUsage, nextUsage.getLastBandwidthBytesInSsl());
                previousUsage.setCumulativeBandwidthBytesInSsl(updatedCumBandwidthBytesInSsl);
            }

            if (previousUsage.getLastBandwidthBytesOut() != null && nextUsage.getLastBandwidthBytesOut() != null) {
                final Long updatedCumBandwidthBytesOut = UsageCalculator.calculateCumBandwidthBytesOut(previousUsage, nextUsage.getLastBandwidthBytesOut());
                previousUsage.setCumulativeBandwidthBytesOut(updatedCumBandwidthBytesOut);
            }

            if (previousUsage.getLastBandwidthBytesOutSsl() != null && nextUsage.getLastBandwidthBytesOutSsl() != null) {
                final Long updatedCumBandwidthBytesOutSsl = UsageCalculator.calculateCumBandwidthBytesOutSsl(previousUsage, nextUsage.getLastBandwidthBytesOutSsl());
                previousUsage.setCumulativeBandwidthBytesOutSsl(updatedCumBandwidthBytesOutSsl);
            }
        }
    }

    public static List<LoadBalancerUsage> createBufferRecordsIfNeeded(LoadBalancerUsage previousUsage, LoadBalancerUsage nextUsage) {
        if (nextUsage.getStartTime().before(previousUsage.getEndTime())) {
            LOG.error(String.format("Usages are out of order! Usage id: %d, Usage endTime: %s, Next Usage id: %d, Next usage startTime: %s,", previousUsage.getId(), previousUsage.getEndTime().getTime(), nextUsage.getId(), nextUsage.getStartTime().getTime()));
//            throw new RuntimeException("cd!");
        }

        List<LoadBalancerUsage> bufferRecords = new ArrayList<LoadBalancerUsage>();

        Calendar previousRecordsEndTime = (Calendar) previousUsage.getEndTime().clone();
        Calendar nextUsagesStartTime = (Calendar) nextUsage.getStartTime().clone();

        while (previousRecordsEndTime.before(nextUsagesStartTime)) {
            if (isEndOfHour(previousRecordsEndTime)) {
                if (previousRecordsEndTime.before(nextUsagesStartTime)
                        && previousRecordsEndTime.get(Calendar.HOUR_OF_DAY) == nextUsagesStartTime.get(Calendar.HOUR_OF_DAY)
                        && previousRecordsEndTime.get(Calendar.DAY_OF_MONTH) == nextUsagesStartTime.get(Calendar.DAY_OF_MONTH)
                        && previousRecordsEndTime.get(Calendar.YEAR) == nextUsagesStartTime.get(Calendar.YEAR)
                        ) {
                    // We need a buffer record for the beginning of the hour.
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, nextUsagesStartTime);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) nextUsagesStartTime.clone();
                } else if (previousRecordsEndTime.getTimeInMillis() != nextUsagesStartTime.getTimeInMillis()) {
                    // We need a buffer record for the whole hour.
                    Calendar newEndTimeForBufferRecord = calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
                }
            } else {
                // We need a buffer record for the end of the hour.
                Calendar newEndTimeForBufferRecord = calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
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
        newBufferRecord.setTags(recentUsage.getTags());
        newBufferRecord.setNumVips(recentUsage.getNumVips());
        newBufferRecord.setStartTime((Calendar) previousRecordsEndTime.clone());
        newBufferRecord.setEndTime((Calendar) newEndTimeForBufferRecord.clone());
        if (UsageEvent.SUSPEND_LOADBALANCER.name().equals(recentUsage.getEventType()) || UsageEvent.SUSPENDED_LOADBALANCER.name().equals(recentUsage.getEventType())) {
            newBufferRecord.setEventType(UsageEvent.SUSPENDED_LOADBALANCER.name());
        }
        return newBufferRecord;
    }

    public static boolean isEndOfHour(Calendar calendar) {
        return calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0 && calendar.get(Calendar.MILLISECOND) == 0;
    }

    public static Calendar calculateEndTime(Calendar recentUsageEndTime, Calendar nextUsageStartTime) {
        if (nextUsageStartTime.before(recentUsageEndTime)) {
            LOG.error(String.format("Usages are out of order! nextUsageStartTime: %s, recentUsageEndTime: %s,", nextUsageStartTime.getTime(), recentUsageEndTime.getTime()));
//            throw new RuntimeException("Usages are not in order!");
        }

        if (recentUsageEndTime.get(Calendar.HOUR_OF_DAY) == nextUsageStartTime.get(Calendar.HOUR_OF_DAY)
                && recentUsageEndTime.get(Calendar.DAY_OF_MONTH) == nextUsageStartTime.get(Calendar.DAY_OF_MONTH)
                && recentUsageEndTime.get(Calendar.MONTH) == nextUsageStartTime.get(Calendar.MONTH)
                && recentUsageEndTime.get(Calendar.YEAR) == nextUsageStartTime.get(Calendar.YEAR)
                && (recentUsageEndTime.get(Calendar.MINUTE) != 0
                || recentUsageEndTime.get(Calendar.SECOND) != 0
                || recentUsageEndTime.get(Calendar.MILLISECOND) != 0)) {
            return nextUsageStartTime;
        }

        // Return a new end time that reaches the very end of the hour
        Calendar newEndTime = Calendar.getInstance();
        newEndTime.setTime(recentUsageEndTime.getTime());
        if ((recentUsageEndTime.get(Calendar.HOUR_OF_DAY) != nextUsageStartTime.get(Calendar.HOUR_OF_DAY)
                || recentUsageEndTime.get(Calendar.DAY_OF_MONTH) != nextUsageStartTime.get(Calendar.DAY_OF_MONTH)
                || recentUsageEndTime.get(Calendar.MONTH) != nextUsageStartTime.get(Calendar.MONTH)
                || recentUsageEndTime.get(Calendar.YEAR) != nextUsageStartTime.get(Calendar.YEAR))
                ||(newEndTime.get(Calendar.MINUTE) != 0
                || newEndTime.get(Calendar.SECOND) != 0
                || newEndTime.get(Calendar.MILLISECOND) != 0)) {
            newEndTime.set(Calendar.MINUTE, 59);
            newEndTime.set(Calendar.SECOND, 59);
            newEndTime.set(Calendar.MILLISECOND, 999);
            newEndTime.add(Calendar.MILLISECOND, 1);
        }
        return newEndTime;
    }

    private Map<Integer, List<LoadBalancerUsage>> createEventUsageMap() {
        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = new HashMap<Integer, List<LoadBalancerUsage>>();

        for (LoadBalancerUsageEvent inOrderUsageEventEntry : inOrderUsageEventEntries) {
            Integer key = inOrderUsageEventEntry.getLoadbalancerId();
            if (newEventUsageMap.containsKey(key)) {
                LoadBalancerUsage newUsage = createNewUsageFromEvent(inOrderUsageEventEntry);
                newEventUsageMap.get(key).add(newUsage);
            } else {
                List<LoadBalancerUsage> usages = new ArrayList<LoadBalancerUsage>();
                LoadBalancerUsage newUsage = createNewUsageFromEvent(inOrderUsageEventEntry);
                usages.add(newUsage);
                newEventUsageMap.put(key, usages);
            }
        }

        return newEventUsageMap;
    }

    public LoadBalancerUsage createNewUsageFromEvent(LoadBalancerUsageEvent inOrderUsageEventEntry) {
        LoadBalancerUsage newUsage = new LoadBalancerUsage();

        newUsage.setAccountId(inOrderUsageEventEntry.getAccountId());
        newUsage.setLoadbalancerId(inOrderUsageEventEntry.getLoadbalancerId());
        newUsage.setNumVips(inOrderUsageEventEntry.getNumVips());
        newUsage.setStartTime(inOrderUsageEventEntry.getStartTime());
        newUsage.setEndTime(inOrderUsageEventEntry.getStartTime());
        if(inOrderUsageEventEntry.getEventType().equals(UsageEvent.SUSPEND_LOADBALANCER.name()) ||
                inOrderUsageEventEntry.getEventType().equals(UsageEvent.SUSPENDED_LOADBALANCER.name())) {
            newUsage.getEndTime().add(Calendar.SECOND, 1);
        }
        newUsage.setNumberOfPolls(0);
        newUsage.setTags(0); // Will most likely change in 2nd pass
        newUsage.setEventType(inOrderUsageEventEntry.getEventType());
        newUsage.setLastBandwidthBytesIn(inOrderUsageEventEntry.getLastBandwidthBytesIn());
        newUsage.setLastBandwidthBytesInSsl(inOrderUsageEventEntry.getLastBandwidthBytesInSsl());
        newUsage.setLastBandwidthBytesOut(inOrderUsageEventEntry.getLastBandwidthBytesOut());
        newUsage.setLastBandwidthBytesOutSsl(inOrderUsageEventEntry.getLastBandwidthBytesOutSsl());

        return newUsage;
    }

    public Map<Integer, LoadBalancerUsage> createRecentUsageMap(Set<Integer> loadBalancerIds) {
        Map<Integer, LoadBalancerUsage> recentUsageMap = new HashMap<Integer, LoadBalancerUsage>();

        for (Integer loadBalancerId : loadBalancerIds) {
            LoadBalancerUsage mostRecentUsageForLoadBalancer = hourlyUsageRepository.getMostRecentUsageForLoadBalancer(loadBalancerId);
            if (mostRecentUsageForLoadBalancer != null)
                recentUsageMap.put(loadBalancerId, mostRecentUsageForLoadBalancer);
        }

        return recentUsageMap;
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, Usage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTags());
        } else {
            tags = new BitTags();
        }

        return calculateTags(accountId, lbId, usageEvent, tags);
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, LoadBalancerUsage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTags());
        } else {
            tags = new BitTags();
        }

        return calculateTags(accountId, lbId, usageEvent, tags);
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, BitTags bitTags) {
        BitTags tags = new BitTags(bitTags.getBitTags());

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
