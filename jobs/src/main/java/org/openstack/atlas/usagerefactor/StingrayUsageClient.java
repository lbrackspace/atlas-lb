package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.util.snmp.RawSnmpUsage;

import java.util.Map;

public interface StingrayUsageClient {

    /*
     *  Given a host ip address, return a map with all the
     *  different values of usage for each virtual server.
     *  Please take note of the value returning to zero
     *  upon the host machine rebooting.
     */
    Map<String, RawSnmpUsage> getHostUsage(String hostIp);

    /*
     * Given a host ip address and virtual server name,
     * return the object populated with all the associated
     * usage values for that virtual server on the given
     * host.  Please take note of the value returning to
     * zero upon the host machine rebooting.
     */
    RawSnmpUsage getVirtualServerUsage(String hostIp, String virtualServerName);
}
