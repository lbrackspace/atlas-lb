package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.CatalogMonitorType;
import org.apache.axis.types.UnsignedInt;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.HealthMonitor;
import org.openstack.atlas.service.domain.entities.HealthMonitorType;

import java.rmi.RemoteException;

public class HealthMonitorIntegrationTest extends ZeusTestBase {

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
    public void verifyHealthMonitorInAdapterAfterAddingToLb() throws InsufficientRequestException, RollBackException, RemoteException {
        try {
            HealthMonitor monitor = new HealthMonitor();
            monitor.setType(HealthMonitorType.HTTP);
            monitor.setAttemptsBeforeDeactivation(10);
            monitor.setBodyRegex("");
            monitor.setStatusRegex("");
            monitor.setPath("/");
            monitor.setDelay(60);
            monitor.setTimeout(90);

            lb.setHealthMonitor(monitor);
            zxtmAdapter.updateHealthMonitor(config, lb);

            String monitorName = monitorName();

            final CatalogMonitorType[] monitorTypeArray = getServiceStubs().getMonitorBinding().getType(new String[]{monitorName});
            Assert.assertEquals(1, monitorTypeArray.length);
            Assert.assertEquals(CatalogMonitorType._http, monitorTypeArray[0].getValue());

            final UnsignedInt[] attemptsBeforeDeactivationArray = getServiceStubs().getMonitorBinding().getFailures(new String[]{monitorName});
            Assert.assertEquals(1, attemptsBeforeDeactivationArray.length);
            Assert.assertEquals(new UnsignedInt(monitor.getAttemptsBeforeDeactivation()), attemptsBeforeDeactivationArray[0]);

            final String[] bodyRegexArray = getServiceStubs().getMonitorBinding().getBodyRegex(new String[]{monitorName});
            Assert.assertEquals(1, bodyRegexArray.length);
            Assert.assertEquals(monitor.getBodyRegex(), bodyRegexArray[0]);

            final String[] statusRegexArray = getServiceStubs().getMonitorBinding().getStatusRegex(new String[]{monitorName});
            Assert.assertEquals(1, statusRegexArray.length);
            Assert.assertEquals(monitor.getStatusRegex(), statusRegexArray[0]);

            final String[] pathRegexArray = getServiceStubs().getMonitorBinding().getPath(new String[]{monitorName});
            Assert.assertEquals(1, pathRegexArray.length);
            Assert.assertEquals(monitor.getPath(), pathRegexArray[0]);

            final UnsignedInt[] delayArray = getServiceStubs().getMonitorBinding().getDelay(new String[]{monitorName});
            Assert.assertEquals(1, delayArray.length);
            Assert.assertEquals(new UnsignedInt(monitor.getDelay()), delayArray[0]);

            final UnsignedInt[] timeoutArray = getServiceStubs().getMonitorBinding().getTimeout(new String[]{monitorName});
            Assert.assertEquals(1, timeoutArray.length);
            Assert.assertEquals(new UnsignedInt(monitor.getTimeout()), timeoutArray[0]);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
    //TODO: more tests...
}
