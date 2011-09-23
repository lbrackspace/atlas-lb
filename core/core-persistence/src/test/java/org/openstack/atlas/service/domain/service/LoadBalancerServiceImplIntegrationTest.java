package org.openstack.atlas.service.domain.service;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
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
public class LoadBalancerServiceImplIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
    @Transactional
    @Service
    public static class WhenCreatingLoadBalancer {

        @Autowired
        private LoadBalancerService loadBalancerService;

        @Autowired
        private LoadBalancerRepository loadBalancerRepository;

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
            node.setAddress("2.2.2.2");
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
            LoadBalancer loadBalancer = loadBalancerRepository.getByIdAndAccountId(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
            Assert.assertNotNull(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrieveingLoadBalancerByWrongAccountId() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.getByIdAndAccountId(dbLoadBalancer.getId(), -99999);
        }

    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
    @Transactional
    @Service
    public static class WhenDeletingLoadBalancer {

        @Autowired
        private LoadBalancerService loadBalancerService;

        @Autowired
        private LoadBalancerRepository loadBalancerRepository;

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
            node.setAddress("2.2.2.2");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            nodes.add(node);
            loadBalancer.setNodes(nodes);
        }

        @After
        public void tearDown() {

        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenLoadBalancerDoesntExist() throws Exception {
            loadBalancer.setId(-999);
            loadBalancerService.preDelete(loadBalancer.getAccountId(), loadBalancer.getId());
        }

        @Test
        public void shouldPutInPendingDeleteStatusWhenPreDeleteSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

            loadBalancerService.preDelete(loadBalancer.getAccountId(), dbLoadBalancer.getId());
            dbLoadBalancer = loadBalancerRepository.getById(dbLoadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), LoadBalancerStatus.PENDING_DELETE);
        }

        @Test
        public void shouldPutInDeletedStatusWhenDeleteSucceeds() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);

            loadBalancerService.delete(dbLoadBalancer);
            dbLoadBalancer = loadBalancerRepository.getById(dbLoadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), LoadBalancerStatus.DELETED);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenDeletingImmutableLoadBalancer() throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            dbLoadBalancer = loadBalancerRepository.changeStatus(dbLoadBalancer, LoadBalancerStatus.PENDING_UPDATE);

            loadBalancerService.preDelete(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId());
        }

    }
}

