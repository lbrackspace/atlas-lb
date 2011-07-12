
package org.openstack.atlas.api.validation.util.IPString;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPv4Test {

    private final String invalidgoogleIpv4 = "www.google.com";
    private final String invalidOctetRangeIpv4 = "192.168.3.510";
    private final String notEnoughOctetsIpv4 = "129.168.3";
    private final String tooManyOctetsIpv4 = "192.168.3.51.32";
    private final String validIpv4 = "192.168.3.51";
    private final byte[] expected_ipv4bytes = {-64, -88, 3, 51};
    private final String expected_ipv4 = "192.168.3.51";
    private final String left_uncompressed = "";
    private final String left_compressed = "";
    
    public IPv4Test() {
    }

    @Before
    public void setUp() {
    }

    @Test(expected = IPStringConversionException.class)
    public void shouldRejectGoogleAddress() throws IPStringConversionException {
        IPv4 ipv4 = new IPv4(invalidgoogleIpv4);
        ipv4.getBytes();
    }

    @Test(expected = IPStringConversionException.class)
    public void shouldRejectInvalidOctetRange() throws IPStringConversionException {
        IPv4 ipv4 = new IPv4(invalidOctetRangeIpv4);
        ipv4.getBytes();
    }

    @Test(expected = IPStringConversionException.class)
    public void shoudRejectNotEnoughOctetsIpv4() throws IPStringConversionException {
        IPv4 ipv4 = new IPv4(notEnoughOctetsIpv4);
        ipv4.getBytes();
    }

    @Test(expected=IPStringConversionException.class)
    public void shouldRejectTooManyOctetsIpv4() throws IPStringConversionException {
        IPv4 ipv4 = new IPv4(tooManyOctetsIpv4);
        ipv4.getBytes();
    }

    @Test
    public void getBytesShouldWork() throws IPStringConversionException {
        IPv4 ipv4 = new IPv4(expected_ipv4);
        byte[] decodedBytes = ipv4.getBytes();
        assertTrue("Expected getBytes to work for \"\"",bytes_match(expected_ipv4bytes,decodedBytes));
    }

    public static boolean bytes_match(byte[] a, byte[] b) {
        int i;
        for (i = 0; i < 4; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
