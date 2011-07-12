package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4Cidr {

    private static final Pattern subnetPattern = Pattern.compile("^(.*)/(.*)$");
    private String cidr;
    private String ip;
    private int subnet;
    private byte[] ipBytes;
    private byte[] maskBytes;

    public IPv4Cidr() {
    }

    public IPv4Cidr(String cidr) throws IPStringConversionException  {
        this.setCidr(cidr);
    }

    public void setCidr(String in) throws IPStringConversionException {
        String msg;
        String ip;
        String subnet;
        Matcher ipMatcher;
        if (in == null) {
            msg = String.format("INVALID SUBNET %s", in);
            throw new IPStringConversionException(msg);
        }

        ipMatcher = subnetPattern.matcher(in);
        int subnetint;
        if (ipMatcher.find()) {
            ip = ipMatcher.group(1);
            subnet = ipMatcher.group(2);
            try {
                subnetint = Integer.parseInt(subnet);
                if (subnetint < 0 || subnetint > 32 || !IPUtils.isValidIpv4String(ip)) {
                    msg = String.format("Subnet %i not in [0,32]", subnetint);
                    throw new IPStringConversionException(msg);
                }
                this.subnet = subnetint;
            } catch (NumberFormatException e) {
                msg = String.format("Error converting %s to integer", subnet);
                throw new IPStringConversionException(msg);
            }
            this.cidr = in;
            ipBytes = new IPv4(ip).getBytes();
            maskBytes = IPUtils.rollMask(subnetint,4);
        } else {
            msg = String.format("INVALID SUBNET: %s", in);
            throw new IPStringConversionException(msg);
        }
    }

    public boolean contains(String ip) throws IPStringConversionException, IpTypeMissMatchException {
        boolean out;
        if(!IPUtils.isValidIpv4String(ip)) {
            return false;
        }
        byte[] theirBytes = new IPv4(ip).getBytes();
        byte[] mySubnet = IPUtils.opBytes(ipBytes, maskBytes, ByteStreamOperation.AND);
        byte[] theirSubnet = IPUtils.opBytes(theirBytes, maskBytes, ByteStreamOperation.AND);
        out = IPUtils.bytesEqual(theirSubnet, mySubnet);
        return out;
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
