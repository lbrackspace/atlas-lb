package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.rackspace.stingray.client.StingrayRestClient;
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

    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
    }

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
    public void resetAfter() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDown() {
        teardownEverything();
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

    @Test
    public void deleteConnectionThrottleDeletingProtection() throws Exception {
        // An updated to accesslist or connection throttle shouldn't remove
        // protection class. An explicit call to remove protection happens during lb removal
        setupConnectionThrottle();
        verifyConnectionThrottle();
        deleteConnectionThrottle();
        verifyEmptyConnectionThrottle();
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
        // max10 is ignored, default is 0
        expectedMax10Connections = 0;
        limits.setRateInterval(rateInterval);
        limits.setMaxConnections(maxConnections);
        limits.setMinConnections(minConnections);
        limits.setMaxConnectionRate(maxConnectionRate);
    }

    private void verifyConnectionThrottle() throws Exception {
        Protection protection = stmClient.getProtection(vsName);
        Assert.assertNotNull(protection);
        ProtectionConnectionLimiting retrievedLimit = protection.getProperties().getConnectionLimiting();
        Assert.assertEquals(rateInterval, (int) retrievedLimit.getRateTimer());
        Assert.assertEquals(maxConnections, (int) retrievedLimit.getMax1Connections());
        Assert.assertEquals(minConnections, (int) retrievedLimit.getMinConnections());
        Assert.assertEquals(maxConnectionRate, (int) retrievedLimit.getMaxConnectionRate());
        Assert.assertEquals(expectedMax10Connections, (int) retrievedLimit.getMax10Connections());
    }

    private void verifyEmptyConnectionThrottle() throws Exception {
        Protection protection = stmClient.getProtection(vsName);
        Assert.assertNotNull(protection);
        ProtectionConnectionLimiting retrievedLimit = protection.getProperties().getConnectionLimiting();
        //Their default is 1 now for rate_timer...........
        Assert.assertEquals(1, (int) retrievedLimit.getRateTimer());
        Assert.assertEquals(0, (int) retrievedLimit.getMax1Connections());
        Assert.assertEquals(0, (int) retrievedLimit.getMinConnections());
        Assert.assertEquals(0, (int) retrievedLimit.getMaxConnectionRate());
        //The following might need to be changed when we figure out what to do about max10
        Assert.assertEquals(0, (int) retrievedLimit.getMax10Connections());
    }


}
