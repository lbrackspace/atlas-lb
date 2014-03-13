package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.*;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

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
                ArrayList<String> vsNames;

                vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

                Assert.assertTrue(vsNames.contains(loadBalancerName()));
                Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
                Assert.assertFalse(vsNames.contains(secureLoadBalancerName()));

                lb.setHttpsRedirect(true);
                zxtmAdapter.updateHttpsRedirect(config, lb);

                vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

                Assert.assertTrue(vsNames.contains(loadBalancerName()));
                Assert.assertTrue(vsNames.contains(redirectLoadBalancerName()));
                Assert.assertFalse(vsNames.contains(secureLoadBalancerName()));

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
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
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
                verifyAddRemoveHttpsSRedirect();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        @Ignore
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
                verifyAddRemoveHttpsSRedirect();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        private void setSslTermination() {
            String sVs = null;

            try {
                sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
            } catch (InsufficientRequestException e) {
                e.printStackTrace();
            }

            try {
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

                //Check to see if VS was created
                String[] virtualServers = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
                boolean doesExist = false;
                for (String vsName : virtualServers) {
                    if (vsName.equals(sVs)) {
                        doesExist = true;
                        break;
                    }
                }
                Assert.assertTrue(doesExist);

                String[] certificate = getServiceStubs().getVirtualServerBinding().getSSLCertificate(new String[]{sVs});
                Assert.assertEquals(sVs, certificate[0]);

                final VirtualServerBasicInfo[] serverBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{sVs});
                Assert.assertEquals(sslTermination.getSecurePort(), serverBasicInfos[0].getPort());
                Assert.assertEquals(true, lb.getProtocol().toString().equalsIgnoreCase(serverBasicInfos[0].getProtocol().toString()));
                Assert.assertEquals(ZxtmNameBuilder.genVSName(lb), serverBasicInfos[0].getDefault_pool());

                boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()});
                Assert.assertEquals(true, vsEnabled[0]);

                boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
                Assert.assertEquals(sslTermination.isEnabled(), vsNonSecureEnabled[0]);

                String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
                Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
                //removeSimpleLoadBalancer();
            }
        }

        private void verifyAddRemoveHttpsSRedirect() throws InsufficientRequestException, ZxtmRollBackException, RemoteException {
            ArrayList<String> vsNames;

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));

            lb.setHttpsRedirect(true);
            zxtmAdapter.updateHttpsRedirect(config, lb);

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertFalse(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertTrue(vsNames.contains(redirectLoadBalancerName()));

            VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{redirectLoadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length); // This is failing because the forwarded_port rule is never removed on repurpose
            Assert.assertEquals(ZxtmAdapterImpl.ruleForceHttpsRedirect, virtualServerRules[0][0]);

            lb.setHttpsRedirect(false);
            zxtmAdapter.updateHttpsRedirect(config, lb);

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
        }

    }

}