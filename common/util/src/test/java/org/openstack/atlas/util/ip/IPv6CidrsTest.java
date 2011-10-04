package org.openstack.atlas.util.ip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv6CidrsTest {

    public IPv6CidrsTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void ShouldMapAddresses() throws IPException {
        IPv6Cidrs ipv6Cidrs = new IPv6Cidrs();
        ipv6Cidrs.getCidrs().add(new IPv6Cidr("2001:07FA:0001::/48"));
        assertTrue(ipv6Cidrs.contains("2001:07fa:0001:0000:0000:0000:0000:0000"));
        assertTrue(ipv6Cidrs.contains("2001:07fa:0001:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(ipv6Cidrs.contains("2001:07fa:0002:0000:0000:0000:0000:0000")); // Except for this one

    }

    @Test
    public void shouldBeContainedIn() throws IPStringConversionException {
        IPv6Cidrs ipv6Cidrs = new IPv6Cidrs();
        ipv6Cidrs.getCidrs().add(new IPv6Cidr("2001:aaaa::/32"));
        ipv6Cidrs.getCidrs().add(new IPv6Cidr("2001:aaaa:aaaa::/48"));
        ipv6Cidrs.getCidrs().add(new IPv6Cidr("2001:aaaa:bbbb::/48"));

        List<String> cidrList;
        Set<String> cidrSet;

        cidrList = ipv6Cidrs.getCidrsContainingIp("2001:aaaa:aaaa:aaaa::");
        cidrSet = new HashSet();
        cidrSet.addAll(cidrList);
        assertTrue(cidrSet.contains("2001:aaaa::/32"));
        assertTrue(cidrSet.contains("2001:aaaa:aaaa::/48"));
        assertFalse(cidrSet.contains("2001:aaaa:bbbb::/48"));

        cidrList = ipv6Cidrs.getCidrsContainingIp("2001:aaaa:dddd:dddd::");
        cidrSet = new HashSet();
        cidrSet.addAll(cidrList);
        assertTrue(cidrSet.contains("2001:aaaa::/32"));
        assertFalse(cidrSet.contains("2001:aaaa:aaaa::/48"));
        assertFalse(cidrSet.contains("2001:aaaa:bbbb::/48"));

               cidrList = ipv6Cidrs.getCidrsContainingIp("2001:aaaa:bbbb:eeee::");
        cidrSet = new HashSet();
        cidrSet.addAll(cidrList);
        assertTrue(cidrSet.contains("2001:aaaa::/32"));
        assertFalse(cidrSet.contains("2001:aaaa:aaaa::/48"));
        assertTrue(cidrSet.contains("2001:aaaa:bbbb::/48"));

    }
}
