package org.openstack.atlas.service.domain.helpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.helpers.SslTerminationHelper;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class SslTerminationHelperTest {

    public static class sslTerminationOperations {
        LoadBalancerRepository lbRepository;
        SslTerminationRepository sslTerminationRepository;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        SslTermination ssl1;
        SslTermination ssl2;
        private SslTermination apiSslTerm;
        private org.openstack.atlas.service.domain.entities.SslTermination dbSslTerm;

        @Before
        public void standUp() {
            lbRepository = mock(LoadBalancerRepository.class);
            sslTerminationRepository = mock(SslTerminationRepository.class);
        }

        @Before
        public void standUpObjects() {
            lb = new LoadBalancer();
            lb2 = new LoadBalancer();
            lbjv = new LoadBalancerJoinVip();
            lbjvs = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
            ssl1 = new SslTermination();
            ssl2 = new SslTermination();

            ssl1.setCertificate("aCert");
            ssl1.setPrivatekey("aKey");
            ssl1.setEnabled(true);
            ssl1.setSecurePort(443);
            ssl1.setSecureTrafficOnly(false);

            ssl2.setEnabled(true);
            ssl2.setSecurePort(446);
            ssl2.setSecureTrafficOnly(false);

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
        }

        @Test
        public void shouldAcceptAllCasesWhereRencryptionisTurnedOff() {
            apiSslTerm = newApiSslTerm();
            dbSslTerm = newDbSslTerm();

            apiSslTerm.setReEncryptionEnabled(false);
            assertTrue(isValidRencrypt(apiSslTerm, null));
            assertTrue(isValidRencrypt(apiSslTerm, dbSslTerm));
        }

        @Test
        public void shouldFailIfDisableingSecureTrafficOnRencryptedLb() {
            apiSslTerm = newApiSslTerm();
            dbSslTerm = newDbSslTerm();

            dbSslTerm.setReEncryptionEnabled(true);
            apiSslTerm.setReEncryptionEnabled(true);

            dbSslTerm.setSecureTrafficOnly(true);
            apiSslTerm.setSecureTrafficOnly(false);

            assertFalse(isValidRencrypt(apiSslTerm, dbSslTerm));
        }

        @Test
        public void shouldFailIfSettingRencryptionWithNoSecureTrafficOnly() {
            apiSslTerm = newApiSslTerm();
            dbSslTerm = newDbSslTerm();
            apiSslTerm.setReEncryptionEnabled(true);
            assertFalse(isValidRencrypt(apiSslTerm, null));
            assertFalse(isValidRencrypt(apiSslTerm, dbSslTerm));
            apiSslTerm.setSecureTrafficOnly(false);
            assertFalse(isValidRencrypt(apiSslTerm, null));
            assertFalse(isValidRencrypt(apiSslTerm, dbSslTerm));

            // But its cool if they enable SecureTraffic only at build time
            apiSslTerm.setSecureTrafficOnly(true);
            assertTrue(isValidRencrypt(apiSslTerm, null));
            assertTrue(isValidRencrypt(apiSslTerm, dbSslTerm));
        }

        @Test
        public void shouldTestRencryptionSslTermTemplate() {
            apiSslTerm = newApiSslTerm();
            dbSslTerm = newDbSslTerm();
        }

        @Test
        public void shouldReturnFailIfSecureProtocol() throws EntityNotFoundException, BadRequestException {
            lb.setProtocol(LoadBalancerProtocol.HTTPS);
        }

        @Test
        public void shouldReturnTrueIfNotSecureProtocol() throws EntityNotFoundException, BadRequestException {
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            Assert.assertTrue(SslTerminationHelper.isProtocolSecure(lb));
        }

        @Test
        public void shouldPassIfSslTermToUpdate() throws EntityNotFoundException, BadRequestException {
            org.openstack.atlas.service.domain.entities.SslTermination sslTermination = new org.openstack.atlas.service.domain.entities.SslTermination();
            sslTermination.setEnabled(true);
            lb2.setSslTermination(sslTermination);
            Assert.assertTrue(SslTerminationHelper.isModifingSslAttrsOnly(ssl2, lb2));
        }

        @Test
        public void shouldFailIfNoSslTermToUpdate() throws EntityNotFoundException, BadRequestException {
            Assert.assertFalse(SslTerminationHelper.isModifingSslAttrsOnly(ssl1, lb));
        }

        @Test(expected = BadRequestException.class)
        public void shouldFailIfProtocolNotValidDNSUDP() throws EntityNotFoundException, BadRequestException {
            lb.setProtocol(LoadBalancerProtocol.DNS_UDP);
            Assert.assertFalse(SslTerminationHelper.isProtocolSecure(lb));
        }

        @Test(expected = BadRequestException.class)
        public void shouldFailIfProtocolNotValidUDP() throws EntityNotFoundException, BadRequestException {
            lb.setProtocol(LoadBalancerProtocol.UDP);
            Assert.assertFalse(SslTerminationHelper.isProtocolSecure(lb));
        }

        @Test(expected = BadRequestException.class)
        public void shouldFailIfProtocolNotValidUDPSTREAM() throws EntityNotFoundException, BadRequestException {
            lb.setProtocol(LoadBalancerProtocol.UDP_STREAM);
            Assert.assertFalse(SslTerminationHelper.isProtocolSecure(lb));
        }

        @Test
        public void shouldPassWithDNSTCPProtocol() throws EntityNotFoundException, BadRequestException {
            lb.setProtocol(LoadBalancerProtocol.DNS_TCP);
            Assert.assertTrue(SslTerminationHelper.isProtocolSecure(lb));
        }

        @Test
        public void shouldMapUpdatedAttributes() throws EntityNotFoundException, BadRequestException {
            org.openstack.atlas.service.domain.entities.SslTermination sslTermination = new org.openstack.atlas.service.domain.entities.SslTermination();
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(true);
            Assert.assertEquals(ssl1.isSecureTrafficOnly(), SslTerminationHelper.verifyAndApplyAttributes(ssl1, sslTermination).isSecureTrafficOnly());
            Assert.assertEquals(ssl1.isEnabled(), SslTerminationHelper.verifyAndApplyAttributes(ssl1, sslTermination).isEnabled());
            Assert.assertEquals((Object) ssl1.getSecurePort(), SslTerminationHelper.verifyAndApplyAttributes(ssl1, sslTermination).getSecurePort());
        }

        @Test
        public void shouldCleanGarbageFromCertKey() throws EntityNotFoundException, BadRequestException {
            org.openstack.atlas.service.domain.entities.SslTermination sslTermination = new org.openstack.atlas.service.domain.entities.SslTermination();
            String cleanSTring = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIERzCCAy+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJVUzEO\n" +
                    "MAwGA1UECBMFVGV4YXMxDjAMBgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3Bh\n" +
                    "dZsGmy48UFF4pBHdhnE8bCAt8KgK3BJb0XqNrUxxI6Jc/Hcl9AfppFIEGw==\n" +
                    "-----END CERTIFICATE-----";

            sslTermination.setCertificate("\n" +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIERzCCAy+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJVUzEO\n" +
                    "MAwGA1UECBMFVGV4YXMxDjAMBgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3Bh\n" +
                    "dZsGmy48UFF4pBHdhnE8bCAt8KgK3BJb0XqNrUxxI6Jc/Hcl9AfppFIEGw==\n" +
                    "-----END CERTIFICATE-----\n" +
                    "\n");

            sslTermination.setPrivatekey("\n    " +
                    "\n\n          \n\n       " +
                    "-----BEGIN CERTIFICATE-----\n" +
                    "MIIERzCCAy+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJVUzEO\n" +
                    "MAwGA1UECBMFVGV4YXMxDjAMBgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3Bh\n" +
                    "dZsGmy48UFF4pBHdhnE8bCAt8KgK3BJb0XqNrUxxI6Jc/Hcl9AfppFIEGw==\n" +
                    "-----END CERTIFICATE-----\n\n\n\n  " +
                    "               \n      \n  ");

            sslTermination.setIntermediateCertificate("\n\n-----BEGIN CERTIFICATE-----\n" +
                    "MIIERzCCAy+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJVUzEO\n" +
                    "MAwGA1UECBMFVGV4YXMxDjAMBgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3Bh\n" +
                    "dZsGmy48UFF4pBHdhnE8bCAt8KgK3BJb0XqNrUxxI6Jc/Hcl9AfppFIEGw==\n" +
                    "-----END CERTIFICATE-----\n        \n");

            SslTerminationHelper.cleanSSLCertKeyEntries(sslTermination);
            Assert.assertEquals(cleanSTring, sslTermination.getCertificate());
            Assert.assertEquals(cleanSTring, sslTermination.getIntermediateCertificate());
            Assert.assertEquals(cleanSTring, sslTermination.getPrivatekey());

        }
    }

    private static SslTermination newApiSslTerm() {
        return new SslTermination();
    }

    private static org.openstack.atlas.service.domain.entities.SslTermination newDbSslTerm() {
        return new org.openstack.atlas.service.domain.entities.SslTermination();
    }

    private static boolean isValidRencrypt(SslTermination apiSsl, org.openstack.atlas.service.domain.entities.SslTermination dbSsl) {
        return SslTerminationHelper.isRencryptionUsingSecureTrafficOnly(apiSsl, dbSsl);
    }
}
