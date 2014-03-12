package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl.HTTP_LOG_FORMAT;
import static org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl.NON_HTTP_LOG_FORMAT;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS;

public class ConnectionLoggingIntegrationTest extends ZeusTestBase {

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
    public void changeProtocolWithConnectionLoggingEnabled() {
        try {
            boolean[] isLoggingEnabled = getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.FALSE, isLoggingEnabled[0]);

            lb.setConnectionLogging(Boolean.TRUE);
            zxtmAdapter.updateConnectionLogging(config, lb);
            boolean[] isLoggingEnabled2 = getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.TRUE, isLoggingEnabled2[0]);
            String[] logFormat = getServiceStubs().getVirtualServerBinding().getLogFormat(new String[]{loadBalancerName()});
            Assert.assertEquals(HTTP_LOG_FORMAT, logFormat[0]);

            lb.setProtocol(HTTPS);
            zxtmAdapter.updateProtocol(config, lb);
            boolean[] isLoggingEnabled3 = getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.TRUE, isLoggingEnabled3[0]);
            String[] logFormat2 = getServiceStubs().getVirtualServerBinding().getLogFormat(new String[]{loadBalancerName()});
            Assert.assertEquals(NON_HTTP_LOG_FORMAT, logFormat2[0]);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
