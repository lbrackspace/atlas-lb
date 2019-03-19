package org.openstack.atlas.api.mapper.dozer;

import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolName;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;

@RunWith(Enclosed.class)
public class DataModelToDomainSslTerminationTest {
    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
    public static class When_mapping_session_persistence_from_datamodel_to_domain {
        private Mapper mapper;
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
            Assert.assertEquals(true, domainSsltermination.getEnabled());
            Assert.assertEquals(false, domainSsltermination.getSecureTrafficOnly());
        }

        @Test
        public void shouldAcceptSecurityProtocols() {
            sslTermination = new SslTermination();
            String TLSMappingMessage = "TLS protocol mapping failed";
            //TLS protocols not specified in request
            domainSsltermination = mapper.map(sslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            Assert.assertTrue(TLSMappingMessage, domainSsltermination.isTls10Enabled());
            Assert.assertTrue(TLSMappingMessage, domainSsltermination.isTls11Enabled());

            //with TLS protocols
            SecurityProtocol sp = new SecurityProtocol();
            sp.setSecurityProtocolName(SecurityProtocolName.TLS_10);
            sp.setSecurityProtocolStatus(SecurityProtocolStatus.DISABLED);
            sslTermination.getSecurityProtocols().add(sp);
            sp = new SecurityProtocol();
            sp.setSecurityProtocolName(SecurityProtocolName.TLS_11);
            sp.setSecurityProtocolStatus(SecurityProtocolStatus.ENABLED);
            sslTermination.getSecurityProtocols().add(sp);

            domainSsltermination = mapper.map(sslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            Assert.assertFalse(TLSMappingMessage, domainSsltermination.isTls10Enabled());
            Assert.assertTrue(TLSMappingMessage, domainSsltermination.isTls11Enabled());
        }
    }
}
