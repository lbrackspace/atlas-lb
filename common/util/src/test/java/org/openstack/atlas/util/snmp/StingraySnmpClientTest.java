package org.openstack.atlas.util.snmp;

import java.lang.reflect.Field;
import org.junit.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpRetryExceededException;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpSetupException;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.VariableBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.openstack.atlas.util.snmp.StingraySnmpClient.getOidFromVirtualServerName;
import static org.openstack.atlas.util.snmp.StingraySnmpClient.getVirtualServerNameFromOid;

public class StingraySnmpClientTest {

    public String baseOid;
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
        client = new StingraySnmpClient();
        client.setMaxRetrys(1);
        client.setAddress(address);
        client.setCommunity(community);
        client.setPort("1161");
        baseOid = "1.3.6.1.4.1.7146.1.2.2.2.1.9";
        knownOidMaps = new HashMap<String, String>();
        knownOidMaps.put("1.3.6.1.4.1.7146.1.2.2.2.1.9.8.77.121.115.113.108.95.86.83", "Mysql_VS");
        knownOidMaps.put("1.3.6.1.4.1.7146.1.2.2.2.1.9.15.108.98.97.97.115.95.97.100.109.105.110.95.97.112.105", "lbaas_admin_api");
        knownOidMaps.put("1.3.6.1.4.1.7146.1.2.2.2.1.9.16.108.98.97.97.115.95.112.117.98.108.105.99.95.97.112.105", "lbaas_public_api");
        baseOid = "1.3.6.1.4.1.7146.1.2.2.2.1.9";
    }

    @Ignore
    @Test
    public void shouldReturnVariableBindingList() throws Exception {
        assertTrue(client.getBulkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS).size() > 0);
    }

    @Ignore
    @Test
    public void shouldReturnMapOfStringByRawUsage() throws Exception {
        Map<String, RawSnmpUsage> map = client.getSnmpUsage();
        assertTrue(map.entrySet().size() > 0);
    }

    @Ignore
    @Test
    public void shouldFailWithInvalidAddress() throws Exception {
        client.setAddress("10.1000.1.1");
        try {
            client.getBulkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS);
        } catch (Exception ex) {
            assertTrue(ex instanceof StingraySnmpGeneralException);
        }
        Assert.fail();
    }

    @Ignore
    @Test
    public void shouldFailWithIncorrectPort() throws Exception {
        client.setPort("1111");
        try {
            client.getBulkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS);
        } catch (Exception ex) {
            assertTrue(ex instanceof StingraySnmpGeneralException);
            return;
        }
        Assert.fail();
    }

    @Ignore
    @Test
    public void shouldFailWithInvalidCommunity() throws Exception {
        client.setCommunity("expensivePradaBag");
        try {
            client.getBulkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS);
        } catch (Exception ex) {
            assertTrue(ex instanceof StingraySnmpGeneralException);
            return;
        }
        Assert.fail();
    }

    @Test
    public void testVsNameFromOid() {
        for (Entry<String, String> ent : knownOidMaps.entrySet()) {
            String oid = ent.getKey();
            String vsName = ent.getValue();
            assertEquals(vsName, getVirtualServerNameFromOid(baseOid, oid));
        }
    }

    @Test
    public void testOidFromVsName() {
        for (Entry<String, String> ent : knownOidMaps.entrySet()) {
            String oid = ent.getKey();
            String vsName = ent.getValue();

            assertEquals(oid, getOidFromVirtualServerName(baseOid, vsName));
        }
    }

    @Ignore
    @Test
    public void testSingleVsByteCountRequest() throws Exception {
        VariableBinding variableBinding = client.getBulkOidBindingList(OIDConstants.VS_BYTES_OUT).get(0);
        String name = getVirtualServerNameFromOid(baseOid, variableBinding.getOid().toString());
        Long value = client.getLongValueForVirtualServer(name, OIDConstants.VS_BYTES_OUT, false, false);
        assertTrue(value >= 0);
        variableBinding = client.getBulkOidBindingList(OIDConstants.VS_BYTES_IN).get(0);
        name = getVirtualServerNameFromOid(baseOid, variableBinding.getOid().toString());
        value = client.getLongValueForVirtualServer(name, OIDConstants.VS_BYTES_IN, false, false);
        assertTrue(value >= 0);
        variableBinding = client.getBulkOidBindingList(OIDConstants.VS_CURRENT_CONNECTIONS).get(0);
        name = getVirtualServerNameFromOid(baseOid, variableBinding.getOid().toString());
        value = client.getLongValueForVirtualServer(name, OIDConstants.VS_CURRENT_CONNECTIONS, false, false);
        assertTrue(value >= 0);
    }

    @Test
    public void testIncRequestIdShouldAlwaysReturnPositive() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        int requestId;
        Field requestIdField = StingraySnmpClient.class.getDeclaredField("requestId");
        requestIdField.setAccessible(true);
        requestId = requestIdField.getInt(null);
        assertTrue(requestId == StingraySnmpClient.getRequestId());

        requestIdField.setInt(null, Integer.MAX_VALUE - 5);
        assertFalse(StingraySnmpClient.getRequestId() == 0);

        assertTrue(StingraySnmpClient.getRequestId() == Integer.MAX_VALUE - 5);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == Integer.MAX_VALUE - 4);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == Integer.MAX_VALUE - 3);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == Integer.MAX_VALUE - 2);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == Integer.MAX_VALUE - 1);
        assertTrue(requestId >= 0);

        // instead of reaching MAX_VALUE the requestId should have fliped to zero

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == 0);
        assertTrue(requestId >= 0);


        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == 1);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == 2);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == 3);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == 4);
        assertTrue(requestId >= 0);

        StingraySnmpClient.incRequestId();
        requestId = StingraySnmpClient.getRequestId();
        assertTrue(requestId == 5);
        assertTrue(requestId >= 0);

    }
}
