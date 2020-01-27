package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client.counters.VirtualServerStatsStatistics;

import java.util.List;

public class CustomMappings {

    public static Stats mapVirtualServerStats(VirtualServerStats virtualServerStats, VirtualServerStats virtualServerStatsSsl) {
        Stats stats = new Stats();
<<<<<<< HEAD
        VirtualServerStatsProperties properties = virtualServerStats.getStatistics();
        VirtualServerStatsProperties propertiesSsl = virtualServerStatsSsl.getStatistics();
        stats.setConnectError(properties.getConnectionErrors());
        stats.setConnectFailure(properties.getConnectionFailures());
        stats.setConnectTimeOut(properties.getConnectTimedOut());
        stats.setCurrentConn(properties.getCurrentConn());
        stats.setDataTimedOut(properties.getDataTimedOut());
        stats.setKeepAliveTimedOut(properties.getKeepaliveTimedOut());
        stats.setMaxConn(properties.getMaxConn());
        stats.setConnectErrorSsl(propertiesSsl.getConnectionErrors());
        stats.setConnectFailureSsl(propertiesSsl.getConnectionFailures());
        stats.setConnectTimeOutSsl(propertiesSsl.getConnectTimedOut());
        stats.setCurrentConnSsl(propertiesSsl.getCurrentConn());
        stats.setDataTimedOutSsl(propertiesSsl.getDataTimedOut());
        stats.setKeepAliveTimedOutSsl(propertiesSsl.getKeepaliveTimedOut());
        stats.setMaxConnSsl(propertiesSsl.getMaxConn());
=======
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
>>>>>>> 67ad2c5... Updating RestAdapter schemas for 7.0
        return stats;
    }

    public static Stats mapVirtualServerStatsLists(List<VirtualServerStats> virtualServerStats, List<VirtualServerStats> virtualServerStatsSsl) {
        long connectionTimedOut = 0L;
        long connectionError = 0L;
        long connectionFailure = 0L;
        long dataTimedOut = 0L;
        long keepaliveTimedOut = 0L;
        long maxConnections = 0L;
        long currentConnections = 0L;
        long connectionTimedOutSsl = 0L;
        long connectionErrorSsl = 0L;
        long connectionFailureSsl = 0L;
        long dataTimedOutSsl = 0L;
        long keepaliveTimedOutSsl = 0L;
        long maxConnectionsSsl = 0L;
        long currentConnectionsSsl = 0L;

        Stats stats = new Stats();

        for (VirtualServerStats vss : virtualServerStats) {
<<<<<<< HEAD
            VirtualServerStatsProperties properties = vss.getStatistics();
            connectionError += properties.getConnectionErrors();
            connectionFailure += properties.getConnectionFailures();
            connectionTimedOut += properties.getConnectTimedOut();
            currentConnections += properties.getCurrentConn();
            dataTimedOut += properties.getDataTimedOut();
            keepaliveTimedOut += properties.getKeepaliveTimedOut();
            long max = properties.getMaxConn();
            if (max > maxConnections) {
                maxConnections = max;
=======
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
>>>>>>> 67ad2c5... Updating RestAdapter schemas for 7.0
            }

        }

        for (VirtualServerStats vsss : virtualServerStatsSsl) {
<<<<<<< HEAD
            VirtualServerStatsProperties propertiesSsl = vsss.getStatistics();
            connectionErrorSsl += propertiesSsl.getConnectionErrors();
            connectionFailureSsl += propertiesSsl.getConnectionFailures();
            connectionTimedOutSsl += propertiesSsl.getConnectTimedOut();
            currentConnectionsSsl += propertiesSsl.getCurrentConn();
            dataTimedOutSsl += propertiesSsl.getDataTimedOut();
            keepaliveTimedOutSsl += propertiesSsl.getKeepaliveTimedOut();
            long maxSsl = propertiesSsl.getMaxConn();
            if (maxSsl > maxConnectionsSsl) {
                maxConnectionsSsl = maxSsl;
=======
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
>>>>>>> 67ad2c5... Updating RestAdapter schemas for 7.0
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
