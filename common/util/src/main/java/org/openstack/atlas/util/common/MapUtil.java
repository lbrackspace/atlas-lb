package org.openstack.atlas.util.common;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public static <K, V> Map<K, Map<K, V>> swapKeys(Map<K, Map<K, V>> swapFromMap) {
        if (swapFromMap == null) {
            return null;
        }
        Map<K, Map<K, V>> swapToMap = new HashMap<K, Map<K, V>>();
        for (K firstKey : swapFromMap.keySet()) {
            for (K secondKey : swapFromMap.get(firstKey).keySet()) {
                Map<K, V> hostMap;
                if (!swapToMap.containsKey(secondKey)) {
                    hostMap = new HashMap<K, V>();
                    swapToMap.put(secondKey, hostMap);
                }
                hostMap = swapToMap.get(secondKey);
                hostMap.put(firstKey, swapFromMap.get(firstKey).get(secondKey));
                swapToMap.put(secondKey, hostMap);
            }
        }
        return swapToMap;
    }

}
