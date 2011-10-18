package org.openstack.atlas.adapter.zxtm.helper;


import org.openstack.atlas.common.ip.IPUtils;

public class IpHelper {

    public static String createZeusIpString(String ipAddress, Integer port) {
        if (IPUtils.isValidIpv4String(ipAddress)) return String.format("%s:%d", ipAddress, port);
        if (IPUtils.isValidIpv6String(ipAddress)) return String.format("[%s]:%d", ipAddress, port);
        throw new RuntimeException("Cannot create string for ip address and port.");
    }
}
