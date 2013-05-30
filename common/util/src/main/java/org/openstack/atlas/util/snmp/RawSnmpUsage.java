package org.openstack.atlas.util.snmp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RawSnmpUsage implements Comparable<RawSnmpUsage> {

    private String vsName = "";
    private long bytesIn = -1;
    private long bytesOut = -1;
    private long concurrentConnections = -1;

    public RawSnmpUsage() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return "RawSnmpUsage{vsName=" + vsName
                + ", bytesIn=" + bytesIn
                + ", bytesOut=" + bytesOut
                + ", concurrentConnections=" + concurrentConnections
                + "}";

    }

    public String getVsName() {
        return vsName;
    }

    public void setVsName(String vsName) {
        this.vsName = vsName;
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

    public long getConcurrentConnections() {
        return concurrentConnections;
    }

    public void setConcurrentConnections(long concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    @Override
    public int compareTo(RawSnmpUsage o) {
        long oBytesIn = o.getBytesIn();
        long oBytesOut = o.getBytesOut();
        long oConcurrentConnections = o.getConcurrentConnections();
        String oVsName = o.getVsName();

        if (bytesOut < oBytesOut) {
            return -1;
        }

        if (bytesOut > oBytesOut) {
            return 1;
        }

        if (bytesIn < oBytesIn) {
            return -1;
        }
        if (bytesIn > oBytesIn) {
            return 1;
        }

        if (concurrentConnections < oConcurrentConnections) {
            return -1;
        }
        if (concurrentConnections > oConcurrentConnections) {
            return 1;
        }
        return vsName.compareTo(oVsName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RawSnmpUsage other = (RawSnmpUsage) obj;
        if ((this.vsName == null) ? (other.vsName != null) : !this.vsName.equals(other.vsName)) {
            return false;
        }
        if (this.bytesIn != other.bytesIn) {
            return false;
        }
        if (this.bytesOut != other.bytesOut) {
            return false;
        }
        if (this.concurrentConnections != other.concurrentConnections) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.vsName != null ? this.vsName.hashCode() : 0);
        hash = 89 * hash + (int) (this.bytesIn ^ (this.bytesIn >>> 32));
        hash = 89 * hash + (int) (this.bytesOut ^ (this.bytesOut >>> 32));
        hash = 89 * hash + (int) (this.concurrentConnections ^ (this.concurrentConnections >>> 32));
        return hash;
    }
}
