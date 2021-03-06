package org.openstack.atlas.service.domain.services;

import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.InOrderImpl;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.repository.CertificateMappingRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.impl.AccountLimitServiceImpl;
import org.openstack.atlas.service.domain.services.impl.CertificateMappingServiceImpl;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerStatusHistoryServiceImpl;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(Enclosed.class)
public class CertificateMappingServiceImplTest {

    public static class whenModifyingCertificateMappings {


        @Mock
        RestApiConfiguration restApiConfiguration;

        @Mock
        LoadBalancerRepository loadBalancerRepository;

        @Mock
        LoadBalancerStatusHistoryServiceImpl loadBalancerStatusHistoryService;

        @Mock
        SslTerminationRepository sslTerminationRepository;

        @Mock
        CertificateMappingRepository certificateMappingRepository;

        @Mock
        AccountLimitServiceImpl accountLimitService;

        @Mock
        NotificationService notificationService;


        @InjectMocks
        CertificateMappingServiceImpl certificateMappingService;

        LoadBalancer loadBalancer;
        CertificateMapping certificateMappingToBeUpdated;
        CertificateMapping dbCertMapping;
        String privateKey;
        String encryptedKey;
        String iv;
        String iv1;
        HashSet<CertificateMapping> certMapSet;
        ArrayList<CertificateMapping> certMapList;

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

