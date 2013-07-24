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
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSimpleRateLimitOperations() {
        String vsName;
        StingrayRestClient client;
        RateLimit rateLimit;
        Bandwidth bandwidth;
        try {
            vsName = ZxtmNameBuilder.genVSName(lb);
            client = new StingrayRestClient();

            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(5);
            setRateLimit(client, vsName, rateLimit);
            bandwidth = getRateLimit(client, vsName);
            Assert.assertNotNull(bandwidth);
            Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());

            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(10);
            updateRateLimit(client, vsName, rateLimit);
            bandwidth = getRateLimit(client, vsName);
            Assert.assertNotNull(bandwidth);
            Assert.assertEquals(bandwidth.getProperties().getBasic().getMaximum(), rateLimit.getMaxRequestsPerSecond());

            deleteRateLimit(client, vsName);
            bandwidth = null;
            try {
                bandwidth = getRateLimit(client, vsName);
            } catch (Exception e) { }
            Assert.assertNull(bandwidth);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRateLimit(StingrayRestClient client, String vsName, RateLimit rateLimit) throws InsufficientRequestException, RollBackException, RemoteException {
        lb.setRateLimit(rateLimit);
        stmAdapter.setRateLimit(config,lb,rateLimit);
    }

    private void updateRateLimit(StingrayRestClient client, String vsName, RateLimit rateLimit) throws InsufficientRequestException, RollBackException, RemoteException {
        lb.setRateLimit(rateLimit);
        stmAdapter.updateRateLimit(config,lb,rateLimit);
    }

    private Bandwidth getRateLimit(StingrayRestClient client, String vsName) throws StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        Bandwidth b = client.getBandwidth(vsName);
        return b;
    }

    private void deleteRateLimit(StingrayRestClient client, String vsName) throws InsufficientRequestException, RollBackException, RemoteException {
        stmAdapter.deleteRateLimit(config, lb);
    }


}
