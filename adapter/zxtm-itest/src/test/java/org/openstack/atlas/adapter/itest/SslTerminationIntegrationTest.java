package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.ObjectDoesNotExist;
import com.zxtm.service.client.VirtualServerBasicInfo;
import com.zxtm.service.client.VirtualServerLocationDefaultRewriteMode;
import com.zxtm.service.client.VirtualServerRule;
import org.apache.axis.types.UnsignedInt;
import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;

public class SslTerminationIntegrationTest extends ZeusTestBase {
    //TODO: robustoize it...

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @After
    public void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void testSSlTerminationOperations() {
        setSslTermination();
        updateSslTermination();
        deleteSslTermination();
    }

    @Test
    public void testSSlTerminationOperationsWhenUpdatingLBAttributes() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        setSslTermination();
        updateLoadBalancerAttributes();
    }

    @Ignore
    @Test
    public void testWhenAddingRateLimitWithSslTermination() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        //Rate limiting is not handled by zxtm any more
        setRateLimitBeforeSsl();
        deleteRateLimit();
        setSslTermination();
        setRateLimit();
    }

    @Test
    public void testWhenAddingAccessListWith() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        verifyAccessListWithOutSsl();
        verifyDeleteAccessList();
        setSslTermination();
        verifyAccessListWithSsl();
    }

    @Test
    public void testErrorPageWhenCreatingSslTermination() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        verifyDeleteErrorPage();
        verifyErrorPage();
    }

    @Test
    public void testConnectionThrottleWhenCreatingSslTermination() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        verifyConnectionThrottle();
    }

    @Test
    public void shouldPassIfCertificateIsRemovedWithSecureVSStillThere() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        setSslTermination();
        updateSslTermination();
        deleteCertificate();
    }

    @Test
    public void verifyHostHeaderRewriteIsNever() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        verifyHostHeaderRewrite();
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
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(443);
            sslTermination.setCertificate(testCert);
            sslTermination.setPrivatekey(testKey);

            ZeusCrtFile zeusCrtFile = new ZeusCrtFile();
            zeusCrtFile.setPublic_cert(testCert);
            zeusCrtFile.setPrivate_key(testKey);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testCert);
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

            boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{ZxtmNameBuilder.genVSName(lb)});
            Assert.assertEquals(true, vsEnabled[0]);

            boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
            Assert.assertEquals(sslTermination.isEnabled(), vsNonSecureEnabled[0]);

            String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
            Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyHostHeaderRewrite() {
        String sVs = null;

        try {
            sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(443);
            sslTermination.setCertificate(testCert);
            sslTermination.setPrivatekey(testKey);

            ZeusCrtFile zeusCrtFile = new ZeusCrtFile();
            zeusCrtFile.setPublic_cert(testCert);
            zeusCrtFile.setPrivate_key(testKey);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testCert);
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

            boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{ZxtmNameBuilder.genVSName(lb)});
            Assert.assertEquals(true, vsEnabled[0]);

            boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
            Assert.assertEquals(sslTermination.isEnabled(), vsNonSecureEnabled[0]);

            String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
            Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

            VirtualServerLocationDefaultRewriteMode[] vsRewrite = getServiceStubs().getVirtualServerBinding().getLocationDefaultRewriteMode(new String[]{sVs});
            Assert.assertEquals(VirtualServerLocationDefaultRewriteMode.never, vsRewrite[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void updateSslTermination() {
        String sVs = null;

        try {
            sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

        }

        try {
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(true);
            sslTermination.setEnabled(false);
            sslTermination.setSecurePort(500);
            sslTermination.setCertificate(testCert);
            sslTermination.setPrivatekey(testKey);

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(testCert);
            zeusCertFile.setPrivate_key(testKey);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testCert);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());

            zxtmAdapter.updateSslTermination(config, lb, zeusSslTermination);

            //Check to see if VS is still here
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

            boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{ZxtmNameBuilder.genVSName(lb)});
            Assert.assertEquals(false, vsEnabled[0]);

            boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
            Assert.assertEquals(sslTermination.isEnabled(), vsNonSecureEnabled[0]);

            String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
            Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void deleteSslTermination() {
        String sVs = null;

        try {
            sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            zxtmAdapter.removeSslTermination(config, lb);
            //Check to see if VS is gone
            String[] virtualServers = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
            boolean doesExist = false;
            for (String vsName : virtualServers) {
                if (vsName.equals(sVs)) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertFalse(doesExist);

            //Check to see if original VS is still here
            String[] virtualServers2 = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
            boolean doesExist1 = false;
            for (String vsName : virtualServers2) {
                if (vsName.equals(ZxtmNameBuilder.genVSName(lb))) {
                    doesExist1 = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist1);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void updateLoadBalancerAttributes() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        //port
        try {
            zxtmAdapter.updatePort(config, lb.getId(), lb.getAccountId(), 8080);

            final VirtualServerBasicInfo[] virtualServerBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos.length);
            Assert.assertEquals(8080, virtualServerBasicInfos[0].getPort());

            //Ports are seperate for vs's
            final VirtualServerBasicInfo[] virtualServerBasicInfos2 = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, virtualServerBasicInfos2.length);
            Assert.assertEquals(443, virtualServerBasicInfos2[0].getPort());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }

        //logging
        try {
            lb.setConnectionLogging(Boolean.TRUE);
            zxtmAdapter.updateConnectionLogging(config, lb);

            Assert.assertEquals(true, getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{secureLoadBalancerName()})[0]);
            Assert.assertEquals(true, getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()})[0]);

            lb.setConnectionLogging(Boolean.FALSE);
            zxtmAdapter.updateConnectionLogging(config, lb);

            Assert.assertEquals(false, getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{secureLoadBalancerName()})[0]);
            Assert.assertEquals(false, getServiceStubs().getVirtualServerBinding().getLogEnabled(new String[]{loadBalancerName()})[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }

    private void verifyErrorPage() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        //setCustom Error file for lb with ssl termination
        try {
            String errorContent = "<html><body>ErrorFileContents</body></html>";

            zxtmAdapter.setErrorFile(config, lb, errorContent);
            Assert.assertEquals(loadBalancerName() + "_error.html", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()})[0]);

            //no ssl yet
            try {
                Assert.assertEquals("", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{secureLoadBalancerName()})[0]);
            } catch (ObjectDoesNotExist odne) {
                Assert.assertTrue("ssl not present", odne.getErrmsg().contains(secureLoadBalancerName()));
            }

            UserPages userPages = new UserPages();
            userPages.setErrorpage(errorContent);
            lb.setUserPages(userPages);

            setSslTermination();

            Assert.assertEquals(loadBalancerName() + "_error.html", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()})[0]);
            Assert.assertEquals(secureLoadBalancerName() + "_error.html", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{secureLoadBalancerName()})[0]);
            Assert.assertEquals(errorContent, new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFileName())));

            //remove error page
            zxtmAdapter.removeAndSetDefaultErrorFile(config, lb);
            Assert.assertEquals("Default", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()})[0]);
            Assert.assertEquals("Default", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{secureLoadBalancerName()})[0]);

            //set error file with ssl already there
            zxtmAdapter.setErrorFile(config, lb, errorContent);
            Assert.assertEquals(loadBalancerName() + "_error.html", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()})[0]);
            Assert.assertEquals(secureLoadBalancerName() + "_error.html", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{secureLoadBalancerName()})[0]);
            Assert.assertEquals(errorContent, new String(getServiceStubs().getZxtmConfExtraBinding().downloadFile(errorFileName())));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void verifyDeleteErrorPage() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        //setCustom Error file for lb with ssl termination
        try {
            String errorContent = "<html><body>ErrorFileContents</body></html>";

            zxtmAdapter.deleteErrorFile(config, lb);
            Assert.assertEquals("Default", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()})[0]);

            //no ssl yet
            try {
                Assert.assertEquals("Default", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{secureLoadBalancerName()})[0]);
            } catch (ObjectDoesNotExist odne) {
                Assert.assertTrue("ssl not present", odne.getErrmsg().contains(secureLoadBalancerName()));
            }

            //remove error page
            zxtmAdapter.removeAndSetDefaultErrorFile(config, lb);
            Assert.assertEquals("Default", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{loadBalancerName()})[0]);

            try {
                Assert.assertEquals("Default", getServiceStubs().getVirtualServerBinding().getErrorFile(new String[]{secureLoadBalancerName()})[0]);
            } catch (ObjectDoesNotExist odne) {
                Assert.assertTrue("ssl not present", odne.getErrmsg().contains(secureLoadBalancerName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void verifyConnectionThrottle() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        String[] vsName = new String[]{lb.getAccountId() + "_" + lb.getId()};
        String[] vsSslName = new String[]{lb.getAccountId() + "_" + lb.getId() + "_S"};
        try {
            ConnectionLimit throttle = new ConnectionLimit();
            throttle.setMaxConnectionRate(10);
            throttle.setMaxConnections(20);
            throttle.setMinConnections(40);
            throttle.setRateInterval(44);

            lb.setConnectionLimit(throttle);
            zxtmAdapter.updateConnectionThrottle(config, lb);
            Assert.assertEquals(new UnsignedInt(10), getServiceStubs().getProtectionBinding().getMaxConnectionRate(vsName)[0]);
            Assert.assertEquals(new UnsignedInt(20), getServiceStubs().getProtectionBinding().getMax1Connections(vsName)[0]);
            Assert.assertEquals(new UnsignedInt(40), getServiceStubs().getProtectionBinding().getMinConnections(vsName)[0]);
            Assert.assertEquals(new UnsignedInt(44), getServiceStubs().getProtectionBinding().getRateTimer(vsName)[0]);

            //set ssl
            setSslTermination();
            Assert.assertEquals(new UnsignedInt(10), getServiceStubs().getProtectionBinding().getMaxConnectionRate(vsSslName)[0]);
            Assert.assertEquals(new UnsignedInt(20), getServiceStubs().getProtectionBinding().getMax1Connections(vsSslName)[0]);
            Assert.assertEquals(new UnsignedInt(40), getServiceStubs().getProtectionBinding().getMinConnections(vsSslName)[0]);
            Assert.assertEquals(new UnsignedInt(44), getServiceStubs().getProtectionBinding().getRateTimer(vsSslName)[0]);

            //removing throttle
            zxtmAdapter.deleteConnectionThrottle(config, lb);
            Assert.assertEquals(new UnsignedInt(0), getServiceStubs().getProtectionBinding().getMaxConnectionRate(vsSslName)[0]);
            Assert.assertEquals(new UnsignedInt(0), getServiceStubs().getProtectionBinding().getMax1Connections(vsSslName)[0]);
            Assert.assertEquals(new UnsignedInt(0), getServiceStubs().getProtectionBinding().getMinConnections(vsSslName)[0]);
            Assert.assertEquals(new UnsignedInt(1), getServiceStubs().getProtectionBinding().getRateTimer(vsSslName)[0]);

            Assert.assertEquals(new UnsignedInt(0), getServiceStubs().getProtectionBinding().getMaxConnectionRate(vsName)[0]);
            Assert.assertEquals(new UnsignedInt(0), getServiceStubs().getProtectionBinding().getMax1Connections(vsName)[0]);
            Assert.assertEquals(new UnsignedInt(0), getServiceStubs().getProtectionBinding().getMinConnections(vsName)[0]);
            Assert.assertEquals(new UnsignedInt(1), getServiceStubs().getProtectionBinding().getRateTimer(vsName)[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void setRateLimit() {
        try {
            final Integer maxRequestsPerSecond = 1000;
            RateLimit rateLimit = new RateLimit();
            rateLimit.setExpirationTime(Calendar.getInstance());
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);

            zxtmAdapter.setRateLimit(config, lb, rateLimit);

            String[] rateNames = getServiceStubs().getZxtmRateCatalogService().getRateNames();
            boolean doesExist = false;
            for (String rateName : rateNames) {
                if (rateName.equals(rateLimitName())) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist);

            final UnsignedInt[] ratePerSecondList = getServiceStubs().getZxtmRateCatalogService().getMaxRatePerSecond(new String[]{rateLimitName()});
            Assert.assertEquals(new UnsignedInt(maxRequestsPerSecond), ratePerSecondList[0]);

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(3, virtualServerRules[0].length);

            for (VirtualServerRule rule : virtualServerRules[0]) {
                if (!(rule.equals(ZxtmAdapterImpl.ruleRateLimitHttp)) && !(rule.equals(ZxtmAdapterImpl.ruleXForwardedProto)) && !(rule.equals(ZxtmAdapterImpl.ruleXForwardedFor))) {
                    Assert.fail("None of the rules matched, test failed!...");
                }
            }
//            Assert.assertEquals(ZxtmAdapterImpl.ruleRateLimitHttp, virtualServerRules[0][1]);

            final VirtualServerRule[][] virtualServerRules1 = getServiceStubs().getVirtualServerBinding().getRules(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, virtualServerRules1.length);
            Assert.assertEquals(3, virtualServerRules1[0].length);
            for (VirtualServerRule rule : virtualServerRules[0]) {
                if (!rule.equals(ZxtmAdapterImpl.ruleRateLimitHttp) && !rule.equals(ZxtmAdapterImpl.ruleXForwardedProto) && !rule.equals(ZxtmAdapterImpl.ruleXForwardedFor)) {
                    Assert.fail("None of the rules matched, test failed!...");
                }
            }

//            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedProto, virtualServerRules1[0][1]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void verifyAccessListWithSsl() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            item1.setIpAddress("0.0.0.0/0");
            item2.setIpAddress("127.0.0.1");
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            zxtmAdapter.updateAccessList(config, lb);

            final String[][] bannedAddresses = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{protectionClassName()});
            Assert.assertEquals(1, bannedAddresses.length);
            Assert.assertEquals(1, bannedAddresses[0].length);
            Assert.assertEquals(item1.getIpAddress(), bannedAddresses[0][0]);

            final String[][] allowedAddresses = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{protectionClassName()});
            Assert.assertEquals(1, allowedAddresses.length);
            Assert.assertEquals(1, allowedAddresses[0].length);
            Assert.assertEquals(item2.getIpAddress(), allowedAddresses[0][0]);

            final String[][] bannedAddresses2 = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{secureProtectionClassName()});
            Assert.assertEquals(1, bannedAddresses2.length);
            Assert.assertEquals(1, bannedAddresses2[0].length);
            Assert.assertEquals(item1.getIpAddress(), bannedAddresses2[0][0]);

            final String[][] allowedAddresses2 = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{secureProtectionClassName()});
            Assert.assertEquals(1, allowedAddresses2.length);
            Assert.assertEquals(1, allowedAddresses2[0].length);
            Assert.assertEquals(item2.getIpAddress(), allowedAddresses2[0][0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void verifyDeleteAccessList() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            item1.setIpAddress("0.0.0.0/0");
            item2.setIpAddress("127.0.0.1");
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            zxtmAdapter.deleteAccessList(config, lb.getId(), lb.getAccountId());

            final String[][] bannedAddresses = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{protectionClassName()});
            Assert.assertEquals(1, bannedAddresses.length);
            Assert.assertEquals(0, bannedAddresses[0].length);

            final String[][] allowedAddresses = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{protectionClassName()});
            Assert.assertEquals(1, allowedAddresses.length);
            Assert.assertEquals(0, allowedAddresses[0].length);

            final String[][] bannedAddresses2 = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{secureProtectionClassName()});
            Assert.assertEquals(1, bannedAddresses2.length);
            Assert.assertEquals(0, bannedAddresses2[0].length);

            final String[][] allowedAddresses2 = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{secureProtectionClassName()});
            Assert.assertEquals(1, allowedAddresses2.length);
            Assert.assertEquals(0, allowedAddresses2[0].length);

        } catch (ObjectDoesNotExist odne) {
            Assert.assertTrue("ssl access list does not exist, expected, ignoring error", odne.getErrmsg().contains(lb.getAccountId() + "_" + lb.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void verifyAccessListWithOutSsl() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            item1.setIpAddress("0.0.0.0/0");
            item2.setIpAddress("127.0.0.1");
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            zxtmAdapter.updateAccessList(config, lb);

            final String[][] bannedAddresses = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{protectionClassName()});
            Assert.assertEquals(1, bannedAddresses.length);
            Assert.assertEquals(1, bannedAddresses[0].length);
            Assert.assertEquals(item1.getIpAddress(), bannedAddresses[0][0]);

            final String[][] allowedAddresses = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{protectionClassName()});
            Assert.assertEquals(1, allowedAddresses.length);
            Assert.assertEquals(1, allowedAddresses[0].length);
            Assert.assertEquals(item2.getIpAddress(), allowedAddresses[0][0]);

            final String[][] bannedAddresses2 = getServiceStubs().getProtectionBinding().getBannedAddresses(new String[]{secureProtectionClassName()});
            final String[][] allowedAddresses2 = getServiceStubs().getProtectionBinding().getAllowedAddresses(new String[]{secureProtectionClassName()});

        } catch (ObjectDoesNotExist odne) {
            Assert.assertTrue("ssl does not exist, expected, ignoring error...", odne.getErrmsg().contains(lb.getAccountId() + "_" + lb.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void setRateLimitBeforeSsl() throws RemoteException, InsufficientRequestException {
        try {
            final Integer maxRequestsPerSecond = 1000;
            RateLimit rateLimit = new RateLimit();
            rateLimit.setExpirationTime(Calendar.getInstance());
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);

            zxtmAdapter.setRateLimit(config, lb, rateLimit);

            String[] rateNames = getServiceStubs().getZxtmRateCatalogService().getRateNames();
            boolean doesExist = false;
            for (String rateName : rateNames) {
                if (rateName.equals(rateLimitName())) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist);

            final UnsignedInt[] ratePerSecondList = getServiceStubs().getZxtmRateCatalogService().getMaxRatePerSecond(new String[]{rateLimitName()});
            Assert.assertEquals(new UnsignedInt(maxRequestsPerSecond), ratePerSecondList[0]);

            final VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(3, virtualServerRules[0].length);
            for (VirtualServerRule rule : virtualServerRules[0]) {
                if (!(rule.equals(ZxtmAdapterImpl.ruleRateLimitHttp)) && !(rule.equals(ZxtmAdapterImpl.ruleXForwardedProto)) && !(rule.equals(ZxtmAdapterImpl.ruleXForwardedFor))) {
                    Assert.fail("None of the rules matched, test failed!...");
                }
            }
//            Assert.assertEquals(ZxtmAdapterImpl.ruleRateLimitHttp, virtualServerRules[0][1]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }

        try {
            final VirtualServerRule[][] virtualServerRules1 = getServiceStubs().getVirtualServerBinding().getRules(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, virtualServerRules1.length);
            Assert.assertEquals(2, virtualServerRules1[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleRateLimitHttp, virtualServerRules1[0][1]);
        } catch (ObjectDoesNotExist odne) {
            Assert.assertTrue("Ssl not present", odne.getErrmsg().contains(secureLoadBalancerName()));
        }

    }

    private void deleteRateLimit() {
        try {
            zxtmAdapter.deleteRateLimit(config, lb);
            String[] rateNames = getServiceStubs().getZxtmRateCatalogService().getRateNames();
            boolean doesExist = false;
            for (String rateName : rateNames) {
                if (rateName.equals(rateLimitName())) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertFalse(doesExist);

            boolean doesExist2 = false;
            for (String rateName : rateNames) {
                if (rateName.equals(secureLoadBalancerName())) { //the rate limit name...
                    doesExist2 = true;
                    break;
                }
            }
            Assert.assertFalse(doesExist2);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }

    private void deleteCertificate() {
        try {
            getServiceStubs().getVirtualServerBinding().setSSLCertificate(new String[]{secureLoadBalancerName()}, new String[]{""});
            getServiceStubs().getZxtmCatalogSSLCertificatesBinding().deleteCertificate(new String[]{secureLoadBalancerName()});
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();

        }
    }
}
