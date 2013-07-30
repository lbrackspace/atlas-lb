package org.openstack.atlas.adapter.itest;


import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.rmi.RemoteException;

public class RateLimitITest extends STMTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeLoadBalancer();
        stmClient.destroy();
    }

    // Test everything together for completeness I guess?
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testSimpleRateLimitOperations() throws StingrayRestClientObjectNotFoundException {
        String vsName;
        StingrayRestClient client;
        RateLimit rateLimit;
        Bandwidth bandwidth;
        try {
            vsName = ZxtmNameBuilder.genVSName(lb);
            client = new StingrayRestClient();

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
        } catch (StingrayRestClientObjectNotFoundException onfe) {
            throw (onfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Test Get (though we need Set to work for this to work)
    @Test
    public void testGetRateLimit() throws Exception {
        String vsName = ZxtmNameBuilder.genVSName(lb);
        StingrayRestClient client = new StingrayRestClient();
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
        String vsName = ZxtmNameBuilder.genVSName(lb);
        StingrayRestClient client = new StingrayRestClient();
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
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteRateLimit() throws Exception {
        String vsName = ZxtmNameBuilder.genVSName(lb);
        StingrayRestClient client = new StingrayRestClient();
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
        stmAdapter.setRateLimit(config, lb, rateLimit);
    }

    private void updateRateLimit(RateLimit rateLimit) throws InsufficientRequestException, RollBackException, RemoteException {
        lb.setRateLimit(rateLimit);
        stmAdapter.updateRateLimit(config, lb, rateLimit);
    }

    private Bandwidth getRateLimit(StingrayRestClient client, String vsName) throws StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        Bandwidth b = client.getBandwidth(vsName);
        return b;
    }

    private void deleteRateLimit() throws InsufficientRequestException, RollBackException, RemoteException {
        stmAdapter.deleteRateLimit(config, lb);
    }
}
