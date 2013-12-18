package org.openstack.atlas.usagerefactor.generator;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.*;

public class UsagePollerGenerator {

    public static List<Host> generateHosts(int numHosts) {
        List<Host> hosts = new ArrayList<Host>();
        for (int i = 0; i < numHosts; i++) {
                hosts.add(new Host());
        }
        return hosts;
    }
    public static Map<Integer, Map<Integer, SnmpUsage>> generateSnmpMap(int numHosts, int numLoadBalancers) {

        Map<Integer, Map<Integer, SnmpUsage>> snmpMap = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        for (int hostId = 1; hostId < numHosts + 1; hostId++) {
            Map<Integer, SnmpUsage> lbMap = new HashMap<Integer, SnmpUsage>();
            snmpMap.put(hostId, lbMap);
            for (int lbId = 123; lbId < numLoadBalancers + 123; lbId++) {
                SnmpUsage snmp = new SnmpUsage();
                snmp.setLoadbalancerId(lbId);
                snmp.setHostId(hostId);
                snmp.setConcurrentConnections(0);
                snmp.setConcurrentConnectionsSsl(0);
                snmp.setBytesIn(0);
                snmp.setBytesOut(0);
                snmp.setBytesInSsl(0);
                snmp.setBytesOutSsl(0);
                lbMap.put(lbId, snmp);
            }
        }
        return snmpMap;
    }

    public static Map<Integer, List<LoadBalancerHostUsage>> generateLoadBalancerHostUsageMap(int numHosts, int numLoadBalancers,
                                                                                             int pollIntervals, Calendar firstPollTime,
                                                                                             int firstLBId) {
        Map<Integer, List<LoadBalancerHostUsage>> lbHostMap = new HashMap<Integer, List<LoadBalancerHostUsage>>();
        int pollerInterval = 1;

        for (int interval = 0; interval < pollIntervals; interval++) {
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, pollerInterval * interval);
            for (int lbId = firstLBId; lbId < numLoadBalancers + 123; lbId++) {
                List<LoadBalancerHostUsage> lbHostUsages;
                if (!lbHostMap.containsKey(lbId)) {
                    lbHostUsages = new ArrayList<LoadBalancerHostUsage>();
                    lbHostMap.put(lbId, lbHostUsages);
                }
                lbHostUsages = lbHostMap.get(lbId);
                for (int hostId = 1; hostId < numHosts + 1; hostId++) {
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
