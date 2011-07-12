package org.openstack.atlas.api.validation.util.IPString;

import org.junit.Test;
import static org.junit.Assert.*;

public class IPUtilsTest {

    public IPUtilsTest() {
    }

    @Test
    public void testShouldAcceptZeroCompressedIpv4MixIn() {
        assertTrue("Expected acceptence of \"::192.168.3.51\"", IPUtils.isValidIpv6String("::192.168.3.51"));
    }

    @Test
    public void testShouldAcceptRightCompressedIpv4MixIn() {
        assertTrue("Expected acceptence of \"efff::192.168.3.51\"", IPUtils.isValidIpv6String("efff::192.168.3.51"));
    }

    @Test
    public void testShouldAcceptLeftCompressedIpv4MixIn() {
        assertTrue("Expected acceptence of \"efff::1234:192.168.3.51\"", IPUtils.isValidIpv6String("efff::1234:192.168.3.51"));
    }

    @Test
    public void testShouldRejectTripleColonIPv4Mixin() {
        assertFalse("Expected rejection of \"ffff:::192.168.3.51\"", IPUtils.isValidIpv6String("ffff:::192.168.3.51"));
    }

    @Test
    public void testShouldRejectGoogleIPv6() {
        assertFalse("expected rejection of \"www.google.com\"", IPUtils.isValidIpv6String("www.google.com"));
    }

    @Test
    public void testShouldAcceptValidIPv61() {
        assertTrue("expected acceptance of \"1234:5678::\"", IPUtils.isValidIpv6String("1234:5678::"));
    }

    @Test
    public void testShouldRejectOutofRangeHexIpv6() {
        assertFalse("expected rejefction of \"fffg::1234\"", IPUtils.isValidIpv6String("fffg::1234\""));
    }

    @Test
    public void testShouldRejectToManyHexDigitsIpv6() {
        assertFalse("expected rejection of \"12345::\"", IPUtils.isValidIpv6String("12345::"));
    }

    @Test
    public void testShouldAcceptAllZeroCompressionIpv6() {
        assertTrue("Expected acceptance of \"::\"", IPUtils.isValidIpv6String("::"));
    }

    @Test
    public void testShouldAcceptLeftZeroCompressionIpv6() {
        assertTrue("Expected acceptance of \"::1234\"", IPUtils.isValidIpv6String("::1234"));
    }

    @Test
    public void testShouldAcceptRightZeroCompressionIpv6() {
        assertTrue("Expected acceptance pf \"1234::\"", IPUtils.isValidIpv6String("1234::"));
    }

    @Test
    public void testShouldRejectToManyOctetsIpv4() {
        assertFalse("expected rejection of 1.2.3.4.5 ", IPUtils.isValidIpv4String("1.2.3.4.5"));
    }

    @Test
    public void testShouldRejectNotEnoughOctetsIpv4() {
        assertFalse("expected rejection of 1.2.3", IPUtils.isValidIpv4String("1.2.3"));
    }

    @Test
    public void testShouldRejecteOctetOutofRangeIpv4() {
        assertFalse("expceted rejection of 1.2.3.256", IPUtils.isValidIpv4String("1.2.3.256"));
    }

    @Test
    public void testShouldRejectNegativeOctetIpv4LOL() {
        assertFalse("Expected rejection of \"1.2.-1.4\"", IPUtils.isValidIpv4String("1.2.-1.4"));
    }

    @Test
    public void testubyte2int() {
        assertEquals(IPUtils.ubyte2int((byte) -1), 255);
        assertEquals(IPUtils.ubyte2int((byte) 0), 0);
        assertEquals(IPUtils.ubyte2int((byte) 127), 127);
        assertEquals(IPUtils.ubyte2int((byte) -127), 129);
        assertEquals(IPUtils.ubyte2int((byte) -128), 128);
        assertEquals(IPUtils.ubyte2int((byte) -126), 130);
    }

    @Test
    public void testint2ubyte() {
        assertEquals(IPUtils.int2ubyte(255), (byte) -1);
        assertEquals(IPUtils.int2ubyte(0), (byte) 0);
        assertEquals(IPUtils.int2ubyte(127), (byte) 127);
        assertEquals(IPUtils.int2ubyte(129), (byte) -127);
        assertEquals(IPUtils.int2ubyte(128), (byte) -128);
        assertEquals(IPUtils.int2ubyte(130), (byte) -126);
    }

