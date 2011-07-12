package org.openstack.atlas.usage.helpers;

public final class ZxtmNameHelper {

    /*
     *  A loadbalancer has the following format in Zeus: 'accountId_loadBalancerId'
     *  For example, account 1234 has load balancer 56. The virtual server name in
     *  Zeus is then '1234_56'.
     */

    public static Integer stripAccountIdFromZxtmName(String zxtmName) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(zxtmName.split("_")[0]);
    }

    public static Integer stripLbIdFromZxtmName(String zxtmName) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(zxtmName.split("_")[1]);
    }
}
