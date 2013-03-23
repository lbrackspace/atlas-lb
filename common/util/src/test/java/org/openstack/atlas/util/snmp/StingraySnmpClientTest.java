package org.openstack.atlas.util.snmp;

import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import static org.openstack.atlas.util.snmp.StingraySnmpClient.getVirtualServerNameFromOid;
import static org.openstack.atlas.util.snmp.StingraySnmpClient.getOidFromVirtualServerName;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class StingraySnmpClientTest {

    public StingraySnmpClient client;
    public String address;
    public String port;
    public String community;
    public Map<String, String> knownOidMaps;

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
        knownOidMaps = new HashMap<String, String>();
        knownOidMaps.put("1.3.6.1.4.1.7146.1.2.2.2.1.9.8.77.121.115.113.108.95.86.83", "Mysql_VS");
        knownOidMaps.put("1.3.6.1.4.1.7146.1.2.2.2.1.9.15.108.98.97.97.115.95.97.100.109.105.110.95.97.112.105", "lbaas_admin_api");
        knownOidMaps.put("1.3.6.1.4.1.7146.1.2.2.2.1.9.16.108.98.97.97.115.95.112.117.98.108.105.99.95.97.112.105", "lbaas_public_api");
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

    @Test
    public void testVsNameFromOid() {
        for (Entry<String, String> ent : knownOidMaps.entrySet()) {
            String oid = ent.getKey();
            String vsName = ent.getValue();
            assertEquals(vsName, getVirtualServerNameFromOid(oid));
        }
    }

    @Test
    public void testOidFromVsName() {
        for (Entry<String, String> ent : knownOidMaps.entrySet()) {
            String oid = ent.getKey();
            String vsName = ent.getValue();
            String baseOid = "1.3.6.1.4.1.7146.1.2.2.2.1.9";
            assertEquals(oid, getOidFromVirtualServerName(baseOid, vsName));
        }
    }
}
