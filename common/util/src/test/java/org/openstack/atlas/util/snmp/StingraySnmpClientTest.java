package org.openstack.atlas.util.snmp;

import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;

import java.util.Map;

import static org.junit.Assert.assertFalse;
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
        try {
            assertTrue(client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS).size() > 0);
        } catch (StingraySnmpGeneralException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void shouldReturnMapOfStringByRawUsage() {
        try {
            Map<String, RawSnmpUsage> map = client.getSnmpUsage();
            assertTrue(map.entrySet().size() > 0);
        } catch (StingraySnmpGeneralException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithInvalidAddress() {
        client.setAddress("10.1000.1.1");
        try {
            client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
        } catch (StingraySnmpGeneralException e) {
            assertFalse(e.getMessage(), true);
        }
    }

    @Test
    public void shouldFailWithIncorrectPort() {
        client.setPort("1111");
        try {
            client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
        } catch (StingraySnmpGeneralException e) {
            assertFalse(e.getMessage(), true);
        }
    }
}
