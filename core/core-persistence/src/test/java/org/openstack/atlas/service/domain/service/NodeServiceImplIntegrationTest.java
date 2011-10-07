package org.openstack.atlas.service.domain.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.core.api.v1.Nodes;
import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class NodeServiceImplIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
    @Transactional
    @Service
    public static class WhenCreatingNodes {

        @Autowired
        private LoadBalancerService loadBalancerService;

        @Autowired
        private NodeService nodeService;

        @Autowired
        private LoadBalancerRepository loadBalancerRepository;

        @Autowired
        private NodeRepository nodeRepository;

        @Autowired
        private AtlasTypeHelper atlasTypeHelper;

        @PersistenceContext(unitName = "loadbalancing")
        private EntityManager entityManager;

        private LoadBalancer loadBalancer;
        private Node node;
        private Nodes nodes;

        @Before
        public void setUp() {
            loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(1000);
            loadBalancer.setName("integration testing");
            loadBalancer.setPort(80);
            loadBalancer.setProtocol(CoreProtocolType.HTTP);

            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setAddress("2.2.2.2");
            node.setPort(80);
            node.setEnabled(true);
            nodes.add(node);
            loadBalancer.setNodes(nodes);
        }

        @After
        public void tearDown() {

        }

        @Test(expected = PersistenceException.class)
        public void shouldThrowExceptionWhenNodeIsNull() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer.setStatus("ACTIVE");
            node = new Node();
            dbLoadBalancer.getNodes().add(node);
            nodeService.createNodes(dbLoadBalancer);
        }

        //TODO: more tests...
    }
}

