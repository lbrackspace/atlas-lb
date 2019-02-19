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

        //Insert empty list if a loadbalancer does not have any LoadBalancerMergedHostUsages
        for (LbIdAccountId lbActiveDuringHour : lbsActiveDuringHour) {
            if (!usagesByLbId.containsKey(lbActiveDuringHour)) {
                usagesByLbId.put(lbActiveDuringHour, new ArrayList<LoadBalancerMergedHostUsage>());
            }
        }

        for (LbIdAccountId lbIdAccountId : usagesByLbId.keySet()) {//for each LbIdAccountId
            List<LoadBalancerMergedHostUsage> lbMergedHostRecordsForLoadBalancer = usagesByLbId.get(lbIdAccountId);

            //process LoadBalancerMergedHostUsages for each LbIdAccountId
            List<Usage> processedRecordsForLb = processRecordsForLb(lbIdAccountId.getAccountId(), lbIdAccountId.getLbId(), lbMergedHostRecordsForLoadBalancer, hourToProcess);
            processedRecords.addAll(processedRecordsForLb);
        }

        return processedRecords;
    }

    /**
     * Process the LoadBalancerMergedHostUsage records for a loadbalancer from hourToProcess till hourToProcess+1 hour.
     * In ideal case the previous hour records also will come in the lbMergedHostUsageRecordsForLoadBalancer to be processed.
     * The previous hour records are used to gather some information for later use but usage counters are not considered.
     * @param accountId
     * @param lbId
     * @param lbMergedHostUsageRecordsForLoadBalancer: Expected to be in order by pollTime.
     * @param hourToProcess: the current processing hour
     * @return
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
            //initialize the Usage record by populating data from the cache or loadbalancer as there are no LoadBalancerMergedHostUsage records
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
            //not sure when this scenario will come, we actually fetch all the records that are before the hourToStop only
            return processedRecords;
        }
        //initialize Usage object from the firstRecordInList, some of the fields are then populated from the previous hour records.
        //the first set of LoadBalancerMergedHostUsages with eventType null that are withinHourToProcess are rolled up into this Usage record.
        Usage newRecordForLb = initializeRecordForLb(firstRecordInList, hourToProcess, hourToStop);
        for (LoadBalancerMergedHostUsage lbMergedHostUsage : lbMergedHostUsageRecordsForLoadBalancer) {
            Calendar pollTime = lbMergedHostUsage.getPollTime();
            boolean equalToHourToProcess = pollTime.equals(hourToProcess);
            UsageEvent event = lbMergedHostUsage.getEventType();
            boolean recordHasEvent = event != null;
            boolean withinHourToProcess = CalendarUtils.isBetween(pollTime, hourToProcess, hourToStop, true);
            boolean withinPreviousHour = CalendarUtils.isBetween(pollTime, previousHour, hourToProcess, false);
            boolean allowedToAddBandwidth = true;

            //this block of code seem to gather information from the previous hour records and identify the mostRecentPreviousRecord for later use.
            //also identify if the loadbalancer had DELETE event, no need to progress further in that case.
            if (pollTime.before(hourToProcess)) {
                if (withinPreviousHour) {//within immediate previous hour
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
                /**
                 * discard old one and initialize new Usage Record:
                 * if there is no previous hour record and the current one is the first record of the hour
                 *  ex: CREATE_LOADBALANCER, there won't be any previous hour records. Also there won't be any bandwidth to be added from such records.
                 * (or)
                 * if the current lbMergedHostUsage is equalToHourToProcess then this would be the mostRecentPreviousRecord.
                 * bandwidth from this record is not added as this would have been already considered in the previous poll.
                 * More details can be found in CLB-764.
                 * */
                newRecordForLb = initializeRecordForLb(lbMergedHostUsage, hourToProcess, hourToStop);
                allowedToAddBandwidth = false;
            }

            if (withinHourToProcess) {//pollTime.compareTo(startTime) >= 0 && pollTime.compareTo(endTime) <= 0;
                //update averageConcurrentConnections and numberOfPolls in the Usage record. NumberOfPolls is the number of LoadBalancerMergedHostUsages added to the Usage record
                RollupUsageHelper.calculateAndSetAverageConcurrentConnections(newRecordForLb, lbMergedHostUsage);

                if (allowedToAddBandwidth) {//the bandwidth is added from the records that are withinHourToProcess only
                    RollupUsageHelper.calculateAndSetBandwidth(newRecordForLb, lbMergedHostUsage);
                }

                //equalToHourToProcess record would have been already considered in the previous poll, no need to add to NumberOfPolls.
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
                        //set empty values as CREATE_LOADBALANCER event will not have any bandwidth and
                        //equalToHourToProcess record would have been already considered in the previous poll.
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
                            //SuspendedEventJob creates SUSPENDED_LOADBALANCER Events, if the previous record has a similar event then set above data
                        }
                    } else {//Not CREATE_LOADBALANCER and SUSPENDED_LOADBALANCER event, handle the other events.

                        if (isFirstRecordOfHourToProcess && !previousHourRecordExists) {
                            //this is exceptional case?Not CREATE_LOADBALANCER and no previousHourRecordExists
                            int tags = getTags(lbId);
                            int numVips = getNumVips(lbId);
                            newRecordForLb.setTags(tags);
                            newRecordForLb.setNumVips(numVips);
                        }

                        if (event.equals(UNSUSPEND_LOADBALANCER)) {
                            newRecordForLb.setNumberOfPolls(0);
                        }

                        //set the endTime of the current Usage record with current lbMergedHostUsage's pollTime
                        newRecordForLb.setEndTime(pollTime);
                        processedRecords.add(newRecordForLb);
                        //create new Usage record and set the eventType. From now on the usage is rolled up into this record for the current event
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

    /**
     * get the tags from the cache or from the most recent Usage record of the loadbalancer.
     * @param lbId
     * @return
     */
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

    /**
     * get the num of vips from the cache or from the most recent Usage record of the loadbalancer.
     * @param lbId
     * @return
     */
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

    /**
     * Creates Usage object when there are no lbMergedHostUsageRecords For LoadBalancer.
     * @param accountId
     * @param loadbalancerId
     * @param startTime
     * @param endTime
     * @return
     */
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

    /**
     * Creates Usage object by initializing some of the fields from the LoadBalancerMergedHostUsage.
     * This will not copy the bandwidth and connections fields.
     *
     * @param lbMergedHostUsage
     * @param startTime
     * @param endTime
     * @return
     */
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