        @BeforeClass
        public static void setUpClass() throws RsaException, NotAnX509CertificateException {
            List<X509CertificateHolder> orderImds = new ArrayList<X509CertificateHolder>();
            long now = System.currentTimeMillis();
            long lastYear = now - (long) 1000 * 24 * 60 * 60 * 365;
            long nextYear = now + (long) 1000 * 24 * 60 * 60 * 365;
            Date notBefore = new Date(lastYear);
            Date notAfter = new Date(nextYear);
            String wtf = String.format("%s\n%s", StaticHelpers.getDateString(notBefore),
                    StaticHelpers.getDateString(notAfter));
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
        public void setUp() throws EntityNotFoundException, NoSuchPaddingException, InvalidAlgorithmParameterException,
                NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
                IOException, UnprocessableEntityException {
            MockitoAnnotations.initMocks(this);
            loadBalancer = new LoadBalancer();
            loadBalancer.setId(613);
            loadBalancer.setAccountId(5806065);
            privateKey = workingUserKey;
            iv = loadBalancer.getAccountId() + "_" + loadBalancer.getId() + "_2";
            iv1 = loadBalancer.getAccountId() + "_" + loadBalancer.getId() + "_1";
            encryptedKey = Aes.b64encryptGCM(privateKey.getBytes(), "testCrypto", iv);
            certificateMappingService.setLoadBalancerRepository(loadBalancerRepository);
            certificateMappingToBeUpdated = new CertificateMapping();
            certificateMappingToBeUpdated.setId(2);
            certificateMappingToBeUpdated.setHostName("h2");
            certificateMappingToBeUpdated.setPrivateKey(privateKey);
            certificateMappingToBeUpdated.setCertificate(workingUserCrt);
            certificateMappingToBeUpdated.setIntermediateCertificate(workingUserChain);
            certMapSet = new HashSet<>();
            certMapSet.add(certificateMappingToBeUpdated);

            dbCertMapping = new CertificateMapping();
            dbCertMapping.setId(1);
            dbCertMapping.setHostName("h1");
            dbCertMapping.setPrivateKey(Aes.b64encryptGCM(privateKey.getBytes(), "testCrypto", iv1));
            dbCertMapping.setCertificate(workingUserCrt);
            dbCertMapping.setIntermediateCertificate(workingUserChain);
            certMapList = new ArrayList<>();
            certMapList.add(dbCertMapping);

            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            loadBalancer.setCertificateMappings(certMapSet);


            when(restApiConfiguration.getString(
                    PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn("testCrypto");
            when(accountLimitService.getLimit(loadBalancer.getAccountId(),
                    AccountLimitType.CERTIFICATE_MAPPING_LIMIT)).thenReturn(2);
            when(loadBalancerRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(loadBalancer);
            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(loadBalancerRepository.getSslTermination(anyInt(), anyInt())).thenReturn(new SslTermination());
            when(loadBalancerRepository.testAndSetStatus(anyInt(), anyInt(), any(), anyBoolean())).thenReturn(true);
            when(loadBalancerStatusHistoryService.save(loadBalancer.getAccountId(),
                    loadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE)).thenReturn(null);

            when(certificateMappingRepository.save(any(), anyInt())).thenReturn(certificateMappingToBeUpdated);
            when(certificateMappingRepository.getAllForLoadBalancerId(anyInt())).thenReturn(certMapList);
            doNothing().when(notificationService).saveAlert(any(), any(), any(), any(), any());
        }

        // Create
        @Test
        public void shouldReturnValidDecryptablePrivateKey() throws Exception {
           CertificateMapping cmap = certificateMappingService.create(loadBalancer);
           String dkey = Aes.b64decryptGCM_str(cmap.getPrivateKey(), "testCrypto", iv);
           Assert.assertEquals(dkey, privateKey);
           Assert.assertEquals(workingUserCrt, cmap.getCertificate());
           Assert.assertEquals(workingUserChain, cmap.getIntermediateCertificate());
        }

        @Test(expected = BadRequestException.class)
        public void shouldFailBecauseInvalidCert() throws Exception {
            loadBalancer.getCertificateMappings().iterator().next().setPrivateKey("broken");
            certificateMappingService.create(loadBalancer);
        }

        @Test(expected = UnprocessableEntityException.class)
        public void shouldFailDuplicateHost() throws Exception {
            loadBalancer.getCertificateMappings().iterator().next().setHostName("h1");
            certificateMappingService.create(loadBalancer);
        }

        @Test(expected = LimitReachedException.class)
        public void shouldFailLimitReached() throws Exception {
            when(accountLimitService.getLimit(loadBalancer.getAccountId(),
                    AccountLimitType.CERTIFICATE_MAPPING_LIMIT)).thenReturn(0);
            certificateMappingService.create(loadBalancer);
        }

        @Test
        public void shouldSaveCertificateMappingTwice() throws Exception {
            ArgumentCaptor<CertificateMapping> captor = ArgumentCaptor.forClass(CertificateMapping.class);
            dbCertMapping.setPrivateKey(privateKey);
            certificateMappingService.create(loadBalancer);
            verify(certificateMappingRepository, times(2)).save(captor.capture(), anyInt());
            List<CertificateMapping> certificateMappings = captor.getAllValues();
            Assert.assertEquals(certificateMappingToBeUpdated, certificateMappings.get(0));
            String deCryptedKey = Aes.b64decryptGCM_str(certificateMappings.get(1).getPrivateKey(), "testCrypto", iv);
            Assert.assertEquals(privateKey, deCryptedKey);
        }

        @Test
        public void shouldPassWithCertMappingIdNullOnCreate() throws Exception {
            when(certificateMappingRepository.save(certificateMappingToBeUpdated, loadBalancer.getId())).thenReturn(dbCertMapping);
            certificateMappingToBeUpdated.setId(null);
            certificateMappingService.create(loadBalancer);
            verify(certificateMappingRepository, times(2)).save(any(), anyInt());
        }

        @Test
        public void shouldPassWithCertMappingIdNullOnCreateRevisedEncryptKey() throws Exception {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn("testCryptoBroken");
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev)).thenReturn("testCrypto");
            when(certificateMappingRepository.save(certificateMappingToBeUpdated, loadBalancer.getId())).thenReturn(dbCertMapping);
            certificateMappingToBeUpdated.setId(null);
            certificateMappingService.create(loadBalancer);
            verify(certificateMappingRepository, times(2)).save(any(), anyInt());
        }

        @Test
        public void shouldPassWithCertMappingIdNullOnCreateRevisedEncryptKeyNull() throws Exception {
            // Will be forced to used the original encryption key
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn("testCryptoBroken");
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev)).thenReturn(null);
            when(certificateMappingRepository.save(certificateMappingToBeUpdated, loadBalancer.getId())).thenReturn(dbCertMapping);
            certificateMappingToBeUpdated.setId(null);
            certificateMappingService.create(loadBalancer);
            verify(certificateMappingRepository, times(2)).save(any(), anyInt());

        }

