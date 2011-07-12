
package org.openstack.atlas.api.validation.util.IPString;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv6Test {

    private final String left_compressed = "::6789";
    private final String left_uncompressed = "0000:0000:0000:0000:0000:0000:0000:6789";
    private final String right_compressed = "1234::";
    private final String right_uncompressed = "1234:0000:0000:0000:0000:0000:0000:0000";
    private final String zero_compressed = "::";
    private final String zero_uncompressed = "0000:0000:0000:0000:0000:0000:0000:0000";
    private final String middle_compressed = "1234::5678";
    private final String middle_uncompressed = "1234:0000:0000:0000:0000:0000:0000:5678";
    private final String ipv4Mixed_compressed = "::ffff:192.168.3.51";
    private final String ipv4Mixed_uncompressed = "0000:0000:0000:0000:0000:FFFF:C0A8:0333";
    private final byte[] ipv4Mixed_bytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -64, -88, 3, 51};

    public IPv6Test() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testRightCompress() throws IPStringConversionException {
        assertTrue("Expected right Compression to work", right_uncompressed.equals(IPv6.expand(right_compressed, 8)));
    }

    @Test
    public void testLeftCompress() throws IPStringConversionException {
        assertTrue("Expected left compression to work ", left_uncompressed.equals(IPv6.expand(left_compressed, 8)));
    }

    @Test
    public void testAllZeroCompress() throws IPStringConversionException {
        assertTrue("Expected All Zero compression to work", zero_uncompressed.equals(IPv6.expand(zero_compressed, 8)));
    }

    @Test
    public void testMiddleCompress() throws IPStringConversionException {
        assertTrue("Expected middle compress to work", middle_uncompressed.equals(IPv6.expand(middle_compressed, 8)));
    }

    @Test(expected = IPStringConversionException.class)
    public void testNotEnoughintVals() throws IPStringConversionException {
        IPv6.expand(":", 8);
    }

    @Test(expected = IPStringConversionException.class)
    public void testRejectMultipleZeroCompress() throws IPStringConversionException {
        IPv6.expand("1234::5678::FFFF", 8);
    }

    @Test(expected = IPStringConversionException.class)
    public void testRejectInvalidHex() throws IPStringConversionException {
        IPv6.expand("1234:x:23::", 8);
    }

    @Test(expected = IPStringConversionException.class)
    public void testRejectHexNot16bit() throws IPStringConversionException {
        IPv6.expand("fffff::", 8);
    }

    @Test
    public void testIPv4Mixed() throws IPStringConversionException {
        assertTrue("Expected IPv4 mixed address to work", bytes_match(ipv4Mixed_bytes,
                IPv6.IpString2bytes(ipv4Mixed_compressed)));
    }

    @Test
    public void getBytes() throws IPStringConversionException {
        IPv6 ip = new IPv6(ipv4Mixed_compressed);
        assertNotNull("Expected Non null bytes",ip.getBytes());
        assertTrue(bytes_match(ip.getBytes(),ipv4Mixed_bytes));
    }

    @Test
    public void testExpand() throws IPStringConversionException {
        IPv6 ip = new IPv6(ipv4Mixed_compressed);
        String expanded_str = ip.expand();
        assertNotNull("Expected non null expanded ip string",expanded_str);
        assertTrue("Expected uncompressed ip to match ip.expand()",expanded_str.equals(ipv4Mixed_uncompressed));
    }

    @Test
    public void testIPv6isValid() {
        assertTrue(String.format("Expected %s to validate",
                ipv4Mixed_compressed),IPUtils.isValidIpv6String(ipv4Mixed_compressed));
        assertFalse("Expected \":::\" to fail validation",IPUtils.isValidIpv6String(":::"));
    }

    @Test
    public void testBytes2IpString() throws IPStringConversionException {
        IPv6 ip = new IPv6(ipv4Mixed_bytes);
        assertTrue("Expected Bytes2IpString to work",ipv4Mixed_uncompressed.equals(ip.getString()));
    }

    public static boolean bytes_match(byte[] a, byte[] b) {
        int i;
        for (i = 0; i < 16; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

}
