package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.ip.exception.IPBigIntegerConversionException;
import java.math.BigInteger;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import java.security.NoSuchAlgorithmException;
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
    private final String ipv4Mixed_uncompressed = "0000:0000:0000:0000:0000:ffff:c0a8:0333";
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
        assertNotNull("Expected Non null bytes", ip.getBytes());
        assertTrue(bytes_match(ip.getBytes(), ipv4Mixed_bytes));
    }

    @Test
    public void testExpand() throws IPStringConversionException {
        IPv6 ip = new IPv6(ipv4Mixed_compressed);
        String expanded_str = ip.expand();
        assertNotNull("Expected non null expanded ip string", expanded_str);
        assertTrue("Expected uncompressed ip to match ip.expand()", expanded_str.equals(ipv4Mixed_uncompressed));
    }

    @Test
    public void testIPv6isValid() {
        assertTrue(String.format("Expected %s to validate",
                ipv4Mixed_compressed), IPUtils.isValidIpv6String(ipv4Mixed_compressed));
        assertFalse("Expected \":::\" to fail validation", IPUtils.isValidIpv6String(":::"));
    }

    @Test
    public void testBytes2IpString() throws IPStringConversionException {
        IPv6 ip = new IPv6(ipv4Mixed_bytes);
        assertTrue("Expected Bytes2IpString to work", ipv4Mixed_uncompressed.equals(ip.getString()));
    }

    @Test
    public void testToBigInteger() throws IPStringConversionException {
        String ip = "ffff::ffff";
        BigInteger expectedIp = new BigInteger("340277174624079928635746076935439056895");
        BigInteger actualIp = new IPv6(ip).toBigInteger();
        assertEquals(expectedIp.compareTo(actualIp), 0);
    }

    @Test
    public void testBigIntToIPv6() throws IPBigIntegerConversionException, IPStringConversionException {
        String ip = "::feef:aeae";
        String expected = new IPv6(ip).expand();
        BigInteger in = new BigInteger("4277120686");
        String actual = new IPv6(in).getString();
        assertEquals(expected, actual);
    }

    @Test
    public void testCompareTo() throws IPStringConversionException {
        assertEquals(new IPv6("::7000").compareTo(new IPv6("::0000:7000")), 0);
        assertEquals(new IPv6("::7000").compareTo(new IPv6("::0000:6000")), 1);
        assertEquals(new IPv6("::7000").compareTo(new IPv6("::0000:8000")), -1);
    }

    @Test
    public void testEquals() {
        assertTrue(new IPv6("ffff::ffff").equals(new IPv6("ffff:0000::0000:ffff")));
        assertFalse(new IPv6("1234::5678").equals(new IPv6("ffff:0000::0000:ffff")));
    }
    
    @Test
    public void testSetCluster() throws IPStringConversionException {
        IPv6 mangle = new IPv6("1234::5678");
        mangle.setClusterPartition(new IPv6Cidr("ffff::1111/64"));
        assertEquals(new IPv6("ffff::5678"),mangle);
        assertFalse(new IPv6("1111::5678").equals(mangle));
        mangle.setClusterPartition(new IPv6Cidr("aaaa:aaaa::aaaa:aaaa:aaaa/64"));
        assertEquals(new IPv6("aaaa:aaaa::5678"),mangle);
        assertFalse(new IPv6("1111::5678").equals(mangle));
    }

    @Test
    public void testSetAccountPartition() throws NoSuchAlgorithmException, IPStringConversionException{
        IPv6 control = new IPv6("ffff::");
        IPv6 expected = new IPv6("ffff::f0c6:5ccc:0000:0000");
        control.setAccountPartition(354934);
        assertEquals(expected, control);
    }

    @Test
    public void testSetLowPartition() throws IPStringConversionException{
        IPv6 control = new IPv6("ffff::");
        IPv6 expected = new IPv6("ffff::7fe6:ffef");
        control.setVipOctets(2145845231); // No way it will ever git this high
        assertEquals(expected,control);
    }

    @Test
    public void testInsertBigInteger() throws IPStringConversionException{
        IPv6 mangle;
        IPv6 expected;
        mangle = new IPv6("::");
        expected = new IPv6("5678:e3fc::feef:1234:cccc:aaaa");
        mangle.insertBigInteger(new BigInteger("feef1234",16),64,32);
        mangle.insertBigInteger(new BigInteger("e",16),16,4);
        mangle.insertBigInteger(new BigInteger("3fc",16),20,12);
        mangle.insertBigInteger(new BigInteger("ccccaaaa",16),96, 32);
        mangle.insertBigInteger(new BigInteger("5678",16),0,16);
        assertEquals(expected,mangle);

        mangle.insertBigInteger(new BigInteger("fefe",16), 80,16);
        expected = new IPv6("5678:e3fc::feef:fefe:cccc:aaaa");
        assertEquals(expected,mangle);

        mangle.insertBigInteger(new BigInteger("0",16), 16, 96);
        expected = new IPv6("5678::aaaa");
        assertEquals(expected,mangle);
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
