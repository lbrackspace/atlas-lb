package org.openstack.atlas.adapter.itest;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm.ROUND_ROBIN;
import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTP;
import static org.openstack.atlas.service.domain.entities.NodeCondition.DISABLED;
import static org.openstack.atlas.service.domain.entities.NodeCondition.ENABLED;

public class Ipv6ITest extends STMTestBase {
    protected static VirtualIpv6 vip1;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
    }

    public static void removeIpv6LoadBalancer() throws Exception {
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        StingrayRestClient client = new StingrayRestClient();
        client.getVirtualServer(loadBalancerName());
    }


    @Test
    public void testCreateIpv6LoadBalancer() throws Exception {
        createSimpleIpv6LoadBalancer();
    }

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testRemoveIpv6LoadBalancer() throws Exception {
        removeIpv6LoadBalancer();
    }

    protected static void setupIvars() {
        Set<LoadBalancerJoinVip6> ipv6VipSet = new HashSet<LoadBalancerJoinVip6>();
        vip1 = new VirtualIpv6();
        vip1.setId(TEST_IPV6_VIP_ID);
        vip1.setAccountId(TEST_ACCOUNT_ID);
        vip1.setCluster(cluster);
        vip1.setVipOctets(1);
        LoadBalancerJoinVip6 loadBalancerJoinVip = new LoadBalancerJoinVip6();
        loadBalancerJoinVip.setVirtualIp(vip1);
        ipv6VipSet.add(loadBalancerJoinVip);

        Set<Node> nodeList = new HashSet<Node>();
        node1 = new Node();
        node2 = new Node();
        node1.setIpAddress("127.0.0.1");
        node2.setIpAddress("127.0.0.2");
        node1.setPort(80);
        node2.setPort(80);
        node1.setCondition(ENABLED);
        node2.setCondition(DISABLED);
        nodeList.add(node1);
        nodeList.add(node2);

        LoadBalancer lb = new LoadBalancer();
        lb.setId(TEST_LOADBALANCER_ID);
        lb.setAccountId(TEST_ACCOUNT_ID);
        lb.setPort(80);
        lb.setAlgorithm(ROUND_ROBIN);
        lb.setName("ipv6_integration_test_lb");
        lb.setProtocol(HTTP);
        lb.setNodes(nodeList);
        lb.setLoadBalancerJoinVip6Set(ipv6VipSet);

        Ipv6ITest.lb = lb;
    }

    protected static void createSimpleIpv6LoadBalancer() {
        StingrayRestClient tclient;
        ResourceTranslator translator = new ResourceTranslator();

        try {
            stmAdapter.createLoadBalancer(config, lb);

            tclient = new StingrayRestClient();
            VirtualServer vs;
            Pool pool;

            vs = tclient.getVirtualServer(loadBalancerName());

            Assert.assertNotNull(vs);
            Assert.assertEquals(true, vs.getProperties().getBasic().getEnabled());
            Assert.assertEquals(lb.getPort(), vs.getProperties().getBasic().getPort());
            Assert.assertEquals(poolName(), vs.getProperties().getBasic().getPool());
            Assert.assertEquals("Default", vs.getProperties().getConnection_errors().getError_file());
            Assert.assertTrue(vs.getProperties().getBasic().getRequest_rules().contains(StmConstants.XFF));
            Assert.assertTrue(vs.getProperties().getBasic().getRequest_rules().contains(StmConstants.XFP));
            Assert.assertEquals(false, vs.getProperties().getBasic().getListen_on_any());
            Assert.assertEquals(false, vs.getProperties().getTcp().getProxy_close());
            Assert.assertEquals(vs.getProperties().getBasic().getListen_on_traffic_ips(), translator.genGroupNameSet(lb));

            Assert.assertEquals("", vs.getProperties().getBasic().getProtection_class());
            Assert.assertEquals("", vs.getProperties().getBasic().getBandwidth_class());

            pool = tclient.getPool(loadBalancerName());
            Assert.assertNotNull(pool);
            Assert.assertEquals(0, pool.getProperties().getBasic().getMonitors().size());
            Assert.assertEquals(lb.getAlgorithm().name().toLowerCase(), pool.getProperties().getLoad_balancing().getAlgorithm());

            TrafficIp vip;
            for (String v : vs.getProperties().getBasic().getListen_on_traffic_ips()) {
                vip = tclient.getTrafficIp(v);
                Assert.assertNotNull(vip);
                Assert.assertEquals(1, vip.getProperties().getBasic().getIpaddresses().size());
                Assert.assertEquals(true, vip.getProperties().getBasic().getEnabled());

                Assert.assertEquals(new HashSet(Arrays.asList(lb.getLoadBalancerJoinVip6Set().iterator().
                        next().getVirtualIp().getDerivedIpString())), vip.getProperties().getBasic().getIpaddresses());
                Set<String> machines = new HashSet<String>();
                machines.add(config.getTrafficManagerName());
                machines.addAll(config.getFailoverTrafficManagerNames());
                Assert.assertEquals(machines, vip.getProperties().getBasic().getMachines());
                Assert.assertEquals(new HashSet(config.getFailoverTrafficManagerNames()), vip.getProperties().getBasic().getSlaves());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());

        }
    }


}
