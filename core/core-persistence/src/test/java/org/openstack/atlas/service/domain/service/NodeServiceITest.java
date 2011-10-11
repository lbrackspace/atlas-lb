package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Set;

@RunWith(Enclosed.class)
public class NodeServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenCreatingNodes extends Base {

        @Autowired
        private NodeService nodeService;

        @Autowired
        private NodeRepository nodeRepository;

        private Node node;

        @Test(expected = PersistenceException.class)
        public void shouldThrowExceptionWhenNodeIsNull() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer.setStatus("ACTIVE");
            node = new Node();
            dbLoadBalancer.getNodes().add(node);
            nodeService.createNodes(dbLoadBalancer);
        }

        @Test
        public void shouldAssignIdNodeWhenCreateSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer.setStatus("ACTIVE");
            node = new Node();
            node.setAddress("2.2.4.4");
            node.setPort(80);
            node.setEnabled(true);

            LoadBalancer pLb = new LoadBalancer();
            pLb.setAccountId(dbLoadBalancer.getAccountId());
            pLb.setId(dbLoadBalancer.getId());
            pLb.getNodes().add(node);

            Set<Node> rNodes = nodeService.createNodes(pLb);
            for (Node node : rNodes) {
                Assert.assertNotNull(node.getId());
            }
        }

        @Test(expected = UnprocessableEntityException.class)
        public void shouldRejectDuplicateNodes() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer.setStatus("ACTIVE");
            node = StubFactory.createHydratedDomainNode();

            LoadBalancer pLb = new LoadBalancer();
            pLb.setAccountId(dbLoadBalancer.getAccountId());
            pLb.setId(dbLoadBalancer.getId());
            pLb.getNodes().add(node);

            Set<Node> rNodes = nodeService.createNodes(pLb);
            for (Node node : rNodes) {
                Assert.assertNotNull(node.getId());
            }
        }

        //TODO: more tests...
    }
}

