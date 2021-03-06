package org.openstack.atlas.api.resources;

import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMappings;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ItemNotFound;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.CertificateMappingService;
import org.openstack.atlas.service.domain.services.impl.CertificateMappingServiceImpl;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;

import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.util.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class CertificateMappingResourceTest {
    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";


    public static class WhenRetrievingCertificateMapping {
        private CertificateMappingResource certificateMappingResource;
        private CertificateMappingServiceImpl certificateMappingService;
        private AsyncService asyncService;
        private Response response;

        private ArrayList<CertificateMapping> certificateMappingList;
        private CertificateMapping certificateMapping;


        @Before
        public void setUp() {

            certificateMappingService = mock(CertificateMappingServiceImpl.class);
            asyncService = mock(AsyncService.class);
            certificateMappingResource = new CertificateMappingResource();
            certificateMappingResource.setAccountId(222222);
            certificateMappingResource.setLoadBalancerId(1234);
            certificateMappingResource.setId(1);
            certificateMappingResource.setCertificateMappingService(certificateMappingService);
            certificateMappingResource.setAsyncService(asyncService);

            certificateMappingResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));

            certificateMappingList = new ArrayList<>();
            certificateMapping = new CertificateMapping();
            certificateMapping.setId(1);
            certificateMapping.setHostName("h1.com");
            certificateMapping.setPrivateKey("pkey");
            certificateMapping.setIntermediateCertificate("intCer");
            certificateMapping.setCertificate("c1");
            certificateMappingList.add(certificateMapping);


        }

        @Test
        public void shouldRetrieveCertificateMapping() throws EntityNotFoundException {
            when(certificateMappingService.getByIdAndLoadBalancerId(anyInt(), anyInt())).thenReturn(certificateMapping);

            response = certificateMappingResource.retrieveCertificateMapping();
            Assert.assertEquals(200, response.getStatus());
            org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping res =
                    (org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping) response.getEntity();
            Assert.assertEquals("h1.com", res.getHostName());
            Assert.assertEquals("c1", res.getCertificate());
            Assert.assertEquals("intCer", res.getIntermediateCertificate());
            Assert.assertNull(res.getPrivateKey());
        }

        @Test()
        public void shouldFailRetrievingCertificateMappings() throws EntityNotFoundException {
            doThrow(EntityNotFoundException.class).when(certificateMappingService).getByIdAndLoadBalancerId(anyInt(), anyInt());

            response = certificateMappingResource.retrieveCertificateMapping();
            Assert.assertEquals(404, response.getStatus());
            ItemNotFound res = (ItemNotFound) response.getEntity();
            Assert.assertEquals("Object not Found", res.getMessage());
        }
    }

    public static class WhenUpdatingCertificateMappings {
        private CertificateMappingResource certificateMappingResource;
        private CertificateMappingService certificateMappingService;
        private AsyncService asyncService;
        private Response response;

        private ArrayList<CertificateMapping> certificateMappingList;
        private org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping apiCertificateMapping;

        String privateKey;

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
        public void setUp() {
            privateKey = workingUserKey;

            certificateMappingService = mock(CertificateMappingServiceImpl.class);
            asyncService = mock(AsyncService.class);
            certificateMappingResource = new CertificateMappingResource();
            certificateMappingResource.setAccountId(222222);
            certificateMappingResource.setLoadBalancerId(1234);
            certificateMappingResource.setId(1);
            certificateMappingResource.setCertificateMappingService(certificateMappingService);
            certificateMappingResource.setAsyncService(asyncService);

            certificateMappingResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));

            certificateMappingList = new ArrayList<>();
            CertificateMapping certificateMapping = new CertificateMapping();
            certificateMapping.setId(1);
            certificateMapping.setHostName("h1.com");
            certificateMapping.setPrivateKey(privateKey);
            certificateMapping.setIntermediateCertificate(workingUserChain);
            certificateMapping.setCertificate(workingUserCrt);
            certificateMappingList.add(certificateMapping);

            apiCertificateMapping = new org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping();
            apiCertificateMapping.setHostName("h1.com");
            apiCertificateMapping.setPrivateKey(privateKey);
            apiCertificateMapping.setIntermediateCertificate(workingUserChain);
            apiCertificateMapping.setCertificate(workingUserCrt);

        }

        @Test
        public void shouldFailValidationForCertificateMapping() {
            response = certificateMappingResource.updateCertificateMapping(new org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping());
            Assert.assertEquals(400, response.getStatus());
            BadRequest res = (BadRequest) response.getEntity();
            Assert.assertEquals("Validation Failure", res.getMessage());
        }

        @Test
        public void shouldUpdateCertificateMapping() throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException, LimitReachedException {

            apiCertificateMapping.setHostName("h2.com");
            response = certificateMappingResource.updateCertificateMapping(apiCertificateMapping);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldFailToUpdateCertificateMapping() throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException, LimitReachedException {
            apiCertificateMapping.setPrivateKey("broken");

            response = certificateMappingResource.updateCertificateMapping(apiCertificateMapping);
            Assert.assertEquals(400, response.getStatus());
            BadRequest res = (BadRequest) response.getEntity();
            Assert.assertEquals("Validation Failure", res.getMessage());
        }
    }
}
