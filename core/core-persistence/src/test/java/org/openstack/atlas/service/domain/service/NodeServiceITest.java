package org.openstack.atlas.service.domain.service;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.PersistenceException;
import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class NodeServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenCreatingNodes extends Base {

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
        }

        @Test
        public void shouldAssignIdNodeWhenCreateSucceeds() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("10.2.2.2");
            node.setPort(90);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);

            Set<Node> updatedNodes = nodeRepository.getNodesByAccountIdLoadBalancerId(loadBalancer.getId(), loadBalancer.getAccountId());

            Assert.assertEquals(2, updatedNodes.size());
            for (Node node1 : updatedNodes) {
                Assert.assertNotNull(node1.getId());
            }
        }

        @Test(expected = PersistenceException.class)
        public void shouldThrowExceptionIfNodesAreEmpty() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);
        }

        @Test
        public void shouldAssignDefaultValuesWhenNotFullyHydrated() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("10.2.2.2");
            node.setPort(90);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);

            Set<Node> updatedNodes = nodeRepository.getNodesByAccountIdLoadBalancerId(loadBalancer.getId(), loadBalancer.getAccountId());

            for (Node node1 : updatedNodes) {
                if (node1.getAddress().equals(node.getAddress())) {
                    Assert.assertNotNull(node1.getId());
                    Assert.assertEquals("10.2.2.2", node1.getAddress());
                    Assert.assertEquals(true, node1.isEnabled());
                    Assert.assertEquals((Object) 1, node1.getWeight());
                }
            }
        }

        @Test
        public void shouldAssignValuesToNewNode() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("10.2.2.2");
            node.setPort(90);
            node.setEnabled(false);
            node.setWeight(10);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);

            Set<Node> updatedNodes = nodeRepository.getNodesByAccountIdLoadBalancerId(loadBalancer.getId(), loadBalancer.getAccountId());

            for (Node node1 : updatedNodes) {
                if (!(node1.getAddress().equals(loadBalancer.getNodes().iterator().next().getAddress()))) {
                    Assert.assertNotNull(node1.getId());
                    Assert.assertEquals(node.getAddress(), node1.getAddress());
                    Assert.assertEquals(node.isEnabled(), node1.isEnabled());
                    Assert.assertEquals(node.getWeight(), node1.getWeight());
                }
            }
        }

        @Test
        public void shouldPutLbInPendingUpdateStatusWhenCreateSucceeds() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("10.2.2.2");
            node.setPort(90);
            node.setEnabled(false);
            node.setWeight(10);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test
        public void shouldRetrieveNodeById() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("10.2.2.2");
            node.setPort(90);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);

            Set<Node> updatedNodes = nodeRepository.getNodesByAccountIdLoadBalancerId(loadBalancer.getId(), loadBalancer.getAccountId());

            for (Node node1 : updatedNodes) {
                if (!(node1.getAddress().equals(loadBalancer.getNodes().iterator().next().getAddress()))) {
                    Assert.assertNotNull(nodeRepository.getNodeById(loadBalancer.getId(), loadBalancer.getAccountId(), node1.getId()));
                }
            }
        }

        @Test(expected = UnprocessableEntityException.class)
        public void shouldThrowExceptionWhenDuplicatesAreDetected() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("10.1.1.1");
            node.setPort(80);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);
        }


        //TODO: Use when vips are populated...
        @Ignore
        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenAddressForIPV6InvalidForUse() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress(loadBalancer.getVirtualIpDozerWrapper().getLoadBalancerJoinVip6Set().iterator().next().getVirtualIp().getDerivedIpString());
            node.setPort(80);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);
        }

        //TODO: Use when vips are populated...
        @Ignore
        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenAddressForIPV4InvalidForUse() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress(loadBalancer.getVirtualIpDozerWrapper().getLoadBalancerJoinVipSet().iterator().next().getVirtualIp().getAddress());
            node.setPort(80);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.createNodes(lb);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrievingByInvalidloadbalancer() throws Exception {
            LoadBalancer lb = new LoadBalancer();
            lb.setId(99777);
            lb.setStatus("ACTIVE");
            nodeRepository.getNodesByLoadBalancer(lb, loadBalancer.getNodes().iterator().next().getId());
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullValues() throws PersistenceServiceException {
            nodeService.createNodes(new LoadBalancer());
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenUpdatingNodes extends Base {

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
        }

        @Test(expected = UnprocessableEntityException.class)
        public void shouldThrowExceptionWhenUpdatingLastNodeToDisabled() throws Exception {
            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setId(loadBalancer.getNodes().iterator().next().getId());
            node.setEnabled(false);
            nodes.add(node);

            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(loadBalancer.getAccountId());
            lb.setId(loadBalancer.getId());
            lb.setNodes(nodes);

            nodeService.updateNode(lb);
        }
        //TODO: more tests...
    }
}

