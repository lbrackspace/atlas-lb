package org.openstack.atlas.util.ip;

import org.openstack.atlas.util.ip.exception.IPException;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class IPv6CidrsTest {


    public IPv6CidrsTest() {
    }

    @Before
    public void setUp(){
        
    }

    @Test
    public void ShouldMapAddresses() throws IPException {
        IPv6Cidrs cidrs = new IPv6Cidrs();
        cidrs.getCidrs().add(new IPv6Cidr("2001:07FA:0001::/48"));
        assertTrue(cidrs.contains("2001:07fa:0001:0000:0000:0000:0000:0000"));
        assertTrue(cidrs.contains("2001:07fa:0001:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(cidrs.contains("2001:07fa:0002:0000:0000:0000:0000:0000")); // Except for this one

    }

}