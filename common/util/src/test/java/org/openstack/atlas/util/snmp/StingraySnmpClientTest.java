package org.openstack.atlas.util.snmp;

import org.junit.Before;
import org.junit.Test;

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
    public void shouldReturnVariableBindingList() {
        assertTrue(client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS).size() > 0);
    }

    @Test
    public void shouldReturnMapOfStringByRawUsage() {
        Map<String, RawSnmpUsage> map = client.getSnmpUsage();
        assertTrue(map.entrySet().size() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidAddress() {
        client.setAddress("10.1000.1.1");
        client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
    }

    @Test
    public void shouldFailWithIncorrectPort() {
        client.setPort("1111");
        client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
    }
}
