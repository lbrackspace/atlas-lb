package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.TCP;

public class ContentCachingIntegrationTest extends ZeusTestBase {

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
    public void changeFromHttpProtocolToNonHttpProtocolWithContentCachingEnabled() {
        try {
            boolean[] isCachingEnabled = getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.FALSE, isCachingEnabled[0]);

            lb.setProtocol(HTTP);
            zxtmAdapter.updateProtocol(config, lb);

            lb.setContentCaching(Boolean.TRUE);
            zxtmAdapter.updateContentCaching(config, lb);

            boolean[] isCachingEnabled2 = getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.TRUE, isCachingEnabled2[0]);

            lb.setProtocol(TCP);
            zxtmAdapter.updateProtocol(config, lb);

            boolean[] isCachingEnabled3 = getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.FALSE, isCachingEnabled3[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
