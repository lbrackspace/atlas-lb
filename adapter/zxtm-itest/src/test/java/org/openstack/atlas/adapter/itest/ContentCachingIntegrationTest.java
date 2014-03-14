package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.UserPages;

import java.rmi.RemoteException;

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
    public void testHttpProtocolToNonHttpProtocolWithContentCachingEnabled() {
        try {
            verifyHttpProtocolToNonHttpProtocolWithContentCachingEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testHttpProtocolToNonHttpProtocolWithContentCachingEnabledWithErrorPage() {
        //Should operate correctly while using a custom error page
        try {
            String efContent = "<html>test ep</html>";
            UserPages up = new UserPages();
            up.setErrorpage(efContent);
            lb.setUserPages(up);
            zxtmAdapter.setErrorFile(config, lb, efContent);
            Assert.assertEquals(efContent,
                    new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFileName())));
            verifyHttpProtocolToNonHttpProtocolWithContentCachingEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void verifyHttpProtocolToNonHttpProtocolWithContentCachingEnabled() throws InsufficientRequestException, RollBackException, RemoteException {
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
    }
}
