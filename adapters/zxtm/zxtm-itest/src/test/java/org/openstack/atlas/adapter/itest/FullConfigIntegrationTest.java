package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.ObjectDoesNotExist;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.service.domain.entities.*;

import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;

public class FullConfigIntegrationTest extends ZeusTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void createFullyConfiguredLoadBalancer() {
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        lb.setSessionPersistence(HTTP_COOKIE);

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
        lb.setConnectionLimit(limit);

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

        lb.setAccessLists(networkItems);

        try {
            removeLoadBalancer();
            zxtmAdapter.createLoadBalancer(config, lb);
            Thread.sleep(1000); // TODO: Verify settings here
            removeLoadBalancer();
        } catch (ObjectDoesNotExist odne) {
            // Ignore
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
