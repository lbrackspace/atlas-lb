package org.openstack.atlas.common.ip;

import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.common.ip.exception.IpTypeMissMatchException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv6Cidr {

    private static final Pattern subnetPattern = Pattern.compile("^(.*)/(.*)$");
    private String cidr;
    private String ip;
    private int subnet;
    private byte[] ipBytes;
    private byte[] maskBytes;

    public IPv6Cidr() {
    }

    public IPv6Cidr(String cidr) throws IPStringConversionException1 {
        this.setCidr(cidr);
    }

    public void setCidr(String in) throws IPStringConversionException1 {
        String msg;
        String ipStr;
        String subnetStr;
        Matcher ipMatcher;
        if (in == null) {
            throw new IPStringConversionException1("INVALID SUBNET, CIDR was NULL");
        }

        ipMatcher = subnetPattern.matcher(in);
        int subnetint;
        if (ipMatcher.find()) {
            ipStr = ipMatcher.group(1);
            subnetStr = ipMatcher.group(2);
            try {
                subnetint = Integer.parseInt(subnetStr);
                if (subnetint < 0 || subnetint > 128 || !IPUtils.isValidIpv6String(ipStr)) {
                    msg = String.format("Subnet %d not in [0,128]", subnetint);
                    throw new IPStringConversionException1(msg);
                }
                this.subnet = subnetint;
            } catch (NumberFormatException e) {
                msg = String.format("Error converting %s to integer", subnetStr);
                throw new IPStringConversionException1(msg);
            }
            this.cidr = in;
            ipBytes = new IPv6(ipStr).getBytes();
            maskBytes = IPUtils.rollMask(subnetint, 16);
        } else {
            msg = String.format("INVALID SUBNET: %s", in);
            throw new IPStringConversionException1(msg);
        }
    }

    public boolean contains(String ip) throws IPStringConversionException1, IpTypeMissMatchException {
        boolean out;
        if(!IPUtils.isValidIpv6String(ip)) {
            return false;
        }
        byte[] theirBytes = new IPv6(ip).getBytes();
        byte[] mySubnet = IPUtils.opBytes(ipBytes, maskBytes, ByteStreamOperation.AND);
        byte[] theirSubnet = IPUtils.opBytes(theirBytes, maskBytes, ByteStreamOperation.AND);
        out = IPUtils.bytesEqual(theirSubnet, mySubnet);
        return out;
    }

    public boolean matches(IPv6Cidr oCidr) throws IpTypeMissMatchException, IPStringConversionException1 {
        if (oCidr == null) {
            return false;
        }
        if (oCidr.getSubnet() != this.subnet) {
            return false;
        }
        byte[] mySubnet = IPUtils.opBytes(ipBytes, maskBytes, ByteStreamOperation.AND);
        byte[] theirSubnet = IPUtils.opBytes(oCidr.getIpBytes(), maskBytes, ByteStreamOperation.AND);

        for (int i = 0; i < mySubnet.length; i++) {
            if (mySubnet[i] != theirSubnet[i]) {
                return false;
            }
        }
        return true;
    }

    public String getCidr() {
        return cidr;
    }

    public String getIp() {
        return ip;
    }

    public int getSubnet() {
        return subnet;
    }

    public byte[] getIpBytes() {
        return ipBytes;
    }

    public byte[] getMaskBytes() {
        return maskBytes;
    }
}
