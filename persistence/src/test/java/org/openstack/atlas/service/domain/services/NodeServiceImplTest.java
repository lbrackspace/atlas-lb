package org.openstack.atlas.service.domain.services;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.services.impl.NodeServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class NodeServiceImplTest {

    public static class NodeOperations {
        Integer accountId = 1234;
        LoadBalancerRepository lbRepository;
        NodeRepository nodeRepository;
        NodeServiceImpl nodeService;
        LoadBalancer lb;
        LoadBalancer lb2;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> lbjvs;
        VirtualIp vip;
        Node node;
        Node node2;
        Set<Node> nodes;
        Set<Node> nodes2;

        @Before
        public void standUp() {
            lbRepository = mock(LoadBalancerRepository.class);
            nodeRepository = mock(NodeRepository.class);
            nodeService = new NodeServiceImpl();
            nodeService.setNodeRepository(nodeRepository);
            nodeService.setLoadBalancerRepository(lbRepository);
        }

        @Before
        public void standUpObjects() {
            lb = new LoadBalancer();
            lb2 = new LoadBalancer();
            lbjv = new LoadBalancerJoinVip();
            lbjvs = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
            node = new Node();
            node2 = new Node();
            nodes = new HashSet<Node>();
            nodes2 = new HashSet<Node>();

            node.setPort(12);
            node2.setPort(11);

            node.setId(12);
            node2.setId(10);

            node.setIpAddress("192.1.1.1");
            node2.setIpAddress("193.1.1.1");

            node.setCondition(NodeCondition.ENABLED);
            node2.setCondition(NodeCondition.DISABLED);

            nodes.add(node);
            nodes2.add(node2);

            lb.setNodes(nodes);
            lb2.setNodes(nodes2);

            vip.setIpAddress("192.3.3.3");
            lbjv.setVirtualIp(vip);
            lbjvs.add(lbjv);
            lb.setLoadBalancerJoinVipSet(lbjvs);
        }

        @Test
        public void shouldReturnFalseWhenNoDuplicateNodesDetected() throws EntityNotFoundException {
            Assert.assertFalse(nodeService.detectDuplicateNodes(lb, lb2));
        }

        @Test
        public void shouldReturnTrueWhenDuplicateNodesDetected() throws EntityNotFoundException {
            node2.setIpAddress("192.1.1.1");
            node2.setPort(12);
            lb2.getNodes().add(node2);
            Assert.assertTrue(nodeService.detectDuplicateNodes(lb, lb2));
        }

        @Test
        public void shouldAllowValidIps() {
            Assert.assertTrue(nodeService.areAddressesValidForUse(nodes,  lb));
        }

        @Test
        public void shouldNotAllowInvalidIps() {
            node2.setIpAddress("192.3.3.3");
            nodes2.add(node2);
            Assert.assertFalse(nodeService.areAddressesValidForUse(nodes2,  lb));
        }

        @Test
        public void shouldReturnFalseIfLastActive() {
            Assert.assertFalse(nodeService.nodeToDeleteIsNotLastActive(lb,  node));
        }

        @Test
        public void shouldReturnTrueIfNotLastActive() {
            node2.setCondition(NodeCondition.ENABLED);
            lb.addNode(node2);
            Assert.assertTrue(nodeService.nodeToDeleteIsNotLastActive(lb,  node));
        }
    }

    public static class whenDeletingANode {

        @Mock
        LoadBalancerRepository lbRepository;
        @Mock
        LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
        @InjectMocks
        NodeServiceImpl nodeServiceImpl;

        LoadBalancer loadBalancer;
        Node node;
        Set<Node> nodes;



        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);

            loadBalancer = new LoadBalancer();
            node = new Node();
            node.setId(1);
            nodes = new HashSet<>();
            nodes.add(node);
            loadBalancer.setId(1);
            loadBalancer.setAccountId(1);

            node.setLoadbalancer(loadBalancer);
            loadBalancer.setNodes(nodes);
            when(lbRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId())).thenReturn(loadBalancer);
        }

        @Test
        public void shouldReturnLoadBalancer() throws Exception {
            LoadBalancer lb2 = nodeServiceImpl.deleteNode(loadBalancer, node);
            Assert.assertEquals(loadBalancer, lb2);

        }
        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowEntityNotFoundException() throws EntityNotFoundException {
            when(lbRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId())).thenThrow(EntityNotFoundException.class);
            nodeServiceImpl.deleteNode(loadBalancer, node);

        }

    }

    public static class whenUpdatingANode {

        @Mock
        LoadBalancerRepository lbRepo;
        @Mock
        NodeRepository nodeRepository;
        @Mock
        LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
        @InjectMocks
        NodeServiceImpl nodeServiceImpl2;

        LoadBalancer loadBalancer;
        Node node;
        Set<Node> nodes;
        Set<Node> nodes2;
        Node node2;
        Node dbNode;


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);

            loadBalancer = new LoadBalancer();
            node = new Node();
            node.setId(1);
            node.setWeight(60);
            nodes = new HashSet<>();
            dbNode = new Node();
            dbNode.setId(1);
            dbNode.setLoadbalancer(loadBalancer);
            dbNode.setType(NodeType.PRIMARY);
            dbNode.setStatus(NodeStatus.ONLINE);
            dbNode.setPort(80);
            dbNode.setWeight(59);
            dbNode.setCondition(NodeCondition.ENABLED);
            loadBalancer.setId(1);
            loadBalancer.setAccountId(1);
            node2 = new Node();
            node2.setId(2);
            nodes2 = new HashSet<>();
            nodes2.add(node2);

            node.setLoadbalancer(loadBalancer);
            nodes.add(dbNode);
            loadBalancer.setNodes(nodes);
            when(lbRepo.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId())).thenReturn(loadBalancer);


        }

        @Test
        public void shouldReturnLoadBalancerWithUpdatedWeight() throws Exception {
            LoadBalancer lb2 = nodeServiceImpl2.updateNode(loadBalancer, node);
            dbNode.setWeight(60);
            Assert.assertEquals(loadBalancer, lb2);
            verify(nodeRepository).update(loadBalancer);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowEntityNotFoundExceptionIfNodeNotInLB() throws EntityNotFoundException, BadRequestException {
            loadBalancer.setNodes(nodes2);
            nodeServiceImpl2.updateNode(loadBalancer, node);

        }




    }



}
