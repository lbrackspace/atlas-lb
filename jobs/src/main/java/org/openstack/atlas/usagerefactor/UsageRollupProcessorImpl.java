package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.RollupUsageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class UsageRollupProcessorImpl implements UsageRollupProcessor {
    final org.apache.commons.logging.Log LOG = LogFactory.getLog(UsageRollupProcessorImpl.class);

    @Autowired
    private UsageRepository usageRepository;
    @Autowired
    private SslTerminationRepository sslTerminationRepository;
    @Autowired
    private LoadBalancerService loadbalancerService;

    @Override
    public Map<Integer, List<LoadBalancerMergedHostUsage>> groupUsagesByLbId(List<LoadBalancerMergedHostUsage> LoadBalancerMergedHostUsages) {
        Map<Integer, List<LoadBalancerMergedHostUsage>> usagesByLbId = new HashMap<Integer, List<LoadBalancerMergedHostUsage>>();

        for (LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage : LoadBalancerMergedHostUsages) {
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
    public List<Usage> processRecords(List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages, Calendar hourToProcess) {
        List<Usage> processedRecords = new ArrayList<Usage>();

        if (loadBalancerMergedHostUsages == null || loadBalancerMergedHostUsages.isEmpty()) {
            return processedRecords;
        }

        Map<Integer, List<LoadBalancerMergedHostUsage>> usagesByLbId = groupUsagesByLbId(loadBalancerMergedHostUsages);

        for (Integer lbId : usagesByLbId.keySet()) {
            List<LoadBalancerMergedHostUsage> lbMergedHostRecordsForLoadBalancer = usagesByLbId.get(lbId);

            List<Usage> processedRecordsForLb = processRecordsForLb(lbMergedHostRecordsForLoadBalancer, hourToProcess);
            processedRecords.addAll(processedRecordsForLb);
        }

        return processedRecords;
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

        Calendar validHourToProcess = stripOutMinsAndSecs(hourToProcess);
        Calendar previousHour = stripOutMinsAndSecs(validHourToProcess);
        previousHour.add(Calendar.HOUR, -1);
        Calendar hourToStopProcess = stripOutMinsAndSecs(validHourToProcess);
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

            if (mergedHostUsagePollTime.compareTo(hourToStopProcess) >= 0) {
                break;
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

    private Calendar stripOutMinsAndSecs(Calendar cal) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(cal.getTime());
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    private Usage createInitializedUsageRecord(LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage) {
        LoadBalancer currentLB = new LoadBalancer();
        currentLB.setId(LoadBalancerMergedHostUsage.getLoadbalancerId());
        currentLB.setAccountId(LoadBalancerMergedHostUsage.getAccountId());
        Usage initUsage = new Usage();
        initUsage.setLoadbalancer(currentLB);
        initUsage.setStartTime(LoadBalancerMergedHostUsage.getPollTime());
        if (LoadBalancerMergedHostUsage.getEventType() != null) {
            initUsage.setEventType(LoadBalancerMergedHostUsage.getEventType().name());
        }
        initUsage.setAccountId(LoadBalancerMergedHostUsage.getAccountId());
        initUsage.setTags(LoadBalancerMergedHostUsage.getTagsBitmask());
        initUsage.setNeedsPushed(true);
        initUsage.setEntryVersion(0);
        return initUsage;
    }

    private Usage processEvents(Usage currentUsage, LoadBalancerMergedHostUsage currentLoadBalancerMergedHost, List<Usage> processedRecords, boolean isFirstOfHour, Integer mostRecentTagsBitmask) {
        if (currentLoadBalancerMergedHost.getEventType() != null) {
            if (currentLoadBalancerMergedHost.getEventType() != UsageEvent.CREATE_LOADBALANCER) {
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
                            BitTags bitTags = getCurrentBitTags(currentUsage.getLoadbalancer().getId(), currentUsage.getAccountId());
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
                    currentUsage.setStartTime(currentLoadBalancerMergedHost.getPollTime());
                    break;
                case DELETE_LOADBALANCER:
                    currentUsage.setEndTime(currentLoadBalancerMergedHost.getPollTime());
                    break;
                case CREATE_VIRTUAL_IP:
                    currentUsage.setNumVips(currentUsage.getNumVips() + 1);
                    break;
                case DELETE_VIRTUAL_IP:
                    currentUsage.setNumVips(currentUsage.getNumVips() - 1);
                    break;
                case SSL_MIXED_ON:
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SSL_ONLY_ON:
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SSL_OFF:
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SSL_ON:
                    currentUsage.setTags(currentLoadBalancerMergedHost.getTagsBitmask());
                    break;
                case SUSPEND_LOADBALANCER:
                    break;
                case UNSUSPEND_LOADBALANCER:
                    break;
                case SUSPENDED_LOADBALANCER:
                    break;
                default:
                    break;
            }
        }

        return currentUsage;
    }

    // TODO: Move to other class?
    private BitTags getCurrentBitTags(Integer lbId, Integer accountId) {
        BitTags bitTags = new BitTags();

        try {
            SslTermination sslTerm = sslTerminationRepository.getSslTerminationByLbId(lbId, accountId);

            if (sslTerm.isEnabled()) {
                bitTags.flipTagOn(BitTag.SSL);
            }

            if (!sslTerm.isSecureTrafficOnly()) {
                bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            }
        } catch (EntityNotFoundException e1) {
            bitTags.flipTagOff(BitTag.SSL);
            bitTags.flipTagOff(BitTag.SSL_MIXED_MODE);
        }

        if (loadbalancerService.isServiceNetLoadBalancer(accountId, lbId)) {
            bitTags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return bitTags;
    }
}
