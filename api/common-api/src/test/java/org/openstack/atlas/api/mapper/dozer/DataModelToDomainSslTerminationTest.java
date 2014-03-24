package org.openstack.atlas.api.mapper.dozer;

import org.dozer.DozerBeanMapper;
import org.dozer.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.util.debug.Debug;

@RunWith(Enclosed.class)
public class DataModelToDomainSslTerminationTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class When_mapping_session_persistence_from_datamodel_to_domain {

        private DozerBeanMapper mapper;
        private SslTermination apiSslTermination;
        private org.openstack.atlas.service.domain.entities.SslTermination domainSsltermination;

        @Before
        public void standUp() {
            mapper = MapperBuilder.getConfiguredMapper(publicDozerConfigFile);
            apiSslTermination = new SslTermination();
            apiSslTermination.setPrivatekey("AKey");
        }

        @Test
        public void shouldMapRencryptionBooleanToTrue() {
            apiSslTermination = new SslTermination();
            apiSslTermination.setReEncryptionEnabled(Boolean.TRUE);
            domainSsltermination = mapper.map(apiSslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            Assert.assertEquals(true, domainSsltermination.isReEncryptionEnabled());
        }

        @Test
        public void shouldMapRencryptionBooleanToFalseWhenNotSet() {
            apiSslTermination = new SslTermination();
            domainSsltermination = mapper.map(apiSslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            Assert.assertEquals(false, domainSsltermination.isReEncryptionEnabled());
        }

        @Test
        public void shouldMapRencryptionBooleanToFalseWhenSetToFalse() {
            apiSslTermination = new SslTermination();
            apiSslTermination.setReEncryptionEnabled(Boolean.FALSE);
            domainSsltermination = mapper.map(apiSslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            Assert.assertEquals(false, domainSsltermination.isReEncryptionEnabled());
        }

        @Test
        public void shouldMapToNoneWhenObjectCorrectly() {
            apiSslTermination = new SslTermination();
            apiSslTermination.setPrivatekey("AKey");
            apiSslTermination.setCertificate("aCert");
            apiSslTermination.setIntermediateCertificate("anIntermediateCert");
            apiSslTermination.setReEncryptionCertificateAuthority("aCA");
            apiSslTermination.setReEncryptionEnabled(Boolean.TRUE);
            apiSslTermination.setSecurePort(443);

            try {
                domainSsltermination = mapper.map(apiSslTermination, org.openstack.atlas.service.domain.entities.SslTermination.class);
            } catch (Exception e) {
                String exMsg = Debug.getExtendedStackTrace(e);
                Assert.fail(String.format("someException caused by converting api to domain SslTermination %s", exMsg));
            }

            Assert.assertEquals(apiSslTermination.getPrivatekey(), domainSsltermination.getPrivatekey());
            Assert.assertEquals(apiSslTermination.getCertificate(), domainSsltermination.getCertificate());
            Assert.assertEquals(apiSslTermination.getIntermediateCertificate(), domainSsltermination.getIntermediateCertificate());
            Assert.assertEquals((long) apiSslTermination.getSecurePort(), domainSsltermination.getSecurePort());
            Assert.assertEquals(true, domainSsltermination.isEnabled());
            Assert.assertEquals(false, domainSsltermination.isSecureTrafficOnly());
            Assert.assertEquals(apiSslTermination.isReEncryptionEnabled(), domainSsltermination.isReEncryptionEnabled());
            Assert.assertEquals(true, domainSsltermination.isReEncryptionEnabled());
            Assert.assertEquals(apiSslTermination.getReEncryptionCertificateAuthority(), domainSsltermination.getReEncryptionCertificateAuthority());
            Assert.assertEquals("aCA", domainSsltermination.getReEncryptionCertificateAuthority());
        }
    }
}
