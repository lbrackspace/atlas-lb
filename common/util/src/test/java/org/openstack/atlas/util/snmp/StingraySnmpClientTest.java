package org.openstack.atlas.util.snmp;

import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpException;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class StingraySnmpClientTest {

    public StingraySnmpClient client;
    public String address;
    public String port;
    public String community;

    public StingraySnmpClientTest() {
    }

    @Before
    public void setUp() {
        address = "10.12.99.19";
        port = "1161";
        community = "public";
        client = new StingraySnmpClient(address, port, community);
    }

    @Test
    public void testSomething(){
    }

}
