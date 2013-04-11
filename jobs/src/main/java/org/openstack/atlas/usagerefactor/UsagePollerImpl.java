package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.HostIdLoadbalancerIdKey;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsagePollerImpl implements UsagePoller {

    final Log LOG = LogFactory.getLog(UsagePollerImpl.class);
    StingrayUsageClientImpl stingrayUsageClient = new StingrayUsageClientImpl();
    HostRepository hostRepository;

    @Override
    public void processRecords() {
        /*
         * 1. Query SNMP
         * 2. Query host usage table for previous records. store time as deleteTimeMarker
         * 3. Process Records For Each LB
         *      a. If no record in host usage table for LB but it is in SNMP results, then write snmp results to hosts table
         *      but do NOT write any data to the Merged LB Usage table.
         *      b. If CREATE_LOADBALANCER encountered in host usage table disregard any earlier records.
         *      c. If UNSUSPEND event encountered in host usage table disregard any earlier records unless earlier record is SUSPEND event.
         *      d. If earlier record's value is greater than current record then a reset happened.
         *          i. Record 0 Usage in Merged LB Usage table
         *          ii. Do not modify the counter in the host usage table.
         *      e. Write SNMP data to LB Host Usage table.
         *      d. Delete records from LB Host Usage table that have an ID less than the markerID
         */
        Map<Integer, Map<Integer, SnmpUsage>> currentLBHostUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        try {
             currentLBHostUsage = getCurrentData();
        } catch (Exception e) {

        }
        Calendar deleteTimeMarker = Calendar.getInstance();
        //Parent key should be hostId, child key should be loadbalancerId
        Map<Integer, List<LoadBalancerHostUsage>> existingLBHostUsages = getLoadBalancerHostUsageRecords();
        List<LoadBalancerHostUsage> newHostUsage = new ArrayList<LoadBalancerHostUsage>();
        Map<Integer, LoadBalancerMergedHostUsage> newMergedHostUsage = new HashMap<Integer, LoadBalancerMergedHostUsage>();
        //Process events that have come in between now and last poller run

        //Now parent key should be loadbalancerId, and child key hostId
        currentLBHostUsage = UsageMappingHelper.swapKeyGrouping(currentLBHostUsage);
        for (Integer loadBalancerId : currentLBHostUsage.keySet()) {

        }

    }

    @Override
    public Map<Integer, List<LoadBalancerHostUsage>> getLoadBalancerHostUsageRecords() {
        Map<Integer, List<LoadBalancerHostUsage>> existingUsages = new HashMap<Integer, List<LoadBalancerHostUsage>>();
        return existingUsages;
    }

    @Override
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        List<Host> hostList = hostRepository.getAllHosts();
        ArrayList<HostThread> hostThreads = new ArrayList<HostThread>();
        for (final Host host : hostList) {
            hostThreads.add(new HostThread(host));
        }
        for (HostThread thread : hostThreads) {
            thread.run();
        }
        return mergedHostsUsage;
    }

    @Override
    public void deleteLoadBalancerHostUsageRecords(Calendar deleteTimeMarker) {

    }

    @Override
    public void insertLoadBalancerUsagePerHost(List<LoadBalancerHostUsage> lbHostUsages) {

    }

    @Override
    public void insertMergedRecords(List<LoadBalancerMergedHostUsage> mergedRecords) {

    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }
}
