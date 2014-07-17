package org.openstack.atlas.service.domain.services.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;

import java.net.Inet6Address;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Arrays;
import java.net.Inet4Address;

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
            String message = String.format("Error converting string to integer for account id: '%s'", accountId);
            LOG.warn(message);
            throw new BadRequestException(message);
        }
    }

    public Integer parseLoadbalancerId(String paramLine) throws Exception {
        String loadbalancerId = parseAcctLbid(paramLine).split("_")[1];

        try {
            return Integer.parseInt(loadbalancerId);
        } catch (NumberFormatException e) {
            String message = String.format("Error converting string to integer for load balancer id: '%s'", accountId);
            LOG.warn(message);
            throw new BadRequestException(message);
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

    private String parseIpAddress(String paramLine) throws Exception {
        String address = null;
        // to to grab address using domain parsing
        try {
            URI possibleURI = new URI(parseAddressObject(paramLine));
            address = possibleURI.getHost();
            return address;
        } catch (URISyntaxException e) {
            LOG.warn("Unable to parse domain, attempting IpV6 and IpV4 parsing.");
        }
        // try to grab address using IpV6 and then IpV4 parsing
        try {
            address = parseIpv6Address(paramLine);
            // verify that the found address is actually an IpV6 address
            if (!verifyIPv6Address(address)) {
                String message = "Unable to validate IpV6 address in the param line.";
                LOG.warn(message);
                throw new BadRequestException(message);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.warn("Unable to parse IpV6 address, attempting IpV4 parsing.");
            address = parseIpv4Address(paramLine);
            // verify that the found address is actually an IpV4 address
            if (!verifyIpV4Address(address)) {
                String message = "Unable to a valid Ip address in the param line.";
                LOG.warn(message);
                throw new BadRequestException(message);
            }
        }
        return address;
    }

    private int parsePort(String paramLine) throws BadRequestException {
        String port = null;
        // try to grab port using domain parsing
        try {
            URI possibleURI = new URI(parseAddressObject(paramLine));
            return possibleURI.getPort();
        } catch (URISyntaxException e) {
            LOG.warn("Unable to parse domain port, attempting IpV6 and IpV4 parsing.");
        }
        // try to grab port using IpV6 parsing
        try {
            port = parseIpV6Port(paramLine);
            return Integer.parseInt(port);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.warn(String.format("Unable to parse IpV6 port, attempting IpV4 parsing."));
        } catch (NumberFormatException e) {
            LOG.warn(String.format("Error converting string to integer for IpV6 port: '%s'", port));
        }
        // try to grab port using IpV4 parsing
        try {
            port = parseIpV4Port(paramLine);
            return Integer.parseInt(port);
        } catch (ArrayIndexOutOfBoundsException e) {
            // no way left to find port, throw bad request exception
            String message = String.format("Unable to parse IpV4 port. Unable to find a valid port in the param line.", port);
            LOG.warn(message);
            throw new BadRequestException(message);
        } catch (NumberFormatException e) {
            // no way left to find port, throw bad request exception
            String message = String.format("Error converting string to integer for IpV4 port: '%s'. Unable to find a valid port in the param line.", port);
            LOG.warn(message);
            throw new BadRequestException(message);
        }
    }

    private boolean verifyIpV4Address(String address) {
        try {
            Inet4Address.getByName(address);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    private static boolean verifyIPv6Address(String address) {
        try {
            Inet6Address.getByName(address);
            return true;
        } catch(Exception e) {
            return false;
        }
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

    private String parseIpv6AddressObject(String paramLine) {
        String ipv6obj = parseAddressObject(paramLine).split("'\\[")[0];
        return ipv6obj.split(" ")[0];
    }

    private String parseIpv4Address(String paramLine) {
        return parseAddressObject(paramLine).split(":")[0];
    }

    private String parseIpV4Port(String paramLine) {
        return parseAddressObject(paramLine).split(":")[1];
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
}
