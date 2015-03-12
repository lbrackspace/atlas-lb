/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.logs.itest;

import java.util.Comparator;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;

public class AccountIdLoadBalancerIdComparator implements Comparator<LoadBalancerIdAndName> {

    @Override
    public int compare(LoadBalancerIdAndName o1, LoadBalancerIdAndName o2) {
        int a1 = o1.getAccountId();
        int a2 = o1.getAccountId();
        int l1 = o1.getLoadbalancerId();
        int l2 = o2.getLoadbalancerId();

        if (a1 < a2) {
            return -1;
        }
        if (a1 > a2) {
            return 1;
        }
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return 1;
        }
        return 0;
    }
}
