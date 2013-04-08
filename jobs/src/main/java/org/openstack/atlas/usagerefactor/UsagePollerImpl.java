package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsagePollerImpl implements UsagePoller {

    final Log LOG = LogFactory.getLog(UsagePollerImpl.class);

    HostRepository hostRepository = new HostRepository();
    StingrayUsageClientImpl stingrayUsageClient = new StingrayUsageClientImpl();

    @Override
    public void processRecords() {
        /*
         * 1. Query SNMP
         * 2. Query host usage table for previous records. markerId = MAX(id)
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
        Map<Integer, LoadBalancerHostUsage> existingLBHostUsages = getLoadBalancerHostUsageRecords();
        List<LoadBalancerHostUsage> newHostUsage = new ArrayList<LoadBalancerHostUsage>();
        List<LoadBalancerMergedHostUsage> newMergedHostUsage = new ArrayList<LoadBalancerMergedHostUsage>();
        for (Integer loadBalancerId : currentLBHostUsage.keySet()) {
            if (existingLBHostUsages.containsKey(loadBalancerId)) {

            }
        }

    }

    @Override
    public Map<Integer, LoadBalancerHostUsage> getLoadBalancerHostUsageRecords() {
        Map<Integer, LoadBalancerHostUsage> lbHostUsages = new HashMap<Integer, LoadBalancerHostUsage>();
        return lbHostUsages;
    }

    // TODO: Run the created threads and merge the host data together to form singular, complete entries.
    @Override
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        List<Host> hostList = hostRepository.getAll();
        ExecutorService threadPool = Executors.newFixedThreadPool(hostList.size());
        for (final Host host : hostList) {
            threadPool.submit(new Runnable() {
                public void run() {
                    try {
                        stingrayUsageClient.getHostUsage(host);
                    } catch (Exception e) {
                        String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
                        LOG.error(retString, e);
                    }
                }
            });
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
}
