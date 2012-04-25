package org.openstack.atlas.usage.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ZxtmNameHelper {
    public static final Log LOG = LogFactory.getLog(ZxtmNameHelper.class);

    /*
     *  A loadbalancer has the following format in Zeus: 'accountId_loadBalancerId'
     *  For example, account 1234 has load balancer 56. The virtual server name in
     *  Zeus is then '1234_56'. If is has ssl then it will be suffixed with '_S'.
     */

    public static Integer stripAccountIdFromZxtmName(String zxtmName) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(zxtmName.split("_")[0]);
    }

    public static Integer stripLbIdFromZxtmName(String zxtmName) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(zxtmName.split("_")[1]);
    }

    public static Map<Integer, Integer> stripLbIdAndAccountIdFromZxtmName(List<String> loadBalancerNames) {
        Map<Integer, Integer> lbIdAccountIdMap = new HashMap<Integer, Integer>();

        for (String loadBalancerName : loadBalancerNames) {
            try {
                Integer accountId = ZxtmNameHelper.stripAccountIdFromZxtmName(loadBalancerName);
                Integer lbId = ZxtmNameHelper.stripLbIdFromZxtmName(loadBalancerName);
                lbIdAccountIdMap.put(lbId, accountId);
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Skipping...", loadBalancerName));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Skipping...", loadBalancerName));
            }
        }

        return lbIdAccountIdMap;
    }

    public static Map<Integer, Integer> stripLbIdAndAccountIdFromZxtmName(Collection<LoadBalancerNameMap> loadBalancerNameMaps) {
        Map<Integer, Integer> lbIdAccountIdMap = new HashMap<Integer, Integer>();

        for (String loadBalancerName : LoadBalancerNameMap.loadBalancerNames(loadBalancerNameMaps)) {
            try {
                Integer accountId = ZxtmNameHelper.stripAccountIdFromZxtmName(loadBalancerName);
                Integer lbId = ZxtmNameHelper.stripLbIdFromZxtmName(loadBalancerName);
                lbIdAccountIdMap.put(lbId, accountId);
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Skipping...", loadBalancerName));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Skipping...", loadBalancerName));
            }
        }

        for (String loadBalancerName : LoadBalancerNameMap.loadBalancerSslNames(loadBalancerNameMaps)) {
            try {
                Integer accountId = ZxtmNameHelper.stripAccountIdFromZxtmName(loadBalancerName);
                Integer lbId = ZxtmNameHelper.stripLbIdFromZxtmName(loadBalancerName);
                lbIdAccountIdMap.put(lbId, accountId);
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Skipping...", loadBalancerName));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Skipping...", loadBalancerName));
            }
        }

        return lbIdAccountIdMap;
    }
}
