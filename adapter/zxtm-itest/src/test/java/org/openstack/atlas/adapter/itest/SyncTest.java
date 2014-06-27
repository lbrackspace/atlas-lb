package org.openstack.atlas.adapter.itest;

import org.apache.axis.types.UnsignedInt;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;

import java.util.List;

public class SyncTest extends ZeusTestBase {

    ZeusSslTermination zterm;
    String[] lbName;
    String[] lbsName;
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
        lbsName = new String[]{secureLoadBalancerName()};
        epName = String.format("%s_error.html", loadBalancerName());
        setupIvars();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void testSyncConnectionLimit() {
        try {
            ConnectionLimit limit = new ConnectionLimit();
            limit.setMaxConnections(MAX_CONN.intValue());
            lb.setConnectionLimit(limit);
            zxtmAdapter.updateConnectionThrottle(config, lb);
            Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));

            getServiceStubs().getProtectionBinding().setMax1Connections(lbName, new UnsignedInt[]{new UnsignedInt(0)});
            Assert.assertTrue(!getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));

            zxtmAdapter.updateLoadBalancer(config, lb);
            Assert.assertTrue(getServiceStubs().getProtectionBinding().getMax1Connections(lbName)[0].equals(MAX_CONN));
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