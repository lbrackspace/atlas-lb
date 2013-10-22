package org.openstack.atlas.usagerefactor;

public class SnmpUsage {

    private int loadbalancerId = 0;
    private int hostId = 0;
    private int concurrentConnections = 0;
    private int concurrentConnectionsSsl = 0;
    private long bytesIn = -1;
    private long bytesOut = -1;
    private long bytesInSsl = -1;
    private long bytesOutSsl = -1;

    @Override
    public String toString() {
        return "SnmpUsage{loadbalancerId=" + loadbalancerId
                + ", hostId=" + hostId
                + ", concurrentConnections=" + concurrentConnections
                + ", concurrentConnectionsSsl=" + concurrentConnectionsSsl
                + ", bytesIn=" + bytesIn
                + ", bytesOut=" + bytesOut
                + ", bytesInSsl=" + bytesInSsl
                + ", bytesOutSsl=" + bytesOutSsl
                + "}";

    }

    public SnmpUsage() {
    }

    public SnmpUsage(SnmpUsage o) {
        hostId = o.getHostId();
        loadbalancerId = o.getLoadbalancerId();
        bytesIn = o.getBytesIn();
        bytesOut = o.getBytesOut();
        bytesOutSsl = o.getBytesOutSsl();
        bytesInSsl = o.getBytesInSsl();
        concurrentConnections = o.getConcurrentConnections();
        concurrentConnectionsSsl = o.getConcurrentConnectionsSsl();
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

    public static SnmpUsage add(SnmpUsage o1, SnmpUsage o2) {
        SnmpUsage sum = new SnmpUsage();
        sum.setLoadbalancerId(o1.getLoadbalancerId());
        sum.setHostId(-1);
        sum.setBytesIn(o1.getBytesIn() + o2.getBytesIn());
        sum.setBytesOut(o1.getBytesOut() + o2.getBytesOut());
        sum.setBytesInSsl(o1.getBytesInSsl() + o2.getBytesInSsl());
        sum.setBytesOutSsl(o1.getBytesOutSsl() + o2.getBytesOutSsl());
        sum.setConcurrentConnections(o1.getConcurrentConnections() + o2.getConcurrentConnections());
        sum.setConcurrentConnectionsSsl(o1.getConcurrentConnectionsSsl() + o2.getConcurrentConnectionsSsl());
        return sum;
    }
}
