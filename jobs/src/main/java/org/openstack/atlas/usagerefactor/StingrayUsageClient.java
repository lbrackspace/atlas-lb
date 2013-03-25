package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Host;

import java.util.Map;

public interface StingrayUsageClient {

    /*
     *  Given a host, returns a map of virtual server names
     *  along with their total connection count. Please note
     *  that the connection count resets to 0 if the host
     *  machine restarts as the values are stored in memory.
     */
    Map<String, Long> getTotalConnections(Host host);

    /*
     *  Given a host, returns a map of virtual server names
     *  along with their total inbound bandwidth count. Please
     *  note that the bandwidth count resets to 0 if the host
     *  machine restarts as the values are stored in memory.
     */
    Map<String, Long> getTotalBandwidthIn(Host host);

    /*
     *  Given a host, returns a map of virtual server names
     *  along with their total outbound bandwidth count. Please
     *  note that the bandwidth count resets to 0 if the host
     *  machine restarts as the values are stored in memory.
     */
    Map<String, Long> getTotalBandwidthOut(Host host);

}
