package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.repository.*;
import org.openstack.atlas.service.domain.services.impl.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(Enclosed.class)
public class LoadBalancerServiceImplTest {

    public static class WhenCheckingForSharedVips {

        private LoadBalancer lb;

        @Mock
        VirtualIpService virtualIpService;

        @InjectMocks
        LoadBalancerServiceImpl lbService;

        @Before
        public void standUp() {
            MockitoAnnotations.initMocks(this);
            lb = new LoadBalancer();
        }

        @Test
        public void ShouldReturnVerifyIfSharedVip() {
            when(virtualIpService.isVipAllocatedToAnotherLoadBalancer(any(), any())).thenReturn(Boolean.TRUE);
            Assert.assertTrue(lbService.isSharedVip4(lb, new VirtualIp()));

            when(virtualIpService.isVipAllocatedToAnotherLoadBalancer(any(), any())).thenReturn(Boolean.FALSE);
            Assert.assertFalse(lbService.isSharedVip4(lb, new VirtualIp()));
        }

        @Test
        public void ShouldReturnVerifyIfSharedVip6() {
            when(virtualIpService.isIpv6VipAllocatedToAnotherLoadBalancer(any(), any())).thenReturn(Boolean.TRUE);
            Assert.assertTrue(lbService.isSharedVip6(lb, new VirtualIpv6()));

            when(virtualIpService.isIpv6VipAllocatedToAnotherLoadBalancer(any(), any())).thenReturn(Boolean.FALSE);
            Assert.assertFalse(lbService.isSharedVip6(lb, new VirtualIpv6()));
        }
    }

    public static class WhenCheckingIfLoadBalancerLimitIsReached {
        Integer accountId = 1234;
        LoadBalancerRepository lbRepository;
        AccountLimitRepository lbLimitRepository;
        LoadBalancerServiceImpl lbService;

        @Before
        public void standUp() {
            lbRepository = mock(LoadBalancerRepository.class);
            lbLimitRepository = mock(AccountLimitRepository.class);
            lbService = new LoadBalancerServiceImpl();
            lbService.setLoadBalancerRepository(lbRepository);
            lbService.setAccountLimitRepository(lbLimitRepository);
        }

        @Test
        @Ignore
        public void shouldReturnFalseWhenBelowLoadBalancerLimit() throws EntityNotFoundException {
//            LoadBalancerLimitGroup lbLimitGroup = new LoadBalancerLimitGroup();
//            lbLimitGroup.setLimit(100);
            Integer numNonDeletedLoadBalancers = 1;

//            when(lbLimitRepository.getByAccountId(Matchers.<Integer>any())).thenReturn(lbLimitGroup);
            when(lbRepository.getNumNonDeletedLoadBalancersForAccount(Matchers.<Integer>any())).thenReturn(numNonDeletedLoadBalancers);

            Assert.assertFalse(lbService.isLoadBalancerLimitReached(accountId));
        }

        @Test
        @Ignore
        public void shouldReturnFalseWhenBelowLoadBalancerLimitByOne() throws EntityNotFoundException {
//            LoadBalancerLimitGroup lbLimitGroup = new LoadBalancerLimitGroup();
//            lbLimitGroup.setLimit(100);
            Integer numNonDeletedLoadBalancers = 99;

//            when(lbLimitRepository.getByAccountId(Matchers.<Integer>any())).thenReturn(lbLimitGroup);
            when(lbRepository.getNumNonDeletedLoadBalancersForAccount(Matchers.<Integer>any())).thenReturn(numNonDeletedLoadBalancers);

            Assert.assertFalse(lbService.isLoadBalancerLimitReached(accountId));
        }

        @Test
        @Ignore
        public void shouldReturnTrueWhenAtLoadBalancerLimit() throws EntityNotFoundException {
//            LoadBalancerLimitGroup lbLimitGroup = new LoadBalancerLimitGroup();
//            lbLimitGroup.setLimit(100);
            Integer numNonDeletedLoadBalancers = 100;

//            when(lbLimitRepository.getByAccountId(Matchers.<Integer>any())).thenReturn(lbLimitGroup);
            when(lbRepository.getNumNonDeletedLoadBalancersForAccount(Matchers.<Integer>any())).thenReturn(numNonDeletedLoadBalancers);

            Assert.assertTrue(lbService.isLoadBalancerLimitReached(accountId));
        }

