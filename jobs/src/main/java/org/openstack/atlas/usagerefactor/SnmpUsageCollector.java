package org.openstack.atlas.usagerefactor;

import java.util.Map;

public interface SnmpUsageCollector {

        /*
     * Key of first Map is hostId
     * Key of nested map is loadBalancerId
     */
    //Parent key should be hostId, child key should be loadbalancerId
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception;
}
