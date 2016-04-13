package org.openstack.atlas.util.snmp;

import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;

public class SnmpNodeKey {

    private String ipAddress;
    private int port;
    private int ipType;

    public SnmpNodeKey(String ipAddress, int port, int ipType) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.ipType = ipType;
    }

    public SnmpNodeKey() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SnmpNodeKey other = (SnmpNodeKey) obj;
        if ((this.ipAddress == null) ? (other.ipAddress != null) : !this.ipAddress.equals(other.ipAddress)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (this.ipType != other.ipType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.ipAddress != null ? this.ipAddress.hashCode() : 0);
        hash = 59 * hash + this.port;
        hash = 59 * hash + this.ipType;
        return hash;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getIpType() {
        return ipType;
    }

    public void setIpType(int ipType) {
        this.ipType = ipType;
    }

    // Don't allow either to be null duing equality
    public static boolean equals(SnmpNodeKey a, SnmpNodeKey b) {
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public String toString() {
        return "{ ipAddress=" + ipAddress + ", port=" + port + ", ipType=" + ipType + "}";
    }
}
