package org.openstack.atlas.service.domain.services;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.pojos.VirtualIpDozerWrapper;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Ignore
@RunWith(Enclosed.class)
public class VirtualIpServiceImplIntegrationTest {

    //When creating a loadBalancer test the vip configs
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
    @Transactional
    public static class WhenCreatingLoadBalancerAndVerifyingVips {

        @Autowired
        private LoadBalancerService loadBalancerService;

        @Autowired
        private VirtualIpService virtualIpService;

        //Used for shared vip tests
        @Autowired
        private ClusterService clusterService;

        @Autowired
        private ClusterRepository clusterRepository;

        @PersistenceContext(unitName = "loadbalancing")
        private EntityManager entityManager;

        private LoadBalancer loadBalancer;
        private LoadBalancer loadBalancer2;
        private LoadBalancer dbLoadBalancer;

        @Before
        public void setUp() throws Exception {
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

            UserPages userPages = new UserPages();
            userPages.setErrorpage("blah Page");
            userPages.setLoadbalancer(loadBalancer);
            loadBalancer.setUserPages(userPages);

            loadBalancer2 = new LoadBalancer();
            loadBalancer2.setAccountId(10002);
            loadBalancer2.setName("integration testing");
            loadBalancer2.setPort(80);
            loadBalancer2.setProtocol(LoadBalancerProtocol.POP3);

            UserPages userPages2 = new UserPages();
            userPages2.setErrorpage("blah Page");
            userPages2.setLoadbalancer(loadBalancer2);
            loadBalancer2.setUserPages(userPages2);

            Set<Node> nodes2 = new HashSet<Node>();
            Node node2 = new Node();
            node.setIpAddress("2.2.2.2");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            nodes.add(node2);
            loadBalancer.setNodes(nodes2);

            virtualIpService.addAccountRecord(1000);


            //Build first loadbalancer
            VirtualIpv6 virtualIpv6 = new VirtualIpv6();
            VirtualIp virtualIp = new VirtualIp();
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
            dbLoadBalancer = loadBalancerService.create(loadBalancer);

        }

        @After
        public void tearDown() {

        }

