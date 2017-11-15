package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.VirtualServerBasicInfo;
import com.zxtm.service.client.VirtualServerRule;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS;

@RunWith(Enclosed.class)
public class HttpsRedirectIntegrationTest extends ZeusTestBase {
    public static class testingBasicHttpsRedirect {

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            lb.setProtocol(HTTPS);
            lb.setPort(443);
            setupSimpleLoadBalancer();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void testAddRemoveHttpsRedirect() {
            try {
                verifyAddRemoveHttpsRedirect();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testAddRemoveHttpsRedirectWithErrorPage() {
            try {
                String efContent = "<html>test ep</html>";
                UserPages up = new UserPages();
                up.setErrorpage(efContent);
                lb.setUserPages(up);
                zxtmAdapter.setErrorFile(config, lb, efContent);
                Assert.assertEquals(efContent,
                        new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFileName())));
                verifyAddRemoveHttpsRedirect();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        public void verifyAddRemoveHttpsRedirect() throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
            ArrayList<String> vsNames;

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(secureLoadBalancerName()));
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);

            lb.setHttpsRedirect(true);
            zxtmAdapter.updateHttpsRedirect(config, lb);

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(secureLoadBalancerName()));
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{redirectLoadBalancerName()})[0]);

            VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{redirectLoadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleForceHttpsRedirect, virtualServerRules[0][0]);

            lb.setHttpsRedirect(false);
            zxtmAdapter.updateHttpsRedirect(config, lb);

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(secureLoadBalancerName()));
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);
        }
    }

    public static class testingSslTerminationHttpsRedirect {

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

        @Before
        public void setupSsl() {
            setSslTermination();
        }

        @Test
        public void testAddRemoveHttpsRedirect() {
            try {
                verifyAddRemoveHttpsRedirect();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testAddRemoveHttpsRedirectWithErrorPage() {
            try {
                String efContent = "<html>test ep</html>";
                UserPages up = new UserPages();
                up.setErrorpage(efContent);
                lb.setUserPages(up);
                zxtmAdapter.setErrorFile(config, lb, efContent);
                Assert.assertEquals(efContent,
                        new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFileName())));
                verifyAddRemoveHttpsRedirect();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        private void setSslTermination() {
            try {
                String sVs = secureLoadBalancerName();
                SslTermination sslTermination = new SslTermination();
                sslTermination.setSecureTrafficOnly(true);
                sslTermination.setEnabled(true);
                sslTermination.setSecurePort(443);
                sslTermination.setCertificate(SslTerminationIntegrationTest.testCert);
                sslTermination.setPrivatekey(SslTerminationIntegrationTest.testKey);

                ZeusCrtFile zeusCrtFile = new ZeusCrtFile();
                zeusCrtFile.setPublic_cert(SslTerminationIntegrationTest.testCert);
                zeusCrtFile.setPrivate_key(SslTerminationIntegrationTest.testKey);

                ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
                zeusSslTermination.setCertIntermediateCert(SslTerminationIntegrationTest.testCert);
                zeusSslTermination.setSslTermination(sslTermination);

                lb.setSslTermination(zeusSslTermination.getSslTermination());

                zxtmAdapter.updateSslTermination(config, lb, zeusSslTermination);

                ArrayList<String> vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));
                Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));

                String[] certificate = getServiceStubs().getVirtualServerBinding().getSSLCertificate(new String[]{sVs});
                Assert.assertEquals(sVs, certificate[0]);

                final VirtualServerBasicInfo[] serverBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{sVs});
                Assert.assertEquals(sslTermination.getSecurePort(), serverBasicInfos[0].getPort());
                Assert.assertEquals(true, lb.getProtocol().toString().equalsIgnoreCase(serverBasicInfos[0].getProtocol().toString()));
                Assert.assertEquals(ZxtmNameBuilder.genVSName(lb), serverBasicInfos[0].getDefault_pool());

                boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()});
                Assert.assertEquals(true, vsEnabled[0]);

                boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
                Assert.assertEquals(sslTermination.getEnabled(), vsNonSecureEnabled[0]);

                String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
                Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        private void verifyAddRemoveHttpsRedirect() throws InsufficientRequestException, ZxtmRollBackException, RemoteException {
            ArrayList<String> vsNames;

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()})[0]);

            lb.setHttpsRedirect(true);
            zxtmAdapter.updateHttpsRedirect(config, lb);

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertFalse(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertTrue(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()})[0]);
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{redirectLoadBalancerName()})[0]);

            VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{redirectLoadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleForceHttpsRedirect, virtualServerRules[0][0]);

            lb.setHttpsRedirect(false);
            zxtmAdapter.updateHttpsRedirect(config, lb);

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()})[0]);
        }

    }

}