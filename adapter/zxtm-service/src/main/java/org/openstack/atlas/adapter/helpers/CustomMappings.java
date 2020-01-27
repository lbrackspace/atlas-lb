package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client_7.counters.VirtualServerStatsStatistics;

import java.util.List;

public class CustomMappings {

    public static Stats mapVirtualServerStats(VirtualServerStats virtualServerStats, VirtualServerStats virtualServerStatsSsl) {
        Stats stats = new Stats();
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
            }

        }

        for (VirtualServerStats vsss : virtualServerStatsSsl) {
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


    public static Stats mapVirtualServer7Stats(org.rackspace.stingray.client_7.counters.VirtualServerStats virtualServerStats, org.rackspace.stingray.client_7.counters.VirtualServerStats virtualServerStatsSsl) {
        Stats stats = new Stats();
        VirtualServerStatsStatistics properties = virtualServerStats.getProperties().getStatistics();
        VirtualServerStatsStatistics propertiesSsl = virtualServerStatsSsl.getProperties().getStatistics();
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
        return stats;
    }

    public static Stats mapVirtualServer7StatsLists(List<org.rackspace.stingray.client_7.counters.VirtualServerStats> virtualServerStats, List<org.rackspace.stingray.client_7.counters.VirtualServerStats> virtualServerStatsSsl) {
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

        for (org.rackspace.stingray.client_7.counters.VirtualServerStats vss : virtualServerStats) {
            VirtualServerStatsStatistics properties = vss.getProperties().getStatistics();
            connectionError += properties.getConnectionErrors();
            connectionFailure += properties.getConnectionFailures();
            connectionTimedOut += properties.getConnectTimedOut();
            currentConnections += properties.getCurrentConn();
            dataTimedOut += properties.getDataTimedOut();
            keepaliveTimedOut += properties.getKeepaliveTimedOut();
            Long max = properties.getMaxConn();
            if (max > maxConnections) {
                maxConnections = max;
            }

        }

        for (org.rackspace.stingray.client_7.counters.VirtualServerStats vsss : virtualServerStatsSsl) {
            VirtualServerStatsStatistics propertiesSsl = vsss.getProperties().getStatistics();
            connectionErrorSsl += propertiesSsl.getConnectionErrors();
            connectionFailureSsl += propertiesSsl.getConnectionFailures();
            connectionTimedOutSsl += propertiesSsl.getConnectTimedOut();
            currentConnectionsSsl += propertiesSsl.getCurrentConn();
            dataTimedOutSsl += propertiesSsl.getDataTimedOut();
            keepaliveTimedOutSsl += propertiesSsl.getKeepaliveTimedOut();
            Long maxSsl = propertiesSsl.getMaxConn();
            if (maxSsl > maxConnectionsSsl) {
                maxConnectionsSsl = maxSsl;
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

