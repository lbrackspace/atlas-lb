package org.openstack.atlas.util.snmp;

import org.junit.Before;
import org.junit.Test;

public class StingraySnmpClientTest {

    public String address;
    public int port;

    public StingraySnmpClientTest() {
    }

    @Before
    public void setUp() {
        this.address = "10.12.99.19";
        this.port = 1161;
    }

    @Test
    public void shouldReturnMap() {
    }
}
