package org.openstack.atlas.api.mapper.dozer;

import java.util.List;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cipher;
import org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers;
import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.util.Constants;

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
        public void shouldMapCipherStringNamesToCiphersType() {
            String cipherNames = "ZES ,  DES  ,  AES   ,      NES";
            Ciphers ciphers = mapper.map(cipherNames, Ciphers.class);
            Assert.assertEquals(ciphers.getCiphers().get(0).getName(), "AES");
            Assert.assertEquals(ciphers.getCiphers().get(1).getName(), "DES");
            Assert.assertEquals(ciphers.getCiphers().get(2).getName(), "NES");  // New Mario algo
            Assert.assertEquals(ciphers.getCiphers().get(3).getName(), "ZES");
            Assert.assertNotSame(ciphers.getCiphers().get(0).getName(), "Pfft");
            // Ciphernames should be alphabetized. To make them caonical
        }

        @Test
        public void shouldMapCiphersType2CipherNamesString() {
            Ciphers ciphers = new Ciphers();
            List<Cipher> cipherList = ciphers.getCiphers();
            String[] unsortedNames = new String[]{"ZES", "NES", "AES", "DES"};

            for (String cipherName : unsortedNames) {
                Cipher cipher = new Cipher();
                cipher.setName(cipherName);
                cipherList.add(cipher);
            }
            String dbCipherNames = mapper.map(ciphers, String.class);
            Assert.assertEquals(dbCipherNames, "AES,DES,NES,ZES");
            // Again ciphernames are alphabetized to make them cononical
            Assert.assertNotSame(dbCipherNames, "SOMETHINGELSE");
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

        @Test
        public void shouldMapCipherProfileNameToDefaultWhenNoProfileAttached() {
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

            Assert.assertEquals(Constants.DEFAUlT_CIPHER_PROFILE_NAME, dataModelSslTermination.getCipherProfile());
        }

        @Test
        public void shouldMapCipherProfileName() {
            final String cipherProfileName = "HIGH SECURE";
            sslTermination = new SslTermination();
            sslTermination.setPrivatekey("AKey");
            sslTermination.setCertificate("aCert");
            sslTermination.setIntermediateCertificate("anIntermediateCert");
            sslTermination.setSecurePort(443);
            SslCipherProfile cipherProfile = new SslCipherProfile();
            cipherProfile.setName(cipherProfileName);
            sslTermination.setCipherProfile(cipherProfile);
            try {
                dataModelSslTermination = mapper.map(sslTermination, org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination.class);
            } catch (Exception e) {
                Assert.fail("Exception caused by ssl termination types being null");
            }

            Assert.assertEquals(cipherProfileName, dataModelSslTermination.getCipherProfile());
        }

        @Test
        public void shouldAcceptSecurityProtocols() {
            org.openstack.atlas.service.domain.entities.SslTermination dbSsl;
            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination apiSsl;
            dbSsl = new org.openstack.atlas.service.domain.entities.SslTermination();
            dbSsl.setTls10Enabled(false);
            apiSsl = mapper.map(dbSsl, org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination.class);
        }
    }
}
