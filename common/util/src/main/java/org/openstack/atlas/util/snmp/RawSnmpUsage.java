package org.openstack.atlas.util.snmp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RawSnmpUsage implements Comparable<RawSnmpUsage> {

    private String vsName = "";
    private long bytesInHi = -1;
    private long bytesInLo = -1;
    private long bytesOutLo = -1;
    private long bytesOutHi = -1;
    private int totalConnections = -1;
    private int concurrentConnections = -1;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return "RawSnmpUsage{vsName=" + vsName
                + ", bytesInHi=" + bytesInHi
                + ", bytesInLo=" + bytesInLo
                + ", bytesOutHi=" + bytesOutHi
                + ", bytesOutLo=" + bytesOutLo
                + ", concurrentConnections=" + concurrentConnections
                + ", totalConnections=" + totalConnections
                + ", bytesIn=" + deriveBytesIn()
                + ", bytesOut=" + deriveBytesOut()
                + "}";

    }

    public void write(DataOutput d) throws IOException {
        d.writeUTF(vsName);
        d.writeLong(bytesInHi);
        d.writeLong(bytesInLo);
        d.writeLong(bytesOutHi);
        d.writeLong(bytesOutLo);
        d.writeInt(totalConnections);
        d.writeInt(concurrentConnections);
    }

    public void readFields(DataInput di) throws IOException {
        this.vsName = di.readUTF();
        this.bytesInHi = di.readLong();
        this.bytesInLo = di.readLong();
        this.bytesOutHi = di.readLong();
        this.bytesOutLo = di.readLong();
        this.totalConnections = di.readInt();
        this.concurrentConnections = di.readInt();
    }

    public long deriveBytesIn() {
        return ((bytesInHi & 0xffffffff) << 32) | (bytesInLo & 0xffffffff);
    }

    public long deriveBytesOut() {
        return ((bytesOutHi & 0xffffffff) << 32) | (bytesOutLo & 0xffffffff);
    }

    public String getVsName() {
        return vsName;
    }

    public void setVsName(String vsName) {
        this.vsName = vsName;
    }

    public long getBytesInHi() {
        return bytesInHi;
    }

    public void setBytesInHi(long bytesInHi) {
        this.bytesInHi = bytesInHi;
    }

    public long getBytesInLo() {
        return bytesInLo;
    }

    public void setBytesInLo(long bytesInLo) {
        this.bytesInLo = bytesInLo;
    }

    public long getBytesOutLo() {
        return bytesOutLo;
    }

    public void setBytesOutLo(long bytesOutLo) {
        this.bytesOutLo = bytesOutLo;
    }

    public long getBytesOutHi() {
        return bytesOutHi;
    }

    public void setBytesOutHi(long bytesOutHi) {
        this.bytesOutHi = bytesOutHi;
    }

    public int getConcurrentConnections() {
        return concurrentConnections;
    }

    public void setConcurrentConnections(int concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    @Override
    public int compareTo(RawSnmpUsage o) {
        long oBytesInHi = o.getBytesInHi();
        long oBytesInLo = o.getBytesInLo();
        long oBytesOutHi = o.getBytesOutHi();
        long oBytesOutLo = o.getBytesOutLo();
        int oConCurrent = o.getConcurrentConnections();
        int oTotalConnections = o.getTotalConnections();

        if (bytesInHi < oBytesInHi) {
            return -1;
        }
        if (bytesInHi > oBytesInHi) {
            return 1;
        }
        if (bytesInLo < oBytesInLo) {
            return -1;
        }
        if (bytesInLo > oBytesInLo) {
            return 1;
        }
        if (bytesOutHi < oBytesOutHi) {
            return -1;
        }
        if (bytesOutHi > oBytesOutHi) {
            return 1;
        }
        if (bytesOutLo < oBytesOutLo) {
            return -1;
        }
        if (bytesOutLo > oBytesOutLo) {
            return 1;
        }

        if (concurrentConnections < oConCurrent) {
            return -1;
        }
        if (concurrentConnections > oConCurrent) {
            return 1;
        }
        if (totalConnections < oTotalConnections) {
            return -1;
        }
        if (totalConnections > oTotalConnections) {
            return 1;
        }
        return vsName.compareTo(o.getVsName());
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(int totalConnections) {
        this.totalConnections = totalConnections;
    }
}
