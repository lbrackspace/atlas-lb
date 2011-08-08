package org.openstack.atlas.usage.helpers;

public final class AdapterNameHelper {

    /*
     *  A loadbalancer has the following format in LB Device: 'accountId_loadBalancerId'
     *  For example, account 1234 has load balancer 56. The virtual server name in
     *  LB Device is then '1234_56'.
     */

    public static Integer stripAccountIdFromName(String name) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(name.split("_")[0]);
    }

    public static Integer stripLbIdFromName(String name) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(name.split("_")[1]);
    }
}
