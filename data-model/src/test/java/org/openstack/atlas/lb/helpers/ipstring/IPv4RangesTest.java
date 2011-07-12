package org.openstack.atlas.lb.helpers.ipstring;

import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPCidrBlockOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv4RangesTest {

    private IPv4Ranges ranges = new IPv4Ranges();
    private IPv4Ranges privateranges = new IPv4Ranges();

    public IPv4RangesTest() {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = IPBlocksOverLapException.class)
    public void shouldThrowOverLapExceptionIfOverlapping() throws IPBlocksOverLapException, IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        ranges = new IPv4Ranges();
        ranges.add("127.0.0.0/24");
        ranges.add("127.0.0.64/23"); // Kaboom
    }

    @Test
    public void shouldAddIpBlocksIfnotOverlapping() throws IPBlocksOverLapException, IPStringConversionException, IPOctetOutOfRangeException, IPCidrBlockOutOfRangeException {
        ranges = new IPv4Ranges();
        ranges.add("127.0.0.1/24");
        ranges.add("127.0.1.0/24");
        ranges.add("127.0.2.0/24");
        assertEquals(3, ranges.getRanges().size());
    }

    @Test
    public void shouldExcept192Ranges() throws IPStringException {
        ranges = getPrivateRanges();
        assertTrue(ranges.contains("192.168.0.12"));
        assertTrue(ranges.contains("192.168.1.1"));
        assertTrue(ranges.contains("192.168.2.32"));
        assertTrue(ranges.contains("192.168.3.44"));
        assertTrue(ranges.contains("192.168.4.41"));
    }

    @Test
    public void shouldExcept172Ranges() throws IPStringException {
        ranges = getPrivateRanges();
        assertTrue(ranges.contains("172.16.0.1"));
        assertTrue(ranges.contains("172.17.0.1"));
        assertTrue(ranges.contains("172.31.255.255"));
    }


    @Test
    public void shouldExcept10Ranges() throws IPStringException {
        ranges = getPrivateRanges();
        assertTrue(ranges.contains("10.0.0.1"));
        assertTrue(ranges.contains("10.250.0.2"));
        assertTrue(ranges.contains("10.33.0.3"));
    }

    private IPv4Ranges getPrivateRanges() throws IPStringException {
        IPv4Ranges out = new IPv4Ranges();
        out.add("10.0.0.0/8");
        out.add("172.16.0.0/12");
        out.add("192.168.0.0/16");
        return out;
    }
}
