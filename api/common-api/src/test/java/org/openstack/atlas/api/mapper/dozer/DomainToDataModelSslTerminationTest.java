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
        private org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination dataModelSslTermination;

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
                dataModelSslTermination = mapper.map(sslTermination, org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination.class);
            } catch (Exception e) {
                Assert.fail("Exception caused by ssl termination types being null");
            }

            Assert.assertNull(dataModelSslTermination.getPrivatekey());
            Assert.assertEquals(sslTermination.getCertificate(), dataModelSslTermination.getCertificate());
            Assert.assertEquals(sslTermination.getIntermediateCertificate(), dataModelSslTermination.getIntermediateCertificate());
            Assert.assertEquals(sslTermination.getSecurePort(), (long) dataModelSslTermination.getSecurePort());
            Assert.assertEquals(true, dataModelSslTermination.isEnabled());
            Assert.assertEquals(false, dataModelSslTermination.isSecureTrafficOnly());
        }
    }
}
