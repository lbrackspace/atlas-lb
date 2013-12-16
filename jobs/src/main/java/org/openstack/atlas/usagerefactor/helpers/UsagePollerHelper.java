package org.openstack.atlas.usagerefactor.helpers;

import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UsagePollerHelper {
    private final org.apache.commons.logging.Log LOG = LogFactory.getLog(UsagePollerHelper.class);
    public static final long MAX_BANDWIDTH_BYTES_THRESHHOLD = 1099511627776L; //1 Terabyte

    @Autowired
    private LoadBalancerMergedHostUsageRepository mergedHostUsageRepository;
    @Autowired
    private UsageRepository usageRepository;
    @Autowired
    private LoadBalancerService loadBalancerService;
    @Autowired
    private VirtualIpRepository virtualIpRepository;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private HostRepository hostRepository;

    private class ResetBandwidth {
        public long incomingTransfer = 0;
        public long outgoingTransfer = 0;
    }

    public UsagePollerHelper() {} 

    public void calculateUsage(SnmpUsage currentUsage, LoadBalancerHostUsage previousRecord,
                               LoadBalancerMergedHostUsage newMergedUsage, Calendar currentPollTime) {
        long totIncomingTransfer = newMergedUsage.getIncomingTransfer();
        long totIncomingTransferSsl = newMergedUsage.getIncomingTransferSsl();
        long totOutgoingTransfer = newMergedUsage.getOutgoingTransfer();
        long totOutgoingTransferSsl = newMergedUsage.getOutgoingTransferSsl();

        ResetBandwidth normal = getPossibleResetBandwidth(currentUsage.getBytesIn(), previousRecord.getIncomingTransfer(),
                                                          currentUsage.getBytesOut(), previousRecord.getOutgoingTransfer(),
                                                         currentPollTime, previousRecord.getPollTime());
        totIncomingTransfer += normal.incomingTransfer;
        totOutgoingTransfer += normal.outgoingTransfer;
        ResetBandwidth ssl = getPossibleResetBandwidth(currentUsage.getBytesInSsl(), previousRecord.getIncomingTransferSsl(),
                                                       currentUsage.getBytesOutSsl(), previousRecord.getOutgoingTransferSsl(),
                                                       currentPollTime, previousRecord.getPollTime());
        totIncomingTransferSsl += ssl.incomingTransfer;
        totOutgoingTransferSsl += ssl.outgoingTransfer;

        newMergedUsage.setIncomingTransfer(totIncomingTransfer);
        newMergedUsage.setIncomingTransferSsl(totIncomingTransferSsl);
        newMergedUsage.setOutgoingTransfer(totOutgoingTransfer);
        newMergedUsage.setOutgoingTransferSsl(totOutgoingTransferSsl);

        //Using concurrent connections regardless of reset since this is not a counter, only a snapshot
        long ccs = currentUsage.getConcurrentConnections() + newMergedUsage.getConcurrentConnections();
        long ccsSsl = currentUsage.getConcurrentConnectionsSsl() + newMergedUsage.getConcurrentConnectionsSsl();
        newMergedUsage.setConcurrentConnections(ccs);
        newMergedUsage.setConcurrentConnectionsSsl(ccsSsl);
    }

    public void calculateUsage(LoadBalancerHostUsage currentRecord, LoadBalancerHostUsage previousRecord,
                               LoadBalancerMergedHostUsage newMergedUsage) {
        long totIncomingTransfer = newMergedUsage.getIncomingTransfer();
        long totIncomingTransferSsl = newMergedUsage.getIncomingTransferSsl();
        long totOutgoingTransfer = newMergedUsage.getOutgoingTransfer();
        long totOutgoingTransferSsl = newMergedUsage.getOutgoingTransferSsl();

        ResetBandwidth normal = getPossibleResetBandwidth(currentRecord.getIncomingTransfer(), previousRecord.getIncomingTransfer(),
                                                          currentRecord.getOutgoingTransfer(), previousRecord.getOutgoingTransfer(),
                                                          currentRecord.getPollTime(), previousRecord.getPollTime());
        totIncomingTransfer += normal.incomingTransfer;
        totOutgoingTransfer += normal.outgoingTransfer;
        ResetBandwidth ssl = getPossibleResetBandwidth(currentRecord.getIncomingTransferSsl(), previousRecord.getIncomingTransferSsl(),
                                                       currentRecord.getOutgoingTransferSsl(), previousRecord.getOutgoingTransferSsl(),
                                                       currentRecord.getPollTime(), previousRecord.getPollTime());
        totIncomingTransferSsl += ssl.incomingTransfer;
        totOutgoingTransferSsl += ssl.outgoingTransfer;

        newMergedUsage.setIncomingTransfer(totIncomingTransfer);
        newMergedUsage.setIncomingTransferSsl(totIncomingTransferSsl);
        newMergedUsage.setOutgoingTransfer(totOutgoingTransfer);
        newMergedUsage.setOutgoingTransferSsl(totOutgoingTransferSsl);

        //Using concurrent connections regardless of reset since this is not a counter, only a snapshot
        long ccs = currentRecord.getConcurrentConnections() + newMergedUsage.getConcurrentConnections();
        long ccsSsl = currentRecord.getConcurrentConnectionsSsl() + newMergedUsage.getConcurrentConnectionsSsl();
        newMergedUsage.setConcurrentConnections(ccs);
        newMergedUsage.setConcurrentConnectionsSsl(ccsSsl);
    }

    public boolean isReset(long currentBandwidth, long previousBandwidth) {
        return currentBandwidth < previousBandwidth;
    }

    public ResetBandwidth getPossibleResetBandwidth(long currentIncoming, long previousIncoming, long currentOutgoing,
                                                    long previousOutgoing, Calendar currentPollTime, Calendar previousPollTime) {
        ResetBandwidth ret = new ResetBandwidth();
        long outDiff = currentOutgoing - previousOutgoing;
        long inDiff = currentIncoming - previousIncoming;
        if ( isReset(currentIncoming, previousIncoming) ||
                isReset(currentOutgoing, previousOutgoing) ||
                previousIncoming < 0 || previousOutgoing < 0) {
            return ret;
        }
        //If the bandwidth to be charged to this load balancer exceeds a certain amount then we assume a bug happened and store 0 bandwidth.
        if (inDiff < MAX_BANDWIDTH_BYTES_THRESHHOLD) {
            ret.incomingTransfer = currentIncoming - previousIncoming;
        }
        if (outDiff < MAX_BANDWIDTH_BYTES_THRESHHOLD) {
            ret.outgoingTransfer = currentOutgoing - previousOutgoing;
        }
        return ret;
    }

    public UsageProcessorResult processCurrentUsage(Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages,
                                                    Map<Integer, Map<Integer, SnmpUsage>> currentUsages,
                                                    Calendar pollTime){
        LOG.info("Retrieving load balancers that are in BUILD status...");
        Map<Integer, LoadBalancer> buildingLoadBalancers = getMapOfBuildingLoadBalancers();
        List<LoadBalancerMergedHostUsage> mergedUsages = new ArrayList<LoadBalancerMergedHostUsage>();
        List<LoadBalancerHostUsage> newLBHostUsages = new ArrayList<LoadBalancerHostUsage>();

        for (Integer loadbalancerId : currentUsages.keySet()) {
            for (Integer hostId : currentUsages.get(loadbalancerId).keySet()) {
                SnmpUsage currentUsage = currentUsages.get(loadbalancerId).get(hostId);
                //Zeus SNMP will sometimes return a negative number for these if it is under heavy load, like it can't give us this information at the moment.
                if (currentUsage.getConcurrentConnections() < 0) {
                    currentUsage.setConcurrentConnections(0);
                }
                if (currentUsage.getConcurrentConnectionsSsl() < 0) {
                    currentUsage.setConcurrentConnectionsSsl(0);
                }
            }
            if(buildingLoadBalancers.containsKey(loadbalancerId)){
                //This is to handle an issue when zeus is under heavy load on the create load balancer call and the
                //api has not inserted the create load balancer record yet.
                LOG.info(String.format("Load balancer '%d' is in BUILD status but SNMP still returned usage for it.",
                        loadbalancerId));
                continue;
            }
            if (!existingUsages.containsKey(loadbalancerId)) {
                //There are no previous records in lb_host_usage for this loadbalancer
                //Attempt to get previous record from lb_merged_host_usage table
                int tagsBitmask = 0;
                int numVips = 1;
                int accountId = -1;
                try {
                    LoadBalancerMergedHostUsage mostRecentMerged = mergedHostUsageRepository.getMostRecentRecordForLoadBalancer(loadbalancerId);
                    tagsBitmask = mostRecentMerged.getTagsBitmask();
                    numVips = mostRecentMerged.getNumVips();
                    accountId = mostRecentMerged.getAccountId();
                } catch(EntityNotFoundException mergedE) {
                    //There was not a previous record in lb_merged_host_usage table
                    //Attempt to grab from loadbalancing.lb_usage table
                    LOG.info("Loadbalancer " + loadbalancerId + " has no previous usage entry in lb_merged_host_usage table." +
                            " Attempting to pull from loadbalancing.lb_usage...");
                    try {
                        Usage mostRecentUsage = usageRepository.getMostRecentUsageForLoadBalancer(loadbalancerId);
                        tagsBitmask = mostRecentUsage.getTags();
                        numVips = mostRecentUsage.getNumVips();
                        accountId = mostRecentUsage.getAccountId();
                    } catch(EntityNotFoundException usageE) {
                        //There was not a previous record in loadbalancing.lb_usaget able
                        //Grab what is possible from ssltermination and virtualip tables
                        LOG.info("Loadbalancer " + loadbalancerId + " has no previous usage entry loadbalancing.lb_usage table." +
                            " Attempting to pull from loadbalancing.loadbalancer table...");
                        try {
                            LoadBalancer loadbalancer = loadBalancerService.get(loadbalancerId);
                            accountId = loadbalancer.getAccountId();
                            BitTags tags = loadBalancerService.getCurrentBitTags(loadbalancerId);
                            //We want to default to nonssl to ensure no overcharges.
                            //Servicenet tags will remain though.
                            tags.flipTagOff(BitTag.SSL);
                            tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                            tagsBitmask = tags.toInt();
                            numVips = virtualIpRepository.getNumIpv4VipsForLoadBalancer(loadbalancer).intValue();
                        } catch (EntityNotFoundException lbE) {
                            //What to do now?? Continue?????????
                            LOG.warn("Loadbalancer " + loadbalancerId + " has usage returned by SNMP but there are no" +
                                    " entries in any table for that loadbalancer. " + lbE.getMessage());
                            continue;
                        }
                    }
                }
                //Create new mergedHostUsage with zero usage and copied values.
                LoadBalancerMergedHostUsage zeroedMergedRecord = new LoadBalancerMergedHostUsage();
                zeroedMergedRecord.setTagsBitmask(tagsBitmask);
                zeroedMergedRecord.setNumVips(numVips);
                zeroedMergedRecord.setPollTime(pollTime);
                zeroedMergedRecord.setLoadbalancerId(loadbalancerId);
                zeroedMergedRecord.setAccountId(accountId);
                mergedUsages.add(zeroedMergedRecord);

                //Create new LoadBalancerHostUsage records using current counters
                for (Integer hostId : currentUsages.get(loadbalancerId).keySet()) {
                    SnmpUsage currentUsage = currentUsages.get(loadbalancerId).get(hostId);
                    LoadBalancerHostUsage newLBHostUsage = convertSnmpUsageToLBHostUsage(currentUsage,
                            accountId, loadbalancerId, tagsBitmask, numVips, hostId, pollTime);
                    newLBHostUsages.add(newLBHostUsage);
                }
                continue;
            }

            //At this point there are previous records to use
            LoadBalancerMergedHostUsage newMergedRecord = null;
            for (Integer hostId : currentUsages.get(loadbalancerId).keySet()) {
                SnmpUsage currentUsage = currentUsages.get(loadbalancerId).get(hostId);

                if(!existingUsages.get(loadbalancerId).containsKey(hostId)) {
                    //No previous record exists for this load balancer. Still need to add the current
                    //counters to the lb_host_usage table. Have to use an existing usage not from
                    //this host to get the correct numVips and tagsBitmask.
                    //There will be issues if there are events that a record for a host got deleted somehow.
                    LoadBalancerHostUsage existingUsage = existingUsages.get(loadbalancerId).entrySet().iterator().next().getValue().get(0);
                    newLBHostUsages.add(convertSnmpUsageToLBHostUsage(currentUsage, existingUsage.getAccountId(),
                            loadbalancerId, existingUsage.getTagsBitmask(),
                            existingUsage.getNumVips(), hostId, pollTime));
                    continue;
                }

                List<LoadBalancerHostUsage> loadBalancerHostUsages = existingUsages.get(loadbalancerId).get(hostId);
                if (loadBalancerHostUsages.size() == 0) {
                    LOG.info("Encountered a list of loadBalancerHostUsages that is empty.  This should not have happened.");
                    continue;
                }

                LoadBalancerHostUsage usageBaseline = loadBalancerHostUsages.get(loadBalancerHostUsages.size() - 1);
                if (loadBalancerHostUsages.size() >= 2) {
                    LoadBalancerHostUsage eventWithSameTime = loadBalancerHostUsages.get(loadBalancerHostUsages.size() - 2);
                    if (eventWithSameTime.getPollTime().equals(usageBaseline.getPollTime())){
                        if (usageBaseline.getEventType() == null) {
                            usageBaseline = eventWithSameTime;
                        }
                    }
                }
                if (newMergedRecord == null) {
                    newMergedRecord = initializeMergedRecord(usageBaseline);
                    newMergedRecord.setPollTime(pollTime);
                    newMergedRecord.setEventType(null);
                }

                calculateUsage(currentUsage, usageBaseline, newMergedRecord, pollTime);
                 newLBHostUsages.add(convertSnmpUsageToLBHostUsage(currentUsage, usageBaseline.getAccountId(),
                         loadbalancerId, usageBaseline.getTagsBitmask(),
                         usageBaseline.getNumVips(), hostId, pollTime));
            }
            mergedUsages.add(newMergedRecord);
        }

        return new UsageProcessorResult(mergedUsages, newLBHostUsages);
    }

    public List<LoadBalancerMergedHostUsage> processExistingEvents(Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages) {
        List<LoadBalancerMergedHostUsage> newMergedEventRecords = new ArrayList<LoadBalancerMergedHostUsage>();
        List<Host> hosts = hostRepository.getAll();
        for (Integer loadBalancerId : existingUsages.keySet()) {
            LinkedHashMap<String, LoadBalancerMergedHostUsage> mergedUsagesMap = new LinkedHashMap<String, LoadBalancerMergedHostUsage>();

            //Group usage records by time and hostId so that it can be later used to determine if there are
            //any records that are missing (for example: due to host being unreachable)
            TreeMap<Calendar, Map<Integer, LoadBalancerHostUsage>> lbHostUsagesMapByTime =
                    getLBUsageGroupedByTimeAndHost(existingUsages.get(loadBalancerId), hosts);
            //For times that do not have an entry for a host, insert null
            insertNullRecordsForHostsWithoutEntries(lbHostUsagesMapByTime, hosts);
            Map<Integer, LoadBalancerHostUsage> previousRecords = null;
            boolean isFirstRecord = true;
            for (Calendar timeKey : lbHostUsagesMapByTime.keySet()) {

                for (Integer hostId : lbHostUsagesMapByTime.get(timeKey).keySet()) {

                    LoadBalancerHostUsage currentUsage = lbHostUsagesMapByTime.get(timeKey).get(hostId);

                    if (currentUsage != null) {
                        if (currentUsage.getConcurrentConnections() < 0) {
                            currentUsage.setConcurrentConnections(0);
                        }
                        if (currentUsage.getConcurrentConnectionsSsl() < 0) {
                            currentUsage.setConcurrentConnectionsSsl(0);
                        }
                    }

                    if (isFirstRecord) {
                        if (currentUsage == null) {
                            if (previousRecords == null) {
                                previousRecords = new HashMap<Integer, LoadBalancerHostUsage>();
                            }
                            previousRecords.put(hostId, currentUsage);
                            continue;
                        }
                        //If an event is the first record then store merged usage as 0
                        if (currentUsage.getEventType() != null) {
                            mergedUsagesMap.put(timeKey.getTime().toString(), initializeMergedRecord(currentUsage));
                        }
                        //set previous record for this host to the current and continue
                        if (previousRecords == null) {
                            previousRecords = new HashMap<Integer, LoadBalancerHostUsage>();
                        }
                        previousRecords.put(hostId, currentUsage);
                        continue;
                    } else if (currentUsage != null && currentUsage.getEventType() == null){
                        continue;
                    }

                    LoadBalancerHostUsage previousUsage = previousRecords.get(hostId);

                    if (!previousRecords.containsKey(hostId) || currentUsage == null || previousUsage == null) {
                        previousRecords.put(hostId, currentUsage);
                        continue;
                    }

                    if (!mergedUsagesMap.containsKey(timeKey.getTime().toString())) {
                        mergedUsagesMap.put(timeKey.getTime().toString(), initializeMergedRecord(currentUsage));
                    }

                    LoadBalancerMergedHostUsage newMergedUsage = mergedUsagesMap.get(timeKey.getTime().toString());
                    calculateUsage(currentUsage, previousUsage, newMergedUsage);

                    // set previous record for this host to the current
                    previousRecords.put(hostId, currentUsage);
                }

                isFirstRecord = false;
            }

            //Add all events into list that shall be returned
            for(String timeKey : mergedUsagesMap.keySet()) {
                newMergedEventRecords.add(mergedUsagesMap.get(timeKey));
            }
        }

        return newMergedEventRecords;
    }

    public LoadBalancerMergedHostUsage initializeMergedRecord(LoadBalancerHostUsage lbHostUsage) {
        LoadBalancerMergedHostUsage newLBMergedHostUsage = new LoadBalancerMergedHostUsage();
        newLBMergedHostUsage.setAccountId(lbHostUsage.getAccountId());
        newLBMergedHostUsage.setLoadbalancerId(lbHostUsage.getLoadbalancerId());
        newLBMergedHostUsage.setNumVips(lbHostUsage.getNumVips());
        newLBMergedHostUsage.setEventType(lbHostUsage.getEventType());
        Calendar pollTime = Calendar.getInstance();
        pollTime.setTime(lbHostUsage.getPollTime().getTime());
        newLBMergedHostUsage.setPollTime(pollTime);
        newLBMergedHostUsage.setTagsBitmask(lbHostUsage.getTagsBitmask());
        return newLBMergedHostUsage;
    }

    public LoadBalancerHostUsage convertSnmpUsageToLBHostUsage(SnmpUsage snmpUsage, int accountId, int loadBalancerId,
                                                               int tagsBitmask, int numVips, int hostId, Calendar pollTime) {
        LoadBalancerHostUsage newlbHostUsage = new LoadBalancerHostUsage();
        newlbHostUsage.setAccountId(accountId);
        newlbHostUsage.setLoadbalancerId(loadBalancerId);
        newlbHostUsage.setTagsBitmask(tagsBitmask);
        newlbHostUsage.setNumVips(numVips);
        newlbHostUsage.setPollTime(pollTime);
        newlbHostUsage.setHostId(hostId);
        if  (snmpUsage != null) {
            newlbHostUsage.setOutgoingTransfer(snmpUsage.getBytesOut());
            newlbHostUsage.setOutgoingTransferSsl(snmpUsage.getBytesOutSsl());
            newlbHostUsage.setIncomingTransfer(snmpUsage.getBytesIn());
            newlbHostUsage.setIncomingTransferSsl(snmpUsage.getBytesInSsl());
            newlbHostUsage.setConcurrentConnections(snmpUsage.getConcurrentConnections());
            newlbHostUsage.setConcurrentConnectionsSsl(snmpUsage.getConcurrentConnectionsSsl());
        }

        return newlbHostUsage;
    }

    private Map<Integer, LoadBalancer> getMapOfBuildingLoadBalancers() {
        List<LoadBalancer> buildingLoadBalancerList = null;
        try {
            buildingLoadBalancerList = loadBalancerRepository.getLoadBalancersWithStatus(LoadBalancerStatus.BUILD);
        } catch(Exception e) {
            LOG.error("Retrieval error of load balancers in BUILD status. " + e);
        }

        Map<Integer, LoadBalancer> buildingLoadBalancerMap = new HashMap<Integer, LoadBalancer>();

        if(buildingLoadBalancerList != null) {
            for(LoadBalancer lb : buildingLoadBalancerList) {
                buildingLoadBalancerMap.put(lb.getId(), lb);
            }
        }

        return buildingLoadBalancerMap;
    }

    private TreeMap<Calendar, Map<Integer, LoadBalancerHostUsage>> getLBUsageGroupedByTimeAndHost(Map<Integer, List<LoadBalancerHostUsage>> existingLBUsages,
                                                                                                      List<Host> hosts) {
        TreeMap<Calendar, Map<Integer, LoadBalancerHostUsage>> lbHostUsagesMapByTime = new TreeMap<Calendar, Map<Integer, LoadBalancerHostUsage>>();

        for (Integer hostId : existingLBUsages.keySet()) {
            for (LoadBalancerHostUsage lbHostUsage : existingLBUsages.get(hostId)) {
                if(!lbHostUsagesMapByTime.containsKey(lbHostUsage.getPollTime())) {
                    Map<Integer, LoadBalancerHostUsage> lbHostUsagesMapByHostId = new HashMap<Integer, LoadBalancerHostUsage>();
                    lbHostUsagesMapByTime.put(lbHostUsage.getPollTime(), lbHostUsagesMapByHostId);
                }
                Map<Integer, LoadBalancerHostUsage> lbHostUsageMapByHostId = lbHostUsagesMapByTime.get(lbHostUsage.getPollTime());
                if(lbHostUsageMapByHostId.containsKey(hostId)){
                    if (lbHostUsage.getEventType() != null) {
                        lbHostUsageMapByHostId.put(hostId, lbHostUsage);
                    }
                } else {
                    lbHostUsageMapByHostId.put(hostId, lbHostUsage);
                }
            }
        }

        return lbHostUsagesMapByTime;
    }

    private void insertNullRecordsForHostsWithoutEntries(TreeMap<Calendar, Map<Integer, LoadBalancerHostUsage>> lbHostUsagesMapByTime,
                                                         List<Host> hosts) {
        for (Calendar timeKey : lbHostUsagesMapByTime.keySet()) {
            for (Host host : hosts) {
                if (!lbHostUsagesMapByTime.get(timeKey).containsKey(host.getId())) {
                    lbHostUsagesMapByTime.get(timeKey).put(host.getId(), null);
                }
            }
        }
    }
}
