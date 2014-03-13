package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.UserPages;

import java.rmi.RemoteException;

import static org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl.HTTP_LOG_FORMAT;
import static org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl.NON_HTTP_LOG_FORMAT;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;

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
    public void testChangeProtocolWithConnectionLoggingEnabled() {
        try {
            verifyChangeProtocolWithConnectionLoggingEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testChangeProtocolWithConnectionLoggingEnabledWithErrorPage() {
        //Should operate correctly while using a custom error page
        try {
            String efContent = "<html>test ep</html>";
            UserPages up = new UserPages();
            up.setErrorpage(efContent);
            lb.setUserPages(up);
            zxtmAdapter.setErrorFile(config, lb, efContent);
            Assert.assertEquals(efContent,
                    new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFileName())));
            //Disable logging
            lb.setConnectionLogging(Boolean.FALSE);
            zxtmAdapter.updateConnectionLogging(config, lb);
            //Update back to HTTP
            lb.setProtocol(HTTP);
            zxtmAdapter.updateProtocol(config, lb);
            boolean[] isLoggingEnabled2 = getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()});
            Assert.assertEquals(Boolean.FALSE, isLoggingEnabled2[0]);
            verifyChangeProtocolWithConnectionLoggingEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void verifyChangeProtocolWithConnectionLoggingEnabled() throws RemoteException, RollBackException, InsufficientRequestException {
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
    }
}
