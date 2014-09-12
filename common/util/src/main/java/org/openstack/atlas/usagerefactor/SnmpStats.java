package org.openstack.atlas.usagerefactor;

public class SnmpStats {
    private int loadbalancerId = 0;
    private int hostId = 0;
    private int concurrentConnections = 0;
    private int concurrentConnectionsSsl = 0;
    private int maxConnections = 0;
    private int maxConnectionsSsl = 0;
    private int connectTimedOut = 0;
    private int connectTimedOutSsl = 0;
    private int dataTimedOut = 0;
    private int dataTimedOutSsl = 0;
    private int keepaliveTimedOut = 0;
    private int keepaliveTimedOutSsl = 0;
    private int connectionErrors = 0;
    private int connectionErrorsSsl = 0;
    private int connectionFailures = 0;
    private int connectionFailuresSsl = 0;

    @Override
    public String toString() {
        return "SnmpUsage{loadbalancerId=" + loadbalancerId
                + ", hostId=" + hostId
                + ", concurrentConnections=" + concurrentConnections
                + ", concurrentConnectionsSsl=" + concurrentConnectionsSsl
                + ", maxConnections=" + maxConnections
                + ", maxConnectionsSsl=" + maxConnectionsSsl
                + ", connectTimedOut=" + connectTimedOut
                + ", connectTimedOutSsl=" + connectTimedOutSsl
                + ", dataTimedOut=" + dataTimedOut
                + ", dataTimedOutSsl=" + dataTimedOutSsl
                + ", keepaliveTimedOut=" + keepaliveTimedOut
                + ", keepaliveTimedOutSsl=" + keepaliveTimedOutSsl
                + ", connectionErrors=" + connectionErrors
                + ", connectionErrorsSsl=" + connectionErrorsSsl
                + ", connectionFailures=" + connectionFailures
                + ", connectionFailuresSsl=" + connectionFailuresSsl
                + "}";

    }

    public SnmpStats() {
    }

