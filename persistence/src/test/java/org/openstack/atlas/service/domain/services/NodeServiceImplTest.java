package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
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

import static org.mockito.Mockito.mock;

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
}
