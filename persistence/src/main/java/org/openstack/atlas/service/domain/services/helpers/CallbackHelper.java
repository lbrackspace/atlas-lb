package org.openstack.atlas.service.domain.services.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;

import java.net.Inet6Address;
import java.net.URI;
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
        parseAddressAndPort(paramLine);
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

    private void parseAddressAndPort(String paramLine) throws Exception {
        try {
            URI domainAddress = new URI(parseAddressObject(paramLine));
            this.ipAddress = domainAddress.getHost();
            this.port = domainAddress.getPort();
            return;
        }
        catch (Exception e) {
            LOG.warn("Unable to parse address and port using domain parsing rules.");
        }
        try {
            this.ipAddress = parseIpV4Address(paramLine);
            this.port = parseIpV4Port(paramLine);
            return;
        }
        catch (Exception e) {
            LOG.warn("Unable to parse address and port using IpV4 parsing rules.");
        }
        try {
            this.ipAddress = parseIpV6Address(paramLine);
            this.port = parseIpV6Port(paramLine);
            return;
        }
        catch (Exception e) {
            LOG.warn("Unable to parse address and port using IpV6 parsing rules.");
        }
        String message = "Unable to find an address and port in the param line.";
        LOG.warn(message);
        throw new BadRequestException(message);
    }

    private String parseIpV4Address(String paramLine) throws Exception {
        String ipV4Address = parseAddressObject(paramLine).split(":")[0];
        if (!verifyIpV4Address(ipV4Address)) {
            LOG.warn("Unable to verify IpV6 address found in the param line.");
            throw new Exception();
        }
        return ipV4Address;
    }

    private int parseIpV4Port(String paramLine) throws Exception {
        String port = parseAddressObject(paramLine).split(":")[1];
        return Integer.parseInt(port);
    }

    private String parseIpV6Address(String paramLine) throws Exception {
        String ipV6Address = paramLine.split("'\\[")[1].split("]:")[0];
        if (!verifyIPv6Address(ipV6Address)) {
            LOG.warn("Unable to verify IpV6 address found in the param line.");
            throw new Exception();
        }
        return ipV6Address;
    }

    private int parseIpV6Port(String paramLine) throws Exception {
        String ipv6obj = parseAddressObject(paramLine).split("'\\[")[0].split(" ")[0];
        return Integer.parseInt(ipv6obj.split("]:")[1]);
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
