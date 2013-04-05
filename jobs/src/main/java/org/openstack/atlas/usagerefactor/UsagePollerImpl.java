package org.openstack.atlas.usagerefactor;

import org.apache.commons.net.nntp.SimpleNNTPHeader;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.*;

public class UsagePollerImpl implements UsagePoller{

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
        Map<Integer, List<SnmpUsage>> currentLBHostUsage = getCurrentData();
        Calendar deleteTimeMarker = Calendar.getInstance();
        Map<Integer, LoadBalancerHostUsage> existingLBHostUsages = getLoadBalancerHostUsageRecords();
        List<LoadBalancerHostUsage> newHostUsage = new ArrayList<LoadBalancerHostUsage>();
        List<LoadBalancerMergedHostUsage> newMergedHostUsage = new ArrayList<LoadBalancerMergedHostUsage>();
        for(Integer loadBalancerId : currentLBHostUsage.keySet()){
            if(existingLBHostUsages.containsKey(loadBalancerId)){

            }
        }

    }

    @Override
    public Map<Integer, LoadBalancerHostUsage> getLoadBalancerHostUsageRecords() {
        Map<Integer, LoadBalancerHostUsage> lbHostUsages = new HashMap<Integer, LoadBalancerHostUsage>();
        return lbHostUsages;
    }

    @Override
    public Map<Integer, List<SnmpUsage>> getCurrentData() {
        Map<Integer, List<SnmpUsage>> currentLBHostUsage = new HashMap<Integer, List<SnmpUsage>>();
        return currentLBHostUsage;
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
