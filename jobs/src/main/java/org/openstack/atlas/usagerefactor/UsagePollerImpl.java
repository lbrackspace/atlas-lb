package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
         *      e. Write SNMP data to LB Host Usage table.
         *      d. Delete records from LB Host Usage table that have an ID less than the markerID
         */
        Map<Integer, Map<Integer, SnmpUsage>> currentLBHostUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        try {
             currentLBHostUsage = getCurrentData();
        } catch (Exception e) {

        }
        Calendar deleteTimeMarker = Calendar.getInstance();
        Map<Integer, Map<Integer, LoadBalancerHostUsage>> existingLBHostUsages = getLoadBalancerHostUsageRecords();
        List<LoadBalancerHostUsage> newHostUsage = new ArrayList<LoadBalancerHostUsage>();
        Map<Integer, LoadBalancerMergedHostUsage> newMergedHostUsage = new HashMap<Integer, LoadBalancerMergedHostUsage>();
        for (Integer hostId : currentLBHostUsage.keySet()) {
            
        }

    }

    @Override
    public Map<Integer, Map<Integer, LoadBalancerHostUsage>> getLoadBalancerHostUsageRecords() {
        //Key should be a Host Id
        Map<Integer, Map<Integer, LoadBalancerHostUsage>> usagesGroupedByHostId = new HashMap<Integer, Map<Integer, LoadBalancerHostUsage>>();
        //Key shoudl be a load balancer id
        Map<Integer, LoadBalancerHostUsage> usagesGroupedByLoadBalancerId = new HashMap<Integer, LoadBalancerHostUsage>();
        return usagesGroupedByHostId;
    }

    @Override
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        List<Host> hostList = hostRepository.getAllHosts();
        Map<Integer, Future> futures = new HashMap<Integer, Future>();

        ExecutorService executor = Executors.newFixedThreadPool(hostList.size());
        for (Host host : hostList) {
            futures.put(host.getId(), executor.submit(new HostThread(host)));
        }
        
        return mergedHostsUsage;
    }

    @Override
    public void deleteLoadBalancerHostUsageRecords(int markerId) {

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
