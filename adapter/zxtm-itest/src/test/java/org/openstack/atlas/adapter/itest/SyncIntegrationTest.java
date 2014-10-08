package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.Certificate;
import com.zxtm.service.client.CatalogMonitorType;
import com.zxtm.service.client.VirtualServerBasicInfo;
import com.zxtm.service.client.VirtualServerProtocol;
import com.zxtm.service.client.VirtualServerRule;
import com.zxtm.service.client.VirtualServerRuleRunFlag;
import com.zxtm.service.client.VirtualServerSSLSite;

import org.apache.axis.types.UnsignedInt;

import java.rmi.RemoteException;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.junit.experimental.runners.Enclosed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Enclosed.class)
public class SyncIntegrationTest extends ZeusTestBase {
    public static class SyncBasicLoadBalancer {
        String[] lbName;
        String epContent = "test";
        String epName;
        final UnsignedInt MAX_CONN = new UnsignedInt(23);

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            setupSimpleLoadBalancer();
        }

        @Before
        public void setUp() throws Exception {
            lbName = new String[]{loadBalancerName()};
            epName = String.format("%s_error.html", loadBalancerName());
            setupIvars();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void testSyncPort() {
            try {
                getServiceStubs().getVirtualServerBinding().setPort(lbName, new UnsignedInt[]{new UnsignedInt(12321)});

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertEquals(getServiceStubs().getVirtualServerBinding().getPort(lbName)[0], new UnsignedInt(lb.getPort()));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncProtocol() {
            try {
                lb.setProtocol(LoadBalancerProtocol.HTTPS);
                lb.setPort(443);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertEquals(getServiceStubs().getVirtualServerBinding().getProtocol(lbName)[0], VirtualServerProtocol.https);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncRuleForHTTP() {
            try {
                lb.setProtocol(LoadBalancerProtocol.HTTPS);
                lb.setPort(443);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertEquals(getServiceStubs().getVirtualServerBinding().getProtocol(lbName)[0], VirtualServerProtocol.https);

                lb.setProtocol(LoadBalancerProtocol.HTTP);
                lb.setPort(80);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertEquals(getServiceStubs().getVirtualServerBinding().getProtocol(lbName)[0], VirtualServerProtocol.http);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getRules(lbName).length > 0);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncNodes() {
            try {
                int check = 0;
                String[][] nodes = new String [1][1];
                nodes[0][0] = "10.0.0.1:80";
                getServiceStubs().getPoolBinding().setNodes(lbName, nodes);
                Assert.assertTrue(getServiceStubs().getPoolBinding().getNodes(lbName)[0].length != lb.getNodes().size());

                zxtmAdapter.updateLoadBalancer(config, lb);
                check += getServiceStubs().getPoolBinding().getNodes(lbName)[0].length;
                check += getServiceStubs().getPoolBinding().getDisabledNodes(lbName)[0].length;
                Assert.assertEquals(check, lb.getNodes().size());
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncConnectionLimit() {
            try {
                ConnectionLimit limit = new ConnectionLimit();
                limit.setMaxConnections(MAX_CONN.intValue());
                lb.setConnectionLimit(limit);
                zxtmAdapter.updateConnectionThrottle(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));

                getServiceStubs().getProtectionBinding().setMax1Connections(lbName, new UnsignedInt[]{new UnsignedInt(0)});
                Assert.assertTrue(!getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncAccessList() {
            try {
                String[][] addresses = new String[1][1];
                addresses[0] = new String[]{"10.0.0.1"};
                // This check is necessary to see if the protection class exists (due to previous tests)
                if (!Arrays.asList(getServiceStubs().getProtectionBinding().getProtectionNames()).contains(lbName[0])) {
                    getServiceStubs().getProtectionBinding().addProtection(lbName);
                }
                getServiceStubs().getProtectionBinding().setAllowedAddresses(lbName, addresses);
                getServiceStubs().getVirtualServerBinding().setProtection(lbName, lbName);
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length > 0);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length == 0);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncContentCaching() {
            try {
                VirtualServerRule[][] rules = new VirtualServerRule[][]{{
                        new VirtualServerRule(ZxtmAdapterImpl.CONTENT_CACHING, true, VirtualServerRuleRunFlag.run_every)}};
                getServiceStubs().getVirtualServerBinding().addRules(lbName, rules);
                getServiceStubs().getVirtualServerBinding().setWebcacheEnabled(lbName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncHalfClosed() {
            try {
                getServiceStubs().getVirtualServerBinding().setProxyClose(lbName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncAlgorithm() {
            try {
                LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.ROUND_ROBIN;
                if (lb.getAlgorithm().equals(algorithm)) {
                    algorithm = LoadBalancerAlgorithm.RANDOM;
                }
                zxtmAdapter.setLoadBalancingAlgorithm(config, lb.getId(), lb.getAccountId(), algorithm);
                Assert.assertEquals(getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(lbName)[0], ZxtmConversionUtils.mapAlgorithm(algorithm));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertEquals(getServiceStubs().getPoolBinding().getLoadBalancingAlgorithm(lbName)[0], ZxtmConversionUtils.mapAlgorithm(lb.getAlgorithm()));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncHealthMonitor() {
            try {
                String[][] monitors = new String[1][1];
                monitors[0] = lbName;
                getServiceStubs().getMonitorBinding().addMonitors(lbName);
                getServiceStubs().getMonitorBinding().setDelay(lbName, new UnsignedInt[]{new UnsignedInt(5)});
                getServiceStubs().getMonitorBinding().setTimeout(lbName, new UnsignedInt[]{new UnsignedInt(10)});
                getServiceStubs().getMonitorBinding().setFailures(lbName, new UnsignedInt[]{new UnsignedInt(3)});
                getServiceStubs().getMonitorBinding().setType(lbName, new CatalogMonitorType[]{CatalogMonitorType.connect});
                getServiceStubs().getPoolBinding().addMonitors(lbName, monitors);
                Assert.assertTrue(Arrays.asList(getServiceStubs().getPoolBinding().getMonitors(lbName)[0]).contains(lbName[0]));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getMonitorBinding().getCustomMonitorNames()).contains(lbName[0]));
                Assert.assertTrue(getServiceStubs().getPoolBinding().getMonitors(lbName)[0].length == 0);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncConnectionLogging() {
            try {
                lb.setConnectionLogging(true);
                zxtmAdapter.updateConnectionLogging(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);

                lb.setConnectionLogging(false);
                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncErrorPage() {
            try {
                UserPages pages = new UserPages();
                pages.setErrorpage(epContent);
                pages.setLoadbalancer(lb);
                lb.setUserPages(pages);

                zxtmAdapter.setErrorFile(config, lb, epContent);
                Assert.assertTrue(Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epName));

                getServiceStubs().getVirtualServerBinding().setErrorFile(lbName, new String[]{"Default"});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{epName});
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epName));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbName)[0].equals(epName));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epName));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    public static class SyncWithSSLMixMode {
        ZeusSslTermination zterm;
        String[] lbName;
        String[] lbsName;
        String epContent = "test";
        String epName;
        String epsName;
        final UnsignedInt MAX_CONN = new UnsignedInt(23);

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            setupSimpleLoadBalancer();
        }

        @Before
        public void setUp() throws Exception {
            lbName = new String[]{loadBalancerName()};
            lbsName = new String[]{secureLoadBalancerName()};
            epName = String.format("%s_error.html", loadBalancerName());
            epsName = String.format("%s_error.html", secureLoadBalancerName());
            setupIvars();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void testSyncConnectionLimitWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                ConnectionLimit limit = new ConnectionLimit();
                limit.setMaxConnections(MAX_CONN.intValue());
                lb.setConnectionLimit(limit);
                zxtmAdapter.updateConnectionThrottle(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbsName)).contains(lbName[0]));

                getServiceStubs().getProtectionBinding().setMax1Connections(lbName, new UnsignedInt[]{new UnsignedInt(0)});
                Assert.assertTrue(!getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncContentCachingWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                VirtualServerRule[][] rules = new VirtualServerRule[][]{{
                        new VirtualServerRule(ZxtmAdapterImpl.CONTENT_CACHING, true, VirtualServerRuleRunFlag.run_every)}};
                getServiceStubs().getVirtualServerBinding().addRules(lbName, rules);
                getServiceStubs().getVirtualServerBinding().addRules(lbsName, rules);
                getServiceStubs().getVirtualServerBinding().setWebcacheEnabled(lbName, new boolean[]{true});
                getServiceStubs().getVirtualServerBinding().setWebcacheEnabled(lbsName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbsName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbsName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncHalfClosedWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                getServiceStubs().getVirtualServerBinding().setProxyClose(lbName, new boolean[]{true});
                getServiceStubs().getVirtualServerBinding().setProxyClose(lbsName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbsName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbsName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncConnectionLoggingWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                lb.setConnectionLogging(true);
                zxtmAdapter.updateConnectionLogging(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbsName)[0]);

                lb.setConnectionLogging(false);
                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbsName)[0]);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncAccessListWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                String[][] addresses = new String[1][1];
                addresses[0] = new String[]{"10.0.0.1"};
                // This check is necessary to see if the protection class exists (due to previous tests)
                if (!Arrays.asList(getServiceStubs().getProtectionBinding().getProtectionNames()).contains(lbName[0])) {
                    getServiceStubs().getProtectionBinding().addProtection(lbName);
                }
                getServiceStubs().getProtectionBinding().setAllowedAddresses(lbName, addresses);
                getServiceStubs().getVirtualServerBinding().setProtection(lbName, lbName);
                getServiceStubs().getVirtualServerBinding().setProtection(lbsName, lbName);
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbsName)).contains(lbName[0]));
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length > 0);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length == 0);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncErrorPageWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                UserPages pages = new UserPages();
                pages.setErrorpage(epContent);
                pages.setLoadbalancer(lb);
                lb.setUserPages(pages);

                zxtmAdapter.setErrorFile(config, lb, epContent);
                List names = Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames());
                Assert.assertTrue(names.contains(epName));
                Assert.assertTrue(names.contains(epsName));

                getServiceStubs().getVirtualServerBinding().setErrorFile(lbName, new String[]{"Default"});
                getServiceStubs().getVirtualServerBinding().setErrorFile(lbsName, new String[]{"Default"});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{epName});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{epsName});
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epName));
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epsName));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbName)[0].equals(epName));
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbsName)[0].equals(epsName));
                names = Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames());
                Assert.assertTrue(names.contains(epName));
                Assert.assertTrue(names.contains(epsName));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncSslTerminationDeleteCert() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                getServiceStubs().getVirtualServerBinding().setSSLDecrypt(lbsName, new boolean[]{false});
                getServiceStubs().getZxtmCatalogSSLCertificatesBinding().deleteCertificate(lbsName);
                certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(!certFileNames.contains(secureLoadBalancerName()));

                zxtmAdapter.updateLoadBalancer(config, lb);
                certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getSSLDecrypt(lbsName)[0]);
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncSslTermModifySecureVirtualServer() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                getServiceStubs().getVirtualServerBinding().setSSLDecrypt(lbsName, new boolean[]{false});
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getSSLDecrypt(lbsName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getSSLDecrypt(lbsName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        private void addSslTermination() {
            zterm = new ZeusSslTermination();
            zterm.setCertIntermediateCert(testCert);
            SslTermination termination = new SslTermination();
            termination.setCertificate(testCert);
            termination.setPrivatekey(testKey);
            termination.setEnabled(true);
            termination.setSecurePort(443);
            termination.setSecureTrafficOnly(false);
            termination.setLoadbalancer(lb);
            zterm.setSslTermination(termination);
            lb.setSslTermination(termination);
        }
    }

    public static class SyncWithSSLOnly {
        ZeusSslTermination zterm;
        String[] lbName;
        String[] lbsName;
        String epContent = "test";
        String epName;
        String epsName;
        final UnsignedInt MAX_CONN = new UnsignedInt(23);

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            setupSimpleLoadBalancer();
        }

        @Before
        public void setUp() throws Exception {
            lbName = new String[]{loadBalancerName()};
            lbsName = new String[]{secureLoadBalancerName()};
            epName = String.format("%s_error.html", lbName[0]);
            epsName = String.format("%s_error.html", lbsName[0]);
            setupIvars();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void testSyncConnectionLimitWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                ConnectionLimit limit = new ConnectionLimit();
                limit.setMaxConnections(MAX_CONN.intValue());
                lb.setConnectionLimit(limit);
                zxtmAdapter.updateConnectionThrottle(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbsName)).contains(lbName[0]));

                getServiceStubs().getProtectionBinding().setMax1Connections(lbName, new UnsignedInt[]{new UnsignedInt(0)});
                Assert.assertTrue(!getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncContentCachingWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                VirtualServerRule[][] rules = new VirtualServerRule[][]{{
                        new VirtualServerRule(ZxtmAdapterImpl.CONTENT_CACHING, true, VirtualServerRuleRunFlag.run_every)}};
                getServiceStubs().getVirtualServerBinding().addRules(lbName, rules);
                getServiceStubs().getVirtualServerBinding().addRules(lbsName, rules);
                getServiceStubs().getVirtualServerBinding().setWebcacheEnabled(lbName, new boolean[]{true});
                getServiceStubs().getVirtualServerBinding().setWebcacheEnabled(lbsName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbsName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getWebcacheEnabled(lbsName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncHalfClosedWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                getServiceStubs().getVirtualServerBinding().setProxyClose(lbName, new boolean[]{true});
                getServiceStubs().getVirtualServerBinding().setProxyClose(lbsName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbsName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbsName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncConnectionLoggingWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                lb.setConnectionLogging(true);
                zxtmAdapter.updateConnectionLogging(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbsName)[0]);

                lb.setConnectionLogging(false);
                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbsName)[0]);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncAccessListWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                String[][] addresses = new String[1][1];
                addresses[0] = new String[]{"10.0.0.1"};
                // This check is necessary to see if the protection class exists (due to previous tests)
                if (!Arrays.asList(getServiceStubs().getProtectionBinding().getProtectionNames()).contains(lbName[0])) {
                    getServiceStubs().getProtectionBinding().addProtection(lbName);
                }
                getServiceStubs().getProtectionBinding().setAllowedAddresses(lbName, addresses);
                getServiceStubs().getVirtualServerBinding().setProtection(lbName, lbName);
                getServiceStubs().getVirtualServerBinding().setProtection(lbsName, lbName);
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbsName)).contains(lbName[0]));
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length > 0);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length == 0);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncErrorPageWithSSLEnabled() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                UserPages pages = new UserPages();
                pages.setErrorpage(epContent);
                pages.setLoadbalancer(lb);
                lb.setUserPages(pages);

                zxtmAdapter.setErrorFile(config, lb, epContent);
                List names = Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames());
                Assert.assertTrue(names.contains(epName));
                Assert.assertTrue(names.contains(epsName));

                getServiceStubs().getVirtualServerBinding().setErrorFile(lbName, new String[]{"Default"});
                getServiceStubs().getVirtualServerBinding().setErrorFile(lbsName, new String[]{"Default"});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{epName});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{epsName});
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epName));
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epsName));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbName)[0].equals(epName));
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbsName)[0].equals(epsName));
                names = Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames());
                Assert.assertTrue(names.contains(epName));
                Assert.assertTrue(names.contains(epsName));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncSslTerminationDeleteCert() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                getServiceStubs().getVirtualServerBinding().setSSLDecrypt(lbsName, new boolean[]{false});
                getServiceStubs().getZxtmCatalogSSLCertificatesBinding().deleteCertificate(lbsName);
                certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(!certFileNames.contains(secureLoadBalancerName()));

                zxtmAdapter.updateLoadBalancer(config, lb);
                certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getSSLDecrypt(lbsName)[0]);
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncSslTermModifySecureVirtualServer() {
            try {
                addSslTermination();

                zxtmAdapter.updateSslTermination(config, lb, zterm);
                List certFileNames = Arrays.asList(getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateNames());
                Assert.assertTrue(certFileNames.contains(secureLoadBalancerName()));

                getServiceStubs().getVirtualServerBinding().setSSLDecrypt(lbsName, new boolean[]{false});
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getSSLDecrypt(lbsName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getSSLDecrypt(lbsName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        private void addSslTermination() {
            zterm = new ZeusSslTermination();
            zterm.setCertIntermediateCert(testCert);
            SslTermination termination = new SslTermination();
            termination.setCertificate(testCert);
            termination.setPrivatekey(testKey);
            termination.setEnabled(true);
            termination.setSecurePort(443);
            termination.setSecureTrafficOnly(true);
            termination.setLoadbalancer(lb);
            zterm.setSslTermination(termination);
            lb.setSslTermination(termination);
        }

        @Test
        public void testSyncCertificateMappings() throws RollBackException, InsufficientRequestException, RemoteException {
            addSslTermination();
            zxtmAdapter.updateSslTermination(config, lb, zterm);

            CertificateMapping certMapping = new CertificateMapping();
            certMapping.setId(1234);
            certMapping.setPrivateKey(testKey);
            certMapping.setCertificate(testCert);
            certMapping.setHostName("sync.host-name.com");

            Set<CertificateMapping> certMappings = new HashSet<CertificateMapping>();
            certMappings.add(certMapping);
            lb.setCertificateMappings(certMappings);

            zxtmAdapter.updateLoadBalancer(config, lb);

            final Certificate[] certificateInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateInfo(new String[]{certificateName(certMapping.getId())});
            Assert.assertEquals(1, certificateInfo.length);

            final VirtualServerSSLSite[][] sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, sslSites.length);
            Assert.assertEquals(1, sslSites[0].length);
            Assert.assertEquals(certificateName(certMapping.getId()), sslSites[0][0].getCertificate());
            Assert.assertEquals(certMapping.getHostName(), sslSites[0][0].getDest_address());
        }
    }

    public static class SyncWithHTTPSRedirect {
        String[] lbName;
        String[] lbrName;
        String epContent = "test";
        String epName;
        String eprName;
        final UnsignedInt MAX_CONN = new UnsignedInt(23);

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars(LoadBalancerProtocol.HTTPS, 443);
            setupSimpleLoadBalancer();
        }

        @Before
        public void setUp() throws Exception {
            lbName = new String[]{loadBalancerName()};
            lbrName = new String[]{redirectLoadBalancerName()};
            epName = String.format("%s_error.html", lbName[0]);
            eprName = String.format("%s_error.html", lbrName[0]);
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void testSyncExtraHttpsRedirect() {
            try {
                getServiceStubs().getVirtualServerBinding().addVirtualServer(lbrName,
                        new VirtualServerBasicInfo[]{new VirtualServerBasicInfo(443, VirtualServerProtocol.https, lbName[0])});

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()).contains(lbrName[0]));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncConnectionLimitWithRedirectEnabled() {
            try {
                addHttpsRedirect();

                ConnectionLimit limit = new ConnectionLimit();
                limit.setMaxConnections(MAX_CONN.intValue());
                lb.setConnectionLimit(limit);
                zxtmAdapter.updateConnectionThrottle(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbrName)).contains(lbName[0]));

                getServiceStubs().getProtectionBinding().setMax1Connections(lbName, new UnsignedInt[]{new UnsignedInt(0)});
                Assert.assertTrue(!getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncHalfClosedWithRedirectEnabled() {
            try {
                addHttpsRedirect();

                getServiceStubs().getVirtualServerBinding().setProxyClose(lbName, new boolean[]{true});
                getServiceStubs().getVirtualServerBinding().setProxyClose(lbrName, new boolean[]{true});
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getProxyClose(lbrName)[0]);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getProxyClose(lbrName)[0]);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncConnectionLoggingWithRedirectEnabled() {
            try {
                addHttpsRedirect();

                lb.setConnectionLogging(true);
                zxtmAdapter.updateConnectionLogging(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getLogEnabled(lbrName)[0]);

                lb.setConnectionLogging(false);
                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbName)[0]);
                Assert.assertTrue(!getServiceStubs().getVirtualServerBinding().getLogEnabled(lbrName)[0]);
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncAccessListWithRedirectEnabled() {
            try {
                addHttpsRedirect();

                String[][] addresses = new String[1][1];
                addresses[0] = new String[]{"10.0.0.1"};
                // This check is necessary to see if the protection class exists (due to previous tests)
                if (!Arrays.asList(getServiceStubs().getProtectionBinding().getProtectionNames()).contains(lbName[0])) {
                    getServiceStubs().getProtectionBinding().addProtection(lbName);
                }
                getServiceStubs().getProtectionBinding().setAllowedAddresses(lbName, addresses);
                getServiceStubs().getVirtualServerBinding().setProtection(lbName, lbName);
                getServiceStubs().getVirtualServerBinding().setProtection(lbrName, lbName);
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbName)).contains(lbName[0]));
                Assert.assertTrue(Arrays.asList(getServiceStubs().getVirtualServerBinding().getProtection(lbrName)).contains(lbName[0]));
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length > 0);

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getProtectionBinding().getAllowedAddresses(lbName)[0].length == 0);
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        @Test
        public void testSyncErrorPageWithRedirectEnabled() {
            try {
                addHttpsRedirect();

                UserPages pages = new UserPages();
                pages.setErrorpage(epContent);
                pages.setLoadbalancer(lb);
                lb.setUserPages(pages);

                zxtmAdapter.setErrorFile(config, lb, epContent);
                List names = Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames());
                Assert.assertTrue(names.contains(epName));
                Assert.assertTrue(names.contains(eprName));

                getServiceStubs().getVirtualServerBinding().setErrorFile(lbName, new String[]{"Default"});
                getServiceStubs().getVirtualServerBinding().setErrorFile(lbrName, new String[]{"Default"});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{epName});
                getServiceStubs().getZxtmConfExtraBinding().deleteFile(new String[]{eprName});
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(epName));
                Assert.assertTrue(!Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames()).contains(eprName));

                zxtmAdapter.updateLoadBalancer(config, lb);
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbName)[0].equals(epName));
                Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getErrorFile(lbrName)[0].equals(eprName));
                names = Arrays.asList(getServiceStubs().getZxtmConfExtraBinding().getFileNames());
                Assert.assertTrue(names.contains(epName));
                Assert.assertTrue(names.contains(eprName));
            } catch(Exception e) {
                Assert.fail(e.getMessage());
            }
        }

        private void addHttpsRedirect() throws InsufficientRequestException, RemoteException, RollBackException {
            lb.setHttpsRedirect(true);
            zxtmAdapter.updateLoadBalancer(config, lb);
        }
    }
}