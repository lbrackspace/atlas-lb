package org.openstack.atlas.usagerefactor;

public class SnmpUsage {

    private int loadbalancerId;
    private int hostId;
    private int concurrentConnections;
    private int concurrentConnectionsSsl;
    private long bytesIn;
    private long bytesOut;
    private long bytesInSsl;
    private long bytesOutSsl;

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

    public long getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public long getBytesInSsl() {
        return bytesInSsl;
    }

    public void setBytesInSsl(long bytesInSsl) {
        this.bytesInSsl = bytesInSsl;
    }

    public long getBytesOutSsl() {
        return bytesOutSsl;
    }

    public void setBytesOutSsl(long bytesOutSsl) {
        this.bytesOutSsl = bytesOutSsl;
    }

}
