package org.openstack.atlas.api.mgmt.resources;

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
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.impl.CertificateMappingServiceImpl;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;
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
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class SyncResourceTest {

     public static class whenSyncingALoadBalancer{

         @Mock
         MessageDataContainer messageDataContainer;
         @Mock
         CertificateMappingServiceImpl certificateMappingService;
         @Mock
         SslTerminationServiceImpl sslTerminationService;
         @Mock
         LoadBalancerServiceImpl loadBalancerService;
         @Mock
         ManagementAsyncService managementAsyncService;

         @InjectMocks
         SyncResource syncResource;

         LoadBalancer loadBalancer;
         SslTermination sslTermination;
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
         private static String iv;

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
             syncResource.setMockitoAuth(true);
             syncResource.setLoadBalancerId(123);
             syncResource.setLoadBalancerService(loadBalancerService);
             syncResource.setManagementAsyncService(managementAsyncService);
             loadBalancer = new LoadBalancer();
             sslTermination = new SslTermination();
             loadBalancer.setId(123);
             loadBalancer.setAccountId(12345);
             iv = loadBalancer.getAccountId() + "_" + loadBalancer.getId();
             loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
             sslTermination.setSecurePort(443);
             sslTermination.setPrivatekey(workingUserKey);
             sslTermination.setCertificate(workingUserCrt);
             sslTermination.setEnabled(true);
             sslTermination.setSecureTrafficOnly(false);
             loadBalancer.setSslTermination(sslTermination);

             when(loadBalancerService.get(anyInt())).thenReturn(loadBalancer);

         }

        @Test
        public void syncShouldReturn202() throws Exception {
            Response response = syncResource.sync();
            verify(sslTerminationService, times(1)).validatePrivateKey(loadBalancer.getId(),loadBalancer.getAccountId(),sslTermination,true);
            verify(certificateMappingService, times(1)).validatePrivateKeys(loadBalancer, true);
            Assert.assertEquals(202, response.getStatus());

        }
        @Test
        public void syncShouldReturn400() throws Exception {
             loadBalancer.setStatus(LoadBalancerStatus.SUSPENDED);
             Response response = syncResource.sync();
             Assert.assertEquals(400, response.getStatus());
        }
        @Test
        public void syncShouldReturn202WithEncryptedKey() throws Exception {
            sslTermination.setPrivatekey(Aes.b64encryptGCM(workingUserKey.getBytes(), "testCrypto", iv));
            Response response = syncResource.sync();
            Assert.assertEquals(202, response.getStatus());
        }

    }


}
