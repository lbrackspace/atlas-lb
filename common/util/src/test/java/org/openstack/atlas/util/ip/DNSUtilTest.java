
package org.openstack.atlas.util.ip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DNSUtilTest {

    private final String testDomain = "developer.rackspace.com";

    public DNSUtilTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void shouldReturnDNSRecords() throws NamingException {
        // developer.rackspace.com has 3 records, one A, two AAA. This could change in the future...
        List<String> records = DnsUtil.lookup(testDomain, "A", "AAAA");
        Assert.assertEquals(3, records.size());
    }

    @Test(expected = NamingException.class)
    public void shouldFailForInvalidType() throws NamingException {
        List<String> records = DnsUtil.lookup(testDomain, "L");
    }

    @Test
    public void shouldReturnNoDNSRecordsForInvalidDomain() throws NamingException {
        List<String> records = DnsUtil.lookup("bork.z", "A");
        Assert.assertEquals(0, records.size());

    }
}
