package org.openstack.atlas.usagerefactor.generator;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.*;

public class UsagePollerGenerator {

    public static Map<Integer, Map<Integer, SnmpUsage>> generateSnmpMap(int numHosts, int numLoadBalancers) {

        Map<Integer, Map<Integer, SnmpUsage>> snmpMap = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        for (int hostId = 1; hostId < numHosts + 1; hostId++) {
            Map<Integer, SnmpUsage> lbMap = new HashMap<Integer, SnmpUsage>();
            snmpMap.put(hostId, lbMap);
            for (int lbId = 123; lbId < numLoadBalancers + 123; lbId++) {
                SnmpUsage snmp = new SnmpUsage();
                snmp.setLoadbalancerId(lbId);
                snmp.setHostId(hostId);
                lbMap.put(lbId, snmp);
            }
        }
        return snmpMap;
    }

    public static Map<Integer, List<LoadBalancerHostUsage>> generateLoadBalancerHostUsageMap(int numHosts, int numLoadBalancers,
                                                                                             int pollIntervals, Calendar firstPollTime) {
        Map<Integer, List<LoadBalancerHostUsage>> lbHostMap = new HashMap<Integer, List<LoadBalancerHostUsage>>();
        int pollerInterval = 1;

        for (int interval = 0; interval < pollIntervals; interval++) {
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, pollerInterval * interval);
            for (int lbId = 123; lbId < numLoadBalancers + 123; lbId++) {
                List<LoadBalancerHostUsage> lbHostUsages = new ArrayList<LoadBalancerHostUsage>();
                lbHostMap.put(lbId, lbHostUsages);
                for (int hostId = 1; hostId < numHosts; hostId++) {
                    LoadBalancerHostUsage lbHostUsage = new LoadBalancerHostUsage();
                    lbHostUsage.setAccountId(1111);
                    lbHostUsage.setLoadbalancerId(lbId);
                    lbHostUsage.setHostId(hostId);
                    lbHostUsage.setPollTime(nextPollTime);
                    lbHostUsages.add(lbHostUsage);
                }
            }
        }
        return lbHostMap;
    }
}
