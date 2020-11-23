package org.openstack.atlas.adapter.helpers;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.ip.IPUtils;

public class IpHelper {

    public static String createZeusIpString(String ipAddress, Integer port) {
        String errMsg;
        String fmt = "(address=\"%s\", port=%s)";
        String ipOut = (ipAddress == null) ? "null" : ipAddress;
        String portOut = (port == null) ? "null" : port.toString();
        String ipMsg = String.format(fmt, ipOut, portOut);
        if (ipAddress == null || port == null) {
            errMsg = String.format("ipAddress or port cann not be null. %s", ipMsg);
            throw new RuntimeException(errMsg);
        }
        if (IPUtils.isValidIpv4String(ipAddress)) {
            return String.format("%s:%d", ipAddress, port);
        }
        if (IPUtils.isValidIpv6String(ipAddress)) {
            return String.format("[%s]:%d", ipAddress, port);
        }
        if (ipAddress.matches(".*[a-zA-Z]+.*")) {
            return String.format("%s:%d", ipAddress, port);
        }
        errMsg = String.format("Cannot create string for ip address and port. %s", ipMsg);
        throw new RuntimeException(errMsg);
    }

    public static boolean isNetworkConnectionException(Exception ex) {
        return Debug.isThrowableCausedByOrAssignableFrom(ex, SocketException.class, SocketTimeoutException.class);
    }
}