    @Test
    public void nibble2int() {
        assertEquals(IPUtils.nibble2int((byte) 'f'), 15);
        assertEquals(IPUtils.nibble2int((byte) 'F'), 15);
        assertEquals(IPUtils.nibble2int((byte) 'X'), -1); // -1 means No conversion possible
        assertEquals(IPUtils.nibble2int((byte) '0'), 0);
        assertEquals(IPUtils.nibble2int((byte) '7'), 7);
    }

    public void testint2bibble() {
        assertEquals(IPUtils.nibble2int((byte) 'f'), 15);
        assertEquals(IPUtils.nibble2int((byte) 'F'), 15);
        assertEquals(IPUtils.nibble2int((byte) 'X'), -1);
        assertEquals(IPUtils.nibble2int((byte) '0'), 0);
        assertEquals(IPUtils.nibble2int((byte) '7'), 7);
    }

    @Test
    public void testint16bit2hex() {
        assertEquals(IPUtils.int16bit2hex(0), "0000");
        assertEquals(IPUtils.int16bit2hex(32768), "8000");
        assertEquals(IPUtils.int16bit2hex(65536), null); // Null means no conversion possible
        assertEquals(IPUtils.int16bit2hex(-1), null); // Null means no conversion possible
        assertEquals(IPUtils.int16bit2hex(65535), "FFFF");
        assertEquals(IPUtils.int16bit2hex(256), "0100");
    }

    @Test
    public void test16bithex2int() {
        assertEquals(IPUtils.hex16bit2int(""), 0); // Allows leading blanks
        assertEquals(IPUtils.hex16bit2int("f"), 15);
        assertEquals(IPUtils.hex16bit2int("fff"), 4095);
        assertEquals(IPUtils.hex16bit2int("ffff"), 65535);
        assertEquals(IPUtils.hex16bit2int("12345"), -1); // -1 means no conversion possible
        assertEquals(IPUtils.hex16bit2int("0000"), 0);
        assertEquals(IPUtils.hex16bit2int("FFff"), 65535); // Test mixed case
        assertEquals(IPUtils.hex16bit2int("0100"), 256);
        assertEquals(IPUtils.hex16bit2int("8000"), 32768);
    }

    @Test
    public void testAcceptValidIPv4Subnet() {
        assertTrue(IPUtils.isValidIpv4Subnet("192.168.3.51/28"));
    }

    @Test
    public void testRejectSubnetOutOfRangeIPv4() {
        assertFalse(IPUtils.isValidIpv4Subnet("192.168.3.51/33"));
        assertFalse(IPUtils.isValidIpv4Subnet("192.168.3.51/-1"));
    }

    @Test
    public void testRejectNullSubnetIPv4() {
        assertFalse(IPUtils.isValidIpv4Subnet(null));
    }

    @Test
    public void testRejectSubnetwithoutnumberIPv4() {
        assertFalse(IPUtils.isValidIpv4Subnet("192.168.3.51/"));
    }

    @Test
    public void testRejectSubnetwithoutIPinIpv4() {
    }

    @Test
    public void ShouldAcceptIPv4Subnets() {
        int i;
        String ip;
        for (i = -65536; i <= 6536; i++) {
            ip = String.format("192.168.3.51/%d", i);
            if (i >= 0 && i <= 32) {
                assertTrue(String.format("Expected acceptance of %s", ip), IPUtils.IP4RegEx(ip));
            } else {
                assertFalse(String.format("Expected rejection of %s", ip), IPUtils.IP4RegEx(ip));
            }
        }
    }

    @Test
    public void ShouldAcceptIPv6Subnets() {
        int i;
        String ip;
        for (i = -65536; i <= 6536; i++) {
            ip = String.format("ffff::/%d", i);
            if (i >= 0 && i <= 128) {
                assertTrue(String.format("Expected acceptance of %s", ip), IPUtils.IP6RegEx(ip));
            } else {
                assertFalse(String.format("Expected rejection of %s", ip), IPUtils.IP6RegEx(ip));
            }
        }
    }
}
