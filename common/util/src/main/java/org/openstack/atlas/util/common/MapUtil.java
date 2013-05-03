package org.openstack.atlas.util.common;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public static <K, V> Map<K, Map<K, V>> swapKeys(Map<K, Map<K, V>> groupedByHosts) {
        Map<K, Map<K, V>> groupedByLoadBalancers = new HashMap<K, Map<K, V>>();
        for (K firstKey : groupedByHosts.keySet()) {
            for (K secondKey : groupedByHosts.get(firstKey).keySet()) {
                Map<K, V> hostMap;
                if (!groupedByLoadBalancers.containsKey(secondKey)) {
                    hostMap = new HashMap<K, V>();
                    groupedByLoadBalancers.put(secondKey, hostMap);
                }
                hostMap = groupedByLoadBalancers.get(secondKey);
                hostMap.put(firstKey, groupedByHosts.get(firstKey).get(secondKey));
                groupedByLoadBalancers.put(secondKey, hostMap);
            }
        }
        return groupedByLoadBalancers;
    }

}
