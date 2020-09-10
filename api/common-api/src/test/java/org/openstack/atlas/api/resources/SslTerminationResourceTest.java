package org.openstack.atlas.api.resources;

import org.bouncycastle.cert.X509CertificateHolder;
import org.dozer.Mapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.SslTerminationService;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;

import java.rmi.RemoteException;
import java.security.KeyPair;
import java.util.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class SslTerminationResourceTest {

    public static class createSsl {

        @Mock
        AsyncService asyncService;
        @Mock
        ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        @Mock
        ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;
        @Mock
        ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        @Mock
        SslTerminationService sslTerminationService;
        @Spy
        ZeusUtils zeusUtils;
        @Mock
        Mapper dozerBeanMapper;
        @Mock
        RestApiConfiguration restApiConfiguration;

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

        @InjectMocks
        SslTerminationResource sslTermResource;

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
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            dozerBeanMapper = mock(Mapper.class);
            restApiConfiguration = mock(RestApiConfiguration.class);
            mock(ZeusUtils.class);
            PowerMockito.whenNew(ZeusUtils.class).withNoArguments().thenReturn(zeusUtils);

            sslTermResource = new SslTerminationResource();
            sslTermResource.setId(1);
            sslTermResource.setLoadBalancerId(1);
            sslTermResource.setAccountId(1234);
            sslTermResource.setSslTerminationService(sslTerminationService);
            sslTermResource.setAsyncService(asyncService);
            sslTermResource.setDozerMapper(dozerBeanMapper);
            sslTermResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
            sslTermResource.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
            sslTermResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            sslTermResource.setRestApiConfiguration(restApiConfiguration);

        }

        @Test
        public void shouldDecryptDatabaseKey() throws Exception {
            // User requests should only ever encounter database keys that are already encrypted
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            doReturn("nZr4u7x!A%D*F-Ja").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.term_crypto_key);
            String eKey = Aes.b64encryptGCM(workingUserKey.getBytes(),
                    "nZr4u7x!A%D*F-Ja",
                    (sslTermResource.getAccountId() + "_" + sslTermResource.getLoadBalancerId()));
            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(eKey);
            sslTerm.setCertificate(workingUserCrt);
            sslTerm.setIntermediateCertificate(workingUserChain);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            org.junit.Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldValidateUserSuppliedData() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(null);
            sslTerm.setCertificate(null);
            sslTerm.setIntermediateCertificate(null);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);
            rsslTerm.setPrivatekey(workingUserKey);
            rsslTerm.setCertificate(workingUserCrt);
            rsslTerm.setIntermediateCertificate(workingUserChain);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            org.junit.Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldInalidateUserSuppliedDataWithBrokenKey() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(null);
            sslTerm.setCertificate(null);
            sslTerm.setIntermediateCertificate(null);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);
            rsslTerm.setPrivatekey("BrokenKey");
            rsslTerm.setCertificate(workingUserCrt);
            rsslTerm.setIntermediateCertificate(workingUserChain);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            org.junit.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldInalidateUserSuppliedDataWithBrokenCert() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(null);
            sslTerm.setCertificate(null);
            sslTerm.setIntermediateCertificate(null);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);
            rsslTerm.setPrivatekey(workingUserKey);
            rsslTerm.setCertificate("BrokenCert");
            rsslTerm.setIntermediateCertificate(workingUserChain);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            org.junit.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldFailToDecryptValidDatabaseKey() throws Exception {
            // If the database key fails to decrypt it most likely is because it was never encrypted and will
            // be encrypted by the service layer prior to storing or updating it.
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            doReturn("nZr4u7x!A%D*F-Ja").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.term_crypto_key);

            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(workingUserKey);
            sslTerm.setCertificate(workingUserCrt);
            sslTerm.setIntermediateCertificate(workingUserChain);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            // since key just wasn't encrypted we should pass validation and get 202 response
            org.junit.Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldFailToDecryptValidDatabaseKeyAndInvalidateBrokenCert() throws Exception {
            // If the database key fails to decrypt it most likely is because it was never encrypted and will
            // be encrypted by the service layer prior to storing or updating it.
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            doReturn("nZr4u7x!A%D*F-Ja").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.term_crypto_key);

            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(workingUserKey);
            sslTerm.setCertificate("BrokenCert");
            sslTerm.setIntermediateCertificate(workingUserChain);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            // since key just wasn't encrypted we should pass validation and get 202 response
            org.junit.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldFailToDecryptInvalidDatabaseKey() throws Exception {
            // If for some chance the key is corrupted encrypted or not validation should return a bad request
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            doReturn("nZr4u7x!A%D*F-Ja").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.term_crypto_key);

            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey("invalidKey");
            sslTerm.setCertificate(workingUserCrt);
            sslTerm.setIntermediateCertificate(workingUserChain);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            org.junit.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldDecryptDatabaseKeyAndInvalidateBrokenCert() throws Exception {
            // If for some chance the key is corrupted encrypted or not validation should return a bad request
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.ssl_termination);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            doReturn("nZr4u7x!A%D*F-Ja").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.term_crypto_key);
            String eKey = Aes.b64encryptGCM(workingUserKey.getBytes(),
                    "nZr4u7x!A%D*F-Ja",
                    (sslTermResource.getAccountId() + "_" + sslTermResource.getLoadBalancerId()));
            SslTermination sslTerm = new SslTermination();
            sslTerm.setPrivatekey(eKey);
            sslTerm.setCertificate("BrokenCert");
            sslTerm.setIntermediateCertificate(workingUserChain);

            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);

            org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination rsslTerm = new org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination();
            rsslTerm.setSecurePort(23);

            ZeusSslTermination rzTerm = new ZeusSslTermination();
            rzTerm.setSslTermination(sslTerm);
            when(sslTerminationService.updateSslTermination(sslTermResource.getLoadBalancerId(),
                    sslTermResource.getAccountId(), rsslTerm, false)).thenReturn(rzTerm);

            response = sslTermResource.createSsl(rsslTerm);
            org.junit.Assert.assertEquals(400, response.getStatus());
        }
    }

    public static class retrieveSsl {

        @Mock
        AsyncService asyncService;
        @Mock
        ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        @Mock
        ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;
        @Mock
        ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        @Mock
        SslTerminationService sslTerminationService;
        @Mock
        Mapper dozerBeanMapper;
        @Mock
        RestApiConfiguration restApiConfiguration;

        private Response response;

        @InjectMocks
        SslTerminationResource sslTermResource;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            dozerBeanMapper = mock(Mapper.class);
            restApiConfiguration = mock(RestApiConfiguration.class);

            sslTermResource = new SslTerminationResource();
            sslTermResource.setId(1);
            sslTermResource.setLoadBalancerId(1);
            sslTermResource.setAccountId(1234);
            sslTermResource.setSslTerminationService(sslTerminationService);
            sslTermResource.setAsyncService(asyncService);
            sslTermResource.setDozerMapper(dozerBeanMapper);
            sslTermResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
            sslTermResource.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
            sslTermResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            sslTermResource.setRestApiConfiguration(restApiConfiguration);

        }

        @Test
        public void shouldReturnA200OnSuccess() throws Exception {
            when(sslTerminationService.getSslTermination(anyInt(), anyInt())).thenReturn(null);
            response = sslTermResource.getSsl();
            org.junit.Assert.assertEquals((String) response.getEntity(), 200, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception {
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(null);
            doThrow(EntityNotFoundException.class).when(sslTerminationService).getSslTermination(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any());
            response = sslTermResource.getSsl();
            org.junit.Assert.assertEquals(404, response.getStatus());
        }

        // Ciphers Tests

        @Test
        public void shouldReturnA200WhenReturningEmptyCiphersList() throws Exception {
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(null);
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnCiphersFromHostOfLB() throws Exception {

        }

        @Test
        public void shouldReturnA200WhenReturningDefaultCiphersList() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("soap").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);
            doReturn("a,b,c,d,3des").when(reverseProxyLoadBalancerService).getSsl3CiphersForLB(anyInt());
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSsl3CiphersForLB(1);
            verify(reverseProxyLoadBalancerStmService, times(0)).getSsl3Ciphers();
            verify(reverseProxyLoadBalancerService, times(1)).getSsl3CiphersForLB(1);
        }

        @Test
        public void shouldReturnA200WhenReturningDefaultCiphersListREST() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.adapter_soap_rest);
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);
            doReturn("a,b,c,d,3des").when(reverseProxyLoadBalancerVTMService).getSsl3CiphersForLB(anyInt());
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSsl3CiphersForLB(1);
            verify(reverseProxyLoadBalancerStmService, times(0)).getSsl3Ciphers();
            verify(reverseProxyLoadBalancerService, times(0)).getSsl3CiphersForLB(1);
        }

        @Test
        public void shouldReturnA200WhenReturningDefinedCiphersList() throws Exception {
            SslTermination sslTerm = new SslTermination();
            sslTerm.setCipherList("a,b,c,d,3des");
            when(sslTerminationService.getSslTermination(anyInt(), anyInt())).thenReturn(sslTerm);
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA500WhenReturningDefaultCiphersListFails() throws Exception {
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);
            doThrow(RemoteException.class).when(reverseProxyLoadBalancerService).getSsl3CiphersForLB(anyInt());
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenReturningDefaultCiphersListFails() throws Exception {
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(anyInt(), anyInt())).thenReturn(sslTerm);
            doThrow(EntityNotFoundException.class).when(sslTerminationService).getSslTermination(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any());
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(404, response.getStatus());
        }

        //TODO: Moar Tests
    }
}

