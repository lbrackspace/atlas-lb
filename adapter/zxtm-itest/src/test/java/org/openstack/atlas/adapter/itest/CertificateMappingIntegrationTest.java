package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.Certificate;
import com.zxtm.service.client.ObjectDoesNotExist;
import com.zxtm.service.client.VirtualServerSSLSite;
import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.CertificateMapping;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public class CertificateMappingIntegrationTest extends ZeusTestBase {

    private CertificateMapping certificateMapping;
    private CertificateMapping certificateMapping2;
    private static Integer CERTIFICATE_MAPPING_ID_1 = 100;
    private static Integer CERTIFICATE_MAPPING_ID_2 = 101;
    private static String CERTIFICATE_MAPPING_HOST_NAME_1 = "1-integration.test.com";
    private static String CERTIFICATE_MAPPING_HOST_NAME_2 = "2-integration.test.com";

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
        setSslTermination();
    }

    @Before
    public void standUp() {
        certificateMapping = new CertificateMapping();
        certificateMapping.setId(CERTIFICATE_MAPPING_ID_1);
        certificateMapping.setPrivateKey(testKey);
        certificateMapping.setCertificate(testCert);
        certificateMapping.setHostName(CERTIFICATE_MAPPING_HOST_NAME_1);

        certificateMapping2 = new CertificateMapping();
        certificateMapping2.setId(CERTIFICATE_MAPPING_ID_2);
        certificateMapping2.setPrivateKey(testKey);
        certificateMapping2.setCertificate(testCert);
        certificateMapping2.setHostName(CERTIFICATE_MAPPING_HOST_NAME_2);

        Set<CertificateMapping> certificateMappings = new HashSet<CertificateMapping>();
        certificateMappings.add(certificateMapping);
        certificateMappings.add(certificateMapping2);
        lb.setCertificateMappings(certificateMappings);
    }

    @AfterClass
    public static void tearDownClass() throws InsufficientRequestException, RemoteException {
        removeSimpleLoadBalancer();
        verifyCertificateIsDeleted(CERTIFICATE_MAPPING_ID_1);
        verifyCertificateIsDeleted(CERTIFICATE_MAPPING_ID_2);
    }

    @Test
    public void verifyCertificateAndMappingAreCreated() throws RollBackException, InsufficientRequestException, RemoteException {
        zxtmAdapter.updateCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);

        verifyCertificateExists(certificateMapping);

        final VirtualServerSSLSite[][] sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(1, sslSites[0].length);
        Assert.assertEquals(certificateName(certificateMapping.getId()), sslSites[0][0].getCertificate());
        Assert.assertEquals(certificateMapping.getHostName(), sslSites[0][0].getDest_address());

        zxtmAdapter.deleteCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);
    }

    @Test
    public void verifyWhenUpdatingHostNameOnly() throws RollBackException, InsufficientRequestException, RemoteException {
        zxtmAdapter.updateCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);

        verifyCertificateExists(certificateMapping);

        VirtualServerSSLSite[][] sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(1, sslSites[0].length);
        Assert.assertEquals(certificateName(certificateMapping.getId()), sslSites[0][0].getCertificate());
        Assert.assertEquals(certificateMapping.getHostName(), sslSites[0][0].getDest_address());

        certificateMapping.setHostName("new-integration.test.com");
        zxtmAdapter.updateCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);

        verifyCertificateExists(certificateMapping);

        sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(1, sslSites[0].length);
        Assert.assertEquals(certificateName(certificateMapping.getId()), sslSites[0][0].getCertificate());
        Assert.assertEquals(certificateMapping.getHostName(), sslSites[0][0].getDest_address());

        zxtmAdapter.deleteCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);
    }

    @Test
    public void verifyWhenAddingTwoSeparateMappings() throws RollBackException, InsufficientRequestException, RemoteException {
        zxtmAdapter.updateCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);

        verifyCertificateExists(certificateMapping);

        VirtualServerSSLSite[][] sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(1, sslSites[0].length);
        Assert.assertEquals(certificateName(certificateMapping.getId()), sslSites[0][0].getCertificate());
        Assert.assertEquals(certificateMapping.getHostName(), sslSites[0][0].getDest_address());

        zxtmAdapter.updateCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping2);

        verifyCertificateExists(certificateMapping2);

        sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(2, sslSites[0].length);

        for (VirtualServerSSLSite sslSite : sslSites[0]) {
            Assert.assertTrue(certificateName(certificateMapping.getId()).equals(sslSite.getCertificate())
                    || certificateName(certificateMapping2.getId()).equals(sslSite.getCertificate()));
            Assert.assertTrue(certificateMapping.getHostName().equals(sslSite.getDest_address())
                    || certificateMapping2.getHostName().equals(sslSite.getDest_address()));
        }

        zxtmAdapter.deleteCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);
        zxtmAdapter.deleteCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping2);
    }

    @Test
    public void verifyWhenAddingThenDeletingMapping() throws RollBackException, InsufficientRequestException, RemoteException {
        zxtmAdapter.updateCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);

        verifyCertificateExists(certificateMapping);

        VirtualServerSSLSite[][] sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(1, sslSites[0].length);
        Assert.assertEquals(certificateName(certificateMapping.getId()), sslSites[0][0].getCertificate());
        Assert.assertEquals(certificateMapping.getHostName(), sslSites[0][0].getDest_address());

        zxtmAdapter.deleteCertificateMapping(config, lb.getId(), lb.getAccountId(), certificateMapping);

        sslSites = getServiceStubs().getVirtualServerBinding().getSSLSites(new String[]{secureLoadBalancerName()});
        Assert.assertEquals(1, sslSites.length);
        Assert.assertEquals(0, sslSites[0].length);

        verifyCertificateIsDeleted(certificateMapping.getId());
    }

    private void verifyCertificateExists(CertificateMapping certMapping) throws RemoteException, InsufficientRequestException {
        final Certificate[] certificateInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateInfo(new String[]{certificateName(certMapping.getId())});
        Assert.assertEquals(1, certificateInfo.length);
    }

    private static void verifyCertificateIsDeleted(Integer certMappingId) throws RemoteException, InsufficientRequestException {
        boolean isDeleted = false;

        try {
            getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getCertificateInfo(new String[]{certificateName(certMappingId)});
        } catch(ObjectDoesNotExist odne) {
            isDeleted = true;
        }

        Assert.assertTrue(isDeleted);
    }
}
