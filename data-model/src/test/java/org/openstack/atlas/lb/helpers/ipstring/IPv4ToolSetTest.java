package org.openstack.atlas.lb.helpers.ipstring;

import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPCidrBlockOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;

import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv4ToolSetTest {

    public IPv4ToolSetTest() {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldConvertIpString2Long() throws IPStringConversionException, IPOctetOutOfRangeException {
        assertEquals(3232235520L, IPv4ToolSet.ip2long("192.168.0.0"));
    }

    @Test
    public void shouldConvertLong2IpString() {
        assertTrue("172.16.0.0".equals(IPv4ToolSet.long2ip(2886729728L)));
    }

    @Test(expected = IPCidrBlockOutOfRangeException.class)
    public void shouldThrowCidrBlockOutOfRangeWhenLessThenSlashneg1() throws IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        IPv4ToolSet.ipv4BlockToRange("123.234.122.111/-1");
    }

    @Test(expected = IPCidrBlockOutOfRangeException.class)
    public void shouldThrowCidrBlockOutofRangeWhenMoreThenSlash32() throws IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        IPv4ToolSet.ipv4BlockToRange("123.111.222.111/33");
    }

    @Test(expected = IPOctetOutOfRangeException.class)
    public void shouldThrowAnOctetOutOfRangeWhenOctetOutOfRange() throws IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        IPv4ToolSet.ipv4BlockToRange("123.234.122.311/32");
    }

    @Test
    public void shouldGet768IpsWhenConverting3ValidClassCNets() throws IPBlocksOverLapException, IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        List<String> blocks = new ArrayList<String>();
        List<String> ipAddresses = new ArrayList<String>();
        blocks.add("127.0.0.0/24");
        blocks.add("127.0.1.0/24");
        blocks.add("127.0.2.0/24");
        ipAddresses = IPv4ToolSet.ipv4BlocksToIpStrings(blocks);
        assertEquals(768, ipAddresses.size());
    }

    @Test
    public void shouldValidate() {
        assertTrue(IPv4ToolSet.isValid("192.168.3.51"));
        assertTrue(IPv4ToolSet.isValid("127.0.0.1"));
        assertTrue(IPv4ToolSet.isValid("10.0.0.1"));
        assertTrue(IPv4ToolSet.isValid("172.17.10.255"));
    }

    @Test
    public void shouldReject() {
        assertFalse(IPv4ToolSet.isValid("www.google.com"));
        assertFalse(IPv4ToolSet.isValid(null));
        assertFalse(IPv4ToolSet.isValid("888.888.888.888"));
        assertFalse(IPv4ToolSet.isValid(""));
        assertFalse(IPv4ToolSet.isValid("...222"));
        assertFalse(IPv4ToolSet.isValid("-1.-1.-1.-1"));
    }
}
