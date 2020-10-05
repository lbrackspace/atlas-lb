
package org.openstack.atlas.util.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.util.ip.DnsUtil;

import javax.naming.NamingException;

import java.util.List;


@Ignore
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
