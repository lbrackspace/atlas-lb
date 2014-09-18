package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancer;

@RunWith(Enclosed.class)
public class DomainToDataModelCertificateMappingTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class DefaultMapping {
        private DozerBeanMapper mapper;
        private CertificateMapping certificateMapping;
        private org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping dataModelCertificateMapping;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            certificateMapping = createHydratedCertificateMapping();
        }

        @Test
        public void shouldMapAllAttributesExceptPrivateKey() {
            dataModelCertificateMapping = mapper.map(certificateMapping, org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping.class);

            Assert.assertNull(dataModelCertificateMapping.getPrivateKey());

            Assert.assertNotNull(dataModelCertificateMapping.getCertificate());
            Assert.assertNotNull(dataModelCertificateMapping.getIntermediateCertificate());
            Assert.assertNotNull(dataModelCertificateMapping.getId());
            Assert.assertNotNull(dataModelCertificateMapping.getHostName());
        }

        @Test
        public void shouldIgnoreNonNullLoadBalancer() {
            LoadBalancer lb = new LoadBalancer();
            certificateMapping.setLoadbalancer(lb);

            dataModelCertificateMapping = mapper.map(certificateMapping, org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping.class);
        }
    }

    public static class HideKeysAndCertificatesMapping {
        private DozerBeanMapper mapper;
        private CertificateMapping certificateMapping;

        private org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping dataModelCertificateMapping;

        @Before
        public void setUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            certificateMapping = createHydratedCertificateMapping();
        }

        @Test
        public void shouldHideKeysAndCertificates() {
            dataModelCertificateMapping = mapper.map(certificateMapping, org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping.class, "HIDE_KEY_AND_CERTS");

            Assert.assertNull(dataModelCertificateMapping.getPrivateKey());
            Assert.assertNull(dataModelCertificateMapping.getCertificate());
            Assert.assertNull(dataModelCertificateMapping.getIntermediateCertificate());

            Assert.assertNotNull(dataModelCertificateMapping.getId());
            Assert.assertNotNull(dataModelCertificateMapping.getHostName());
        }

        @Test
        public void shouldIgnoreNonNullLoadBalancer() {
            LoadBalancer lb = new LoadBalancer();
            certificateMapping.setLoadbalancer(lb);

            dataModelCertificateMapping = mapper.map(certificateMapping, org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping.class, "HIDE_KEY_AND_CERTS");
        }
    }

    static private CertificateMapping createHydratedCertificateMapping() {
        CertificateMapping certificateMapping = new CertificateMapping();

        certificateMapping.setId(1);
        certificateMapping.setCertificate("certificate");
        certificateMapping.setIntermediateCertificate("intermediate certificate");
        certificateMapping.setPrivateKey("private key");
        certificateMapping.setHostName("host name");

        return certificateMapping;
    }
}
