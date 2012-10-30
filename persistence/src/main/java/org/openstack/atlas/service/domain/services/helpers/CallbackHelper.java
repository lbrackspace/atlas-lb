package org.openstack.atlas.service.domain.services.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CallbackHelper {
    private static final Log LOG = LogFactory.getLog(CallbackHelper.class);

    public Integer accountId;
    public Integer loadBalancerId;
    public String detailedMessage;
    public String ipAddress;
    public int port;

    public static final String NODE_FAIL_TAG = "nodefail"; // Taken from zxtm api
    public static final String NODE_WORKING_TAG = "nodeworking"; // Taken from zxtm api
    public static final String MONITOR_FAIL_TAG = "monitorfail"; // Taken from zxtm api
    public static final String MONITOR_WORKING_TAG = "monitorok"; // Taken from zxtm api

    public CallbackHelper(String paramLine) throws Exception {
        this.detailedMessage = parseDetailedMessage(paramLine);
        this.loadBalancerId = parseLoadbalancerId(paramLine);
        this.accountId = parseAccountId(paramLine);
        this.ipAddress = parseIpAddress(paramLine);
        this.port = parsePort(paramLine);
    }

    public Integer parseAccountId(String paramLine) throws Exception {
        String accountId = parseAcctLbid(paramLine).split("_")[0];

        try {
            return Integer.parseInt(accountId);
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Error converting string to integer for account id: '%s'", accountId));
            throw new Exception(e);
        }
    }

    public Integer parseLoadbalancerId(String paramLine) throws Exception {
        String loadbalancerId = parseAcctLbid(paramLine).split("_")[1];

        try {
            return Integer.parseInt(loadbalancerId);
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Error converting string to integer for load balancer id: '%s'", loadbalancerId));
            throw new Exception(e);
        }
    }

    private String parseDetailedMessage(String paramLine) {
        try {
            return paramLine.split("': ")[1];
        } catch (ArrayIndexOutOfBoundsException ae) {
            LOG.warn("No detailed message found, everything must be ok.");
        }
        return "";
    }

    private String parseIpAddress(String paramLine) {
      String address = null;
        try {
            address = parseIpv6Address(paramLine);
        } catch (ArrayIndexOutOfBoundsException e) {
            //silent
            address = parseIpv4Address(paramLine);
        }
        return address;
    }

    private int parsePort(String paramLine) {
        String port = null;
        try {
            port = parseIpV6Port(paramLine);
        } catch (ArrayIndexOutOfBoundsException e) {
            //silent
//            LOG.warn(String.format("Error converting string to integer for ipv6 port:"));
            port = parseIpV4Port(paramLine);
        }

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Error converting string to integer for ipv4 port: '%s'", port));
        }
        return Integer.parseInt(port);
    }

    private String parseAcctLbid(String paramLine) throws Exception {
        String monObj = paramLine.split(" ")[1];
        String acctLbId = monObj.split("/")[1];
        return acctLbId;
    }

    private String parseAddressObject(String paramLine) {
        String initObj = paramLine.split("'")[1];
        return initObj.split("'")[0];
    }


    private String parseIpv4Address(String paramLine) {
        return parseAddressObject(paramLine).split(":")[0];
    }

    private String parseIpV4Port(String paramLine) {
        return parseAddressObject(paramLine).split(":")[1];
    }

    private String parseIpv6AddressObject(String paramLine) {
        String ipv6obj = parseAddressObject(paramLine).split("'\\[")[0];
        return ipv6obj.split(" ")[0];
    }

    private String parseIpv6Address(String paramLine) {
        String ipv6obj = paramLine.split("'\\[")[1];
        return ipv6obj.split("]:")[0];
    }

    private String parseIpV6Port(String paramLine) {
        String ipv6obj = parseIpv6AddressObject(paramLine).split("'\\[")[0];
        return ipv6obj.split("]:")[1];
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
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
    //    public static String getIpAddress(String paramLine) {
////        String nodesObject = paramLine.split(" ")[2];
////        String ipAddressWithPort = nodesObject.split("/")[1];
////        return ipAddressWithPort.split(":")[0].replace("[", "");
//        String nodeLine = paramLine.split("Node ")[1];
//        return nodeLine.split(" ")[0];
//    }

//    public static Integer getIpPort(String paramLine) throws Exception {
//        String object = paramLine.split("'")[1];
//        String ipAddressWithPort = object.split("'")[0];
//        String port = ipAddressWithPort.split(":")[1];
//
//        try {
//            return Integer.parseInt(port);
//        } catch (NumberFormatException e) {
//            LOG.info("Error parsing paramline for ipv4, trying for ipv6");
//        }
//        return getIpPortForIpv6(paramLine);
//    }
//
//    public static Integer getIpPortForIpv6(String paramLine) throws Exception {
//        String object = paramLine.split("'")[1];
//        String ipAddressWithPort = object.split(" ")[0];
//        String port = ipAddressWithPort.split("]:")[1];
//
//        try {
//            return Integer.parseInt(port);
//        } catch (NumberFormatException e) {
//            LOG.warn(String.format("Error converting string to integer for port: '%s'", port));
//            throw new Exception(e);
//        }
//    }
    //    public static Integer getIpPort(String paramLine) throws Exception {
//        String nodesObject = paramLine.split(" ")[2];
//        String ipAddressWithPort = nodesObject.split("/")[1];
//        String port = ipAddressWithPort.split(":")[1];
//
//        try {
//            return Integer.parseInt(port);
//        } catch (NumberFormatException e) {
//            LOG.info("Error parsing paramline for ipv4, trying for ipv6");
//        }
//        return getIpPortForIpv6(paramLine);
//    }

//    public static Integer getIpPortForIpv6(String paramLine) throws Exception {
//        String nodesObject = paramLine.split(" ")[2];
//        String ipAddressWithPort = nodesObject.split("/")[1];
//        String port = ipAddressWithPort.split("]:")[1];
//
//        try {
//            return Integer.parseInt(port);
//        } catch (NumberFormatException e) {
//            LOG.warn(String.format("Error converting string to integer for port: '%s'", port));
//            throw new Exception(e);
//        }
//    }
}