        @Test
        public void shouldReturnIpv4AndIpv6ForPublicTypeOnly() throws Exception {
            VirtualIpv6 virtualIpv6 = new VirtualIpv6();
            VirtualIp virtualIp = new VirtualIp();
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
            VirtualIp virtualIp = new VirtualIp();
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
            VirtualIp virtualIp = new VirtualIp();
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
            VirtualIp virtualIp = new VirtualIp();
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
            VirtualIp virtualIp = new VirtualIp();
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

        @Test
        public void shouldReturnIpv4AndIpv6ForSharedV6() throws Exception {
            //Build first loadbalancer
            VirtualIpv6 virtualIpv6 = new VirtualIpv6();
            VirtualIp virtualIp = new VirtualIp();
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

            Assert.assertNotNull(loadBalancer);
            for (LoadBalancerJoinVip lbjv : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertNotNull(lbjv.getVirtualIp().getId());
            }

            for (LoadBalancerJoinVip6 lbjv : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                Assert.assertNotNull(lbjv.getVirtualIp().getId());
            }

            //Build second load balancer and share first load balancer's ipv6
            VirtualIpv6 virtualIpv62 = new VirtualIpv6();
            VirtualIp virtualIp2 = new VirtualIp();
            Set<LoadBalancerJoinVip> loadBalancerJoinVipSet2 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip loadBalancerJoinVip2 = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set2 = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 loadBalancerJoinVip62 = new LoadBalancerJoinVip6();

            loadBalancerJoinVip.setVirtualIp(virtualIp2);

            for (LoadBalancerJoinVip6 jvip : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                virtualIpv62.setId(jvip.getVirtualIp().getId());
                break;
            }

            loadBalancerJoinVip6.setVirtualIp(virtualIpv62);
            loadBalancerJoinVipSet.add(loadBalancerJoinVip2);
            loadBalancerJoinVip6Set.add(loadBalancerJoinVip62);
            VirtualIpDozerWrapper virtualIpDozerWrapper2 = new VirtualIpDozerWrapper(loadBalancerJoinVipSet2, loadBalancerJoinVip6Set2);
            loadBalancer2.setVirtualIpDozerWrapper(virtualIpDozerWrapper2);
            LoadBalancer dbLoadBalancer2 = loadBalancerService.create(loadBalancer2);
            LoadBalancer loadBalancer3 = loadBalancerService.get(dbLoadBalancer2.getId(), dbLoadBalancer2.getAccountId());

            Assert.assertNotNull(loadBalancer3);
            for (LoadBalancerJoinVip6 jvip : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                for (LoadBalancerJoinVip6 jvip2 : dbLoadBalancer2.getLoadBalancerJoinVip6Set()) {
                    Assert.assertEquals(jvip2.getVirtualIp().getId(), jvip.getVirtualIp().getId());
                }
            }
        }

        @Test
        public void shouldReturnIpv4AndIpv6ForSharedV4() throws Exception {
            //Build first loadbalancer
            VirtualIpv6 virtualIpv6 = new VirtualIpv6();
            VirtualIp virtualIp = new VirtualIp();
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

            Assert.assertNotNull(loadBalancer);
            for (LoadBalancerJoinVip lbjv : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertNotNull(lbjv.getVirtualIp().getId());
            }

            for (LoadBalancerJoinVip6 lbjv : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                Assert.assertNotNull(lbjv.getVirtualIp().getId());
            }

            //Build second load balancer and share first load balancer's ipv4
            VirtualIpv6 virtualIpv62 = new VirtualIpv6();
            VirtualIp virtualIp2 = new VirtualIp();
            Set<LoadBalancerJoinVip> loadBalancerJoinVipSet2 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip loadBalancerJoinVip2 = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set2 = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 loadBalancerJoinVip62 = new LoadBalancerJoinVip6();
            loadBalancerJoinVip.setVirtualIp(virtualIp2);

            for (LoadBalancerJoinVip jvip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                virtualIp2.setId(jvip.getVirtualIp().getId());
                break;
            }

            loadBalancerJoinVip6.setVirtualIp(virtualIpv62);
            loadBalancerJoinVipSet.add(loadBalancerJoinVip2);
            loadBalancerJoinVip6Set.add(loadBalancerJoinVip62);
            VirtualIpDozerWrapper virtualIpDozerWrapper2 = new VirtualIpDozerWrapper(loadBalancerJoinVipSet2, loadBalancerJoinVip6Set2);
            loadBalancer2.setVirtualIpDozerWrapper(virtualIpDozerWrapper2);
            LoadBalancer dbLoadBalancer2 = loadBalancerService.create(loadBalancer2);
            LoadBalancer loadBalancer3 = loadBalancerService.get(dbLoadBalancer2.getId(), dbLoadBalancer2.getAccountId());

            Assert.assertNotNull(loadBalancer3);
            for (LoadBalancerJoinVip jvip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                for (LoadBalancerJoinVip jvip2 : dbLoadBalancer2.getLoadBalancerJoinVipSet()) {
                    Assert.assertEquals(jvip2.getVirtualIp().getId(), jvip.getVirtualIp().getId());
                }
            }
        }

        @Test(expected = UnprocessableEntityException.class)
        public void shouldFailIfSharedVipIsInDifferentCluster() throws Exception {
            LoadBalancer loadBalancer = loadBalancerService.get(dbLoadBalancer.getId(), dbLoadBalancer.getAccountId());

            Assert.assertNotNull(loadBalancer);
            for (LoadBalancerJoinVip lbjv : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
                Assert.assertNotNull(lbjv.getVirtualIp().getId());
            }

            for (LoadBalancerJoinVip6 lbjv : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                Assert.assertNotNull(lbjv.getVirtualIp().getId());
            }

            //Build second load balancer and share first load balancer's ipv6
            VirtualIpv6 virtualIpv62 = new VirtualIpv6();
            VirtualIp virtualIp2 = new VirtualIp();
            Set<LoadBalancerJoinVip> loadBalancerJoinVipSet2 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip loadBalancerJoinVip2 = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set2 = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 loadBalancerJoinVip62 = new LoadBalancerJoinVip6();
            loadBalancerJoinVip2.setVirtualIp(virtualIp2);

            for (LoadBalancerJoinVip6 jvip : dbLoadBalancer.getLoadBalancerJoinVip6Set()) {
                virtualIpv62.setId(jvip.getVirtualIp().getId());
                break;
            }

            loadBalancerJoinVip62.setVirtualIp(virtualIpv62);
            loadBalancerJoinVipSet2.add(loadBalancerJoinVip2);
            loadBalancerJoinVip6Set2.add(loadBalancerJoinVip62);
            VirtualIpDozerWrapper virtualIpDozerWrapper2 = new VirtualIpDozerWrapper(loadBalancerJoinVipSet2, loadBalancerJoinVip6Set2);
            loadBalancer2.setVirtualIpDozerWrapper(virtualIpDozerWrapper2);

            List<Cluster> clusters = clusterService.getAll();

            if (clusters.size() > 1) {
                Cluster cluster = clusters.get(0);
                cluster.setStatus(ClusterStatus.INACTIVE);
                clusterRepository.update(cluster);
            }

             loadBalancerService.create(loadBalancer2);
        }
    }
}