package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client.counters.VirtualServerStatsStatistics;

import java.util.List;

public class CustomMappings {

    public static Stats mapVirtualServerStats(VirtualServerStats virtualServerStats, VirtualServerStats virtualServerStatsSsl) {
        Stats stats = new Stats();
        VirtualServerStatsStatistics properties = virtualServerStats.getProperties().getStatistics();
        VirtualServerStatsStatistics propertiesSsl = virtualServerStatsSsl.getProperties().getStatistics();
        stats.setConnectError(new int[]{properties.getConnectionErrors()});
        stats.setConnectFailure(new int[]{properties.getConnectionFailures()});
        stats.setConnectTimeOut(new int[]{properties.getConnectTimedOut()});
        stats.setCurrentConn(new int[]{properties.getCurrentConn()});
        stats.setDataTimedOut(new int[]{properties.getDataTimedOut()});
        stats.setKeepAliveTimedOut(new int[]{properties.getKeepaliveTimedOut()});
        stats.setMaxConn(new int[]{properties.getMaxConn()});
        stats.setConnectErrorSsl(new int[]{propertiesSsl.getConnectionErrors()});
        stats.setConnectFailureSsl(new int[]{propertiesSsl.getConnectionFailures()});
        stats.setConnectTimeOutSsl(new int[]{propertiesSsl.getConnectTimedOut()});
        stats.setCurrentConnSsl(new int[]{propertiesSsl.getCurrentConn()});
        stats.setDataTimedOutSsl(new int[]{propertiesSsl.getDataTimedOut()});
        stats.setKeepAliveTimedOutSsl(new int[]{propertiesSsl.getKeepaliveTimedOut()});
        stats.setMaxConnSsl(new int[]{propertiesSsl.getMaxConn()});
        return stats;
    }

    public static Stats mapVirtualServerStatsLists(List<VirtualServerStats> virtualServerStats, List<VirtualServerStats> virtualServerStatsSsl) {
        int[] connectionTimedOut = new int[1];
        int[] connectionError = new int[1];
        int[] connectionFailure = new int[1];
        int[] dataTimedOut = new int[1];
        int[] keepaliveTimedOut = new int[1];
        int[] maxConnections = new int[]{0};
        int[] currentConnections = new int[1];
        int[] connectionTimedOutSsl = new int[1];
        int[] connectionErrorSsl = new int[1];
        int[] connectionFailureSsl = new int[1];
        int[] dataTimedOutSsl = new int[1];
        int[] keepaliveTimedOutSsl = new int[1];
        int[] maxConnectionsSsl = new int[1];
        int[] currentConnectionsSsl = new int[1];

        Stats stats = new Stats();

        for (VirtualServerStats vss : virtualServerStats) {
            VirtualServerStatsStatistics properties = vss.getProperties().getStatistics();
            connectionError[0] += properties.getConnectionErrors();
            connectionFailure[0] += properties.getConnectionFailures();
            connectionTimedOut[0] += properties.getConnectTimedOut();
            currentConnections[0] += properties.getCurrentConn();
            dataTimedOut[0] += properties.getDataTimedOut();
            keepaliveTimedOut[0] += properties.getKeepaliveTimedOut();
            Integer max = properties.getMaxConn();
            if (max > maxConnections[0]) {
                maxConnections[0] = max;
            }

        }

        for (VirtualServerStats vsss : virtualServerStatsSsl) {
            VirtualServerStatsStatistics propertiesSsl = vsss.getProperties().getStatistics();
            connectionErrorSsl[0] += propertiesSsl.getConnectionErrors();
            connectionFailureSsl[0] += propertiesSsl.getConnectionFailures();
            connectionTimedOutSsl[0] += propertiesSsl.getConnectTimedOut();
            currentConnectionsSsl[0] += propertiesSsl.getCurrentConn();
            dataTimedOutSsl[0] += propertiesSsl.getDataTimedOut();
            keepaliveTimedOutSsl[0] += propertiesSsl.getKeepaliveTimedOut();
            Integer maxSsl = propertiesSsl.getMaxConn();
            if (maxSsl > maxConnectionsSsl[0]) {
                maxConnectionsSsl[0] = maxSsl;
            }
        }

        stats.setConnectTimeOut(connectionTimedOut);
        stats.setConnectError(connectionError);
        stats.setConnectFailure(connectionFailure);
        stats.setDataTimedOut(dataTimedOut);
        stats.setKeepAliveTimedOut(keepaliveTimedOut);
        stats.setMaxConn(maxConnections);
        stats.setCurrentConn(currentConnections);
        stats.setConnectTimeOutSsl(connectionTimedOutSsl);
        stats.setConnectErrorSsl(connectionErrorSsl);
        stats.setConnectFailureSsl(connectionFailureSsl);
        stats.setDataTimedOutSsl(dataTimedOutSsl);
        stats.setKeepAliveTimedOutSsl(keepaliveTimedOutSsl);
        stats.setMaxConnSsl(maxConnectionsSsl);
        stats.setCurrentConnSsl(currentConnectionsSsl);
        return stats;
    }
}
