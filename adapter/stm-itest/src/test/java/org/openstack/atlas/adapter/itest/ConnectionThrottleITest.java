package org.openstack.atlas.adapter.itest;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;

import java.util.HashSet;
import java.util.Set;


public class ConnectionThrottleITest extends STMTestBase {

    private String vsName;
    private ConnectionLimit limits;
    private int rateInterval;
    private int maxConnections;
    private int minConnections;
    private int maxConnectionRate;
    private int expectedMax10Connections;

    @Before
    public void standUp() throws InterruptedException, InsufficientRequestException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();

        limits = new ConnectionLimit();
        setConnectionLimitParams(54, 55, 56, 57);

        vsName = ZxtmNameBuilder.genVSName(lb);
    }

    @After
    public void tearDown() {
        removeLoadBalancer();
        stmClient.destroy();
    }

    @Test
    public void createAndVerifyBasicConnectionThrottle() throws Exception {
        setupConnectionThrottle();
        verifyConnectionThrottle();
    }

    @Test
    public void nullConnectionThrottleAndVerifyNoChange() throws Exception {
        setupConnectionThrottle();
        nullConnectionThrottle(); //nulling out the throttle should not cause any change
        verifyConnectionThrottle();
    }

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void deleteConnectionThrottleDeletingProtection() throws Exception {
        setupConnectionThrottle();
        verifyConnectionThrottle();
        deleteConnectionThrottle();
        stmClient.getProtection(vsName);
    }

    @Test
    public void deleteConnectionThrottleUpdatingProtection() throws Exception {
        setupFakeAccessList();
        setupConnectionThrottle();
        verifyConnectionThrottle();
        deleteConnectionThrottle();
        verifyEmptyConnectionThrottle();
    }

    private void setupConnectionThrottle() throws Exception {
        lb.setConnectionLimit(limits);
        stmAdapter.updateLoadBalancer(config, lb, lb);
    }

    private void setupFakeAccessList() {
        Set<AccessList> accessLists = new HashSet<AccessList>();
        AccessList accessList = new AccessList();
        accessList.setType(AccessListType.ALLOW);
        accessList.setIpAddress("127.0.0.1");
        accessLists.add(accessList);
        lb.setAccessLists(accessLists);
    }

    private void nullConnectionThrottle() throws Exception {
        lb.setConnectionLimit(null);
        stmAdapter.updateLoadBalancer(config, lb, lb);
    }

    private void deleteConnectionThrottle() throws Exception {
        stmAdapter.deleteConnectionThrottle(config, lb);
    }

    private void setConnectionLimitParams(Integer rateInterval, Integer maxConnections,
                                          Integer minConnections, Integer maxConnectionRate) {
        this.rateInterval = rateInterval;
        this.maxConnections = maxConnections;
        this.minConnections = minConnections;
        this.maxConnectionRate = maxConnectionRate;
        expectedMax10Connections = 10 * maxConnections;
        limits.setRateInterval(rateInterval);
        limits.setMaxConnections(maxConnections);
        limits.setMinConnections(minConnections);
        limits.setMaxConnectionRate(maxConnectionRate);
    }

    private void verifyConnectionThrottle() throws Exception {
        Protection protection = stmClient.getProtection(vsName);
        Assert.assertNotNull(protection);
        ProtectionConnectionLimiting retrievedLimit = protection.getProperties().getConnection_limiting();
        Assert.assertEquals(rateInterval, (int) retrievedLimit.getRate_timer());
        Assert.assertEquals(maxConnections, (int) retrievedLimit.getMax_1_connections());
        Assert.assertEquals(minConnections, (int) retrievedLimit.getMin_connections());
        Assert.assertEquals(maxConnectionRate, (int) retrievedLimit.getMax_connection_rate());
        Assert.assertEquals(expectedMax10Connections, (int) retrievedLimit.getMax_10_connections());
    }

    private void verifyEmptyConnectionThrottle() throws Exception {
        Protection protection = stmClient.getProtection(vsName);
        Assert.assertNotNull(protection);
        ProtectionConnectionLimiting retrievedLimit = protection.getProperties().getConnection_limiting();
        //Their default is 1 now for rate_timer...........
        Assert.assertEquals(1, (int) retrievedLimit.getRate_timer());
        Assert.assertEquals(0, (int) retrievedLimit.getMax_1_connections());
        Assert.assertEquals(0, (int) retrievedLimit.getMin_connections());
        Assert.assertEquals(0, (int) retrievedLimit.getMax_connection_rate());
        //The following might need to be changed when we figure out what to do about max10
        Assert.assertEquals(0, (int) retrievedLimit.getMax_10_connections());
    }


}
