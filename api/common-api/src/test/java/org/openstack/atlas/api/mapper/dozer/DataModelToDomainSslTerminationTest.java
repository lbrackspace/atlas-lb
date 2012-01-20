package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.dozer.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;

@RunWith(Enclosed.class)
public class DataModelToDomainSslTerminationTest {
    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    public static class When_mapping_session_persistence_from_datamodel_to_domain {
        private DozerBeanMapper mapper;
        private SslTermination sslTermination;
        private org.openstack.atlas.service.domain.entities.SslTermination domainSsltermination;


        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            sslTermination = new SslTermination();
            sslTermination.setPrivatekey("AKey");
        }

        @Test
        public void shouldMapToNoneWhenObjectCorrectly() {
            sslTermination = new SslTermination();
            sslTermination.setPrivatekey("AKey");
            sslTermination.setCertificate("aCert");
            sslTermination.setIntermediateCertificate("anIntermediateCert");
            sslTermination.setSecurePort(443);

            try {
                domainSsltermination = mapper.map(sslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            }
            catch (Exception e) {
                Assert.fail("Exception caused by ssl termination type being null");
            }

            Assert.assertEquals(sslTermination.getPrivatekey(), domainSsltermination.getPrivatekey());
            Assert.assertEquals(sslTermination.getCertificate(), domainSsltermination.getCertificate());
            Assert.assertEquals(sslTermination.getIntermediateCertificate(), domainSsltermination.getIntermediateCertificate());
            Assert.assertEquals((long)sslTermination.getSecurePort(), domainSsltermination.getSecurePort());
            Assert.assertEquals(true, domainSsltermination.isEnabled());
            Assert.assertEquals(false, domainSsltermination.isSecureTrafficOnly());
        }
    }
}
