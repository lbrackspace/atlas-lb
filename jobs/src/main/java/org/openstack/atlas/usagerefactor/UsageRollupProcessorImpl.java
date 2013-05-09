package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.RollupUsageHelper;
import org.openstack.atlas.util.common.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.openstack.atlas.service.domain.events.UsageEvent.*;

public class UsageRollupProcessorImpl implements UsageRollupProcessor {
    final org.apache.commons.logging.Log LOG = LogFactory.getLog(UsageRollupProcessorImpl.class);

    @Autowired
    private UsageRepository usageRepository;
    @Autowired
    private LoadBalancerService loadbalancerService;

    @Override
    public Map<Integer, List<LoadBalancerMergedHostUsage>> groupUsagesByLbId(List<LoadBalancerMergedHostUsage> lbMergedHostUsages) {
        Map<Integer, List<LoadBalancerMergedHostUsage>> usagesByLbId = new HashMap<Integer, List<LoadBalancerMergedHostUsage>>();

        for (LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage : lbMergedHostUsages) {
            List<LoadBalancerMergedHostUsage> usageList;

            if (!usagesByLbId.containsKey(LoadBalancerMergedHostUsage.getLoadbalancerId())) {
                usageList = new ArrayList<LoadBalancerMergedHostUsage>();
                usagesByLbId.put(LoadBalancerMergedHostUsage.getLoadbalancerId(), usageList);
            }

            usageList = usagesByLbId.get(LoadBalancerMergedHostUsage.getLoadbalancerId());
            usageList.add(LoadBalancerMergedHostUsage);
        }

        return usagesByLbId;
    }

    @Override
    public List<Usage> processRecords(List<LoadBalancerMergedHostUsage> lbMergedHostUsages, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        if (lbMergedHostUsages == null || lbMergedHostUsages.isEmpty()) {
            return processedRecords;
        }

        Map<Integer, List<LoadBalancerMergedHostUsage>> usagesByLbId = groupUsagesByLbId(lbMergedHostUsages);

        for (Integer lbId : usagesByLbId.keySet()) {
            List<LoadBalancerMergedHostUsage> lbMergedHostRecordsForLoadBalancer = usagesByLbId.get(lbId);

            List<Usage> processedRecordsForLb = processRecsForLb(lbMergedHostRecordsForLoadBalancer, hourToProcess);
            processedRecords.addAll(processedRecordsForLb);
        }

        return processedRecords;
    }


