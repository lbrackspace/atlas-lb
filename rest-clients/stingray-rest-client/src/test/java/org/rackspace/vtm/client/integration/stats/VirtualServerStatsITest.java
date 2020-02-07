package org.rackspace.vtm.client.integration.stats;

import org.junit.*;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.integration.VTMTestBase;
import org.rackspace.vtm.client.config.ClientConfigKeys;
import org.rackspace.vtm.client.counters.VirtualServerStats;
import org.rackspace.vtm.client.counters.VirtualServerStatsProperties;
import org.rackspace.vtm.client.counters.VirtualServerStatsStatistics;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolProperties;
import org.rackspace.vtm.client.virtualserver.VirtualServer;
import org.rackspace.vtm.client.virtualserver.VirtualServerBasic;
import org.rackspace.vtm.client.virtualserver.VirtualServerProperties;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VirtualServerStatsITest extends VTMTestBase {
    String vsName;
    String poolName;
    URI endpoint;
    Pool pool;
    int port;
    VirtualServer virtualServer;
    VirtualServerBasic basic;
    VirtualServerProperties properties;
    VirtualServerStats stats;
    VirtualServerStatsProperties statsProperties;
    VirtualServerStatsStatistics statistics;

    /**
     * This method is the beginning for every test following.  Initial steps to the testing are completed here.
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        stats = new VirtualServerStats();
        statsProperties = new VirtualServerStatsProperties();
        statistics = new VirtualServerStatsStatistics();
        vsName = TESTNAME;
        endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint)
                + config.getString(ClientConfigKeys.stingray_stats_base_uri));
        statsProperties.setStatistics(statistics);
        virtualServer = new VirtualServer();
        properties = new VirtualServerProperties();
        basic = new VirtualServerBasic();
        basic.setEnabled(true);
        poolName = TESTNAME;
        vsName = TESTNAME;
        port = 8998;
        pool = new Pool();
        pool.setProperties(new PoolProperties());
        basic.setPool(poolName);
        basic.setPort(port);
        properties.setBasic(basic);
        virtualServer.setProperties(properties);
    }


    /**
     * This method tests the create virtual server request, and will verify its creation with a get request.
     *
     * @throws VTMRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testRetrieveVirtualServerStats() {
        List<Child> children = new ArrayList<Child>();
        try {
            client.createPool(poolName, pool);
        } catch (Exception e) {
            Assert.fail("Failed to create pool " + poolName + " for virtual server.");
        }
        try {
            client.createVirtualServer(vsName, virtualServer);
        } catch (Exception e) {
            Assert.fail("Failed to create a virtual server");
        }
        try {
            children = client.getVirtualServers();
        } catch (Exception e) {
            Assert.fail("");
        }
        Boolean containsVirtualServer = false;
        for (Child child : children) {
            if (child.getName().equals(vsName)) {
                containsVirtualServer = true;
            }
        }
        Assert.assertTrue(containsVirtualServer);
        try {
            stats = client.getVirtualServerStats(vsName, endpoint);
        } catch (Exception e) {
            Assert.fail("Exception thrown when retrieving virtual server stats for " + vsName + ".");
        }
        statistics = stats.getProperties().getStatistics();
        Assert.assertNotNull(statistics);
        Assert.assertNotNull(statistics.getConnectTimedOut());
        Assert.assertNotNull(statistics.getConnectionErrors());
        Assert.assertNotNull(statistics.getConnectionFailures());
        Assert.assertNotNull(statistics.getDataTimedOut());
        Assert.assertNotNull(statistics.getKeepaliveTimedOut());
        Assert.assertNotNull(statistics.getMaxConn());
        Assert.assertNotNull(statistics.getCurrentConn());
    }


    @Ignore
    @Test
    public void testRetrieveVirtualServerStatsLiveTemp() {
        // comment out @after for safety, DO NOT SET VSNAME...
        String b = "accountid_LBID<_S>";
        List<Child> children = new ArrayList<Child>();

        try {
            children = client.getVirtualServers();
        } catch (Exception e) {
            Assert.fail("");
        }
        Boolean containsVirtualServer = false;
        for (Child child : children) {
            if (child.getName().equals(b)) {
                containsVirtualServer = true;
            }
        }
        Assert.assertTrue(containsVirtualServer);
        try {
            stats = client.getVirtualServerStats(b, endpoint);
        } catch (Exception e) {
            Assert.fail("Exception thrown when retrieving virtual server stats for " + b + ".");
        }
        statistics = stats.getProperties().getStatistics();
        Assert.assertNotNull(statistics);
        Assert.assertNotNull(statistics.getConnectTimedOut());
        Assert.assertNotNull(statistics.getConnectionErrors());
        Assert.assertNotNull(statistics.getConnectionFailures());
        Assert.assertNotNull(statistics.getDataTimedOut());
        Assert.assertNotNull(statistics.getKeepaliveTimedOut());
        Assert.assertNotNull(statistics.getMaxConn());
        Assert.assertNotNull(statistics.getCurrentConn());
    }

    /**
     * This method is to clean up the built pool and virtual server after running the test.
     *
     * @throws VTMRestClientException, StingrayRestClientObjectNotFoundException
     */
    @After
    public void cleanUp() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        client.deleteVirtualServer(vsName);
        try {
            client.getVirtualServer(vsName);
        } catch (VTMRestClientObjectNotFoundException e) {
            // Object not found, this is expected.
        }
        client.deletePool(poolName);
        try {
            client.getPool(poolName);
        } catch (VTMRestClientObjectNotFoundException e) {
            // Object not found, this is expected.
        }
    }
}