package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.converters.BitConverters;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPUtilsTest {

    private byte[] a1;
    private byte[] a2;
    private byte[] and;
    private byte[] or;
    private byte[] xor;

    public IPUtilsTest() {
    }

    @Before
    public void setUp() {
        a1 = new byte[]{85, 85, 85, 85};
        a2 = new byte[]{51, 51, 51, 51};
        and = new byte[]{17, 17, 17, 17};
        or = new byte[]{119, 119, 119, 119};
        xor = new byte[]{102, 102, 102, 102};
    }

    @Test
    public void shouldMapHighestVipOctet() throws IPStringConversionException {
        int expected_vipoctet = 2147483647;
        IPv6 highest = new IPv6("cccc:cccc:cccc:cccc:aaaa:aaaa:7fff:ffff");
        assertEquals(expected_vipoctet,highest.getVipOctets());
    }

    @Test(expected=IPStringConversionException.class)
    public void shouldRejectNegativeVipOctets() throws IPStringConversionException {
        int expected_vipOctet = -1;
        IPv6 neg = new IPv6("cccc:cccc:cccc:cccc:aaaa:aaaa:8000:0000");
        assertEquals(expected_vipOctet,neg.getVipOctets());
    }

    @Test
    public void shouldMapLowestVipOctet() throws IPStringConversionException {
        int expected_vipOctet = 0;
        IPv6 lowest = new IPv6("cccc:cccc:cccc:cccc:aaaa:aaaa:0000:0000");
        assertEquals(expected_vipOctet,lowest.getVipOctets());
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
        assertEquals(BitConverters.ubyte2int((byte) -1), 255);
        assertEquals(BitConverters.ubyte2int((byte) 0), 0);
        assertEquals(BitConverters.ubyte2int((byte) 127), 127);
        assertEquals(BitConverters.ubyte2int((byte) -127), 129);
        assertEquals(BitConverters.ubyte2int((byte) -128), 128);
        assertEquals(BitConverters.ubyte2int((byte) -126), 130);
    }

    @Test
    public void testint2ubyte() {
        assertEquals(BitConverters.int2ubyte(255), (byte) -1);
        assertEquals(BitConverters.int2ubyte(0), (byte) 0);
        assertEquals(BitConverters.int2ubyte(127), (byte) 127);
        assertEquals(BitConverters.int2ubyte(129), (byte) -127);
        assertEquals(BitConverters.int2ubyte(128), (byte) -128);
        assertEquals(BitConverters.int2ubyte(130), (byte) -126);
    }

    @Test
    public void nibble2int() {
        assertEquals(BitConverters.nibble2Int((byte) 'f'), 15);
        assertEquals(BitConverters.nibble2Int((byte) 'F'), 15);
        assertEquals(BitConverters.nibble2Int((byte) 'X'), -1); // -1 means No conversion possible
        assertEquals(BitConverters.nibble2Int((byte) '0'), 0);
        assertEquals(BitConverters.nibble2Int((byte) '7'), 7);
    }

    public void testint2bibble() {
        assertEquals(BitConverters.nibble2Int((byte) 'f'), 15);
        assertEquals(BitConverters.nibble2Int((byte) 'F'), 15);
        assertEquals(BitConverters.nibble2Int((byte) 'X'), -1);
        assertEquals(BitConverters.nibble2Int((byte) '0'), 0);
        assertEquals(BitConverters.nibble2Int((byte) '7'), 7);
    }

    @Test
    public void testint16bit2hex() {
        assertEquals(BitConverters.int16bit2hex(0), "0000");
        assertEquals(BitConverters.int16bit2hex(32768), "8000");
        assertEquals(BitConverters.int16bit2hex(65536), null); // Null means no conversion possible
        assertEquals(BitConverters.int16bit2hex(-1), null); // Null means no conversion possible
        assertEquals(BitConverters.int16bit2hex(65535), "ffff");
        assertEquals(BitConverters.int16bit2hex(256), "0100");
    }

    @Test
    public void test16bithex2int() {
        assertEquals(BitConverters.hex16bit2int(""), 0); // Allows leading blanks
        assertEquals(BitConverters.hex16bit2int("f"), 15);
        assertEquals(BitConverters.hex16bit2int("fff"), 4095);
        assertEquals(BitConverters.hex16bit2int("ffff"), 65535);
        assertEquals(BitConverters.hex16bit2int("12345"), -1); // -1 means no conversion possible
        assertEquals(BitConverters.hex16bit2int("0000"), 0);
        assertEquals(BitConverters.hex16bit2int("FFff"), 65535); // Test mixed case
        assertEquals(BitConverters.hex16bit2int("0100"), 256);
        assertEquals(BitConverters.hex16bit2int("8000"), 32768);
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
    public void shouldRollbits() {
        byte[] mask;
        mask = IPUtils.rollMask(12, 4);
        assertTrue(IPUtils.bytesEqual(mask, new byte[]{-1, -16, 0, 0}));
        nop();
    }

    @Test
    public void shouldInvMask() {
        byte[] mask = {-1, -16, 0, 0};
        byte[] invmask = {0, 15, -1, -1};
        assertTrue(IPUtils.bytesEqual(IPUtils.invBytes(mask), invmask));
    }

    @Test
    public void shouldCorrectlyMapByteStreamOps() throws IPException {
        byte[] res;

        res = IPUtils.opBytes(a1, a2, ByteStreamOperation.AND);
        assertTrue(IPUtils.bytesEqual(res, and));

        res = IPUtils.opBytes(a1, a2, ByteStreamOperation.OR);
        assertTrue(IPUtils.bytesEqual(res, or));

        res = IPUtils.opBytes(a1, a2, ByteStreamOperation.XOR);
        assertTrue(IPUtils.bytesEqual(res, xor));

    }

    @Test
    public void shouldMatchIpv6Addresses() throws IPException {
        // From section 2.2.2 of RFC4291
        int i;
        byte[] unCompressedBytes;
        byte[] compressedBytes;
        String unCompressed[] = new String[]{"2001:DB8:0:0:8:800:200C:417A", "FF01:0:0:0:0:0:0:101",
            "0:0:0:0:0:0:0:1", "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:13.1.68.3",
            "0:0:0:0:0:ffff:129.144.52.38"};
        String compressed[] = new String[]{"2001:DB8::8:800:200C:417A", "FF01::101", "::1", "::",
            "::13.1.68.3", "::FFFF:129.144.52.38"};
        for (i = 0; i < unCompressed.length; i++) {
            unCompressedBytes = new IPv6(unCompressed[i]).getBytes();
            compressedBytes = new IPv6(compressed[i]).getBytes();
            assertTrue(IPUtils.bytesEqual(unCompressedBytes, compressedBytes));
        }
    }

    private void nop() {
    }
}
