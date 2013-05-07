package org.openstack.atlas.usagerefactor.snmp;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;

import java.util.List;
import java.util.Map;

public interface StingrayUsageClient {

    /*
     *  Given a host ip address, return a map with all the
     *  different values of usage for each virtual server.
     *  Please take note of the value returning to zero
     *  upon the host machine rebooting.
     */
    Map<Integer, SnmpUsage> getHostUsage(Host host) throws StingraySnmpGeneralException;


    /*
     * Given a host ip address and virtual server name,
     * return the object populated with all the associated
     * usage values for that virtual server on the given
     * host.  Please take note of the value returning to
     * zero upon the host machine rebooting.
     */
    SnmpUsage getVirtualServerUsage(Host host, LoadBalancer lb) throws StingraySnmpGeneralException;

    List<SnmpUsage> getHostUsageList(Host host) throws StingraySnmpGeneralException;
}
