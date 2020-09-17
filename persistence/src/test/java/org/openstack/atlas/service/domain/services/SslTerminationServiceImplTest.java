package org.openstack.atlas.service.domain.services;

import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;

import org.openstack.atlas.service.domain.services.impl.SslTerminationServiceImpl;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;

import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@RunWith(Enclosed.class)
public class SslTerminationServiceImplTest {

    public static class whenUpdatingSslTermination {


        @Mock
        RestApiConfiguration restApiConfiguration;

        @Mock
        LoadBalancerRepository loadBalancerRepository;

        @Mock
        SslTerminationRepository sslTerminationRepository;

        @Mock
        VirtualIpRepository virtualIpRepository;
        @Mock
        SslCipherProfileService sslCipherProfileService;


        @InjectMocks
        SslTerminationServiceImpl sslTerminationService;

        private Response response;
        private static KeyPair userKey;
        private static X509CertificateHolder userCrt;
        private static Set<X509CertificateHolder> imdCrts;
        private static X509CertificateHolder rootCA;
        private static final int keySize = 512; // Keeping the key small for testing
        private static List<X509ChainEntry> chainEntries;
        // These are for testing pre defined keys and certs
        private static String workingRootCa;
        private static String workingUserKey;
        private static String workingUserCrt;
        private static String workingUserChain;


        LoadBalancer loadBalancer;
        org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination sslTermination;
        SslTermination sslTerminationToBeUpdated;
        String iv;

        @BeforeClass
        public static void setUpClass() throws RsaException, NotAnX509CertificateException {
            List<X509CertificateHolder> orderImds = new ArrayList<X509CertificateHolder>();
            long now = System.currentTimeMillis();
            long lastYear = now - (long) 1000 * 24 * 60 * 60 * 365;
            long nextYear = now + (long) 1000 * 24 * 60 * 60 * 365;
            Date notBefore = new Date(lastYear);
            Date notAfter = new Date(nextYear);
            String wtf = String.format("%s\n%s", StaticHelpers.getDateString(notBefore), StaticHelpers.getDateString(notAfter));
            List<String> subjNames = new ArrayList<String>();
            // Root SubjName
            subjNames.add("CN=RootCA");

            // Add the middle subjs
            for (int i = 1; i <= 7; i++) {
                String fmt = "CN=Intermedite Cert %s";
                String subjName = String.format(fmt, i);
                subjNames.add(subjName);
            }

            // Lastly add the end user subj
            String subjName = "CN=www.rackexp.org";
            subjNames.add(subjName);
            chainEntries = X509PathBuilder.newChain(subjNames, keySize, notBefore, notAfter);
            int lastIdx = chainEntries.size() - 1;
            rootCA = chainEntries.get(0).getX509Holder();
            userCrt = chainEntries.get(lastIdx).getX509Holder();
            userKey = chainEntries.get(lastIdx).getKey();

            imdCrts = new HashSet<X509CertificateHolder>();
            for (int i = 1; i < lastIdx; i++) {
                imdCrts.add(chainEntries.get(i).getX509Holder());
                orderImds.add(chainEntries.get(i).getX509Holder());
            }

            workingRootCa = PemUtils.toPemString(rootCA);
            workingUserCrt = PemUtils.toPemString(userCrt);
            workingUserKey = PemUtils.toPemString(userKey);
            Collections.reverse(orderImds);
            StringBuilder sb = new StringBuilder();
            for (X509CertificateHolder imd : orderImds) {
                sb = sb.append(PemUtils.toPemString(imd)).append("\n");
            }
            workingUserChain = sb.toString();
        }


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadBalancer = new LoadBalancer();
            sslTerminationService.setSslTerminationRepository(sslTerminationRepository);
            sslTerminationService.setLoadBalancerRepository(loadBalancerRepository);
            sslTermination = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            sslTerminationToBeUpdated = new SslTermination();
            sslTerminationToBeUpdated.setPrivatekey(workingUserKey);
            sslTerminationToBeUpdated.setCertificate(workingUserCrt);
            sslTerminationToBeUpdated.setEnabled(true);
            sslTerminationToBeUpdated.setSecureTrafficOnly(false);
            sslTerminationToBeUpdated.setIntermediateCertificate(workingUserChain);
            sslTermination.setSecurePort(443);
            sslTermination.setPrivatekey(workingUserKey);
            sslTermination.setCertificate(workingUserCrt);
            sslTermination.setEnabled(true);
            sslTermination.setSecureTrafficOnly(false);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            loadBalancer.setSslTermination(sslTerminationToBeUpdated);
            loadBalancer.setId(613);
            loadBalancer.setAccountId(5806065);
            iv = loadBalancer.getAccountId() + "_" + loadBalancer.getId();

            when(loadBalancerRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(loadBalancer);
            when(sslTerminationRepository.getSslTerminationByLbId(anyInt(), anyInt())).thenReturn(sslTerminationToBeUpdated);
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn("testCrypto");

        }

        @Test
        public void shouldReturnEncryptedPrivateKey() throws Exception {
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);
            Assert.assertNotEquals(zeusSslTermination.getSslTermination().getPrivatekey(), workingUserKey);
            String dtest = Aes.b64decryptGCM_str(zeusSslTermination.getSslTermination().getPrivatekey(), "testCrypto", iv);
            Assert.assertEquals(dtest, workingUserKey);

        }

        @Test
        public void shouldNotThrowErrorWhenPrivateKeyIsEncryptedFromDB() throws Exception {
            String privateKey = Aes.b64encryptGCM(workingUserKey.getBytes(), "testCrypto", iv);
            sslTerminationToBeUpdated.setPrivatekey(privateKey);
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);
            Assert.assertNotEquals(zeusSslTermination.getSslTermination().getPrivatekey(), workingUserKey);
            String dtest = Aes.b64decryptGCM_str(zeusSslTermination.getSslTermination().getPrivatekey(), "testCrypto", iv);
            Assert.assertEquals(dtest, workingUserKey);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowErrorWhenCrpytoKeyIsNotFoud() throws BadRequestException, ImmutableEntityException, UnprocessableEntityException, EntityNotFoundException {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn(null);
            ZeusSslTermination zeusSslTermination =  sslTerminationService.updateSslTermination(613, 5806065, sslTermination, true);

        }

    }

}