    public SnmpStats(SnmpStats o) {
        hostId = o.getHostId();
        loadbalancerId = o.getLoadbalancerId();
        concurrentConnections = o.getConcurrentConnections();
        concurrentConnectionsSsl = o.getConcurrentConnectionsSsl();
        maxConnections = o.getMaxConnections();
        maxConnectionsSsl = o.getMaxConnectionsSsl();
        connectTimedOut = o.getConnectTimedOut();
        connectTimedOutSsl = o.getConnectTimedOutSsl();
        dataTimedOut = o.getDataTimedOut();
        dataTimedOutSsl = o.getDataTimedOutSsl();
        keepaliveTimedOut = o.getKeepaliveTimedOut();
        keepaliveTimedOutSsl = o.getKeepaliveTimedOutSsl();
        connectionErrors = o.getConnectionErrors();
        connectionErrorsSsl = o.getConnectionErrorsSsl();
        connectionFailures = o.getConnectionFailures();
        connectionFailuresSsl = o.getConnectionFailuresSsl();
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getConcurrentConnections() {
        return concurrentConnections;
    }

    public void setConcurrentConnections(int concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public int getConcurrentConnectionsSsl() {
        return concurrentConnectionsSsl;
    }

    public void setConcurrentConnectionsSsl(int concurrentConnectionsSsl) {
        this.concurrentConnectionsSsl = concurrentConnectionsSsl;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxConnectionsSsl() {
        return maxConnectionsSsl;
    }

    public void setMaxConnectionsSsl(int maxConnectionsSsl) {
        this.maxConnectionsSsl = maxConnectionsSsl;
    }

    public int getConnectTimedOut() {
        return connectTimedOut;
    }

    public void setConnectTimedOut(int connectTimedOut) {
        this.connectTimedOut = connectTimedOut;
    }

    public int getConnectTimedOutSsl() {
        return connectTimedOutSsl;
    }

    public void setConnectTimedOutSsl(int connectTimedOutSsl) {
        this.connectTimedOutSsl = connectTimedOutSsl;
    }

    public int getDataTimedOut() {
        return dataTimedOut;
    }

    public void setDataTimedOut(int dataTimedOut) {
        this.dataTimedOut = dataTimedOut;
    }

    public int getDataTimedOutSsl() {
        return dataTimedOutSsl;
    }

    public void setDataTimedOutSsl(int dataTimedOutSsl) {
        this.dataTimedOutSsl = dataTimedOutSsl;
    }

    public int getKeepaliveTimedOut() {
        return keepaliveTimedOut;
    }

    public void setKeepaliveTimedOut(int keepaliveTimedOut) {
        this.keepaliveTimedOut = keepaliveTimedOut;
    }

    public int getKeepaliveTimedOutSsl() {
        return keepaliveTimedOutSsl;
    }

    public void setKeepaliveTimedOutSsl(int keepaliveTimedOutSsl) {
        this.keepaliveTimedOutSsl = keepaliveTimedOutSsl;
    }

    public int getConnectionErrors() {
        return connectionErrors;
    }

    public void setConnectionErrors(int connectionErrors) {
        this.connectionErrors = connectionErrors;
    }

    public int getConnectionErrorsSsl() {
        return connectionErrorsSsl;
    }

    public void setConnectionErrorsSsl(int connectionErrorsSsl) {
        this.connectionErrorsSsl = connectionErrorsSsl;
    }

    public int getConnectionFailures() {
        return connectionFailures;
    }

    public void setConnectionFailures(int connectionFailures) {
        this.connectionFailures = connectionFailures;
    }

    public int getConnectionFailuresSsl() {
        return connectionFailuresSsl;
    }

    public void setConnectionFailuresSsl(int connectionFailuresSsl) {
        this.connectionFailuresSsl = connectionFailuresSsl;
    }

    public static SnmpStats add(SnmpStats o1, SnmpStats o2) {
        SnmpStats sum = new SnmpStats();
        sum.setLoadbalancerId(o1.getLoadbalancerId());
        sum.setHostId(-1);

        sum.setConcurrentConnections(o1.getConcurrentConnections() + o2.getConcurrentConnections());
        sum.setConcurrentConnectionsSsl(o1.getConcurrentConnectionsSsl() + o2.getConcurrentConnectionsSsl());
        sum.setConnectTimedOut(o1.getConnectTimedOut() + o2.getConnectTimedOut());
        sum.setConnectTimedOutSsl(o1.getConnectTimedOutSsl() + o2.getConnectTimedOutSsl());
        sum.setDataTimedOut(o1.getDataTimedOut() + o2.getDataTimedOut());
        sum.setDataTimedOutSsl(o1.getDataTimedOutSsl() + o2.getDataTimedOutSsl());
        sum.setKeepaliveTimedOut(o1.getKeepaliveTimedOut() + o2.getKeepaliveTimedOut());
        sum.setKeepaliveTimedOutSsl(o1.getKeepaliveTimedOutSsl() + o2.getKeepaliveTimedOutSsl());
        sum.setConnectionErrors(o1.getConnectionErrors() + o2.getConnectionErrors());
        sum.setConnectionErrorsSsl(o1.getConnectionErrorsSsl() + o2.getConnectionErrorsSsl());
        sum.setConnectionFailures(o1.getConnectionFailures() + o2.getConnectionFailures());
        sum.setConnectionFailuresSsl(o1.getConnectionFailuresSsl() + o2.getConnectionFailuresSsl());

        if (o1.getMaxConnections() > o2.getMaxConnections()) {
            sum.setMaxConnections(o1.getMaxConnections());
        } else {
            sum.setMaxConnections(o2.getMaxConnections());
        }

        if (o1.getMaxConnectionsSsl() > o2.getMaxConnectionsSsl()) {
            sum.setMaxConnectionsSsl(o1.getMaxConnectionsSsl());
        } else {
            sum.setMaxConnectionsSsl(o2.getMaxConnectionsSsl());
        }

        return sum;
    }
}
