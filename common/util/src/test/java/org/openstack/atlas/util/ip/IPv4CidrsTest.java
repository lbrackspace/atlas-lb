package org.openstack.atlas.util.ip;

import java.util.HashSet;
import java.util.Set;
import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv4CidrsTest {

    private String[] expectedAddresses;
    private String[] unexpectedAddresses;
    private IPv4Cidrs ipv4Cidrs;

    public IPv4CidrsTest() {
    }

    @Before
    public void setUp() throws IPStringConversionException {
        ipv4Cidrs = new IPv4Cidrs();
        ipv4Cidrs.getCidrs().add(new IPv4Cidr("192.168.0.0/16"));
        ipv4Cidrs.getCidrs().add(new IPv4Cidr("10.0.0.0/8"));
        ipv4Cidrs.getCidrs().add(new IPv4Cidr("172.16.0.0/12"));
        expectedAddresses = new String[]{"10.0.0.0", "192.168.3.51",
                    "172.16.0.1", "172.17.1.1", "192.168.1.254", "10.1.2.3"};
        unexpectedAddresses = new String[]{"1.2.3.4", "12.13.14.15", "172.15.23.42"};
    }

    @Test
    public void shouldBeContainedIn() {
        Set<String> cidrSet;
        List<String> cidrList = ipv4Cidrs.getCidrsContainingIp("192.168.3.52");
        cidrSet = new HashSet();
        cidrSet.addAll(cidrList);
        assertTrue(cidrSet.contains("192.168.0.0/16"));
        assertFalse(cidrSet.contains("10.0.0.0/8"));
        assertFalse(cidrSet.contains("172.16.0.0/12"));

    }

    @Test
    public void shouldContainAddress() throws IPException {
        for (String ip : expectedAddresses) {
            assertTrue(ipv4Cidrs.contains(ip));
        }
    }

    @Test
    public void shouldNotContainAddress() throws IPException{
        for(String ip : unexpectedAddresses) {
            assertFalse(ipv4Cidrs.contains(ip));
        }
    }

}
