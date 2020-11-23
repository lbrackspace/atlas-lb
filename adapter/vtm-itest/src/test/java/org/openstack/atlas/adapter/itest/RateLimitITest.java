package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.VTMNameBuilder;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.rmi.RemoteException;

public class RateLimitITest extends VTMTestBase {

    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    // Test everything together for completeness I guess?
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testSimpleRateLimitOperations() throws VTMRestClientObjectNotFoundException {
        String vsName;
        VTMRestClient client;
        RateLimit rateLimit;
        Bandwidth bandwidth;
        try {
            vsName = VTMNameBuilder.genVSName(lb);
            client = new VTMRestClient();

            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(5);
            setRateLimit(rateLimit);
            bandwidth = getRateLimit(client, vsName);
            Assert.assertNotNull(bandwidth);
            Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());

            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(10);
            updateRateLimit(rateLimit);
            bandwidth = getRateLimit(client, vsName);
            Assert.assertNotNull(bandwidth);
            Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());

            deleteRateLimit();
            bandwidth = getRateLimit(client, vsName);
            Assert.assertNull(bandwidth);
        } catch (VTMRestClientObjectNotFoundException onfe) {
            throw (onfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Test Get (though we need Set to work for this to work)
    @Test
    public void testGetRateLimit() throws Exception {
        String vsName = VTMNameBuilder.genVSName(lb);
        VTMRestClient client = new VTMRestClient();
        RateLimit rateLimit = new RateLimit();
        Bandwidth bandwidth;

        rateLimit.setMaxRequestsPerSecond(5);
        setRateLimit(rateLimit);
        bandwidth = getRateLimit(client, vsName);
        Assert.assertNotNull(bandwidth);
        Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());
    }

    // Test Set (though we need Get to work for this to work)
    @Test
    public void testSetRateLimit() throws Exception {
        String vsName = VTMNameBuilder.genVSName(lb);
        VTMRestClient client = new VTMRestClient();
        RateLimit rateLimit = new RateLimit();
        Bandwidth bandwidth;

        rateLimit.setMaxRequestsPerSecond(10);
        setRateLimit(rateLimit);
        bandwidth = getRateLimit(client, vsName);
        Assert.assertNotNull(bandwidth);
        Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());

        rateLimit = new RateLimit();
        rateLimit.setMaxRequestsPerSecond(15);
        updateRateLimit(rateLimit);
        bandwidth = getRateLimit(client, vsName);
        Assert.assertNotNull(bandwidth);
        Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());
    }

    // Test Delete (though we need Set and Get for this to work)
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteRateLimit() throws Exception {
        String vsName = VTMNameBuilder.genVSName(lb);
        VTMRestClient client = new VTMRestClient();
        RateLimit rateLimit = new RateLimit();
        Bandwidth bandwidth;

        try {
            rateLimit.setMaxRequestsPerSecond(20);
            setRateLimit(rateLimit);
            bandwidth = getRateLimit(client, vsName);
            Assert.assertNotNull(bandwidth);
            Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        deleteRateLimit();
        bandwidth = getRateLimit(client, vsName); //should throw an exception
        Assert.assertNull(bandwidth);
    }


    private void setRateLimit(RateLimit rateLimit) throws InsufficientRequestException, RollBackException, RemoteException {
        lb.setRateLimit(rateLimit);
        vtmAdapter.setRateLimit(config, lb, rateLimit);
    }

    private void updateRateLimit(RateLimit rateLimit) throws InsufficientRequestException, RollBackException, RemoteException {
        lb.setRateLimit(rateLimit);
        vtmAdapter.updateRateLimit(config, lb, rateLimit);
    }

    private Bandwidth getRateLimit(VTMRestClient client, String vsName) throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Bandwidth b = client.getBandwidth(vsName);
        return b;
    }

    private void deleteRateLimit() throws InsufficientRequestException, RollBackException, RemoteException {
        vtmAdapter.deleteRateLimit(config, lb);
    }
}
