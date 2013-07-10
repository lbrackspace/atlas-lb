package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.RollupUsageHelper;
import org.openstack.atlas.util.common.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;

@Component
public class UsageRollupProcessorImpl implements UsageRollupProcessor {
    final org.apache.commons.logging.Log LOG = LogFactory.getLog(UsageRollupProcessorImpl.class);

    @Autowired
    private UsageRepository usageRepository;
    @Autowired
    private LoadBalancerService loadbalancerService;

    private Map<Integer, Integer> tagsCache = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> numVipsCache = new HashMap<Integer, Integer>();
    private Set<Integer> suspendedLbsCache = new HashSet<Integer>();

    @Override
    public Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> groupUsagesByLbIdAccountId(List<LoadBalancerMergedHostUsage> lbMergedHostUsages) {
        Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> usagesByLbId = new HashMap<LbIdAccountId, List<LoadBalancerMergedHostUsage>>();

        for (LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage : lbMergedHostUsages) {
            List<LoadBalancerMergedHostUsage> usageList;
            LbIdAccountId key = new LbIdAccountId(LoadBalancerMergedHostUsage.getLoadbalancerId(), LoadBalancerMergedHostUsage.getAccountId());

            if (!usagesByLbId.containsKey(key)) {
                usageList = new ArrayList<LoadBalancerMergedHostUsage>();
                usagesByLbId.put(key, usageList);
            }

            usageList = usagesByLbId.get(key);
            usageList.add(LoadBalancerMergedHostUsage);
        }

        return usagesByLbId;
    }

    @Override
    public List<Usage> processRecords(List<LoadBalancerMergedHostUsage> lbMergedHostUsages, Calendar hourToProcess, Set<LbIdAccountId> lbsActiveDuringHour) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        Map<LbIdAccountId, List<LoadBalancerMergedHostUsage>> usagesByLbId = groupUsagesByLbIdAccountId(lbMergedHostUsages);

        for (LbIdAccountId lbActiveDuringHour : lbsActiveDuringHour) {
            if (!usagesByLbId.containsKey(lbActiveDuringHour)) {
                usagesByLbId.put(lbActiveDuringHour, new ArrayList<LoadBalancerMergedHostUsage>());
            }
        }

        for (LbIdAccountId lbIdAccountId : usagesByLbId.keySet()) {
            List<LoadBalancerMergedHostUsage> lbMergedHostRecordsForLoadBalancer = usagesByLbId.get(lbIdAccountId);

            List<Usage> processedRecordsForLb = processRecordsForLb(lbIdAccountId.getAccountId(), lbIdAccountId.getLbId(), lbMergedHostRecordsForLoadBalancer, hourToProcess);
            processedRecords.addAll(processedRecordsForLb);
        }