        @Test
        @Ignore
        public void shouldReturnTrueWhenOverLoadBalancerLimit() throws EntityNotFoundException {
//            LoadBalancerLimitGroup lbLimitGroup = new LoadBalancerLimitGroup();
//            lbLimitGroup.setLimit(100);
            Integer numNonDeletedLoadBalancers = 9999;

//            when(lbLimitRepository.getByAccountId(Matchers.<Integer>any())).thenReturn(lbLimitGroup);
            when(lbRepository.getNumNonDeletedLoadBalancersForAccount(Matchers.<Integer>any())).thenReturn(numNonDeletedLoadBalancers);

            Assert.assertTrue(lbService.isLoadBalancerLimitReached(accountId));
        }

        @Test
        @Ignore
        public void shouldReturnTrueWhenOverLoadBalancerLimitByOne() throws EntityNotFoundException {
//            LoadBalancerLimitGroup lbLimitGroup = new LoadBalancerLimitGroup();
//            lbLimitGroup.setLimit(100);
            Integer numNonDeletedLoadBalancers = 101;

//            when(lbLimitRepository.getByAccountId(Matchers.<Integer>any())).thenReturn(lbLimitGroup);
            when(lbRepository.getNumNonDeletedLoadBalancersForAccount(Matchers.<Integer>any())).thenReturn(numNonDeletedLoadBalancers);

            Assert.assertTrue(lbService.isLoadBalancerLimitReached(accountId));
        }
    }

    public static class WhenAddingDefaultValues {
        private LoadBalancer lb;
        LoadBalancerRepository lbRepository;
        LoadBalancerServiceImpl lbService;
        LoadBalancerProtocolObject defaultProtocol;

        @Before
        public void standUp() {
            lb = new LoadBalancer();
            lbRepository = mock(LoadBalancerRepository.class);
            lbService = new LoadBalancerServiceImpl();
            lbService.setLoadBalancerRepository(lbRepository);

            defaultProtocol = new LoadBalancerProtocolObject(LoadBalancerProtocol.HTTP, "HTTP Protocol", 80, true);
            when(lbRepository.getDefaultProtocol()).thenReturn(defaultProtocol);
        }

        @Test
        public void shouldAddDefaultValuesWhenNoValuesAreSet() {
            lbService.addDefaultValues(lb);

            Assert.assertEquals(LoadBalancerAlgorithm.RANDOM, lb.getAlgorithm());
            Assert.assertEquals(LoadBalancerProtocol.HTTP, lb.getProtocol());
            Assert.assertFalse(lb.isConnectionLogging());
            Assert.assertEquals(defaultProtocol.getPort(), lb.getPort());
            Assert.assertEquals(SessionPersistence.NONE, lb.getSessionPersistence());
            Assert.assertEquals(LoadBalancerStatus.BUILD, lb.getStatus());
            Assert.assertEquals(false, lb.getHalfClosed());
        }

        @Test
        public void shouldNotAddDefaultValuesWhenValuesAreSet() {
            lb.setAlgorithm(LoadBalancerAlgorithm.LEAST_CONNECTIONS);
            lb.setProtocol(LoadBalancerProtocol.IMAPv3);
            lb.setConnectionLogging(true);
            lb.setPort(1234);
            lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);

            lbService.addDefaultValues(lb);

            Assert.assertEquals(LoadBalancerAlgorithm.LEAST_CONNECTIONS, lb.getAlgorithm());
            Assert.assertEquals(LoadBalancerProtocol.IMAPv3, lb.getProtocol());
            Assert.assertTrue(lb.isConnectionLogging());
            Assert.assertEquals(1234, lb.getPort().intValue());
            Assert.assertEquals(SessionPersistence.HTTP_COOKIE, lb.getSessionPersistence());
        }

        @Test
        public void shouldSetStatusToBuildWhenStatusIsModified() {
            lb.setStatus(LoadBalancerStatus.ERROR);

            lbService.addDefaultValues(lb);

            Assert.assertEquals(LoadBalancerStatus.BUILD, lb.getStatus());
        }

