package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv6CidrTest {

    public IPv6CidrTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void shouldMatchCidrs() throws IPStringConversionException, IpTypeMissMatchException{
        IPv6Cidr cidr1 = new IPv6Cidr("cccc:cccc:cccc:cccc::192.168.3.51/64");
        IPv6Cidr cidr2 = new IPv6Cidr("cccc:cccc:cccc:cccc:aaaa:aaaa:aaaa:aaaa/64");
        assertTrue(cidr1.matches(cidr2));
    }

    @Test
    public void shouldnotMatch() throws IpTypeMissMatchException, IPStringConversionException {
        IPv6Cidr cidr1 = new IPv6Cidr("cccc:cccc:cccc:cccc::192.168.3.51/64");
        IPv6Cidr cidr2 = new IPv6Cidr("cccc:ffff:cccc:cccc:aaaa:aaaa:aaaa:aaaa/64");
        assertFalse(cidr1.matches(cidr2));
    }

    @Test
    public void shouldContainIps48through63() throws Exception {
        int i;
        String ipStr;
        IPv6Cidr cidr = new IPv6Cidr("ffff::192.168.3.48/124");
        assertFalse(cidr.contains("ffff::192.168.3.47"));
        for (i = 48; i <= 63; i++) {
            ipStr = String.format("ffff::192.168.3.%d", i);
        }
        assertFalse(cidr.contains("ffff::192.168.3.64"));
    }
}
