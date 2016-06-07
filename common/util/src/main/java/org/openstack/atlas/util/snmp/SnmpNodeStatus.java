package org.openstack.atlas.util.snmp;

import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;

public class SnmpNodeStatus {

    public static final int ALIVE = 1;
    public static final int DEAD = 2;
    public static final int UNKNOWN = 3;
    private int ipType = -1;
    private int port = -1;
    private int status = -1;
    private String clientKey = null;
    private String hostName = null;
    private String ipAddress = null;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ipType=");
        switch (ipType) {
            case IPUtils.HOST_NAME:
                sb.append("HOST_NAME");
                break;
            case IPUtils.IPv4:
                sb.append("IPv4");
                break;
            case IPUtils.IPv6:
                sb.append("IPv6");
                break;
            default:
                sb.append("");
                break;
        }
        sb.append(", status=");
        switch (status) {
            case SnmpNodeStatus.ALIVE:
                sb.append("alive");
                break;
            case SnmpNodeStatus.DEAD:
                sb.append("dead");
                break;
            case SnmpNodeStatus.UNKNOWN:
                sb.append("unknown");
                break;
            default:
                sb.append("");
                break;
        }
        sb.append(", ipAddress=").append(ipAddress).
                append(", host=").append(hostName).
                append(", port=").append(port).
                append(", clientKey=").append(clientKey).
                append(" }");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SnmpNodeStatus other = (SnmpNodeStatus) obj;
        if (this.ipType != other.ipType) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if ((this.hostName == null) ? (other.hostName != null) : !this.hostName.equals(other.hostName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.ipType;
        hash = 53 * hash + (this.ipAddress != null ? this.ipAddress.hashCode() : 0);
        hash = 53 * hash + this.port;
        hash = 53 * hash + (this.clientKey != null ? this.clientKey.hashCode() : 0);
        hash = 53 * hash + (this.hostName != null ? this.hostName.hashCode() : 0);
        hash = 53 * hash + this.status;
        return hash;
    }

    public SnmpNodeStatus(String ipAddress, int port, String clientKey, String hostName, int status) throws IPStringConversionException {
        this.ipAddress = IPUtils.canonicalIp(ipAddress);
        this.port = port;
        this.clientKey = clientKey;
        this.hostName = hostName;
        this.status = status;
    }

    public SnmpNodeStatus() {
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

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIpType() {
        return ipType;
    }

    public void setIpType(int ipType) {
        this.ipType = ipType;
    }
}
