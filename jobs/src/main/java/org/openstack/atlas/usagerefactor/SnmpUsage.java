package org.openstack.atlas.usagerefactor;

public class SnmpUsage {

    private int loadbalancerId = 0;
    private int hostId = 0;
    private int concurrentConnections = 0;
    private int concurrentConnectionsSsl = 0;
    private long bytesIn = 0;
    private long bytesOut = 0;
    private long bytesInSsl = 0;
    private long bytesOutSsl = 0;

    @Override
    public String toString() {
        return "SnmpUsage{loadbalancerId=" + loadbalancerId
                + ", hostId=" + hostId
                + ", concurrentConnections=" + concurrentConnections
                + ", concurrentConnectionsSsl=" + concurrentConnectionsSsl
                + ", bytesIn=" + bytesIn
                + ", bytesOut=" + bytesOut
                + ", bytesInSsl" + bytesInSsl
                + ", bytesOutSsl" + bytesOutSsl
                + "}";

    }

    // Add the two rows together but after this the HostId is meaningless since its joined
    // Accross multiple hosts if you bork the loadbalancerId then the same problem happens
    public SnmpUsage add(SnmpUsage o) {
        SnmpUsage sum = new SnmpUsage();
        sum.setLoadbalancerId(loadbalancerId);
        sum.setHostId(-1);
        sum.setBytesIn(bytesIn + o.getBytesIn());
        sum.setBytesOut(bytesOut + o.getBytesOut());
        sum.setBytesInSsl(bytesInSsl + o.getBytesInSsl());
        sum.setBytesOutSsl(bytesOutSsl + o.getBytesOutSsl());
        sum.setConcurrentConnections(concurrentConnections + o.getConcurrentConnections());
        sum.setConcurrentConnectionsSsl(concurrentConnectionsSsl + o.getConcurrentConnectionsSsl());
        return sum;
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
