package org.openstack.atlas.adapter.itest;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;


public class ConnectionThrottleITest extends STMTestBase {

    private String vsName;
    private String secureVsName;
    private ConnectionLimit limits;
    private int rateInterval;
    private int maxConnections;
    private int minConnections;
    private int maxConnectionRate;
    private int expectedMax10Connections;


    @Before
    public void standUp() {
        limits = new ConnectionLimit();
        expectedMax10Connections = 0;
        setConnectionLimitParams(54, 55, 56, 57);


        setupIvars();
        createSimpleLoadBalancer();

        try {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            vsName = ZxtmNameBuilder.genVSName(lb);
            secureVsName = ZxtmNameBuilder.genSslVSName(lb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @After
    public void tearDown() {
        removeLoadBalancer();
    }

    @Test
    public void createAndVerifyBasicConnectionThrottle() throws Exception {
        setupConnectionThrottle();
        verifyConnectionThrottle();
    }

    @Test
    public void nullAndVerifyBasicConnectionThrottle() throws Exception {
        setupConnectionThrottle();
        nullConnectionThrottle();
        verifyConnectionThrottle();
    }

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void recreateAndDeleteBasicConnectionThrottle() throws Exception {
        setupConnectionThrottle();
        verifyConnectionThrottle();
        deleteConnectionThrottle();
        stmClient.getProtection(vsName);
    }

    public void setupConnectionThrottle() throws Exception {
        lb.setConnectionLimit(limits);
        stmAdapter.updateLoadBalancer(config, lb, lb);
    }

    public void nullConnectionThrottle() throws Exception {
        lb.setConnectionLimit(null);
        stmAdapter.updateLoadBalancer(config, lb, lb);
    }

    public void deleteConnectionThrottle() throws Exception {
        stmAdapter.deleteConnectionThrottle(config, lb);
    }

    public void setConnectionLimitParams(Integer rateInterval, Integer maxConnections,
                                         Integer minConnections, Integer maxConnectionRate) {
        this.rateInterval = rateInterval;
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.maxConnectionRate = maxConnectionRate;
        limits.setRateInterval(rateInterval);
        limits.setMaxConnections(maxConnections);
        limits.setMinConnections(minConnections);
        limits.setMaxConnectionRate(maxConnectionRate);
    }

    public void verifyConnectionThrottle() throws Exception {
        Protection protection = stmClient.getProtection(vsName);
        Assert.assertNotNull(protection);
        ProtectionConnectionLimiting retrievedLimit = protection.getProperties().getConnection_limiting();
        Assert.assertEquals(rateInterval, (int) retrievedLimit.getRate_timer());
        Assert.assertEquals(maxConnections, (int) retrievedLimit.getMax_1_connections());
        Assert.assertEquals(minConnections, (int) retrievedLimit.getMin_connections());
        Assert.assertEquals(maxConnectionRate, (int) retrievedLimit.getMax_connection_rate());
        Assert.assertEquals(expectedMax10Connections, (int) retrievedLimit.getMax_10_connections());
    }


}
