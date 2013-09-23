package org.openstack.atlas.logs.itest;

import java.util.Comparator;

public class AccountIdLoadBalancerIdKeyComparator implements Comparator<AccountIdLoadBalancerIdKey> {

    @Override
    public int compare(AccountIdLoadBalancerIdKey o1, AccountIdLoadBalancerIdKey o2) {
                if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        int a1 = o1.getAccountId();
        int a2 = o2.getAccountId();
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
