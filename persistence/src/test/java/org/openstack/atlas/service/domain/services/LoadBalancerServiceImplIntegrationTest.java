package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.VirtualIpDozerWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;

@RunWith(Enclosed.class)
@Ignore
public class LoadBalancerServiceImplIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
    @Transactional
    public static class WhenCreatingLoadBalancer {

        @Autowired
        private LoadBalancerService loadBalancerService;

        @PersistenceContext(unitName = "loadbalancing")
        private EntityManager entityManager;

        private LoadBalancer loadBalancer;

        @Before
        public void setUp() {
            loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(1000);
            loadBalancer.setName("integration testing");
            loadBalancer.setPort(80);
            loadBalancer.setProtocol(LoadBalancerProtocol.POP3);

            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setIpAddress("2.2.2.2");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            nodes.add(node);
            loadBalancer.setNodes(nodes);
        }

        @After
        public void tearDown() {

        }

        @Test(expected = PersistenceException.class)
        public void shouldThrowExceptionWhenLoadBalancerIsNull() throws Exception {
            loadBalancer = new LoadBalancer();
            loadBalancerService.create(loadBalancer);
        }

        @Test
        public void shouldAssignIdLoadBalancerWhenCreateSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            Assert.assertNotNull(dbLoadBalancer.getId());
        }

        @Test
        public void shouldPutInBuildStatusWhenCreateSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            Assert.assertEquals(dbLoadBalancer.getStatus(), LoadBalancerStatus.BUILD);
        }

        @Test
        public void shouldRetrieveLoadBalancerByIdAndAccountId() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            Assert.assertNotNull(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrieveingLoadBalancerByWrongAccountId() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), -99999);
        }

        @Test
        public void shouldRetrieveingLoadBalancerByStatus() throws Exception {
            List<LoadBalancer> dbLoadBalancers = loadBalancerService.getLoadbalancersGeneric(loadBalancer.getAccountId(), "BUILD", null, null, null, null, null);
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            List<LoadBalancer> dbLoadBalancers1 = loadBalancerService.getLoadbalancersGeneric(loadBalancer.getAccountId(), "BUILD", null, null, null, null, null);
            Assert.assertEquals(dbLoadBalancers.size(), dbLoadBalancers1.size() - 1);
        }

            //TODO:Move..
        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
        @Transactional
        public static class WhenBatchDeletingLoadBalancer {

            @Autowired
            private LoadBalancerService loadBalancerService;

            @PersistenceContext(unitName = "loadbalancing")
            private EntityManager entityManager;

            private LoadBalancer loadBalancer;
            private List<LoadBalancer> loadBalancers;
            private List<Integer> ids;

            @Before
            public void setUp() {
                loadBalancer = new LoadBalancer();
                loadBalancer.setAccountId(1000);
                loadBalancer.setName("integration testing");
                loadBalancer.setPort(80);
                loadBalancer.setProtocol(LoadBalancerProtocol.POP3);

                Set<Node> nodes = new HashSet<Node>();
                Node node = new Node();
                node.setIpAddress("2.2.2.2");
                node.setPort(80);
                node.setCondition(NodeCondition.ENABLED);
                nodes.add(node);
                loadBalancer.setNodes(nodes);

            }

            @After
            public void tearDown() {

            }

            @Test()
            public void shouldPassDeleteLbsToNextLayer() throws Exception {
                ids = new ArrayList<Integer>();
                loadBalancers = new ArrayList<LoadBalancer>();

                //not needed here...
                for (int i = 0; i <= 10; i++) {
                    loadBalancer = new LoadBalancer();
                    loadBalancer.setAccountId(1000);
                    loadBalancer.setName("integration testing");
                    loadBalancer.setPort(80);
                    loadBalancer.setProtocol(LoadBalancerProtocol.POP3);

                    Set<Node> nodes = new HashSet<Node>();
                    Node node = new Node();
                    node.setIpAddress("2.2.2.2");
                    node.setPort(80);
                    node.setCondition(NodeCondition.ENABLED);
                    nodes.add(node);
                    loadBalancer.setNodes(nodes);
                    loadBalancers.add(loadBalancerService.create(loadBalancer));
                }

                for (LoadBalancer l : loadBalancers) {
                    l.setStatus(LoadBalancerStatus.ACTIVE);
                    ids.add(l.getId());
                }
                loadBalancerService.prepareForDelete(1000, ids);
            }

            @Test(expected = BadRequestException.class)
            public void shouldThrowBadRequestExceptionIfOneOrManyNotActive() throws Exception {
                 ids = new ArrayList<Integer>();
                loadBalancers = new ArrayList<LoadBalancer>();

                //not needed here...
                for (int i = 0; i <= 10; i++) {
                    loadBalancer = new LoadBalancer();
                    loadBalancer.setAccountId(1000);
                    loadBalancer.setName("integration testing");
                    loadBalancer.setPort(80);
                    loadBalancer.setProtocol(LoadBalancerProtocol.POP3);

                    Set<Node> nodes = new HashSet<Node>();
                    Node node = new Node();
                    node.setIpAddress("2.2.2.2");
                    node.setPort(80);
                    node.setCondition(NodeCondition.ENABLED);
                    nodes.add(node);
                    loadBalancer.setNodes(nodes);
                    loadBalancers.add(loadBalancerService.create(loadBalancer));
                }
                for (LoadBalancer l : loadBalancers) {
                    ids.add(l.getId());
                }
                loadBalancerService.prepareForDelete(1000, ids);
            }
        }
    }
}