package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.util.debug.Debug;

@RunWith(Enclosed.class)
public class DomainToDataModelSslTerminationTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class When_mapping_ssl_termination_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private SslTermination domainSslTermination;
        private org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination apiSsltermination;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        }

        @Test
        public void shouldMapToNullWhenSessionPersistenceTypeIsNONE() {
            domainSslTermination = new SslTermination();
            domainSslTermination.setPrivatekey("AKey");
            domainSslTermination.setCertificate("aCert");
            domainSslTermination.setReEncryptionCertificateAuthority("aCA");
            domainSslTermination.setReEncryptionEnabled(Boolean.TRUE);
            domainSslTermination.setIntermediateCertificate("anIntermediateCert");
            domainSslTermination.setSecurePort(443);
            try {
                apiSsltermination = mapper.map(domainSslTermination, org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination.class);
            } catch (Exception e) {
                String exMsg = Debug.getExtendedStackTrace(e);
                Assert.fail(String.format("someException caused by converting domain to api SslTermination %s", exMsg));
            }

            Assert.assertEquals(domainSslTermination.getPrivatekey(), apiSsltermination.getPrivatekey());
            Assert.assertEquals(domainSslTermination.getCertificate(), apiSsltermination.getCertificate());
            Assert.assertEquals(domainSslTermination.getIntermediateCertificate(), apiSsltermination.getIntermediateCertificate());
            Assert.assertEquals(domainSslTermination.getSecurePort(), (long) apiSsltermination.getSecurePort());
            Assert.assertEquals(true, apiSsltermination.isEnabled());
            Assert.assertEquals(false, apiSsltermination.isSecureTrafficOnly());
            Assert.assertEquals(true, apiSsltermination.isReEncryptionEnabled());
            Assert.assertEquals(domainSslTermination.isReEncryptionEnabled(), apiSsltermination.isReEncryptionEnabled());
            Assert.assertEquals("aCA", apiSsltermination.getReEncryptionCertificateAuthority());
            Assert.assertEquals(domainSslTermination.getReEncryptionCertificateAuthority(), apiSsltermination.getReEncryptionCertificateAuthority());
        }
    }
}
