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
    public void shouldReturnMap() {
        try {
            Map<String, Long> map = client.getWalkRequest(OIDConstants.ALL_VS_CURRENT_CONNECTIONS);
            assertTrue(map.entrySet().size() > 0);
        } catch (StingraySnmpException sse) {
            System.out.println(sse.getMessage());
        }
    }

    @Test
    public void shouldReturnMapWithByteCounts() {
        try {
            Map<String, Long> map = client.getWalkRequest(OIDConstants.ALL_VS_TOTAL_BYTES_IN);
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                System.out.println(entry.toString());
            }
//            assertTrue(map.entrySet().size() > 0);
        } catch (StingraySnmpException sse) {
            System.out.println(sse.getMessage());
        }
    }
}
