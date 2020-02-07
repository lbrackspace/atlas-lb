package org.rackspace.stingray.client.integration.stats;

import org.junit.*;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.integration.StingrayTestBase;
import org.rackspace.stingray.client.config.ClientConfigKeys;
import org.rackspace.stingray.client.counters.VirtualServerStats;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolProperties;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VirtualServerStatsITest extends StingrayTestBase {
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

    /**
     * This method is the beginning for every test following.  Initial steps to the testing are completed here.
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        stats = new VirtualServerStats();
        statsProperties = new VirtualServerStatsProperties();
        vsName = TESTNAME;
        endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint)
                + config.getString(ClientConfigKeys.stingray_stats_base_uri));
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
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
        statsProperties = stats.getStatistics();
        Assert.assertNotNull(statsProperties);
        Assert.assertNotNull(statsProperties.getConnectTimedOut());
        Assert.assertNotNull(statsProperties.getConnectionErrors());
        Assert.assertNotNull(statsProperties.getConnectionFailures());
        Assert.assertNotNull(statsProperties.getDataTimedOut());
        Assert.assertNotNull(statsProperties.getKeepaliveTimedOut());
        Assert.assertNotNull(statsProperties.getMaxConn());
        Assert.assertNotNull(statsProperties.getCurrentConn());
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
        statsProperties = stats.getStatistics();
        Assert.assertNotNull(statsProperties);
        Assert.assertNotNull(statsProperties.getConnectTimedOut());
        Assert.assertNotNull(statsProperties.getConnectionErrors());
        Assert.assertNotNull(statsProperties.getConnectionFailures());
        Assert.assertNotNull(statsProperties.getDataTimedOut());
        Assert.assertNotNull(statsProperties.getKeepaliveTimedOut());
        Assert.assertNotNull(statsProperties.getMaxConn());
        Assert.assertNotNull(statsProperties.getCurrentConn());
    }

    /**
     * This method is to clean up the built pool and virtual server after running the test.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @After
    public void cleanUp() throws StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        client.deleteVirtualServer(vsName);
        try {
            client.getVirtualServer(vsName);
        } catch (StingrayRestClientObjectNotFoundException e) {
            // Object not found, this is expected.
        }
        client.deletePool(poolName);
        try {
            client.getPool(poolName);
        } catch (StingrayRestClientObjectNotFoundException e) {
            // Object not found, this is expected.
        }
    }
}