    public List<Usage> processRecsForLb(List<LoadBalancerMergedHostUsage> lbMergedHostUsageRecordsForLoadBalancer, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();
        Calendar previousHour;
        Calendar hourToStop;
        boolean previousHourRecordExists = false;
        boolean isFirstRecordOfHourToProcess = true;

        if (lbMergedHostUsageRecordsForLoadBalancer == null || lbMergedHostUsageRecordsForLoadBalancer.isEmpty()) {
            return processedRecords;
        }

        hourToProcess = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        previousHour = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        previousHour.add(Calendar.HOUR, -1);
        hourToStop = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        hourToStop.add(Calendar.HOUR, 1);

        Usage newRecordForLb = initializeRecordForLb(lbMergedHostUsageRecordsForLoadBalancer.get(0), hourToProcess, hourToStop);

        for (LoadBalancerMergedHostUsage lbMergedHostUsage : lbMergedHostUsageRecordsForLoadBalancer) {
            Calendar pollTime = lbMergedHostUsage.getPollTime();
            boolean equalToHourToProcess = pollTime.equals(hourToProcess);
            boolean recordHasEvent = lbMergedHostUsage.getEventType() != null;
            boolean withinHourToProcess = CalendarUtils.isBetween(pollTime, hourToProcess, hourToStop, true);
            boolean withinPreviousHour = CalendarUtils.isBetween(pollTime, previousHour, hourToProcess, false);
            boolean allowedToAddBandwidth = true;

            if (pollTime.before(hourToProcess)) {
                if (withinPreviousHour) {
                    previousHourRecordExists = true;
                }
                newRecordForLb.setTags(lbMergedHostUsage.getTagsBitmask());
                continue;
            }

            if ((!previousHourRecordExists && isFirstRecordOfHourToProcess) || equalToHourToProcess) {
                newRecordForLb = initializeRecordForLb(lbMergedHostUsage, hourToProcess, hourToStop);
                allowedToAddBandwidth = false;
            }

            if (withinHourToProcess) {
                isFirstRecordOfHourToProcess = false;
                RollupUsageHelper.calculateAndSetAverageConcurrentConnections(newRecordForLb, lbMergedHostUsage);

                if (allowedToAddBandwidth) {
                    RollupUsageHelper.calculateAndSetBandwidth(newRecordForLb, lbMergedHostUsage);
                }

                if (equalToHourToProcess && newRecordForLb.getNumberOfPolls() > 0) {
                    newRecordForLb.setNumberOfPolls(newRecordForLb.getNumberOfPolls() - 1);
                }

                if (recordHasEvent) {
                    if (lbMergedHostUsage.getEventType().equals(CREATE_LOADBALANCER) || equalToHourToProcess) {
                        newRecordForLb.setStartTime(pollTime);
                        newRecordForLb.setEventType(lbMergedHostUsage.getEventType().name());
                    } else {
                        newRecordForLb.setEndTime(pollTime);
                        processedRecords.add(newRecordForLb);
                        newRecordForLb = initializeRecordForLb(lbMergedHostUsage, pollTime, hourToStop);
                        newRecordForLb.setEventType(lbMergedHostUsage.getEventType().name());
                    }

                    if (lbMergedHostUsage.getEventType().equals(DELETE_LOADBALANCER)) {
                        newRecordForLb.setEndTime(pollTime);
                    }
                }
            }
        }

        if (newRecordForLb.getStartTime().before(hourToStop)) {
            processedRecords.add(newRecordForLb);
        }

        return processedRecords;
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


    /*
        @param lbMergedHostUsageRecordsForLoadBalancer: Expected to be in order by pollTime.
     */
    @Override
    public List<Usage> processRecordsForLb(List<LoadBalancerMergedHostUsage> lbMergedHostUsageRecordsForLoadBalancer, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();
        Integer mostRecentTagsBeforeHourToProcess = null;
        boolean useBandwidthFromRecord = false;
        boolean isFirstRecord = true;

        if (lbMergedHostUsageRecordsForLoadBalancer == null || lbMergedHostUsageRecordsForLoadBalancer.isEmpty()) {
            return processedRecords;
        }

        Calendar validHourToProcess = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        Calendar previousHour = CalendarUtils.stripOutMinsAndSecs(validHourToProcess);
        previousHour.add(Calendar.HOUR, -1);
        Calendar hourToStopProcess = CalendarUtils.stripOutMinsAndSecs(validHourToProcess);
        hourToStopProcess.add(Calendar.HOUR, 1);

        Usage newUsage = createInitializedUsageRecord(lbMergedHostUsageRecordsForLoadBalancer.get(0));
        newUsage.setStartTime(validHourToProcess);

        for (LoadBalancerMergedHostUsage loadBalancerMergedHostUsage : lbMergedHostUsageRecordsForLoadBalancer) {
            Calendar mergedHostUsagePollTime = loadBalancerMergedHostUsage.getPollTime();

            if (mergedHostUsagePollTime.compareTo(validHourToProcess) < 0) {
                if (mergedHostUsagePollTime.compareTo(previousHour) >= 0) {
                    useBandwidthFromRecord = true;
                }
                mostRecentTagsBeforeHourToProcess = loadBalancerMergedHostUsage.getTagsBitmask();
                continue;
            }

            if (mergedHostUsagePollTime.compareTo(hourToStopProcess) > 0) {
                break;
            }

            if (isFirstRecord) {
                if (loadBalancerMergedHostUsage.getEventType() == null) {
                    newUsage.setEventType(null);
                } else {
                    newUsage.setEventType(loadBalancerMergedHostUsage.getEventType().name());
                }
            }

            if (CalendarUtils.isTopOfTheHour(mergedHostUsagePollTime) && mergedHostUsagePollTime.equals(validHourToProcess)) {
                useBandwidthFromRecord = true;
                continue;
            }

            // Works only if usage is in order by time. Be careful when modifying.
            if (!useBandwidthFromRecord) {
                loadBalancerMergedHostUsage.setOutgoingTransfer(0);
                loadBalancerMergedHostUsage.setOutgoingTransferSsl(0);
                loadBalancerMergedHostUsage.setIncomingTransfer(0);
                loadBalancerMergedHostUsage.setIncomingTransferSsl(0);
                useBandwidthFromRecord = true;
            }

            RollupUsageHelper.calculateAndSetBandwidth(newUsage, loadBalancerMergedHostUsage);
            RollupUsageHelper.calculateAndSetAverageConcurrentConnections(newUsage, loadBalancerMergedHostUsage);
            newUsage = processEvents(newUsage, loadBalancerMergedHostUsage, processedRecords, isFirstRecord, mostRecentTagsBeforeHourToProcess);
            isFirstRecord = false;
        }

        if (newUsage.getEndTime() == null) {
            newUsage.setEndTime(hourToStopProcess);
        }

        processedRecords.add(newUsage);
        return processedRecords;
    }

    private Usage createInitializedUsageRecord(LoadBalancerMergedHostUsage loadBalancerMergedHostUsage) {
        LoadBalancer currentLB = new LoadBalancer();
        Usage initUsage = new Usage();

        currentLB.setId(loadBalancerMergedHostUsage.getLoadbalancerId());
        currentLB.setAccountId(loadBalancerMergedHostUsage.getAccountId());

        initUsage.setLoadbalancer(currentLB);
        initUsage.setStartTime(loadBalancerMergedHostUsage.getPollTime());
        initUsage.setAccountId(loadBalancerMergedHostUsage.getAccountId());
        initUsage.setTags(loadBalancerMergedHostUsage.getTagsBitmask());
        initUsage.setNeedsPushed(true);
        initUsage.setEntryVersion(0);

        if (loadBalancerMergedHostUsage.getEventType() != null) {
            initUsage.setEventType(loadBalancerMergedHostUsage.getEventType().name());
        }

        return initUsage;
    }

    private Usage processEvents(Usage currentUsage, LoadBalancerMergedHostUsage currentLoadBalancerMergedHost, List<Usage> processedRecords, boolean isFirstOfHour, Integer mostRecentTagsBitmask) {
        if (currentLoadBalancerMergedHost.getEventType() != null) {

            boolean containsNonCreateEvent = currentLoadBalancerMergedHost.getEventType() != CREATE_LOADBALANCER;
            boolean isTopOfTheHour = CalendarUtils.isTopOfTheHour(currentLoadBalancerMergedHost.getPollTime());
            boolean createBufferRecord = containsNonCreateEvent && !isTopOfTheHour;

            if (createBufferRecord) {
                currentUsage.setEndTime(currentLoadBalancerMergedHost.getPollTime());
                if (isFirstOfHour) {
                    if (mostRecentTagsBitmask == null) {
                        Usage mostRecentUsageForLoadBalancer;
                        try {
                            mostRecentUsageForLoadBalancer = usageRepository.getMostRecentUsageForLoadBalancer(currentUsage.getLoadbalancer().getId());
                            mostRecentTagsBitmask = mostRecentUsageForLoadBalancer.getTags();
                        } catch (EntityNotFoundException e) {
                            // TODO: Put an alert and monitor it!
                            LOG.error("Unable to get proper tags for record. Please verify manually!", e);
                            BitTags bitTags = loadbalancerService.getCurrentBitTags(currentUsage.getLoadbalancer().getId(), currentUsage.getAccountId());
                            mostRecentTagsBitmask = bitTags.getBitTags();
                        }
                    }

                    currentUsage.setTags(mostRecentTagsBitmask);
                    currentUsage.setEventType(null);
                }

                processedRecords.add(currentUsage);
                currentUsage = createInitializedUsageRecord(currentLoadBalancerMergedHost);
            }

            switch (currentLoadBalancerMergedHost.getEventType()) {
                case CREATE_LOADBALANCER:
                    currentUsage.setEventType(CREATE_LOADBALANCER.name());
                    currentUsage.setStartTime(currentLoadBalancerMergedHost.getPollTime());
                    break;
                case DELETE_LOADBALANCER:
                    currentUsage.setEventType(DELETE_LOADBALANCER.name());
                    currentUsage.setNumVips(0);
                    currentUsage.setEndTime(currentLoadBalancerMergedHost.getPollTime());
                    break;
                case CREATE_VIRTUAL_IP:
                    currentUsage.setEventType(CREATE_VIRTUAL_IP.name());
                    currentUsage.setNumVips(currentUsage.getNumVips() + 1);
                    break;
                case DELETE_VIRTUAL_IP:
                    currentUsage.setEventType(DELETE_VIRTUAL_IP.name());
                    currentUsage.setNumVips(currentUsage.getNumVips() - 1);
                    break;
                case SSL_MIXED_ON:
                    currentUsage.setEventType(SSL_MIXED_ON.name());
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SSL_ONLY_ON:
                    currentUsage.setEventType(SSL_ONLY_ON.name());
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SSL_OFF:
                    currentUsage.setEventType(SSL_OFF.name());
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SSL_ON:
                    currentUsage.setEventType(SSL_ON.name());
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SUSPEND_LOADBALANCER:
                    currentUsage.setEventType(SUSPEND_LOADBALANCER.name());
                    break;
                case UNSUSPEND_LOADBALANCER:
                    currentUsage.setEventType(UNSUSPEND_LOADBALANCER.name());
                    break;
                case SUSPENDED_LOADBALANCER:
                    currentUsage.setNumberOfPolls(0);
                    currentUsage.setEventType(SUSPENDED_LOADBALANCER.name());
                    break;
                default:
                    break;
            }
        }

        return currentUsage;
    }
}