        return processedRecords;
    }


    /*
        @param lbMergedHostUsageRecordsForLoadBalancer: Expected to be in order by pollTime.
     */
    @Override
    public List<Usage> processRecordsForLb(Integer accountId, Integer lbId, List<LoadBalancerMergedHostUsage> lbMergedHostUsageRecordsForLoadBalancer, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();
        Calendar previousHour;
        Calendar hourToStop;
        boolean previousHourRecordExists = false;
        boolean isFirstRecordOfHourToProcess = true;
        LoadBalancerMergedHostUsage mostRecentPreviousRecord = null;

        hourToProcess = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        previousHour = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        previousHour.add(Calendar.HOUR, -1);
        hourToStop = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        hourToStop.add(Calendar.HOUR, 1);

        if (lbMergedHostUsageRecordsForLoadBalancer == null || lbMergedHostUsageRecordsForLoadBalancer.isEmpty()) {
            Usage zeroedOutUsage = initializeRecordForLb(accountId, lbId, hourToProcess, hourToStop);
            if (zeroedOutUsage.getEventType() == null && suspendedLbsCache.contains(lbId)) {
                zeroedOutUsage.setEventType(SUSPENDED_LOADBALANCER.name());
            }
            processedRecords.add(zeroedOutUsage);
            return processedRecords;
        }

        final LoadBalancerMergedHostUsage firstRecordInList = lbMergedHostUsageRecordsForLoadBalancer.get(0);

        if (firstRecordInList.getEventType() != null
                && firstRecordInList.getEventType().equals(CREATE_LOADBALANCER)
                && firstRecordInList.getPollTime().compareTo(hourToStop) >= 0) {
            return processedRecords;
        }

        Usage newRecordForLb = initializeRecordForLb(firstRecordInList, hourToProcess, hourToStop);

        for (LoadBalancerMergedHostUsage lbMergedHostUsage : lbMergedHostUsageRecordsForLoadBalancer) {
            Calendar pollTime = lbMergedHostUsage.getPollTime();
            boolean equalToHourToProcess = pollTime.equals(hourToProcess);
            UsageEvent event = lbMergedHostUsage.getEventType();
            boolean recordHasEvent = event != null;
            boolean withinHourToProcess = CalendarUtils.isBetween(pollTime, hourToProcess, hourToStop, true);
            boolean withinPreviousHour = CalendarUtils.isBetween(pollTime, previousHour, hourToProcess, false);
            boolean allowedToAddBandwidth = true;

            if (pollTime.before(hourToProcess)) {
                if (withinPreviousHour) {
                    previousHourRecordExists = true;
                    mostRecentPreviousRecord = lbMergedHostUsage;

                    if (mostRecentPreviousRecord.getEventType() != null) {
                        if (mostRecentPreviousRecord.getEventType().equals(DELETE_LOADBALANCER)) {
                            return processedRecords;
                        } else if (mostRecentPreviousRecord.getEventType().equals(SUSPEND_LOADBALANCER) || mostRecentPreviousRecord.getEventType().equals(SUSPENDED_LOADBALANCER)) {
                            newRecordForLb.setEventType(SUSPENDED_LOADBALANCER.name());
                            suspendedLbsCache.add(lbId);
                        } else {
                            newRecordForLb.setEventType(null);
                        }

                        if (mostRecentPreviousRecord.getEventType().equals(UNSUSPEND_LOADBALANCER)) {
                            suspendedLbsCache.remove(lbId);
                        }

                        if (mostRecentPreviousRecord.getEventType().equals(CREATE_VIRTUAL_IP) || mostRecentPreviousRecord.getEventType().equals(DELETE_VIRTUAL_IP)) {
                            newRecordForLb.setNumVips(lbMergedHostUsage.getNumVips());
                            numVipsCache.put(lbId, lbMergedHostUsage.getNumVips());
                        }
                    }

                }
                newRecordForLb.setTags(lbMergedHostUsage.getTagsBitmask());
                continue;
            }

            if ((!previousHourRecordExists && isFirstRecordOfHourToProcess) || equalToHourToProcess) {
                newRecordForLb = initializeRecordForLb(lbMergedHostUsage, hourToProcess, hourToStop);
                allowedToAddBandwidth = false;
            }

            if (withinHourToProcess) {
                RollupUsageHelper.calculateAndSetAverageConcurrentConnections(newRecordForLb, lbMergedHostUsage);

                if (allowedToAddBandwidth) {
                    RollupUsageHelper.calculateAndSetBandwidth(newRecordForLb, lbMergedHostUsage);
                }

                if (equalToHourToProcess && newRecordForLb.getNumberOfPolls() > 0) {
                    newRecordForLb.setNumberOfPolls(newRecordForLb.getNumberOfPolls() - 1);
                }

                if (recordHasEvent) {
                    if (event.equals(SUSPEND_LOADBALANCER) || event.equals(SUSPENDED_LOADBALANCER)) {
                        suspendedLbsCache.add(lbId);
                    } else if (event.equals(UNSUSPEND_LOADBALANCER)) {
                        suspendedLbsCache.remove(lbId);
                    }

                    if (event.equals(CREATE_LOADBALANCER) || equalToHourToProcess) {
                        processedRecords.clear();
                        newRecordForLb.setStartTime(pollTime);
                        newRecordForLb.setEventType(event.name());
                        newRecordForLb.setAverageConcurrentConnections(0.0);
                        newRecordForLb.setAverageConcurrentConnectionsSsl(0.0);
                        newRecordForLb.setIncomingTransfer(0l);
                        newRecordForLb.setIncomingTransferSsl(0l);
                        newRecordForLb.setOutgoingTransfer(0l);
                        newRecordForLb.setOutgoingTransferSsl(0l);

                        if (!equalToHourToProcess) {
                            newRecordForLb.setNumberOfPolls(1);
                        }
                    } else if (event.equals(SUSPENDED_LOADBALANCER)) {
                        if (previousHourRecordExists && mostRecentPreviousRecord.getEventType() != null && (mostRecentPreviousRecord.getEventType().equals(SUSPEND_LOADBALANCER) || mostRecentPreviousRecord.getEventType().equals(SUSPENDED_LOADBALANCER))) {
                            newRecordForLb.setEventType(SUSPENDED_LOADBALANCER.name());
                            newRecordForLb.setEndTime(hourToStop);
                            newRecordForLb.setNumberOfPolls(0);
                        }
                    } else {

                        if (isFirstRecordOfHourToProcess && !previousHourRecordExists) {
                            int tags = getTags(lbId);
                            int numVips = getNumVips(lbId);
                            newRecordForLb.setTags(tags);
                            newRecordForLb.setNumVips(numVips);
                        }

                        if (event.equals(UNSUSPEND_LOADBALANCER)) {
                            newRecordForLb.setNumberOfPolls(0);
                        }

                        newRecordForLb.setEndTime(pollTime);
                        processedRecords.add(newRecordForLb);

                        newRecordForLb = initializeRecordForLb(lbMergedHostUsage, pollTime, hourToStop);
                        newRecordForLb.setEventType(event.name());
                    }

                    if (event.equals(CREATE_VIRTUAL_IP) || event.equals(DELETE_VIRTUAL_IP)) {
                        newRecordForLb.setNumVips(lbMergedHostUsage.getNumVips());
                        numVipsCache.put(lbId, lbMergedHostUsage.getNumVips());
                    }

                    if (event.equals(DELETE_LOADBALANCER)) {
                        newRecordForLb.setEndTime(pollTime);
                    }
                }

                isFirstRecordOfHourToProcess = false;
            }
        }

        if (newRecordForLb.getStartTime().before(hourToStop)) {
            if (newRecordForLb.getEventType() == null && suspendedLbsCache.contains(lbId)) {
                newRecordForLb.setEventType(SUSPENDED_LOADBALANCER.name());
            }

            processedRecords.add(newRecordForLb);
        }

        return processedRecords;
    }

    private int getTags(Integer lbId) {
        int mostRecentTagsBitmask;

        try {
            if (!tagsCache.containsKey(lbId)) {
                Usage mostRecentUsageForLoadBalancer = usageRepository.getMostRecentUsageForLoadBalancer(lbId);
                mostRecentTagsBitmask = mostRecentUsageForLoadBalancer.getTags();
                tagsCache.put(lbId, mostRecentTagsBitmask);
            } else {
                mostRecentTagsBitmask = tagsCache.get(lbId);
            }
        } catch (EntityNotFoundException e) {
            // TODO: Put an alert and monitor it!
            LOG.error("Unable to get proper tags for record. Please verify manually!", e);
            BitTags bitTags = loadbalancerService.getCurrentBitTags(lbId);
            bitTags.flipTagOff(BitTag.SSL);
            bitTags.flipTagOff(BitTag.SSL_MIXED_MODE);
            mostRecentTagsBitmask = bitTags.toInt();
            tagsCache.put(lbId, mostRecentTagsBitmask);
        }

        return mostRecentTagsBitmask;
    }

    private int getNumVips(Integer lbId) {
        final int DEFAULT_NUM_VIPS = 1;
        int numVips = DEFAULT_NUM_VIPS;

        try {
            if (!numVipsCache.containsKey(lbId)) {
                Usage mostRecentUasageForLoadBalancer = usageRepository.getMostRecentUsageForLoadBalancer(lbId);
                numVips = mostRecentUasageForLoadBalancer.getNumVips();
                numVipsCache.put(lbId, numVips);
            } else {
                numVips = numVipsCache.get(lbId);
            }
        } catch (EntityNotFoundException e) {
            // TODO: Put an alert and monitor it!
            LOG.error("Unable to get proper vips for record. Please verify manually!", e);
            numVipsCache.put(lbId, numVips);
        }

        return numVips;
    }

    private Usage initializeRecordForLb(Integer accountId, Integer loadbalancerId, Calendar startTime, Calendar endTime) {
        Usage usage = new Usage();

        LoadBalancer lb = new LoadBalancer();
        lb.setAccountId(accountId);
        lb.setId(loadbalancerId);

        usage.setLoadbalancer(lb);
        usage.setAccountId(accountId);
        usage.setStartTime(startTime);
        usage.setEndTime(endTime);
        usage.setTags(getTags(loadbalancerId));
        usage.setNumVips(getNumVips(loadbalancerId));
        usage.setCorrected(false);
        usage.setNeedsPushed(true);
        usage.setEntryVersion(0);

        return usage;
    }

    private Usage initializeRecordForLb(LoadBalancerMergedHostUsage lbMergedHostUsage, Calendar startTime, Calendar endTime) {
        Usage usage = new Usage();

        LoadBalancer lb = new LoadBalancer();
        lb.setId(lbMergedHostUsage.getLoadbalancerId());
        lb.setAccountId(lbMergedHostUsage.getAccountId());

        usage.setLoadbalancer(lb);
        usage.setAccountId(lbMergedHostUsage.getAccountId());
        usage.setStartTime(startTime);
        usage.setEndTime(endTime);
        usage.setTags(lbMergedHostUsage.getTagsBitmask());
        usage.setNumVips(lbMergedHostUsage.getNumVips());
        usage.setCorrected(false);
        usage.setNeedsPushed(true);
        usage.setEntryVersion(0);

        return usage;
    }
}
