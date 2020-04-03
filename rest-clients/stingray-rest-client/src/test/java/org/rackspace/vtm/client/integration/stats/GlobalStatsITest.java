package org.rackspace.vtm.client.integration.stats;

import org.junit.*;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.config.ClientConfigKeys;
import org.rackspace.vtm.client.counters.GlobalCounters;
import org.rackspace.vtm.client.counters.GlobalCountersProperties;
import org.rackspace.vtm.client.counters.GlobalCountersStatistics;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.integration.VTMTestBase;
import org.rackspace.vtm.client.pool.Pool;

import java.net.URI;
import java.net.URISyntaxException;
public class GlobalStatsITest extends VTMTestBase {
    String vsName;
    String poolName;
    URI endpoint;
    Pool pool;
    int port;
    GlobalCountersStatistics stats;
    GlobalCountersProperties statsProperties;
    GlobalCounters counters;

    /**
     * This method is the beginning for every test following.  Initial steps to the testing are completed here.
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        counters = new GlobalCounters();
        statsProperties = new GlobalCountersProperties();
        stats = new GlobalCountersStatistics();
        vsName = TESTNAME;
        endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint)
                + config.getString(ClientConfigKeys.stingray_base_uri));
        statsProperties.setStatistics(stats);
    }


    @Test
    public void testRetrieveVirtualServerStats() throws VTMRestClientObjectNotFoundException, VTMRestClientException, URISyntaxException {
        GlobalCounters s = client.getGlobalCounters(endpoint);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.getProperties().getStatistics().getUpTime() > 0);
    }
}