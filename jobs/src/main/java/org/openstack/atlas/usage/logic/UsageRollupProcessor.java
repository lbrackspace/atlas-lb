package org.openstack.atlas.usage.logic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.Tier;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.usage.helpers.ConfigurationKeys;
import org.openstack.atlas.usage.helpers.EsbConfiguration;
import org.openstack.atlas.usage.helpers.TimeZoneHelper;

import java.util.*;

public class UsageRollupProcessor {
    private static final Log LOG = LogFactory.getLog(UsageRollupProcessor.class);

    private UsageRepository rollUpUsageRepository;

    private List<Usage> usagesToCreate;
    private List<Usage> usagesToUpdate;
    private Configuration configuration = new EsbConfiguration();
    private List<LoadBalancerUsage> inOrderPollingUsages;

    public UsageRollupProcessor(List<LoadBalancerUsage> inOrderPollingUsages, UsageRepository rollUpUsageRepository) {
        this.inOrderPollingUsages = inOrderPollingUsages;
        this.rollUpUsageRepository = rollUpUsageRepository;
        this.usagesToCreate = new ArrayList<Usage>();
        this.usagesToUpdate = new ArrayList<Usage>();
    }

    public List<Usage> getUsagesToCreate() {
        return usagesToCreate;
    }

    public List<Usage> getUsagesToUpdate() {
        return usagesToUpdate;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public UsageRollupProcessor process() {
        LOG.info(String.format("Processing %d polling usages...", inOrderPollingUsages.size()));

        Map<Integer, List<LoadBalancerUsage>> newUsageMap = generateLbIdUsagesMap(inOrderPollingUsages);
        Map<Integer, Usage> recentUsageMap = createRecentUsageMap(newUsageMap.keySet());
        Map<Integer, List<UsagesForHour>> contiguousDailyUsageMap = generateContiguousLbIdUsagesMap(newUsageMap, recentUsageMap);

        updateTimestampsAndTagsForUsages(contiguousDailyUsageMap, recentUsageMap);

        LOG.info(String.format("%d polling usages processed.", inOrderPollingUsages.size()));
        return this;
    }

    private void updateTimestampsAndTagsForUsages(Map<Integer, List<UsagesForHour>> newUsageMap, Map<Integer, Usage> recentUsageMap) {
        final String timeZoneCode = configuration.getString(ConfigurationKeys.usage_timezone_code);

        for (Integer lbId : newUsageMap.keySet()) {
            final Usage recentUsage = recentUsageMap.get(lbId);

            for (UsagesForHour usagesForHour : newUsageMap.get(lbId)) {
                List<Usage> mergedUsageRecords;

                if (recentUsage != null 
                        && usagesForHour.getDayOfYear() == TimeZoneHelper.getCalendarForTimeZone(recentUsage.getStartTime(), TimeZone.getTimeZone(timeZoneCode)).get(Calendar.DAY_OF_YEAR)
                        && usagesForHour.getHourOfDay() == TimeZoneHelper.getCalendarForTimeZone(recentUsage.getStartTime(), TimeZone.getTimeZone(timeZoneCode)).get(Calendar.HOUR_OF_DAY)) {
                    mergedUsageRecords = mergeUsageRecords(usagesForHour.getUsages(), recentUsage);
                } else {
                    mergedUsageRecords = mergeUsageRecords(usagesForHour.getUsages(), null);
                }

                for (Usage mergedUsageRecord : mergedUsageRecords) {
                    if (mergedUsageRecord.getId() == null) {
                        usagesToCreate.add(mergedUsageRecord);
                    } else {
                        usagesToUpdate.add(mergedUsageRecord);
                    }
                }
            }
        }
    }

    private List<Usage> mergeUsageRecords(List<LoadBalancerUsage> newUsages, Usage recentUsage) {
        List<Usage> mergedUsageRecords = new ArrayList<Usage>();
        boolean canAddCurrUsageRecord = false;
        Usage latestCustomerFacingRecord;

        if (recentUsage == null) latestCustomerFacingRecord = Usage.createNullUsageRecord();
        else latestCustomerFacingRecord = recentUsage;

        for (LoadBalancerUsage usageRecordToMerge : newUsages) {
            Tier.Level usageToMergeTier = Tier.calculateTierLevel(usageRecordToMerge.getAverageConcurrentConnections()); // Only usage non-ssl traffic for tier calculation
            Tier.Level currUsageTier = Tier.calculateTierLevel(latestCustomerFacingRecord.getAverageConcurrentConnections());
            boolean aUsageHasNoPolls = (usageRecordToMerge.getNumberOfPolls() == 0 || latestCustomerFacingRecord.getNumberOfPolls() == 0);

            if (((latestCustomerFacingRecord.getEventType() == null && usageRecordToMerge.getEventType() == null)
                    || (latestCustomerFacingRecord.getEventType() != null && usageRecordToMerge.getEventType() == null)
                    || (UsageEvent.SUSPEND_LOADBALANCER.name().equals(latestCustomerFacingRecord.getEventType()) && UsageEvent.SUSPEND_LOADBALANCER.name().equals(usageRecordToMerge.getEventType()))
                    || (UsageEvent.SUSPENDED_LOADBALANCER.name().equals(latestCustomerFacingRecord.getEventType()) && UsageEvent.SUSPENDED_LOADBALANCER.name().equals(usageRecordToMerge.getEventType())))
                    && usageRecordToMerge.getTags().equals(latestCustomerFacingRecord.getTags())
                    && usageRecordToMerge.getNumVips().equals(latestCustomerFacingRecord.getNumVips())
                    && (usageToMergeTier.equals(currUsageTier) || aUsageHasNoPolls)) {
                // Merge records if tags, vips and tier are the same and eventsType are both null or first is not null and second is...or the have the same suspension event type
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

    public Map<Integer, List<UsagesForHour>> generateContiguousLbIdUsagesMap(Map<Integer, List<LoadBalancerUsage>> newUsageMap, Map<Integer, Usage> recentUsageMap) {
        Map<Integer, List<UsagesForHour>> lbIdUsageMap = new HashMap<Integer, List<UsagesForHour>>();

        for (Integer lbId : newUsageMap.keySet()) {
            List<LoadBalancerUsage> contiguousUsages = new ArrayList<LoadBalancerUsage>();
            final List<LoadBalancerUsage> loadBalancerUsagesForId = newUsageMap.get(lbId);
            Usage recentUsage = recentUsageMap.get(lbId);

            if (recentUsage != null) {
                final LoadBalancerUsage firstNewUsage = loadBalancerUsagesForId.get(0);
                List<LoadBalancerUsage> bufferRecords = UsageRollupProcessor.createBufferRecordsIfNeeded(recentUsage, firstNewUsage);
                contiguousUsages.addAll(bufferRecords);
            }

            for (int i = 0; i < loadBalancerUsagesForId.size(); i++) {
                if (i < loadBalancerUsagesForId.size() - 1) {
                    LoadBalancerUsage firstUsage = loadBalancerUsagesForId.get(i);
                    LoadBalancerUsage secondUsage = loadBalancerUsagesForId.get(i + 1);
                    List<LoadBalancerUsage> bufferRecords = UsageEventProcessor.createBufferRecordsIfNeeded(firstUsage, secondUsage);
                    contiguousUsages.add(firstUsage);
                    contiguousUsages.addAll(bufferRecords);
                } else {
                    LoadBalancerUsage lastUsage = loadBalancerUsagesForId.get(i);

                    Calendar maxedOutEndTime = (Calendar) lastUsage.getEndTime().clone();
                    maxedOutEndTime.set(Calendar.MINUTE, 59);
                    maxedOutEndTime.set(Calendar.SECOND, 59);
                    maxedOutEndTime.set(Calendar.MILLISECOND, 999);
                    maxedOutEndTime.add(Calendar.MILLISECOND, 1);
                    lastUsage.setEndTime(maxedOutEndTime);

                    contiguousUsages.add(lastUsage);
                }
            }

            List<UsagesForHour> contiguousUsagesByHour = generateUsagesPerHourList(contiguousUsages, recentUsage);
            lbIdUsageMap.put(lbId, contiguousUsagesByHour);
        }

        return lbIdUsageMap;
    }

    private Map<Integer, List<LoadBalancerUsage>> generateLbIdUsagesMap(List<LoadBalancerUsage> inOrderUsages) {
        Map<Integer, List<LoadBalancerUsage>> lbIdUsageMap = new HashMap<Integer, List<LoadBalancerUsage>>();

        for (LoadBalancerUsage inOrderUsage : inOrderUsages) {
            Integer key = inOrderUsage.getLoadbalancerId();
            if (lbIdUsageMap.containsKey(key)) {
                lbIdUsageMap.get(key).add(inOrderUsage);
            } else {
                List<LoadBalancerUsage> usages = new ArrayList<LoadBalancerUsage>();
                usages.add(inOrderUsage);
                lbIdUsageMap.put(key, usages);
            }
        }

        return lbIdUsageMap;
    }

    private List<UsagesForHour> generateUsagesPerHourList(List<LoadBalancerUsage> usages, Usage recentUsage) {
        List<UsagesForHour> usagesPerHourList = new ArrayList<UsagesForHour>();

        for (LoadBalancerUsage usage : usages) {
            String timeZoneCode = configuration.getString(ConfigurationKeys.usage_timezone_code);
            Calendar startTimeForTimeZone = TimeZoneHelper.getCalendarForTimeZone(usage.getStartTime(), TimeZone.getTimeZone(timeZoneCode));
            int dayOfYear = startTimeForTimeZone.get(Calendar.DAY_OF_YEAR);
            int hourOfDay = startTimeForTimeZone.get(Calendar.HOUR_OF_DAY);

            boolean addedUsageRecord = false;
            for (UsagesForHour usagesForHourOfYear : usagesPerHourList) {
                if (usagesForHourOfYear.getDayOfYear() == dayOfYear && usagesForHourOfYear.getHourOfDay() == hourOfDay) {
                    usagesForHourOfYear.getUsages().add(usage);
                    addedUsageRecord = true;
                }
            }

            if (!addedUsageRecord) {
                UsagesForHour usagesForHour = new UsagesForHour();
                usagesForHour.setDayOfYear(dayOfYear);
                usagesForHour.setHourOfDay(hourOfDay);
                usagesForHour.getUsages().add(usage);
                usagesPerHourList.add(usagesForHour);
            }
        }

        // Update suspended loadbalancer events to suspend loadbalancer events if in the same hour. Due to the creation of buffer records.
        for (UsagesForHour usagesForHour : usagesPerHourList) {
            boolean suspendStatus = false;

            if(recentUsage != null && recentUsage.getStartTime().get(Calendar.HOUR_OF_DAY) == usagesForHour.getHourOfDay()
                    && recentUsage.getStartTime().get(Calendar.DAY_OF_YEAR) == usagesForHour.getDayOfYear()) {
                suspendStatus = true;
            }

            for (LoadBalancerUsage loadBalancerUsage : usagesForHour.getUsages()) {
                if (UsageEvent.SUSPEND_LOADBALANCER.name().equals(loadBalancerUsage.getEventType())) suspendStatus = true;
                if (suspendStatus && UsageEvent.SUSPENDED_LOADBALANCER.name().equals(loadBalancerUsage.getEventType())) loadBalancerUsage.setEventType(UsageEvent.SUSPEND_LOADBALANCER.name());
            }
        }

        return usagesPerHourList;
    }

    private Map<Integer, Usage> createRecentUsageMap(Set<Integer> loadBalancerIds) {
        Map<Integer, Usage> lbIdRollupUsageMap = new HashMap<Integer, Usage>();

        List<Usage> rollUpUsages = rollUpUsageRepository.getMostRecentUsageForLoadBalancers(loadBalancerIds); // TODO: Batch? One by One?

        for (Usage rollUpUsage : rollUpUsages) {
            lbIdRollupUsageMap.put(rollUpUsage.getLoadbalancer().getId(), rollUpUsage);
        }

        return lbIdRollupUsageMap;
    }

    public static List<LoadBalancerUsage> createBufferRecordsIfNeeded(Usage previousUsage, LoadBalancerUsage nextUsage) {
        if (nextUsage.getStartTime().before(previousUsage.getEndTime())) {
            LOG.error(String.format("Usages are out of order! Usage id: %d, Usage endTime: %s, Next Usage id: %d, Next usage startTime: %s,", previousUsage.getId(), previousUsage.getEndTime().getTime(), nextUsage.getId(), nextUsage.getStartTime().getTime()));
//            throw new RuntimeException("Usages are not in order!");
        }

        List<LoadBalancerUsage> bufferRecords = new ArrayList<LoadBalancerUsage>();

        Calendar previousRecordsEndTime = (Calendar) previousUsage.getEndTime().clone();
        Calendar nextUsagesStartTime = (Calendar) nextUsage.getStartTime().clone();

        while (previousRecordsEndTime.before(nextUsagesStartTime)) {
            if (UsageEventProcessor.isEndOfHour(previousRecordsEndTime)) {
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
                    Calendar newEndTimeForBufferRecord = UsageEventProcessor.calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
                }
            } else {
                // We need a buffer record for the end of the hour.
                Calendar newEndTimeForBufferRecord = UsageEventProcessor.calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                bufferRecords.add(newBufferRecord);
                previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
            }
        }

        return bufferRecords;
    }

    private static LoadBalancerUsage instantiateAndPopulateBufferRecord(Usage recentUsage, Calendar previousRecordsEndTime, Calendar newEndTimeForBufferRecord) {
        LoadBalancerUsage newBufferRecord = new LoadBalancerUsage();
        newBufferRecord.setAccountId(recentUsage.getAccountId());
        newBufferRecord.setLoadbalancerId(recentUsage.getLoadbalancer().getId());
        newBufferRecord.setTags(recentUsage.getTags());
        newBufferRecord.setNumVips(recentUsage.getNumVips());
        newBufferRecord.setStartTime((Calendar) previousRecordsEndTime.clone());
        newBufferRecord.setEndTime((Calendar) newEndTimeForBufferRecord.clone());
        if(UsageEvent.SUSPEND_LOADBALANCER.name().equals(recentUsage.getEventType()) || UsageEvent.SUSPENDED_LOADBALANCER.name().equals(recentUsage.getEventType())) {
            newBufferRecord.setEventType(UsageEvent.SUSPENDED_LOADBALANCER.name());
        }
        return newBufferRecord;
    }
}
