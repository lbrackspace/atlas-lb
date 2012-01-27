package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LoadBalancerServiceImplTest {

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
            Assert.assertEquals(NodeStatus.OFFLINE, node2.getStatus());
            Assert.assertEquals(NodeStatus.OFFLINE, node3.getStatus());

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
        public void standUp() throws EntityNotFoundException {
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
        }

        @Test(expected = BadRequestException.class)
        public void shouldRejectUpdateProtocolIfUsingSslTermination() throws Exception {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTPS);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);

            lbService.prepareForUpdate(loadBalancer);

        }
    }
}