        // Update
        @Test
        public void shouldAcceptValidDataForUpdate() throws Exception {
            certificateMappingService.update(loadBalancer);

            String dkey = Aes.b64decryptGCM_str(
                    loadBalancer.getCertificateMappings().iterator().next().getPrivateKey(), "testCrypto", iv);
            Assert.assertEquals(dkey, privateKey);
            verify(certificateMappingRepository, times(1)).update(loadBalancer);
        }

        @Test
        public void shouldAcceptValidDataForUpdateWithRevisedEncryptKey() throws Exception {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn(null);
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev)).thenReturn("testCrypto");

            certificateMappingService.update(loadBalancer);

            String dkey = Aes.b64decryptGCM_str(
                    loadBalancer.getCertificateMappings().iterator().next().getPrivateKey(), "testCrypto", iv);
            Assert.assertEquals(privateKey, dkey);
            verify(certificateMappingRepository, times(1)).update(loadBalancer);
        }

        @Test
        public void shouldAcceptValidDataForUpdateReencryptWithRevisedEncryptKey() throws Exception {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev)).thenReturn("testCrypto2");

            certificateMappingService.update(loadBalancer);

            String dkey = Aes.b64decryptGCM_str(
                    loadBalancer.getCertificateMappings().iterator().next().getPrivateKey(), "testCrypto2", iv);
            Assert.assertEquals(privateKey, dkey);
            verify(certificateMappingRepository, times(1)).update(loadBalancer);
        }

        @Test(expected = InternalProcessingException.class)
        public void shouldFailValidDataForUpdateWithRevisedEncryptKeyNull() throws Exception {
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn(null);
            when(restApiConfiguration.getString(PublicApiServiceConfigurationKeys.term_crypto_key_rev)).thenReturn(null);

            certificateMappingService.update(loadBalancer);

            verify(certificateMappingRepository, times(0)).update(loadBalancer);
            verify(notificationService, times(1)).saveAlert(any(), any(), any(), any(), any());
        }

        @Test
        public void shouldAcceptValidDataWithoutHostnameForUpdate() throws Exception {
            loadBalancer.getCertificateMappings().iterator().next().setHostName(null);
            certificateMappingService.update(loadBalancer);

            String dkey = Aes.b64decryptGCM_str(
                    loadBalancer.getCertificateMappings().iterator().next().getPrivateKey(), "testCrypto", iv);
            Assert.assertEquals(dkey, privateKey);
            verify(certificateMappingRepository, times(1)).update(loadBalancer);
        }

        @Test
        public void shouldAcceptValidChangeCertDataForUpdate() throws Exception {
            dbCertMapping.setPrivateKey(encryptedKey);
            LoadBalancer lb = new LoadBalancer();
            loadBalancer.setId(613);
            loadBalancer.setAccountId(5806065);
            dbCertMapping.setId(2);
            lb.getCertificateMappings().add(dbCertMapping);
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            when(loadBalancerRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(lb);

            // Trigger using db supplied key
            loadBalancer.getCertificateMappings().iterator().next().setPrivateKey(null);
            certificateMappingService.update(loadBalancer);

            // The key should have been decrypted, validated with updated certs and re-encrypted
            Assert.assertNotEquals(encryptedKey, lb.getCertificateMappings().iterator().next().getPrivateKey());
            String dkey = Aes.b64decryptGCM_str(
                    lb.getCertificateMappings().iterator().next().getPrivateKey(), "testCrypto", iv);
            Assert.assertEquals(privateKey, dkey);
            verify(certificateMappingRepository, times(1)).update(lb);
        }

        @Test
        public void shouldAcceptValidChangeCertDataWithoutHostnameForUpdate() throws Exception {
            dbCertMapping.setPrivateKey(encryptedKey);
            LoadBalancer lb = new LoadBalancer();
            loadBalancer.setId(613);
            loadBalancer.setAccountId(5806065);
            dbCertMapping.setId(2);
            lb.getCertificateMappings().add(dbCertMapping);
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            when(loadBalancerRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(lb);

            loadBalancer.getCertificateMappings().iterator().next().setHostName(null);
            // Trigger using db supplied key
            loadBalancer.getCertificateMappings().iterator().next().setPrivateKey(null);
            certificateMappingService.update(loadBalancer);

            // The key should have been decrypted, validated with updated certs and re-encrypted
            Assert.assertNotEquals(encryptedKey, lb.getCertificateMappings().iterator().next().getPrivateKey());
            String dkey = Aes.b64decryptGCM_str(
                    lb.getCertificateMappings().iterator().next().getPrivateKey(), "testCrypto", iv);
            Assert.assertEquals(privateKey, dkey);
            verify(certificateMappingRepository, times(1)).update(lb);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldFailToLocateMappingForUpdate() throws Exception {
            when(loadBalancerRepository.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(new LoadBalancer());

            certificateMappingService.update(loadBalancer);
        }

        @Test(expected = InternalProcessingException.class)
        public void shouldFailToValidateDecryptedPrivateKeys() throws Exception{
            // all private keys should be encrypted at this point, any failures here should bubble up and be resolved
            certificateMappingService.validatePrivateKeys(loadBalancer, true);
            verify(notificationService, times(1)).saveAlert(any(), any(), any(), any(), any());
        }

        @Test
        public void shouldValidateEncryptedPrivateKeys() throws Exception {
            certificateMappingToBeUpdated.setPrivateKey(encryptedKey);
            certificateMappingService.validatePrivateKeys(loadBalancer, true);
        }

        @Test(expected = InternalProcessingException.class)
        public void shouldThrowExceptionWithBadPrivateKey() throws Exception {
            certificateMappingToBeUpdated.setPrivateKey(null);
            certificateMappingService.validatePrivateKeys(loadBalancer, true);
            verify(notificationService, times(1)).saveAlert(any(), any(), any(), any(), any());
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenUnableToReadUsrCrt() throws Exception {
            certificateMappingToBeUpdated.setPrivateKey(encryptedKey);
            certificateMappingToBeUpdated.setCertificate("badCrt");
            certificateMappingService.validatePrivateKeys(loadBalancer, true);
        }

        @Test
        public void shouldValidateCertificateMappingWithNoIntermediateKey() throws Exception {
            certificateMappingToBeUpdated.setPrivateKey(encryptedKey);
            certificateMappingToBeUpdated.setIntermediateCertificate(null);
            certificateMappingService.validatePrivateKeys(loadBalancer, true);
        }

        @Test
        public void shouldValidateLBWithMultipleCertMappings() throws Exception {
            certificateMappingToBeUpdated.setPrivateKey(Aes.b64encryptGCM(privateKey.getBytes(),
                    "testCrypto", iv));
            loadBalancer.getCertificateMappings().add(dbCertMapping);
            certificateMappingService.validatePrivateKeys(loadBalancer, true);

        }

        // Delete
        @Test
        public void shouldLoopThroughCertMappingForLB() throws Exception {
            certMapList.add(certificateMappingToBeUpdated);
            when(certificateMappingService.getAllForLoadBalancerId(loadBalancer.getId())).thenReturn(certMapList);
            certificateMappingService.deleteAllCertMappingForLB(loadBalancer.getId());
            verify(certificateMappingRepository, times(1)).delete(loadBalancer, dbCertMapping.getId());
            verify(certificateMappingRepository, times(1)).delete(loadBalancer, certificateMappingToBeUpdated.getId());
        }

        @Test
        public void shouldNotLoopThroughEmptyCertMappingListForLB()throws Exception{
            certMapList = new ArrayList<>();
            when(certificateMappingService.getAllForLoadBalancerId(loadBalancer.getId())).thenReturn(certMapList);
            certificateMappingService.deleteAllCertMappingForLB(loadBalancer.getId());
            verify(certificateMappingRepository, times(0)).delete(any(LoadBalancer.class), anyInt());
        }



    }

}
