package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;

public class CustomMappings {

    public static Stats mapVirtualServerStats(VirtualServerStats virtualServerStats, VirtualServerStats virtualServerStatsSsl) {
        Stats stats = new Stats();
        VirtualServerStatsProperties properties = virtualServerStats.getStatistics();
        VirtualServerStatsProperties propertiesSsl = virtualServerStatsSsl.getStatistics();
        stats.setConnectError(new int[] {properties.getConnection_errors()});
        stats.setConnectFailure(new int[] {properties.getConnection_failures()});
        stats.setConnectTimeOut(new int[] {properties.getConnect_timed_out()});
        stats.setCurrentConn(new int[] {properties.getCurrent_conn()});
        stats.setDataTimedOut(new int[] {properties.getData_timed_out()});
        stats.setKeepAliveTimedOut(new int[] {properties.getKeepalive_timed_out()});
        stats.setMaxConn(new int[] {properties.getMax_conn()});
        stats.setConnectErrorSsl(new int[] {propertiesSsl.getConnection_errors()});
        stats.setConnectFailureSsl(new int[] {propertiesSsl.getConnection_failures()});
        stats.setConnectTimeOutSsl(new int[] {propertiesSsl.getConnect_timed_out()});
        stats.setCurrentConnSsl(new int[] {propertiesSsl.getCurrent_conn()});
        stats.setDataTimedOutSsl(new int[] {propertiesSsl.getData_timed_out()});
        stats.setKeepAliveTimedOutSsl(new int[] {propertiesSsl.getKeepalive_timed_out()});
        stats.setMaxConnSsl(new int[] {propertiesSsl.getMax_conn()});
        return stats;
    }
}
