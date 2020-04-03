package org.openstack.atlas.adapter.itest;


import org.bouncycastle.asn1.x509.Certificate;
import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.protection.Protection;
import org.rackspace.vtm.client.protection.ProtectionConnectionRate;
import org.rackspace.vtm.client.protection.ProtectionProperties;
import org.rackspace.vtm.client.util.EnumFactory;
import org.rackspace.vtm.client.virtualserver.VirtualServer;
import org.rackspace.vtm.client.virtualserver.VirtualServerBasic;
import org.rackspace.vtm.client.virtualserver.VirtualServerServerCertHostMapping;
import org.rackspace.vtm.client.virtualserver.VirtualServerSsl;

import java.io.File;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;

public class SslTerminationITest extends VTMTestBase {

    private String normalName;
    private String secureName;


    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }


    @Before
    public void setupClass() throws Exception {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
        normalName = ZxtmNameBuilder.genVSName(lb);
        secureName = ZxtmNameBuilder.genSslVSName(lb);
    }

    @After
    public void destroy() {
        removeSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
        teardownEverything();
    }

    @Test
    public void testSSlTerminationOperations() {
        setSslTermination();
        updateSslTermination();
        deleteSslTermination();
    }

    @Test
    public void testSSlTerminationOperationsWhenUpdatingLBAttributes() throws Exception {
        setSslTermination();
        updateLoadBalancerAttributes();
    }

    @Test
    public void testAddingRateLimitWithSslTermination() throws Exception {
        setRateLimitBeforeSsl();
        deleteRateLimit();
        setSslTermination();
        setRateLimit();
    }

    @Test
    public void testDeletingOnlyAccessListWithSslTermination() throws Exception {
        // An updated to accesslist or connection throttle shouldn't remove
        // protection class. An explicit call to remove protection happens during lb removal
        verifyAccessListWithoutSsl();
        verifyDeleteAccessListWithoutConnectionThrottling();
    }

    @Test
    public void testAddingAccessListAndConnectionThrottlingWithSslTermination() throws Exception {
        ConnectionLimit cl = new ConnectionLimit();
        cl.setMaxConnectionRate(25);
        lb.setConnectionLimit(cl);
        vtmAdapter.updateConnectionThrottle(config, lb);
        verifyAccessListWithoutSsl();
        verifyDeleteAccessListWithConnectionThrottling();
        setSslTermination();
        verifyAccessListWithSsl();
    }

    @Test
    public void testCertificateMappings() throws Exception {
        verifyUpdateCertificateMappings();
        verifyRemoveCertificateMappings();
    }

    @Test
    public void testConnectionThrottleWhenCreatingSslTermination() throws Exception {
        verifyConnectionThrottle();
    }

    @Test
    public void testErrorPageWhenCreatingSslTermination() throws Exception {
        verifyDeleteErrorPage();
        verifyErrorPage();
    }

    @Test
    public void shouldPassIfCertificateIsRemovedWithSecureVSStillThere() throws Exception {
        setSslTermination();
        vtmClient.deleteKeypair(secureName);
        updateSslTermination();
    }

    @Test
    public void verifyHostHeaderRewriteIsNever() {
        verifyHostHeaderRewrite();
    }

    @Test
    public void veriyHttpsRedirect() {
        verifyHttpsRedirectWithSSLTermination();
    }

    @Test
    public void veriyHttpsRedirectSecureTrafficOnly() {
        verifyHttpsRedirectWithSSLTerminationSecureOnly();
    }

    @Test
    public void veriyHttpsRedirectUpdateSslTermination() {
        verifyHttpsRedirectUpdateSslTerm();
    }

    @Test
    public void veriyHttpsRedirectUpdateAndRevert() {
        verifyHttpsRedirectSslOnlyBackToDefault();
        verifyHttpsRedirectSslOnlyInverseBackToDefault();
    }

    private void setSslTermination() {
        boolean isSslTermEnabled = true;
        boolean allowSecureTrafficOnly = false;
        setSslTermination(isSslTermEnabled, allowSecureTrafficOnly);
    }

    private void updateSslTermination() {
        boolean isSslTermEnabled = true;
        boolean allowSecureTrafficOnly = true;
        setSslTermination(isSslTermEnabled, allowSecureTrafficOnly);
    }

    private void setSslTermination(boolean isSslTermEnabled, boolean allowSecureTrafficOnly) {
        try {
            boolean isVsEnabled = true;
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(allowSecureTrafficOnly);
            sslTermination.setEnabled(isSslTermEnabled);
            sslTermination.setSecurePort(VTMTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(VTMTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(VTMTestConstants.SSL_KEY);
            sslTermination.setTls10Enabled(true);
            sslTermination.setTls11Enabled(false);

            SslCipherProfile cipherProfile = new SslCipherProfile();
            cipherProfile.setCiphers(VTMTestConstants.CIPHER_LIST);
            cipherProfile.setComments("cipherpro1");
            cipherProfile.setName("datenameid");
            sslTermination.setCipherProfile(cipherProfile);
            sslTermination.setCipherList(cipherProfile.getCiphers());

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(VTMTestConstants.SSL_CERT);
            zeusCertFile.setPrivate_key(VTMTestConstants.SSL_KEY);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(VTMTestConstants.SSL_CERT);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());
            VirtualServer createdSecureVs = null;
            VirtualServer createdNormalVs = null;
            try {
                vtmAdapter.updateSslTermination(config, lb, zeusSslTermination);
                createdSecureVs = vtmClient.getVirtualServer(secureName);
                if (!lb.isSecureOnly() && (lb.getHttpsRedirect() != null && !lb.getHttpsRedirect())) {
                    createdNormalVs = vtmClient.getVirtualServer(normalName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Assert.assertNotNull(createdSecureVs);
            if (!lb.isSecureOnly() && (lb.getHttpsRedirect() != null && !lb.getHttpsRedirect())) {
                Assert.assertNotNull(createdNormalVs);
            }

            VirtualServerBasic secureBasic = createdSecureVs.getProperties().getBasic();
            Assert.assertEquals(VTMTestConstants.LB_SECURE_PORT, (int) secureBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(secureBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, secureBasic.getEnabled());
            Assert.assertEquals(normalName, secureBasic.getPool().toString());
            Assert.assertEquals(isSslTermEnabled, secureBasic.getSslDecrypt());
            Assert.assertEquals(VTMTestConstants.CIPHER_LIST, createdSecureVs.getProperties().getSsl().getCipherSuites());
            Assert.assertEquals(VirtualServerSsl.SupportTls1.ENABLED, createdSecureVs.getProperties().getSsl().getSupportTls1());
            Assert.assertEquals(VirtualServerSsl.SupportTls11.DISABLED, createdSecureVs.getProperties().getSsl().getSupportTls11());

            if (!lb.isSecureOnly() && (lb.getHttpsRedirect() != null && !lb.getHttpsRedirect())) {
                VirtualServerBasic normalBasic = createdNormalVs.getProperties().getBasic();
                Assert.assertEquals(VTMTestConstants.LB_PORT, (int) normalBasic.getPort());
                Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(normalBasic.getProtocol().toString()));
                if (allowSecureTrafficOnly) {
                    Assert.assertEquals(!isVsEnabled, normalBasic.getEnabled());
                } else {
                    Assert.assertEquals(isVsEnabled, normalBasic.getEnabled());
                }
                Assert.assertEquals(normalName, normalBasic.getPool().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void deleteSslTermination() {
        try {
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String vsSslName = ZxtmNameBuilder.genSslVSName(lb);
            try {
                VirtualServer createdNormalVs = vtmClient.getVirtualServer(normalName);
            } catch (Exception e) {
                if (!lb.isSecureOnly() && (lb.getHttpsRedirect() != null && !lb.getHttpsRedirect())) {
                    // default vs should have existed..
                    e.printStackTrace();
                    Assert.fail(e.getMessage());
                    removeSimpleLoadBalancer();
                }
                // expected
            }
            vtmAdapter.removeSslTermination(config, lb);
            List<String> names = new ArrayList<String>();
            for (Child child : vtmClient.getVirtualServers()) {
                names.add(child.getName());
            }
            Assert.assertFalse(names.contains(vsSslName));
            if (!lb.isSecureOnly() && (lb.getHttpsRedirect() != null && !lb.getHttpsRedirect())) {
                Assert.assertTrue(names.contains(vsName));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void updateLoadBalancerAttributes() {

        try {
            //Should us updateSslTermination
            int securePort = VTMTestConstants.LB_SECURE_PORT;
            int normalPort = VTMTestConstants.LB_PORT;
            boolean isConnectionLogging = true;
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String normalVsName = ZxtmNameBuilder.genVSName(lb);
            vtmAdapter.updateSslTermination(config, lb, new ZeusSslTermination());
            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            Assert.assertEquals(securePort, (int) createdSecureVs.getProperties().getBasic().getPort());
            VirtualServer createdNormalVs = vtmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(normalPort, (int) createdNormalVs.getProperties().getBasic().getPort());

            LoadBalancer nlb = new LoadBalancer();
            lb.setConnectionLogging(isConnectionLogging);
            nlb.setConnectionLogging(isConnectionLogging);
            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            createdNormalVs = vtmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
            Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());

            isConnectionLogging = false;
            lb.setConnectionLogging(isConnectionLogging);
            nlb.setConnectionLogging(isConnectionLogging);
            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            createdNormalVs = vtmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
            Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyUpdateCertificateMappings() {
        // Need ssltermination virtual server
        setSslTermination();

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String cname1 = ZxtmNameBuilder.generateCertificateName(lb.getId(), lb.getAccountId(), 1);
            String cname2 = ZxtmNameBuilder.generateCertificateName(lb.getId(), lb.getAccountId(), 2);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            VirtualServerSsl vssl = createdSecureVs.getProperties().getSsl();
            List<VirtualServerServerCertHostMapping> schm = vssl.getServerCertHostMapping();
            Assert.assertEquals(0, schm.size());

            Set<CertificateMapping> cmappings = new HashSet<>();
            CertificateMapping cm1 = new CertificateMapping();
            cm1.setIntermediateCertificate(VTMTestConstants.CMAPPINGS_INTERMEDIATES);
            cm1.setCertificate(VTMTestConstants.CMAPPINGS_CERT);
            cm1.setPrivateKey(VTMTestConstants.CMAPPINGS_KEY);
            cm1.setHostName("h1");
            cm1.setId(1);
//            cmappings.add(cm1);

            CertificateMapping cm2 = new CertificateMapping();
            cm2.setCertificate(VTMTestConstants.SSL_CERT);
            cm2.setPrivateKey(VTMTestConstants.SSL_KEY);
            cm2.setHostName("h2");
            cm2.setId(2);
            cmappings.add(cm2);
            lb.setCertificateMappings(cmappings);

            // Create one
            vtmAdapter.updateCertificateMapping(config, lb, cm2);
            createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            vssl = createdSecureVs.getProperties().getSsl();
            schm = vssl.getServerCertHostMapping();
            Assert.assertEquals(1, schm.size());
            Assert.assertEquals("h2", schm.get(0).getHost());
            Assert.assertEquals(cname2, schm.get(0).getCertificate());

            // Create the second
            cmappings.add(cm1);
            lb.setCertificateMappings(cmappings);
            vtmAdapter.updateCertificateMapping(config, lb, cm1);
            createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            vssl = createdSecureVs.getProperties().getSsl();
            schm = vssl.getServerCertHostMapping();
            Assert.assertEquals(2, schm.size());
            boolean failed = true;
            for (VirtualServerServerCertHostMapping chm : schm) {
                if (chm.getHost().equals("h1")) {
                    Assert.assertEquals(cname1, chm.getCertificate());
                    failed = false;
                }
            }
            if (failed) {
                Assert.fail("Second certificate mapping not found");
            }


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyRemoveCertificateMappings() {

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            VirtualServerSsl vssl = createdSecureVs.getProperties().getSsl();
            List<VirtualServerServerCertHostMapping> schm = vssl.getServerCertHostMapping();
            Assert.assertEquals(2, schm.size());

            Set<CertificateMapping> cmappings = new HashSet<>();
            CertificateMapping cm1 = new CertificateMapping();
            cm1.setIntermediateCertificate(VTMTestConstants.CMAPPINGS_INTERMEDIATES);
            cm1.setCertificate(VTMTestConstants.CMAPPINGS_CERT);
            cm1.setPrivateKey(VTMTestConstants.CMAPPINGS_KEY);
            cm1.setHostName("h1");
            cm1.setId(1);
            cmappings.add(cm1);

            CertificateMapping cm2 = new CertificateMapping();
            cm2.setCertificate(VTMTestConstants.SSL_CERT);
            cm2.setPrivateKey(VTMTestConstants.SSL_KEY);
            cm2.setHostName("h2");
            cm2.setId(2);
            cmappings.add(cm2);
            lb.setCertificateMappings(cmappings);

            // Remove one
            vtmAdapter.deleteCertificateMapping(config, lb, cm2);
            createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            vssl = createdSecureVs.getProperties().getSsl();
            schm = vssl.getServerCertHostMapping();
            Assert.assertEquals(1, schm.size());
            //check..

            // Remove the second
            vtmAdapter.deleteCertificateMapping(config, lb, cm1);
            createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            vssl = createdSecureVs.getProperties().getSsl();
            schm = vssl.getServerCertHostMapping();
            Assert.assertEquals(0, schm.size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }


    private void verifyHttpsRedirectWithSSLTermination() {
        // Need ssltermination virtual server
        setSslTermination();

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String redirectVsName = ZxtmNameBuilder.genRedirectVSName(lb);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

            lb.setHttpsRedirect(Boolean.TRUE); //enable redirect, which disables but doesnt delete the 'regular vs'
            vtmAdapter.updateLoadBalancer(config, lb, lb);

            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server succesfully removed", true);
            }
            VirtualServer redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());

            createdSecureVs= vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyHttpsRedirectWithSSLTerminationSecureOnly() {
        // Need ssltermination virtual server
        setSslTermination();

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String redirectVsName = ZxtmNameBuilder.genRedirectVSName(lb);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

            lb.setHttpsRedirect(Boolean.TRUE); //enable redirect, which disables but doesnt delete the 'regular vs'
            lb.getSslTermination().setSecureTrafficOnly(true);
            vtmAdapter.updateLoadBalancer(config, lb, lb);

            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server succesfully removed", true);
            }
            VirtualServer redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());

            createdSecureVs= vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyHttpsRedirectUpdateSslTerm() {
        // Need ssltermination virtual server
        setSslTermination();

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String redirectVsName = ZxtmNameBuilder.genRedirectVSName(lb);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

            lb.setHttpsRedirect(Boolean.TRUE); //enable redirect, which disables but doesnt delete the 'regular vs'
            vtmAdapter.updateLoadBalancer(config, lb, lb);
            VirtualServer updateVs;
            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server succesfully removed", true);
            }
            VirtualServer redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());

            lb.getSslTermination().setSecureTrafficOnly(true);
            setSslTermination(true, true);

            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server succesfully removed", true);
            }
            redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());

            createdSecureVs= vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyHttpsRedirectSslOnlyBackToDefault() {
        // Need ssltermination virtual server
        setSslTermination();

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String redirectVsName = ZxtmNameBuilder.genRedirectVSName(lb);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

            // Update settings and verify virtual servers are correctly created/enabled...
            lb.setHttpsRedirect(Boolean.TRUE); //enable redirect which disables and deletes the 'regular/default vs'
            vtmAdapter.updateLoadBalancer(config, lb, lb);
            VirtualServer updateVs;
            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server succesfully removed", true);
            }
            VirtualServer redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());


            // now update ssl termination to secure only, still with httpsredirect
            lb.getSslTermination().setSecureTrafficOnly(true);
            setSslTermination(true, true);

            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server removed", true);
            }
            redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());


            // Now undo the updates and verify the vs's are disabled/removed as expected
            lb.getSslTermination().setSecureTrafficOnly(false);
            setSslTermination(true, false);

            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                // httpsRedirect is still set at this point, this is expected
                Assert.assertTrue("Default virtual server removed", true);
            }
            redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());

            lb.setHttpsRedirect(Boolean.FALSE); //enable redirect, which disables but doesnt delete the 'regular vs'
            vtmAdapter.updateLoadBalancer(config, lb, lb);
            try {
                // Default virtual server should have been recreated and enabled
                updateVs = vtmClient.getVirtualServer(vsName);
                Assert.assertTrue(updateVs.getProperties().getBasic().getEnabled());
            } catch (Exception ex) {
                Assert.fail("Default virtual server should be created and enabled..");
            }
            try {
                redirectVs = vtmClient.getVirtualServer(redirectVsName);
                Assert.assertFalse(redirectVs.getProperties().getBasic().getEnabled());
            } catch (Exception ex) {
                Assert.assertTrue("Redirect virtual server removed", true);
            }

            // Ensure ssl virtual server is in tact...
            createdSecureVs= vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyHttpsRedirectSslOnlyInverseBackToDefault() {
        // Need ssltermination virtual server
        setSslTermination();

        try {
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String redirectVsName = ZxtmNameBuilder.genRedirectVSName(lb);

            VirtualServer createdSecureVs = vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

            // Update settings and verify virtual servers are correctly created/enabled...
            lb.setHttpsRedirect(Boolean.TRUE); //enable redirect which disables but doesnt delete the 'regular/default vs'
            vtmAdapter.updateLoadBalancer(config, lb, lb);
            VirtualServer updateVs;
            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server succesfully removed", true);
            }
            VirtualServer redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());


            // now update ssl termination to secure only, still with httpsredirect
            lb.getSslTermination().setSecureTrafficOnly(true);
            setSslTermination(true, true);

            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                Assert.assertTrue("Default virtual server removed", true);
            }
            redirectVs = vtmClient.getVirtualServer(redirectVsName);
            Assert.assertTrue(redirectVs.getProperties().getBasic().getEnabled());


            // Now undo the updates and verify the vs's are disabled/removed as expected
            lb.setHttpsRedirect(Boolean.FALSE); //enable redirect, which disables but doesnt delete the 'regular vs'
            vtmAdapter.updateLoadBalancer(config, lb, lb);
            try {
                vtmClient.getVirtualServer(vsName);
            } catch (Exception ex) {
                // secureTrafficOnly is still set at this point, this is expected
                Assert.assertTrue("Default virtual server removed", true);
            }
            try {
                redirectVs = vtmClient.getVirtualServer(redirectVsName);
                Assert.assertFalse(redirectVs.getProperties().getBasic().getEnabled());
            } catch (Exception ex) {
                Assert.assertTrue("Redirect virtual server removed", true);
            }

            lb.getSslTermination().setSecureTrafficOnly(false);
            setSslTermination(true, false);

            try {
                // Default virtual server should have been recreated and enabled
                updateVs = vtmClient.getVirtualServer(vsName);
                Assert.assertTrue(updateVs.getProperties().getBasic().getEnabled());
            } catch (Exception ex) {
                Assert.fail("Default virtual server should be created and enabled..");
            }

            // Ensure ssl virtual server is in tact...
            createdSecureVs= vtmClient.getVirtualServer(secureVsName);
            Assert.assertTrue(createdSecureVs.getProperties().getBasic().getEnabled());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void setRateLimit() {
        try {
            int maxRequestsPerSecond = 1000;
            String ticketComment = "HI";
            RateLimit rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);
            Ticket ticket = new Ticket();
            ticket.setComment(ticketComment);
            rateLimit.setTicket(ticket);
            vtmAdapter.setRateLimit(config, lb, rateLimit);


            Bandwidth createdNormalBandwidth = vtmClient.getBandwidth(normalName);
            Assert.assertNotNull(createdNormalBandwidth);
            Assert.assertEquals(maxRequestsPerSecond, (int) createdNormalBandwidth.getProperties().getBasic().getMaximum());
            Assert.assertEquals(ticketComment, createdNormalBandwidth.getProperties().getBasic().getNote());

            VirtualServer createdServer = vtmClient.getVirtualServer(normalName);
            Assert.assertTrue(createdServer.getProperties().getHttp().getAddXForwardedFor());
            Assert.assertTrue(createdServer.getProperties().getHttp().getAddXForwardedProto());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }


    }

    private void setRateLimitBeforeSsl() {
        try {
            int maxRequestsPerSecond = 1000;
            String ticketComment = "HI";
            RateLimit rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);
            Ticket ticket = new Ticket();
            ticket.setComment(ticketComment);
            rateLimit.setTicket(ticket);
            vtmAdapter.setRateLimit(config, lb, rateLimit);

            Bandwidth createdNormalBandwidth = vtmClient.getBandwidth(normalName);
            Assert.assertNotNull(createdNormalBandwidth);
            Assert.assertEquals(maxRequestsPerSecond, (int) createdNormalBandwidth.getProperties().getBasic().getMaximum());
            Assert.assertEquals(ticketComment, createdNormalBandwidth.getProperties().getBasic().getNote());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void deleteRateLimit() throws Exception {
        Boolean notFound = false;
        vtmAdapter.deleteRateLimit(config, lb);
        try {
            vtmClient.getBandwidth(normalName);
        } catch (VTMRestClientObjectNotFoundException notFoundException) {
            notFound = true;
        }
        Assert.assertTrue(notFound);
    }

    private void connectionThrottleHelper(String vsName, int maxConnectionRate, int maxConnections,
                                          int minConnections, int rateInterval, int expectedMax10) {
        try {
            vtmAdapter.updateConnectionThrottle(config, lb);


            Protection protection = vtmClient.getProtection(vsName);

            Assert.assertNotNull(protection);
            ProtectionConnectionRate createdThrottle = protection.getProperties().getConnectionRate();
            Assert.assertEquals(0, (int) createdThrottle.getMaxConnectionRate());
            // Max1 is only used, rest default to 0 or 1
            Assert.assertEquals(1, (int) createdThrottle.getRateTimer());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void errorPageHelper(String expectedContent) {
        try {
            String normalErrorFileName = vtmClient.getVirtualServer(normalName).getProperties().getConnectionErrors().getErrorFile();
//            Assert.assertEquals(normalName + "_error.html", normalErrorFileName);
            File normalFile = vtmClient.getExtraFile(normalErrorFileName);
            Scanner reader = new Scanner(normalFile);
            String content = "";
            while (reader.hasNextLine()) content += reader.nextLine();
            reader.close();
            Assert.assertEquals(content, expectedContent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyHostHeaderRewrite() {
        try {
            boolean allowSecureTrafficOnly = false;
            boolean isSslTermEnabled = true;
            setSslTermination(isSslTermEnabled, allowSecureTrafficOnly);
            VirtualServer createdVs = null;
            createdVs = vtmClient.getVirtualServer(ZxtmNameBuilder.genSslVSName(lb));


            Assert.assertEquals(EnumFactory.AcceptFrom.NEVER.toString(),
                    createdVs.getProperties().getHttp().getLocationRewrite().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyErrorPage() {
        try {
            String errorContent = "HI";
            String errorFileNormalName = normalName + "_error.html";

            LoadBalancer nlb = new LoadBalancer();
            UserPages up = new UserPages();
            up.setErrorpage(errorContent);
            nlb.setUserPages(up);

            lb.getUserPages().setErrorpage(errorFileNormalName);
            nlb.getUserPages().setErrorpage(errorFileNormalName);
            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            vtmAdapter.setErrorFile(config, lb, errorContent);
            errorPageHelper(errorContent);
            lb.getUserPages().setErrorpage(null);
            nlb.getUserPages().setErrorpage(null);

            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            vtmAdapter.setErrorFile(config, lb, "Default");
            //TODO: wont have a file for default, assert just that the name is default
//            errorPageHelper("Default");
            lb.getUserPages().setErrorpage("Default");
            nlb.getUserPages().setErrorpage("Default");
            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            vtmAdapter.setErrorFile(config, lb, errorContent);
            errorPageHelper(errorContent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyDeleteErrorPage() {
        String errorContent = "HI";
        String errorFileNormalName = normalName + "_error.html";
        try {
            UserPages userPages = new UserPages();
            userPages.setLoadbalancer(lb);
            userPages.setErrorpage(errorContent);

            LoadBalancer nlb = new LoadBalancer();
            lb.setUserPages(userPages);
            nlb.setUserPages(userPages);
            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            vtmAdapter.setErrorFile(config, lb, errorContent);
            errorPageHelper(errorContent);
            lb.getUserPages().setErrorpage(null);
            nlb.getUserPages().setErrorpage(null);
            vtmAdapter.updateLoadBalancer(config, lb, nlb);
            vtmAdapter.setErrorFile(config, lb, "Default");
//            errorPageHelper("Default");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyConnectionThrottle() {
        try {
            ConnectionLimit throttle = new ConnectionLimit();
            int maxConnectionRate = 10;
            int maxConnections = 20;
            int minConnections = 40;
            int rateInterval = 44;
            int expectedMax10 = maxConnections * 10;

            throttle.setMaxConnectionRate(maxConnectionRate);
            throttle.setMaxConnections(maxConnections);
            throttle.setMinConnections(minConnections);
            throttle.setRateInterval(rateInterval);

            lb.setConnectionLimit(throttle);

            setSslTermination();
            connectionThrottleHelper(normalName, maxConnectionRate, maxConnections, minConnections, rateInterval, expectedMax10);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyDeleteAccessListWithoutConnectionThrottling() throws Exception {
        verifyAccessListWithSsl();
        List<Integer> deletionList = new ArrayList<Integer>();
        for (AccessList item : lb.getAccessLists()) {
            deletionList.add(item.getId());
        }
        vtmAdapter.deleteAccessList(config, lb, deletionList);
        Protection normalProtection = vtmClient.getProtection(normalName);
        Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getBanned().isEmpty());
        Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getAllowed().isEmpty());
        Assert.assertEquals(Integer.valueOf(0), normalProtection.getProperties().getConnectionRate().getMaxConnectionRate());
        Assert.assertEquals(Integer.valueOf(1), normalProtection.getProperties().getConnectionRate().getRateTimer());
    }

    private void verifyAccessListWithSsl() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            String ipAddressOne = "0.0.0.0/0";
            String ipAddressTwo = "127.0.0.1";
            item1.setIpAddress(ipAddressOne);
            item2.setIpAddress(ipAddressTwo);
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            vtmAdapter.updateAccessList(config, lb);

            Protection normalProtection = vtmClient.getProtection(normalName);
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getAllowed().contains(ipAddressTwo));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyDeleteAccessListWithConnectionThrottling() {
        try {
            verifyAccessListWithSsl();
            List<Integer> deletionList = new ArrayList<Integer>();
            for (AccessList item : lb.getAccessLists()) {
                deletionList.add(item.getId());
            }
            vtmAdapter.deleteAccessList(config, lb, deletionList);
            Protection protection = vtmClient.getProtection(normalName);
            ProtectionProperties properties = protection.getProperties();
            Assert.assertTrue(properties.getAccessRestriction().getAllowed().isEmpty());
            Assert.assertTrue(properties.getAccessRestriction().getBanned().isEmpty());
            Assert.assertEquals(Integer.valueOf(0), properties.getConnectionRate().getMaxConnectionRate());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyAccessListWithoutSsl() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            String ipAddressOne = "0.0.0.0/0";
            String ipAddressTwo = "127.0.0.1";
            item1.setIpAddress(ipAddressOne);
            item2.setIpAddress(ipAddressTwo);
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            vtmAdapter.updateAccessList(config, lb);
            Protection normalProtection = vtmClient.getProtection(normalName);
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getAllowed().contains(ipAddressTwo));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    public static void removeSimpleLoadBalancer() {
        try {
            vtmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            String output = "Failure to delete load balancer:\n";
            for (StackTraceElement line : e.getStackTrace()) {
                output += line.toString() + "\n";
            }
            System.err.println(output);
        }
    }
}
