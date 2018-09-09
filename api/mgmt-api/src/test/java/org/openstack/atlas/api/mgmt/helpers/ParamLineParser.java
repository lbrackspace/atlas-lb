package org.openstack.atlas.api.mgmt.helpers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class ParamLineParser {
    private String ipv4Paramline = "INFO pools/501148_11066 nodes/10.179.78.70:80 nodeworking Node 10.179.78.70 is working again";
    private String ipv6Paramline = "INFO pools/501148_11066 nodes/[fe80::4240:adff:fe5c:c9ee]:90 nodeworking Node fe80::4240:adff:fe5c:c9ee is working again";


    public ParamLineParser() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testParamLineParsing() {
        String test4addy = getIpAddress(ipv4Paramline);
        String test6addy = getIpAddress(ipv6Paramline);
        try {
            Integer test4port = getIpPort(ipv4Paramline);
            Integer test6port = getIpPort(ipv6Paramline);
            assertTrue("IPV4 addy does not match", test4addy.equals("10.179.78.70"));
            assertTrue("IPV6 addy does not match", test6addy.equals("fe80::4240:adff:fe5c:c9ee"));
            assertTrue("IPV4 port does not match", test4port.equals(80));
            assertTrue("IPV6 port does not match", test6port.equals(90));
        } catch (Exception e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

     public String getIpAddress(String paramLine) {
//        String nodesObject = paramLine.split(" ")[2];
//        String ipAddressWithPort = nodesObject.split("/")[1];
//        return ipAddressWithPort.split(":")[0].replace("[", "");
        String nodeLine = paramLine.split("Node ")[1];
        return nodeLine.split(" ")[0];
    }

    public Integer getIpPort(String paramLine) throws Exception {
        String nodesObject = paramLine.split(" ")[2];
        String ipAddressWithPort = nodesObject.split("/")[1];
        String port = ipAddressWithPort.split(":")[1];

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing for port, trying ipv6");
        }
        return getIpPortForIpv6(paramLine);
    }

    public Integer getIpPortForIpv6(String paramLine) throws Exception {
        String nodesObject = paramLine.split(" ")[2];
        String ipAddressWithPort = nodesObject.split("/")[1];
        String port = ipAddressWithPort.split("]:")[1];

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            org.junit.Assert.fail(e.getMessage());
        }
        return null;
    }
}
