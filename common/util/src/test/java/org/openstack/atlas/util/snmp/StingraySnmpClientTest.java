package org.openstack.atlas.util.snmp;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;

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
        BasicConfigurator.configure();
        address = "10.12.99.19";
        port = "1161";
        community = "public";
        client = new StingraySnmpClient(address, port, community);
        client.setMaxRetrys(1);
    }

    @Test
    public void shouldReturnVariableBindingList() throws Exception {
        assertTrue(client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS).size() > 0);
    }

    @Test
    public void shouldReturnMapOfStringByRawUsage() throws Exception {
        Map<String, RawSnmpUsage> map = client.getSnmpUsage();
        assertTrue(map.entrySet().size() > 0);
    }

    @Test(expected = StingraySnmpGeneralException.class)
    public void shouldFailWithInvalidAddress() throws Exception {
        client.setAddress("10.1000.1.1");
        client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
    }

    @Test(expected = StingraySnmpGeneralException.class)
    public void shouldFailWithIncorrectPort() throws Exception {
        client.setPort("1111");
        client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
    }

    @Test(expected = StingraySnmpGeneralException.class)
    public void shouldFailWithInvalidCommunity() throws Exception {
        client.setCommunity("expensivePradaBag");
        client.getWalkOidBindingList(OIDConstants.VS_TOTAL_CONNECTIONS);
    }
}
