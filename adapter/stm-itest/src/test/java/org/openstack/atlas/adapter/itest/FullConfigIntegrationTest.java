package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.service.domain.entities.*;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;

public class FullConfigIntegrationTest extends STMTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeLoadBalancer();
    }

    @Test
    public void createFullyConfiguredLoadBalancer() {
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        lb.setSessionPersistence(HTTP_COOKIE);
        lb.setTimeout(99);

        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(HealthMonitorType.CONNECT);
        monitor.setDelay(10);
        monitor.setTimeout(20);
        monitor.setAttemptsBeforeDeactivation(3);
        lb.setHealthMonitor(monitor);

        ConnectionLimit limit = new ConnectionLimit();
        limit.setMaxConnections(50);
        limit.setRateInterval(10);
        limit.setMaxConnectionRate(10);
        limit.setMinConnections(1);
//        lb.setConnectionLimit(limit);

        lb.setConnectionLogging(true);

        Set<AccessList> networkItems = new HashSet<AccessList>();
        AccessList item1 = new AccessList();
        AccessList item2 = new AccessList();
        item1.setIpAddress("0.0.0.0/0");
        item2.setIpAddress("127.0.0.1");
        item1.setType(DENY);
        item2.setType(ALLOW);
        networkItems.add(item1);
        networkItems.add(item2);

//        lb.setAccessLists(networkItems);

        try {
            removeLoadBalancer();
            stmAdapter.createLoadBalancer(config, lb);
            Thread.sleep(1000);
            StingrayRestClient tclient = new StingrayRestClient();
            verifyVS(tclient);
            verifyPool(tclient);
            removeLoadBalancer();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private VirtualServer verifyVS(StingrayRestClient client) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        VirtualServer vs = client.getVirtualServer(loadBalancerName());
        ResourceTranslator translator = new ResourceTranslator();

        Assert.assertNotNull(vs);
        Assert.assertEquals(true, vs.getProperties().getBasic().getEnabled());
        Assert.assertEquals(lb.getPort(), vs.getProperties().getBasic().getPort());
        Assert.assertEquals(poolName(), vs.getProperties().getBasic().getPool());
        Assert.assertEquals("Default", vs.getProperties().getConnection_errors().getError_file());
        Assert.assertTrue(vs.getProperties().getBasic().getRequest_rules().contains(StmConstants.XFF));
        Assert.assertTrue(vs.getProperties().getBasic().getRequest_rules().contains(StmConstants.XFP));
        Assert.assertEquals(false, vs.getProperties().getBasic().getListen_on_any());
        Assert.assertEquals(false, vs.getProperties().getTcp().getProxy_close());
        Assert.assertEquals(vs.getProperties().getBasic().getListen_on_traffic_ips(), translator.genGroupNameSet(lb));

//        Assert.assertEquals(protectionClassName(), vs.getProperties().getBasic().getProtection_class());
        return null;
    }

    private VirtualServer verifyPool(StingrayRestClient client) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Pool pool = client.getPool(loadBalancerName());
        Assert.assertNotNull(pool);
        Assert.assertEquals(1, pool.getProperties().getBasic().getMonitors().size());
        Assert.assertEquals(lb.getAlgorithm().name().toLowerCase(), pool.getProperties().getLoad_balancing().getAlgorithm());
        Assert.assertEquals(1, pool.getProperties().getBasic().getNodes().size());

        return null;
    }
}