        @Test
        public void shouldUpdateNodesStatusAndWeightsAppropriately() {
            Set<Node> nodes = new HashSet<Node>();
            Node node1 = new Node();
            Node node2 = new Node();
            Node node3 = new Node();

            node1.setCondition(NodeCondition.ENABLED);
            node2.setCondition(NodeCondition.DRAINING);
            node3.setCondition(NodeCondition.DISABLED);
            node1.setWeight(null);
            node2.setWeight(0);
            node3.setWeight(10);
            nodes.add(node1);
            nodes.add(node2);
            nodes.add(node3);
            lb.setNodes(nodes);

            lbService.addDefaultValues(lb);

            Assert.assertEquals(NodeStatus.ONLINE, node1.getStatus());
            Assert.assertEquals(NodeStatus.ONLINE, node2.getStatus());
            Assert.assertEquals(NodeStatus.ONLINE, node3.getStatus());

            Assert.assertEquals(1, node1.getWeight().intValue());
            Assert.assertEquals(0, node2.getWeight().intValue());
            Assert.assertEquals(10, node3.getWeight().intValue());
        }
    }

    public static class WhenVerifyingSslTermination {
        private LoadBalancer lb;
        LoadBalancerRepository lbRepository;
        LoadBalancerServiceImpl lbService;
        LoadBalancerProtocolObject defaultProtocol;

        @Before
        public void standUp() throws EntityNotFoundException, UnprocessableEntityException {
            lb = new LoadBalancer();
            lbRepository = mock(LoadBalancerRepository.class);
            lbService = new LoadBalancerServiceImpl();
            lbService.setLoadBalancerRepository(lbRepository);


            SslTermination sslTermination = new SslTermination();
            sslTermination.setIntermediateCertificate("iCert");
            sslTermination.setCertificate("cert");
            sslTermination.setPrivatekey("aKey");
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(445);
            sslTermination.setSecureTrafficOnly(false);

            lb.setSslTermination(sslTermination);
            lb.setStatus(LoadBalancerStatus.ACTIVE);


            defaultProtocol = new LoadBalancerProtocolObject(LoadBalancerProtocol.HTTP, "HTTP Protocol", 80, true);
            when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(lb);
//            when(lbRepository.testAndSetStatus(Matchers.<Integer>any(), Matchers.<Integer>any(),Matchers.<LoadBalancerStatus>any(), Matchers.<Boolean>any())).thenReturn(true);

        }

        @Test(expected = BadRequestException.class)
        public void shouldRejectUpdateProtocolIfUsingSslTermination() throws Exception {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTPS);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);

            lbService.prepareForUpdate(loadBalancer);

        }

        @Test(expected = BadRequestException.class)
        public void shouldFailWhenUpdatingPortToSSLPort() throws Exception {

            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setPort(445);

            lbService.prepareForUpdate(loadBalancer);

        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenVerifyingProtocols {
        private LoadBalancer lb;
        @Mock
        LoadBalancerRepository lbRepository;
        @Mock
        LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;
        @Mock
        LoadBalancerStatusHistoryServiceImpl loadBalancerStatusHistoryService;

        @InjectMocks
        LoadBalancerServiceImpl lbService;

        LoadBalancerProtocolObject defaultProtocol;

        @Before
        public void standUp() throws EntityNotFoundException, UnprocessableEntityException {
            MockitoAnnotations.initMocks(this);

            lb = new LoadBalancer();
//            lbService = new LoadBalancerServiceImpl();
//            lbService.setLoadBalancerRepository(lbRepository);
//            lbService.setLoadBalancerStatusHistoryRepository(loadBalancerStatusHistoryRepository);
//            loadBalancerStatusHistoryServiceimpl.setLoadBalancerStatusHistoryRepository(loadBalancerStatusHistoryRepository);

            HealthMonitor hm = new HealthMonitor();
            hm.setType(HealthMonitorType.CONNECT);
            lb.setHealthMonitor(hm);

            lb.setId(1);
            lb.setAccountId(11111);
            lb.setStatus(LoadBalancerStatus.ACTIVE);
            lb.setProtocol(LoadBalancerProtocol.HTTPS);
            lb.setHalfClosed(false);

            defaultProtocol = new LoadBalancerProtocolObject(LoadBalancerProtocol.HTTP, "HTTP Protocol", 80, true);
            when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(lb);
            when(lbRepository.testAndSetStatus(Matchers.<Integer>anyInt(), Matchers.<Integer>anyInt(), Matchers.anyObject(), Matchers.<Boolean>anyBoolean())).thenReturn(true);
            when(lbRepository.update(Matchers.anyObject())).thenReturn(lb);
            when(loadBalancerStatusHistoryService.save(Matchers.anyInt(),Matchers.anyInt(), Matchers.anyObject())).thenReturn(new LoadBalancerStatusHistory());

        }

        @Test()
        public void shouldAcceptUpdateNonUDPProtocolIfUsingHealthMonitor() throws Exception {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(11111);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTPS);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setUserName("bob");

            lbService.prepareForUpdate(loadBalancer);
            Mockito.verify(lbRepository, times(1)).update(lb);

        }

        @Test(expected = BadRequestException.class)
        public void shouldRejectUpdateUDPProtocolIfUsingHealthMonitor() throws Exception {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(11111);
            loadBalancer.setPort(20);
            loadBalancer.setProtocol(LoadBalancerProtocol.UDP);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setUserName("bob");

            when(lbRepository.canUpdateToNewPort(Matchers.anyInt(), Matchers.anyObject())).thenReturn(true);

            lbService.prepareForUpdate(loadBalancer);
        }

        @Test(expected = BadRequestException.class)
        public void shouldRejectUpdateToHTTPSProtocolIfContentCachingEnabled() throws Exception {
            // Allowing content caching for non-http loadbalancer is aa bug, CLB-1007
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(11111);
            loadBalancer.setPort(20);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTPS);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setUserName("bob");

            lb.setHealthMonitor(null);
            lb.setContentCaching(Boolean.TRUE);
            when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(lb);

            when(lbRepository.canUpdateToNewPort(Matchers.anyInt(), Matchers.anyObject())).thenReturn(true);

            lbService.prepareForUpdate(loadBalancer);
        }

        @Test(expected = BadRequestException.class)
        public void shouldRejectUpdateToUDPProtocolIfContentCachingEnabled() throws Exception {
            // Allowing content caching for non-http loadbalancer is aa bug, CLB-1007
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(11111);
            loadBalancer.setPort(20);
            loadBalancer.setProtocol(LoadBalancerProtocol.UDP);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setUserName("bob");

            lb.setHealthMonitor(null);
            lb.setContentCaching(Boolean.TRUE);
            when(lbRepository.getByIdAndAccountId(Matchers.<Integer>any(), Matchers.<Integer>any())).thenReturn(lb);

            when(lbRepository.canUpdateToNewPort(Matchers.anyInt(), Matchers.anyObject())).thenReturn(true);

            lbService.prepareForUpdate(loadBalancer);
        }

        @Test
        public void shouldRemoveErrorPageWhenProtocolChangedToNonHttp() throws Exception {
            UserPages up = new UserPages();
            up.setErrorpage("Custom Error page");
            lb.setUserPages(up);//dbLoadBalancer
            lb.setProtocol(LoadBalancerProtocol.HTTP);

            //new changes to update the lb
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(11111);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTPS);

            when(lbRepository.removeErrorPage(Matchers.anyInt(), Matchers.anyObject())).thenReturn(true);
            lbService.prepareForUpdate(loadBalancer);
            Mockito.verify(lbRepository, times(1)).removeErrorPage(loadBalancer.getId(), loadBalancer.getAccountId());
        }
    }

    public static class WhenVerifyingReassignHosts {
        private LoadBalancer lb;
        @Mock
        LoadBalancerRepository lbRepository;
        @Mock
        ClusterRepository clusterRepository;
        @Mock
        HostRepository hostRepository;
        @Mock
        VirtualIpRepository virtualIpRepository;
        @Mock
        LoadBalancerProtocolObject defaultProtocol;
        @Mock
        LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;

        @InjectMocks
        LoadBalancerServiceImpl lbService;
        @InjectMocks
        HostServiceImpl hostService;
        @InjectMocks
        ClusterServiceImpl clusterService;
        @InjectMocks
        LoadBalancerStatusHistoryServiceImpl loadBalancerStatusHistoryService;

        @Before
        public void standUp() throws EntityNotFoundException, UnprocessableEntityException, ClusterStatusException, NoAvailableClusterException {
            MockitoAnnotations.initMocks(this);
            lb = new LoadBalancer();
            lbService = new LoadBalancerServiceImpl();
            lbService.setLoadBalancerRepository(lbRepository);
            lbService.setVirtualIpRepository(virtualIpRepository);


            hostService = new HostServiceImpl();
            hostService.setLoadBalancerRepository(lbRepository);
            hostService.setHostRepository(hostRepository);

            clusterService = new ClusterServiceImpl();
            clusterService.setLoadBalancerRepository(lbRepository);
            clusterService.setClusterRepository(clusterRepository);

            loadBalancerStatusHistoryService.setLoadBalancerStatusHistoryRepository(loadBalancerStatusHistoryRepository);

            hostService.setClusterRepository(clusterRepository);
//            lbService.setHostService(hostService);
//            lbService.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);

            lb.setStatus(LoadBalancerStatus.ACTIVE);

            lb.setAccountId(555555);
            lb.setId(3333);
            lb.setPort(33);
            lb.setProtocol(LoadBalancerProtocol.HTTP);

            Host host = new Host();
            host.setId(2);
            host.setHostStatus(HostStatus.ACTIVE);

            Cluster cluster = new Cluster();
            cluster.setId(3);

            lb.setHost(host);

            when(hostRepository.getById(Matchers.<Integer>any())).thenReturn(host);
            when(hostRepository.getDefaultActiveHost(Matchers.<Integer>any(), anyInt())).thenReturn(host);
            when(clusterRepository.getActiveCluster(null, false)).thenReturn(cluster);
            when(hostService.getById(ArgumentMatchers.<Integer>any())).thenReturn(host);
            when(loadBalancerStatusHistoryRepository.save(Matchers.<LoadBalancerStatusHistory>anyObject())).thenReturn(new LoadBalancerStatusHistory());

//            when(loadBalancerStatusHistoryService.save(lb.getAccountId(), lb.getId(), status);)
//            when(lbRepository.testAndSetStatus(Matchers.<Integer>any(), Matchers.<Integer>any(),Matchers.<LoadBalancerStatus>any(), Matchers.<Boolean>any())).thenReturn(true);

        }

//        @Test
//        public void shouldRetrieveDBLb() throws Exception {
//            when(lbRepository.getById(Matchers.<Integer>any())).thenReturn(lb);
//
//            List<LoadBalancer> lbs = new ArrayList<LoadBalancer>();
//            LoadBalancer loadBalancer = new LoadBalancer();
//            loadBalancer.setId(3333);
//            lbs.add(loadBalancer);
//
//            List<LoadBalancer> newLbs;
//            newLbs = lbService.reassignLoadBalancerHost(lbs);
//
//            LoadBalancer newLb;
//            newLb = newLbs.get(0);
//
//            Assert.assertEquals((Object) 555555, newLb.getAccountId());
//            Assert.assertEquals((Object) 3333, newLb.getId());
//        }

        @Test(expected = BadRequestException.class)
        public void shouldFailIfLbisSticky() throws Exception {
            when(lbRepository.getById(Matchers.<Integer>any())).thenReturn(lb);

            List<LoadBalancer> lbs = new ArrayList<LoadBalancer>();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(3333);
            lb.setIsSticky(true);
            lbs.add(loadBalancer);

            List<LoadBalancer> newLbs;
            newLbs = lbService.reassignLoadBalancerHost(lbs);

            LoadBalancer newLb;
            newLb = newLbs.get(0);

            Assert.assertEquals((Object) 555555, newLb.getAccountId());

        }

        @Test(expected = BadRequestException.class)
        public void shouldVerifySharedVipLbs() throws Exception {

            List<LoadBalancer> lbs = new ArrayList<LoadBalancer>();
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(3333);
            loadBalancer.setAccountId(55555);

            Set<LoadBalancerJoinVip> jvips = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip jvip = new LoadBalancerJoinVip();

            jvip.setVirtualIp(new VirtualIp());
            jvip.setId(new LoadBalancerJoinVip.Id(loadBalancer.getId(), 676));
            jvip.setLoadBalancer(lb);
            jvips.add(jvip);
            loadBalancer.setLoadBalancerJoinVipSet(jvips);

            List<LoadBalancer> sharedlbs = new ArrayList<LoadBalancer>();
            LoadBalancer sharedlb = new LoadBalancer();
            sharedlb.setId(9844);
            sharedlbs.add(sharedlb);
//            doReturn(sharedlbs).when(virtualIpRepository).getLoadBalancersByVipId(ArgumentMatchers.anyInt());
//            doReturn(loadBalancer).when(lbRepository).getById(ArgumentMatchers.anyInt());
            when(lbRepository.getById(ArgumentMatchers.anyInt())).thenReturn(loadBalancer);
            when(virtualIpRepository.getLoadBalancersByVipId((Integer) ArgumentMatchers.any())).thenReturn(sharedlbs);

            lbs.add(loadBalancer);
            List<LoadBalancer> newLbs;
            newLbs = lbService.reassignLoadBalancerHost(lbs);

            LoadBalancer newLb;
            newLb = newLbs.get(0);

            Assert.assertEquals((Object) 55555, newLb.getAccountId());

        }
    }
}
