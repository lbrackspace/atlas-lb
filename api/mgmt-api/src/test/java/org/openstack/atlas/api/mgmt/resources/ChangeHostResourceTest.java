package org.openstack.atlas.api.mgmt.resources;

import org.bouncycastle.cert.X509CertificateHolder;
import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import sun.net.util.IPAddressUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ChangeHostResourceTest {

    public static class WhenChangingHost {
        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        private ManagementAsyncService asyncService;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        private ChangeHostResource changeHostResource;
        private HostService hostService;
        private HostRepository hostRepository;
        private LoadBalancerService loadBalancerService;
        private VirtualIpService virtualIpService;
        private OperationResponse operationResponse;
        private Host host;
        private Host host2;
        private Hostssubnet hostssubnet1;
        private Hostssubnet hostssubnet2;
        private LoadBalancer loadBalancer;
        private LoadBalancer loadBalancer2;
        private SslTermination sslTermination;
        private SslTermination sslTermination2;

        @Mock
        private RestApiConfiguration config;


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
        private String encryptedKey;
        private String iv;
        private String encryptedKey2;
        private String iv2;

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
        public void setUp() throws EntityNotFoundException, UnprocessableEntityException, IOException, VTMRestClientException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, DecryptException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
            MockitoAnnotations.initMocks(this);
            changeHostResource = new ChangeHostResource();
            changeHostResource.setMockitoAuth(true);
            HostRepository hrepo = mock(HostRepository.class);
            asyncService = mock(ManagementAsyncService.class);
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            changeHostResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            hostService = mock(HostService.class);
            changeHostResource.setManagementAsyncService(asyncService);
            changeHostResource.setHostRepository(hrepo);
            changeHostResource.setHostService(hostService);
            hostRepository = mock(HostRepository.class);
            changeHostResource.setHostRepository(hostRepository);
            loadBalancerService = mock(LoadBalancerService.class);
            changeHostResource.setLoadBalancerService(loadBalancerService);
            virtualIpService = mock(VirtualIpService.class);
            changeHostResource.setVirtualIpService(virtualIpService);
            changeHostResource.setConfiguration(config);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);

            changeHostResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            host = new Host();
            host.setMaxConcurrentConnections(2);
            host.setHostStatus(HostStatus.ACTIVE);
            host.setId(1);

            host2 = new Host();
            host2.setMaxConcurrentConnections(2);
            host2.setHostStatus(HostStatus.ACTIVE);
            host2.setId(2);

            loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(12345);
            loadBalancer.setHost(host);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);

            iv = loadBalancer.getAccountId() + "_" + loadBalancer.getId() + "_1";
            encryptedKey = Aes.b64encryptGCM(workingUserKey.getBytes(), "testCrypto", iv);

            sslTermination = new SslTermination();
            sslTermination.setPrivatekey(encryptedKey);
            sslTermination.setCertificate(workingUserCrt);
            sslTermination.setIntermediateCertificate(workingUserChain);
            sslTermination.setId(1);

            VirtualIp vip1 = new VirtualIp();
            vip1.setIpAddress("192.168.0.10");
            Set<LoadBalancerJoinVip> vipset = new HashSet<>();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            jv.setVirtualIp(vip1);
            vipset.add(jv);
            loadBalancer.setLoadBalancerJoinVipSet(vipset);

            loadBalancer2 = new LoadBalancer();
            loadBalancer2.setId(2);
            loadBalancer2.setAccountId(54321);
            loadBalancer2.setHost(host2);
            loadBalancer2.setStatus(LoadBalancerStatus.ACTIVE);

            iv2 = loadBalancer2.getAccountId() + "_" + loadBalancer2.getId() + "_2";
            encryptedKey2 = Aes.b64encryptGCM(workingUserKey.getBytes(), "testCrypto", iv2);

            sslTermination2 = new SslTermination();
            sslTermination2.setPrivatekey(encryptedKey2);
            sslTermination2.setCertificate(workingUserCrt);
            sslTermination2.setIntermediateCertificate(workingUserChain);
            sslTermination2.setId(2);

            VirtualIp vip2 = new VirtualIp();
            vip2.setIpAddress("192.168.0.8");
            Set<LoadBalancerJoinVip> vipset2 = new HashSet<>();
            LoadBalancerJoinVip jv2 = new LoadBalancerJoinVip();
            jv2.setVirtualIp(vip2);
            vipset2.add(jv2);
            loadBalancer2.setLoadBalancerJoinVipSet(vipset2);

            when(hrepo.getById(anyInt())).thenReturn(host);
            when(hostService.getById(1)).thenReturn(host);
            when(hostService.getById(2)).thenReturn(host2);
            when(hostRepository.getDefaultActiveHost(anyInt())).thenReturn(host);
            when(loadBalancerService.get(1)).thenReturn(loadBalancer);
            when(loadBalancerService.get(2)).thenReturn(loadBalancer2);
            when(loadBalancerService.testAndSetStatus(any(), any())).thenReturn(true);
            when(hrepo.getNumberOfUniqueAccountsForHost(anyInt())).thenReturn(3);
            when(hrepo.getActiveLoadBalancerForHost(anyInt())).thenReturn((long) 3);
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

            hostssubnet1 = new Hostssubnet();
            Hostsubnet hs1 = new Hostsubnet();
            List<Hostsubnet> hostsubnetList1 = new ArrayList<>();
            NetInterface ni1 = new NetInterface();
            Cidr ci1 = new Cidr();
            ci1.setBlock("192.168.0.0/24");
            ni1.getCidrs().add(ci1);
            hs1.getNetInterfaces().add(ni1);
            hostsubnetList1.add(hs1);
            hostssubnet1.setHostsubnets(hostsubnetList1);

            hostssubnet2 = new Hostssubnet();
            Hostsubnet hs2 = new Hostsubnet();
            List<Hostsubnet> hostsubnetList2 = new ArrayList<>();
            NetInterface ni2 = new NetInterface();
            Cidr ci2 = new Cidr();
            ci2.setBlock("172.168.0.0/24");
            ni2.getCidrs().add(ci2);
            hs2.getNetInterfaces().add(ni2);
            hostsubnetList2.add(hs2);
            hostssubnet2.setHostsubnets(hostsubnetList2);

            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(host)).thenReturn(hostssubnet1);
            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(host2)).thenReturn(hostssubnet2);

            when(config.getString(PublicApiServiceConfigurationKeys.term_crypto_key)).thenReturn("testCrypto");


        }

        @Test
        public void shouldMoveSimpleLB() throws Exception {
            changeHostResource.setLoadBalancerId(2);
            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(202, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
            verify(asyncService,
                    times(1)).callAsyncLoadBalancingOperation(eq(Operation.CHANGE_HOST),
                    any(MessageDataContainer.class));
        }

        @Test
        public void shouldMoveSimpleSharedLBs() throws Exception {
            changeHostResource.setLoadBalancerId(2);
            List<LoadBalancer> sharedLbs = new ArrayList<>();
            sharedLbs.add(loadBalancer);
            when(virtualIpService.isVipAllocatedToMultipleLoadBalancers(any())).thenReturn(true);
            when(loadBalancerService.testAndSetStatus(any(), any())).thenReturn(true);
            when(virtualIpService.getLoadBalancerByVipId(any())).thenReturn(sharedLbs);
            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(202, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
            verify(asyncService,
                    times(1)).callAsyncLoadBalancingOperation(eq(Operation.CHANGE_HOST),
                    any(MessageDataContainer.class));
        }

        @Test
        public void shouldMoveLBWithSslTermination() throws Exception {
            changeHostResource.setLoadBalancerId(2);
            loadBalancer2.setSslTermination(sslTermination2);
            when(loadBalancerService.get(2)).thenReturn(loadBalancer2);

            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(202, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
            verify(asyncService,
                    times(1)).callAsyncLoadBalancingOperation(eq(Operation.CHANGE_HOST),
                    any(MessageDataContainer.class));
        }

        @Test
        public void shouldFailToMoveLBWithBrokenCertSslTermination() throws Exception {
            changeHostResource.setLoadBalancerId(2);
            sslTermination2.setCertificate("broken");
            loadBalancer2.setSslTermination(sslTermination2);
            when(loadBalancerService.get(2)).thenReturn(loadBalancer2);

            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(400, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host);
            verify(asyncService,
                    times(0)).callAsyncLoadBalancingOperation(eq(Operation.CHANGE_HOST),
                    any(MessageDataContainer.class));
            Assert.assertEquals("This operation will result in this Load Balancer (or another LB sharing a VIP)  entering ERROR status, as the sslTermination is invalid. Consider deleting the sslTermination on this LB before attempting to change hosts.", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(0));
            Assert.assertEquals("Unable to read userCrt", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(2));

        }

        @Test
        public void shouldFailToMoveLBWithBrokenKeySslTermination() throws Exception {
            changeHostResource.setLoadBalancerId(2);
            sslTermination2.setPrivatekey("broken");
            loadBalancer2.setSslTermination(sslTermination2);
            when(loadBalancerService.get(2)).thenReturn(loadBalancer2);

            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(400, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host);
            verify(asyncService,
                    times(0)).callAsyncLoadBalancingOperation(eq(Operation.CHANGE_HOST),
                    any(MessageDataContainer.class));
            Assert.assertEquals("SSL Termination key decryption failed for LB #2", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(1));

        }

        @Test
        public void shouldFailToMoveSimpleLBIfVipNotOnHost() throws Exception {
            changeHostResource.setLoadBalancerId(1);
            when(hostService.getById(2)).thenReturn(host2);
            Response resp = changeHostResource.changeHost(2, null);
            Assert.assertEquals(400, resp.getStatus());
            Assert.assertEquals("The loadbalancer's IP address is not found in the target host machine subnet mappings.", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldFailToMoveSimpleLBToSameHost() throws Exception {
            changeHostResource.setLoadBalancerId(1);
            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(400, resp.getStatus());
            Assert.assertEquals("The supplied newHostId is the same as the Load Balancer's existing HostID. No action will be performed.", ((BadRequest) resp.getEntity()).getMessage());
        }

        @Test
        public void shouldRejectSuspendedLB() throws Exception {
            changeHostResource.setLoadBalancerId(1);
            loadBalancer.setStatus(LoadBalancerStatus.SUSPENDED);
            when(loadBalancerService.get(1)).thenReturn(loadBalancer);

            Response resp = changeHostResource.changeHost(2, null);
            Assert.assertEquals(400, resp.getStatus());
            Assert.assertEquals("This Load Balancer (or another LB sharing VIP) is Suspended, Please Check With Operations For Further Information...", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldFailToGetPendingLock() throws Exception {
            when(loadBalancerService.testAndSetStatus(any(), any())).thenReturn(false);

            changeHostResource.setLoadBalancerId(2);

            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(400, resp.getStatus());
            Assert.assertEquals("Can't lock LB. No action will be performed.", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldFailToGetPendingLockForSharedLB() throws Exception {
            changeHostResource.setLoadBalancerId(2);
            List<LoadBalancer> sharedLbs = new ArrayList<>();
            sharedLbs.add(loadBalancer);
            when(virtualIpService.isVipAllocatedToMultipleLoadBalancers(any())).thenReturn(true);
            when(loadBalancerService.testAndSetStatus(any(), any())).thenReturn(false);
            when(virtualIpService.getLoadBalancerByVipId(any())).thenReturn(sharedLbs);

            Response resp = changeHostResource.changeHost(1, null);
            Assert.assertEquals(400, resp.getStatus());
            Assert.assertEquals("This Load Balancer uses a shared VIP, and a lock could not be established on all LBs sharing that VIP. No action will be performed.", ((BadRequest) resp.getEntity()).getValidationErrors().getMessages().get(0));
        }


    }


//    public static class WhenAddingOrRemovingSubnets {
//        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
//
//        private ManagementAsyncService asyncService;
//        private HostResource hostResource;
//        private OperationResponse operationResponse;
//        private Hostsubnet hsub;
//        private Hostssubnet hsubs;
//        private NetInterface ni;
//        private Cidr cidr;
//        private Hostsubnet hsub2;
//        private Hostssubnet hsubs2;
//        private NetInterface ni2;
//        private Cidr cidr2;
//        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
//        private HostService hostService;
//        private Host host;
//        private org.openstack.atlas.service.domain.pojos.Hostssubnet hostssubnet;
//        private org.openstack.atlas.service.domain.pojos.NetInterface netInterface;
//        private org.openstack.atlas.service.domain.pojos.NetInterface netInterface2;
//        private org.openstack.atlas.service.domain.pojos.Cidr cidr3;
//        // TODO: Refactor rest for annotations
//        @Mock
//        private RestApiConfiguration config;
//
//        @Before
//        public void setUp() throws EntityNotFoundException {
//            MockitoAnnotations.initMocks(this);
//            hostResource = new HostResource();
//            hostResource.setMockitoAuth(true);
//            HostRepository hrepo = mock(HostRepository.class);
//            asyncService = mock(ManagementAsyncService.class);
//            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
//            hostService = mock(HostService.class);
//            hostResource.setManagementAsyncService(asyncService);
//            hostResource.setHostService(hostService);
//            hostResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
//            hostResource.setConfiguration(config);
//
//            hostResource.setId(1);
//            hostResource.setHostRepository(hrepo);
//            operationResponse = new OperationResponse();
//            operationResponse.setExecutedOkay(true);
//            hostResource.setDozerMapper(DozerBeanMapperBuilder.create()
//                    .withMappingFiles(mappingFile)
//                    .build());
//            host = new Host();
//            host.setMaxConcurrentConnections(2);
//            host.setHostStatus(HostStatus.ACTIVE);
//            netInterface = new org.openstack.atlas.service.domain.pojos.NetInterface();
//            netInterface2 = new org.openstack.atlas.service.domain.pojos.NetInterface();
//            cidr3 = new org.openstack.atlas.service.domain.pojos.Cidr();
//            cidr3.setBlock("123.78.1.2/27");
//            hostssubnet = new org.openstack.atlas.service.domain.pojos.Hostssubnet();
//            org.openstack.atlas.service.domain.pojos.Hostsubnet h1 = new org.openstack.atlas.service.domain.pojos.Hostsubnet();
//            h1.setName("t1");
//            hostssubnet.getHostsubnets().add(h1);
//
//            h1.getNetInterfaces().add(netInterface);
//            h1.getNetInterfaces().add(netInterface2);
//
//            when(hrepo.getById(anyInt())).thenReturn(host);
//            when(hostService.getById(anyInt())).thenReturn(host);
//            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
//
//        }
//
//        @Before
//        public void standUpSubnet() {
//            hsub = new Hostsubnet();
//            hsubs = new Hostssubnet();
//            ni = new NetInterface();
//            cidr = new Cidr();
//            ni.setName("name");
//
//            hsub2 = new Hostsubnet();
//            hsubs2 = new Hostssubnet();
//            ni2 = new NetInterface();
//            cidr2 = new Cidr();
//            ni2.setName("name2");
//            cidr2.setBlock("123.78.1.1/27");
//            ni2.getCidrs().add(cidr2);
//            hsub2.getNetInterfaces().add(ni2);
//            hsubs2.getHostsubnets().add(hsub2);
//        }
//
//        @Test
//        public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv6() throws Exception {
//             cidr.setBlock("fe80::200:f8ff:fe21:67cf/16");
//
//            ni.getCidrs().add(cidr);
//            hsub.getNetInterfaces().add(ni);
//            hsubs.getHostsubnets().add(hsub);
//            Response resp = hostResource.putHostsSubnetMappings(hsubs, false, false);
//            Assert.assertEquals(202, resp.getStatus());
//            verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
//            verify(asyncService, times(1)).callAsyncLoadBalancingOperation(
//                    eq(Operation.SET_HOST_SUBNET_MAPPINGS), any(EsbRequest.class));        }
//
//        @Test
//        public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv4() throws Exception {
//            cidr.setBlock("192.168.0.1/24");
//
//            ni.getCidrs().add(cidr);
//            hsub.getNetInterfaces().add(ni);
//            hsubs.getHostsubnets().add(hsub);
//            Response resp = hostResource.putHostsSubnetMappings(hsubs, false, false);
//            Assert.assertEquals(202, resp.getStatus());
//            verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
//            verify(asyncService, times(1)).callAsyncLoadBalancingOperation(
//                    eq(Operation.SET_HOST_SUBNET_MAPPINGS), any(EsbRequest.class));
//        }
//
//        @Test
//        public void shouldFailWithMultipleSubnets() throws Exception {
//            cidr.setBlock("192.168.0.1/24");
//
//            ni.getCidrs().add(cidr);
//            hsub.getNetInterfaces().add(ni);
//            hsubs.getHostsubnets().add(hsub);
//            hsubs.getHostsubnets().add(hsub);
//            Response resp = hostResource.putHostsSubnetMappings(hsubs, false, false);
//            Assert.assertEquals(400, resp.getStatus());
//            verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
//            verify(asyncService, times(0)).callAsyncLoadBalancingOperation(
//                    eq(Operation.SET_HOST_SUBNET_MAPPINGS), any(EsbRequest.class));
//        }
//
//        @Test
//        public void shouldReturn404WhenNoMatchingSubnetMappingsFound() throws Exception {
//            netInterface.setName("name3");
//            netInterface2.setName("name4");
//            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);
//
//            Response response = hostResource.delHostsSubnetMappings(hsubs2);
//            Assert.assertEquals(404, response.getStatus());
//        }
//
//        @Test
//        public void shouldReturn200WhenMatchingSubnetMappingsFoundInList() throws Exception {
//            netInterface.setName("name2");
//            netInterface2.setName("name");
//            hsub2.getNetInterfaces().add(ni);
//            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);
//
//            Response response = hostResource.delHostsSubnetMappings(hsubs2);
//            Assert.assertEquals(200, response.getStatus());
//        }
//
//        @Test
//        public void shouldReturn404IfOneNetinterfaceDoesNotMatch() throws Exception {
//            netInterface.setName("name2");
//            netInterface2.setName("brokenName");
//            hsub2.getNetInterfaces().add(ni);
//            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);
//
//            Response response = hostResource.delHostsSubnetMappings(hsubs2);
//            Assert.assertEquals(404, response.getStatus());
//        }
//
//        @Test
//        public void shouldReturn200WhenMatchingSubnetMappingsFound() throws Exception {
//            netInterface.setName("name2");
//            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);
//
//            Response response = hostResource.delHostsSubnetMappings(hsubs2);
//            Assert.assertEquals(200, response.getStatus());
//        }
//        @Test
//        public void shouldReturn404WhenNoSubnetMappingsFound() throws Exception {
//            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);
//
//            Response response = hostResource.delHostsSubnetMappings(hsubs2);
//            Assert.assertEquals(404, response.getStatus());
//        }
//
//        @Test
//        public void shouldIpv6Pass() {
//            Assert.assertTrue(IPAddressUtil.isIPv6LiteralAddress("fe80::200:f8ff:fe21:67cf"));
//            Assert.assertTrue(IPAddressUtil.isIPv6LiteralAddress("3ffe:1900:4545:3:200:f8ff:fe21:67cf"));
//            Assert.assertTrue(IPAddressUtil.isIPv6LiteralAddress("FE80:0000:0000:0000:0202:B3FF:FE1E:8329"));
//
//        }
//        @Test
//        public void shouldIpv6Fail() {
//            Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("fe80::200:f8ff:fe21:67cfffffffalfaldf"));
//            Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("000fe80::200:f8ff:fe21:67cff"));
//            Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("fe80::200:::f8ff:fe21:67cff"));
//            Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("fe80::200:f8ff:fe21:67cff/166666699909"));
//        }
//        @Test
//        public void shouldIpv4Pass() {
//            Assert.assertTrue(IPAddressUtil.isIPv4LiteralAddress("192.168.0.0"));
//        }
//        @Test
//        public void shouldIpv4Fail() {
//            Assert.assertFalse(IPAddressUtil.isIPv4LiteralAddress("192.168.1.1.111"));
//        }
//    }
//
//
//    public static class WhenEnablingDisablingEndpoint {
//        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
//
//        private HostResource hostResource;
//        private HostService hostService;
//        // TODO: Refactor rest for annotations
//        @Mock
//        private RestApiConfiguration config;
//
//        @Before
//        public void setUp() throws EntityNotFoundException {
//            MockitoAnnotations.initMocks(this);
//            hostResource = new HostResource();
//            hostResource.setMockitoAuth(true);
//            HostRepository hrepo = mock(HostRepository.class);
//            hostService = mock(HostService.class);
//            hostResource.setHostService(hostService);
//            hostResource.setConfiguration(config);
//
//            hostResource.setId(1);
//            hostResource.setHostRepository(hrepo);
//
//            doNothing().when(hostService).updateHost(anyObject());
//            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
//
//        }
//
//        @Test
//        public void shouldEnableRestEndpoint() throws Exception {
//            Response resp = hostResource.enableEndPoint();
//            Assert.assertEquals(200, resp.getStatus());
//
//            ArgumentCaptor<Host> argument = ArgumentCaptor.forClass(Host.class);
//            verify(hostService, times(1)).updateHost(argument.capture());
//            Assert.assertEquals(1, argument.getValue().getId().intValue());
//            Assert.assertTrue(argument.getValue().getRestEndpointActive());
//            Assert.assertNull(argument.getValue().getSoapEndpointActive());
//        }
//
//        @Test
//        public void shouldReturn500FailToEnableRestEndpoint() throws Exception {
//            doThrow(Exception.class).when(hostService).updateHost(anyObject());
//
//            Response resp = hostResource.enableEndPoint();
//            Assert.assertEquals(500, resp.getStatus());
//
//            ArgumentCaptor<Host> argument = ArgumentCaptor.forClass(Host.class);
//            verify(hostService, times(1)).updateHost(argument.capture());
//            Assert.assertEquals(1, argument.getValue().getId().intValue());
//            Assert.assertTrue(argument.getValue().getRestEndpointActive());
//            Assert.assertNull(argument.getValue().getSoapEndpointActive());
//        }
//
//        @Test
//        public void shouldDisableRestEndpoint() throws Exception {
//            Response resp = hostResource.disableEndPoint();
//            Assert.assertEquals(200, resp.getStatus());
//
//            ArgumentCaptor<Host> argument = ArgumentCaptor.forClass(Host.class);
//            verify(hostService, times(1)).updateHost(argument.capture());
//            Assert.assertEquals(1, argument.getValue().getId().intValue());
//            Assert.assertFalse(argument.getValue().getRestEndpointActive());
//            Assert.assertNull(argument.getValue().getSoapEndpointActive());
//        }
//
//        @Test
//        public void shouldReturn500FailToDisableRestEndpoint() throws Exception {
//            doThrow(Exception.class).when(hostService).updateHost(anyObject());
//
//            Response resp = hostResource.disableEndPoint();
//            Assert.assertEquals(500, resp.getStatus());
//
//            ArgumentCaptor<Host> argument = ArgumentCaptor.forClass(Host.class);
//            verify(hostService, times(1)).updateHost(argument.capture());
//            Assert.assertEquals(1, argument.getValue().getId().intValue());
//            Assert.assertFalse(argument.getValue().getRestEndpointActive());
//            Assert.assertNull(argument.getValue().getSoapEndpointActive());
//        }
//    }
}

