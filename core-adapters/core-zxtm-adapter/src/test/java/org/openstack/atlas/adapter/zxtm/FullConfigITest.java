package org.openstack.atlas.adapter.zxtm;

import com.zxtm.service.client.ObjectDoesNotExist;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.datamodel.CoreAlgorithmType;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.SessionPersistence;

public class FullConfigITest extends ITestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupLb1();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void createFullyConfiguredLoadBalancer() {
        SessionPersistence sessionPersistence = new SessionPersistence();
        sessionPersistence.setPersistenceType(CorePersistenceType.HTTP_COOKIE);

        HealthMonitor healthMonitor = new HealthMonitor();
        healthMonitor.setType(CoreHealthMonitorType.CONNECT);
        healthMonitor.setDelay(10);
        healthMonitor.setTimeout(20);
        healthMonitor.setAttemptsBeforeDeactivation(3);

        ConnectionThrottle limit = new ConnectionThrottle();
        limit.setRateInterval(10);
        limit.setMaxRequestRate(10);

        lb_1.setProtocol(CoreProtocolType.HTTP);
        lb_1.setPort(80);
        lb_1.setAlgorithm(CoreAlgorithmType.LEAST_CONNECTIONS);
        lb_1.setSessionPersistence(sessionPersistence);
        lb_1.setHealthMonitor(healthMonitor);
        lb_1.setConnectionThrottle(limit);
        try {
            removeLoadBalancer();
            zxtmAdapter.createLoadBalancer(config, lb_1);
            // TODO: Verify settings here if you want, but most have been verified in SimpleITest
        } catch (AdapterException e) {
            if (e.getCause() instanceof ObjectDoesNotExist) {
                // Ignore
            } else {
                Assert.fail(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
