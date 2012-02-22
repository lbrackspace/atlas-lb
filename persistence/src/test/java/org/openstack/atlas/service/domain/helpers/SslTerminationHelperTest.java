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

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class SslTerminationHelperTest {

    public static class sslTerminationOperations {
        Integer accountId = 1234;
        LoadBalancerRepository lbRepository;
        SslTerminationRepository sslTerminationRepository;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        SslTermination ssl1;
        SslTermination ssl2;

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


//            lb.setSslTermination(ssl1);
//            lb2.setSslTermination(ssl2);

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
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
            Assert.assertTrue(SslTerminationHelper.modificationStatus(ssl2, lb2));
        }

        @Test
        public void shouldFailIfNoSslTermToUpdate() throws EntityNotFoundException, BadRequestException {
            Assert.assertFalse(SslTerminationHelper.modificationStatus(ssl1, lb));
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
            Assert.assertEquals(ssl1.isSecureTrafficOnly(), SslTerminationHelper.verifyAttributes(ssl1, sslTermination).isSecureTrafficOnly());
            Assert.assertEquals(ssl1.isEnabled(), SslTerminationHelper.verifyAttributes(ssl1, sslTermination).isEnabled());
            Assert.assertEquals((Object) ssl1.getSecurePort(), SslTerminationHelper.verifyAttributes(ssl1, sslTermination).getSecurePort());
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
}
