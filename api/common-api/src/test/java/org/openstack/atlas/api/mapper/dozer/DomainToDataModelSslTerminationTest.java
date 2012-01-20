package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.SslTermination;

@RunWith(Enclosed.class)
public class DomainToDataModelSslTerminationTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class When_mapping_ssl_termination_from_domain_to_datamodel {

        private DozerBeanMapper mapper;
        private SslTermination sslTermination;
        private org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination domainSsltermination;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
        }

        @Test
        public void shouldMapToNullWhenSessionPersistenceTypeIsNONE() {
            sslTermination = new SslTermination();
            sslTermination.setPrivatekey("AKey");
            sslTermination.setCertificate("aCert");
            sslTermination.setIntermediateCertificate("anIntermediateCert");
            sslTermination.setSecurePort(443);
            try {
                domainSsltermination = mapper.map(sslTermination, org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination.class);
            } catch (Exception e) {
                Assert.fail("Exception caused by ssl termination types being null");
            }

            Assert.assertEquals(sslTermination.getPrivatekey(), domainSsltermination.getPrivatekey());
            Assert.assertEquals(sslTermination.getCertificate(), domainSsltermination.getCertificate());
            Assert.assertEquals(sslTermination.getIntermediateCertificate(), domainSsltermination.getIntermediateCertificate());
            Assert.assertEquals(sslTermination.getSecurePort(), (long)domainSsltermination.getSecurePort());
            Assert.assertEquals(true, domainSsltermination.isEnabled());
            Assert.assertEquals(false, domainSsltermination.isSecureTrafficOnly());
        }
    }
}
