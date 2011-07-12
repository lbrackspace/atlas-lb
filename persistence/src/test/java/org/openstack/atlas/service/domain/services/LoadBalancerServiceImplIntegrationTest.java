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
        public static class WhenCreatingLoadBalancerAndVerifyingVips {

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

            @Test
            public void shouldReturnIpv4AndIpv6ForPublicTypeOnly() throws Exception {
                VirtualIpv6 virtualIpv6 = new VirtualIpv6();
                org.openstack.atlas.service.domain.entities.VirtualIp virtualIp = new org.openstack.atlas.service.domain.entities.VirtualIp();
                Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();

                loadBalancerJoinVip.setVirtualIp(virtualIp);
                loadBalancerJoinVip6.setVirtualIp(virtualIpv6);

                loadBalancerJoinVipSet.add(loadBalancerJoinVip);
                loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);

                VirtualIpDozerWrapper virtualIpDozerWrapper = new VirtualIpDozerWrapper(loadBalancerJoinVipSet, loadBalancerJoinVip6Set);

                loadBalancer.setVirtualIpDozerWrapper(virtualIpDozerWrapper);

                LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
                LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());
                Assert.assertEquals(1, dbLoadBalancer.getLoadBalancerJoinVip6Set().size());
                Assert.assertEquals(1, dbLoadBalancer.getLoadBalancerJoinVipSet().size());
            }

            @Test
            public void shouldReturnIpv4AndIpv6WithIdsForPublicTypeOnly() throws Exception {
                VirtualIpv6 virtualIpv6 = new VirtualIpv6();
                org.openstack.atlas.service.domain.entities.VirtualIp virtualIp = new org.openstack.atlas.service.domain.entities.VirtualIp();
                Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();

                loadBalancerJoinVip.setVirtualIp(virtualIp);
                loadBalancerJoinVip6.setVirtualIp(virtualIpv6);

                loadBalancerJoinVipSet.add(loadBalancerJoinVip);
                loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);

                VirtualIpDozerWrapper virtualIpDozerWrapper = new VirtualIpDozerWrapper(loadBalancerJoinVipSet, loadBalancerJoinVip6Set);

                loadBalancer.setVirtualIpDozerWrapper(virtualIpDozerWrapper);

                LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
                LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

                for (LoadBalancerJoinVip lbjv : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                    Assert.assertNotNull(lbjv.getVirtualIp().getId());
                }

                for (LoadBalancerJoinVip6 lbjv : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                    Assert.assertNotNull(lbjv.getVirtualIp().getId());
                }
            }

            @Test
            public void shouldReturnIpv4AndIpv6WithAddressesForPublicTypeOnly() throws Exception {
                VirtualIpv6 virtualIpv6 = new VirtualIpv6();
                org.openstack.atlas.service.domain.entities.VirtualIp virtualIp = new org.openstack.atlas.service.domain.entities.VirtualIp();
                Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();

                loadBalancerJoinVip.setVirtualIp(virtualIp);
                loadBalancerJoinVip6.setVirtualIp(virtualIpv6);

                loadBalancerJoinVipSet.add(loadBalancerJoinVip);
                loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);

                VirtualIpDozerWrapper virtualIpDozerWrapper = new VirtualIpDozerWrapper(loadBalancerJoinVipSet, loadBalancerJoinVip6Set);

                loadBalancer.setVirtualIpDozerWrapper(virtualIpDozerWrapper);

                LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
                LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

                for (LoadBalancerJoinVip lbjv : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                    Assert.assertNotNull(lbjv.getVirtualIp().getIpAddress());
                }

                for (LoadBalancerJoinVip6 lbjv : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                    Assert.assertNotNull(lbjv.getVirtualIp().getDerivedIpString());
                }
            }

            @Test
            public void shouldReturnIpv4AWithValuesForIPV4Only() throws Exception {
                org.openstack.atlas.service.domain.entities.VirtualIp virtualIp = new org.openstack.atlas.service.domain.entities.VirtualIp();
                Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

                loadBalancerJoinVip.setVirtualIp(virtualIp);

                loadBalancerJoinVipSet.add(loadBalancerJoinVip);

                VirtualIpDozerWrapper virtualIpDozerWrapper = new VirtualIpDozerWrapper(loadBalancerJoinVipSet, loadBalancerJoinVip6Set);

                loadBalancer.setVirtualIpDozerWrapper(virtualIpDozerWrapper);

                LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
                LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

                for (LoadBalancerJoinVip lbjv : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                    Assert.assertNotNull(lbjv.getVirtualIp().getIpAddress());
                    Assert.assertEquals(VirtualIpType.PUBLIC, lbjv.getVirtualIp().getVipType());
                }

                Assert.assertEquals(0, dbLoadBalancer.getLoadBalancerJoinVip6Set().size());
                Assert.assertEquals(1, dbLoadBalancer.getLoadBalancerJoinVipSet().size());
            }

            @Test
            public void shouldReturnIpv6WithValuesForPublic() throws Exception {
                VirtualIpv6 virtualIpv6 = new VirtualIpv6();
                org.openstack.atlas.service.domain.entities.VirtualIp virtualIp = new org.openstack.atlas.service.domain.entities.VirtualIp();
                Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
                Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();

                loadBalancerJoinVip6.setVirtualIp(virtualIpv6);

                loadBalancerJoinVip6Set.add(loadBalancerJoinVip6);

                VirtualIpDozerWrapper virtualIpDozerWrapper = new VirtualIpDozerWrapper(loadBalancerJoinVipSet, loadBalancerJoinVip6Set);

                loadBalancer.setVirtualIpDozerWrapper(virtualIpDozerWrapper);

                LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
                LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

                for (LoadBalancerJoinVip6 lbjv : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                    Assert.assertNotNull(lbjv.getVirtualIp().getDerivedIpString());
                    Assert.assertNotNull(lbjv.getVirtualIp().getId());
                }
                Assert.assertEquals(1, dbLoadBalancer.getLoadBalancerJoinVip6Set().size());
                Assert.assertEquals(0, dbLoadBalancer.getLoadBalancerJoinVipSet().size());
            }
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

            @Test(expected = EntityNotFoundException.class)
            public void shouldThrowEntityNotFoundExceptionWhenlbsNotFound() throws Exception {
                ids = new ArrayList<Integer>();
                for (int i=0;i<=10;i++) {
                    ids.add(i);
                }
                loadBalancerService.prepareForDelete(1000, ids);
            }

            @Test()
            public void shouldDeleteLbs() throws Exception {
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

