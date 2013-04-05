package org.openstack.atlas.usagerefactor;

import org.apache.commons.net.nntp.SimpleNNTPHeader;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsagePollerImpl implements UsagePoller{

    @Override
    public void processRecords() {

    }

    @Override
    public List<LoadBalancerHostUsage> getLoadBalancerHostUsageRecords() {
        List<LoadBalancerHostUsage> lbHostUsages = new ArrayList<LoadBalancerHostUsage>();
        return lbHostUsages;
    }

    @Override
    public Map<Integer, SnmpUsage> getCurrentData() {
        Map<Integer, SnmpUsage> mergedHostsUsage = new HashMap<Integer, SnmpUsage>();
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
