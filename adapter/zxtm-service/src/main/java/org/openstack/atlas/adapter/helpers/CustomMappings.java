package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.stingray.pojo.counters.VirtualServerStats;
import org.rackspace.stingray.pojo.counters.Statistics;

import java.util.List;

public class CustomMappings {

    public static Stats mapVirtualServerStats(VirtualServerStats virtualServerStats, VirtualServerStats virtualServerStatsSsl) {
        Stats stats = new Stats();
        Statistics properties = virtualServerStats.getStatistics();
        Statistics propertiesSsl = virtualServerStatsSsl.getStatistics();
        stats.setConnectError(new int[]{properties.getConnection_errors()});
        stats.setConnectFailure(new int[]{properties.getConnection_failures()});
        stats.setConnectTimeOut(new int[]{properties.getConnect_timed_out()});
        stats.setCurrentConn(new int[]{properties.getCurrent_conn()});
        stats.setDataTimedOut(new int[]{properties.getData_timed_out()});
        stats.setKeepAliveTimedOut(new int[]{properties.getKeepalive_timed_out()});
        stats.setMaxConn(new int[]{properties.getMax_conn()});
        stats.setConnectErrorSsl(new int[]{propertiesSsl.getConnection_errors()});
        stats.setConnectFailureSsl(new int[]{propertiesSsl.getConnection_failures()});
        stats.setConnectTimeOutSsl(new int[]{propertiesSsl.getConnect_timed_out()});
        stats.setCurrentConnSsl(new int[]{propertiesSsl.getCurrent_conn()});
        stats.setDataTimedOutSsl(new int[]{propertiesSsl.getData_timed_out()});
        stats.setKeepAliveTimedOutSsl(new int[]{propertiesSsl.getKeepalive_timed_out()});
        stats.setMaxConnSsl(new int[]{propertiesSsl.getMax_conn()});
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
            Statistics properties = vss.getStatistics();
            connectionError[0] += properties.getConnection_errors();
            connectionFailure[0] += properties.getConnection_failures();
            connectionTimedOut[0] += properties.getConnect_timed_out();
            currentConnections[0] += properties.getCurrent_conn();
            dataTimedOut[0] += properties.getData_timed_out();
            keepaliveTimedOut[0] += properties.getKeepalive_timed_out();
            Integer max = properties.getMax_conn();
            if (max > maxConnections[0]) {
                maxConnections[0] = max;
            }

        }

        for (VirtualServerStats vsss : virtualServerStatsSsl) {
            Statistics propertiesSsl = vsss.getStatistics();
            connectionErrorSsl[0] += propertiesSsl.getConnection_errors();
            connectionFailureSsl[0] += propertiesSsl.getConnection_failures();
            connectionTimedOutSsl[0] += propertiesSsl.getConnect_timed_out();
            currentConnections[0] += propertiesSsl.getCurrent_conn();
            dataTimedOutSsl[0] += propertiesSsl.getData_timed_out();
            keepaliveTimedOutSsl[0] += propertiesSsl.getKeepalive_timed_out();
            Integer maxSsl = propertiesSsl.getMax_conn();
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
