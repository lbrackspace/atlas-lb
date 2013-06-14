package org.rackspace.stingray.client.integration;

import org.junit.Assert;
import org.junit.Test;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolLoadbalancing;
import org.rackspace.stingray.client.pool.PoolProperties;
import org.rackspace.stingray.client.util.EnumFactory;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.Set;

public class StingrayRestClientITest {
    //TODO: clean these up, make more...

    @Test
    public void verifyStingrayPoolManagerGet() throws Exception {
        StingrayRestClient client = new StingrayRestClient();

        VirtualServer pool = client.getVirtualServer("528830_770");
        Assert.assertNotNull(pool);
    }

    @Test
    public void verifyStingrayPoolManagerCreateAndUpdate() throws Exception {
        StingrayRestClient client = new StingrayRestClient();

        Pool pool = new Pool();
        PoolProperties poolProperties = new PoolProperties();
        PoolBasic poolBasic = new PoolBasic();

        Set<String> nodes = new HashSet<String>();
        nodes.add("10.1.1.1:80");
        poolBasic.setNodes(nodes);

        poolBasic.setPassive_monitoring(false);

        PoolLoadbalancing lbalgo = new PoolLoadbalancing();
        lbalgo.setAlgorithm(EnumFactory.Accept_from.WEIGHTED_ROUND_ROBIN.toString());

        poolProperties.setBasic(poolBasic);
        poolProperties.setLoad_balancing(lbalgo);

        pool.setProperties(poolProperties);

        Pool rpool = client.createPool("ctest_001", pool);

        Assert.assertNotNull(rpool);
        Assert.assertEquals(EnumFactory.Accept_from.WEIGHTED_ROUND_ROBIN.toString(), rpool.getProperties().getLoad_balancing().getAlgorithm());

        rpool.getProperties().getBasic().getNodes().add("10.2.2.2:8080");
        Pool upool = client.updatePool("ctest_001", rpool);

        Assert.assertEquals(2, upool.getProperties().getBasic().getNodes().size());

        client.deletePool("ctest_001");
    }

    @Test
    public void verifyErrorResponseParsing() throws Exception, JAXBException {
//        StingrayRestClient client = new StingrayRestClient();
//        ClientResponse response;
//
//        response = client.test();
//
//        String tjson = "{\"error_id\":\"json.parse_error\",\"error_text\":\"Invalid JSON data: Didn't find ':' after Hash key\"}";
//
//        response.setEntityInputStream(new ByteArrayInputStream(tjson.getBytes()));
//
//        ValidationError error = response.getEntity(ValidationError.class);
//        Assert.assertNotNull(error);
//
////        System.out.printf("ERROR: %s", error);
//        Assert.assertEquals("json.parse_error", error.getError_id());
//        Assert.assertEquals("Invalid JSON data: Didn't find ':' after Hash key", error.getError_text());
    }


//
//    @Test
//    public void verifyUpdateNodeOnPool() throws Exception, JAXBException {
//        StingrayRestClient client = new StingrayRestClient();
//
//        Pool pool = new Pool();
//        PoolProperties properties = new PoolProperties();
//        PoolBasic basic = new PoolBasic();
//
//        ClientResponse response = client.getResource("pools/528830_770");
////        System.out.print("BEFORE UPDATE: " + response.getEntity(String.class));
//
//        Pool read = response.getEntity(Pool.class);
//
//        read.getProperties().getBasic().getNodes().add("10.1.1.2:9090");
//
////        String jsonstring = "{\"properties\":{\"basic\":{\"bandwidth_class\":\"\",\"disabled\":[],\"draining\":[],\"failure_pool\":\"\",\"max_idle_connections_pernode\":50,\"monitors\":[],\"node_connection_attempts\":3,\"nodes\":[\"50.57.174.153:9090\",\"50.57.174.150:9090\"],\"note\":\"\",\"passive_monitoring\":false,\"persistence_class\":\"\",\"transparent\":false},\"auto_scaling\":{\"cloud_credentials\":\"\",\"cluster\":\"\",\"data_center\":\"\",\"data_store\":\"\",\"enabled\":false,\"external\":true,\"hysteresis\":20,\"imageid\":\"\",\"ips_to_use\":\"publicips\",\"last_node_idle_time\":3600,\"max_nodes\":4,\"min_nodes\":1,\"name\":\"\",\"port\":80,\"refractory\":180,\"response_time\":1000,\"scale_down_level\":95,\"scale_up_level\":40,\"size_id\":\"\"},\"connection\":{\"max_connect_time\":4,\"max_connections_per_node\":0,\"max_queue_size\":0,\"max_reply_time\":30,\"queue_timeout\":10},\"ftp\":{\"support_rfc_2428\":false},\"http\":{\"keepalive\":true,\"keepalive_non_idempotent\":false},\"load_balancing\":{\"algorithm\":\"random\",\"node_weighting\":[],\"priority_enabled\":false,\"priority_nodes\":1,\"priority_values\":[\"50.57.174.153:9090:2\",\"50.57.174.150:9090:2\"]},\"node\":{\"close_on_death\":false,\"retry_fail_time\":60},\"smtp\":{\"send_starttls\":true},\"ssl\":{\"client_auth\":false,\"enable\":false,\"enhance\":false,\"send_close_alerts\":false,\"server_name\":false,\"strict_verify\":false},\"tcp\":{\"nagle\":true},\"udp\":{\"accept_from\":\"dest_only\",\"accept_from_mask\":\"\"}}}";
//        ClientResponse response2 = client.updatePool("pools/528830_770", read);
////        System.out.print("AFTER UPDATE: " + response2.getEntity(String.class));
//        Pool updatedpool = response2.getEntity(Pool.class);
////        System.out.print("PoolRead: " + response2.getEntity(PoolUpdate.class));
//        org.junit.Assert.assertEquals(4, updatedpool.getProperties().getBasic().getNodes().size());
//    }
//
//    @Test
//    public void verifyGetAllPools() throws Exception {
//        StingrayRestClient client = new StingrayRestClient();
//        ClientResponse response = client.getPools("pools");
//        assertNotNull(response.getEntity(String.class));
//    }
}
