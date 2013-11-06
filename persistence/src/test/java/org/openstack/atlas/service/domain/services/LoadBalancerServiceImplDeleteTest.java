package org.openstack.atlas.service.domain.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.Base;
import org.openstack.atlas.service.domain.deadlock.DeadLockRetryAspect;
import org.openstack.atlas.service.domain.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class LoadBalancerServiceImplDeleteTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenPseudoDeletingALoadBalancer extends Base {

        @Autowired
        private DeadLockRetryAspect deadLockRetryAspect;

        @Before
        public void standUp() throws Exception {
            super.standUp();


            loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(1234);
            loadBalancer.setName("Pseudo Delete Test");
            loadBalancer.setPort(80);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            loadBalancer.setUserName("Rackspace Cloud");

            Set<LoadBalancerJoinVip6> lbJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 lbJoinVip6 = new LoadBalancerJoinVip6(80, loadBalancer, new VirtualIpv6());
            lbJoinVip6Set.add(lbJoinVip6);
            loadBalancer.setLoadBalancerJoinVip6Set(lbJoinVip6Set);

            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setIpAddress("198.9.23.94");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            node.setType(NodeType.PRIMARY);
            nodes.add(node);
            loadBalancer.setNodes(nodes);

            loadBalancer = loadBalancerService.create(loadBalancer);
            System.out.println();
        }

        @After
        public void tearDown() throws Exception {
            loadBalancer = loadBalancerService.pseudoDelete(loadBalancer);
            loadBalancerRepository.delete(loadBalancer);

            super.tearDown();
        }

        @Test
        public void shouldCallDeadlockCode() throws Throwable {
            loadBalancer = loadBalancerService.get(loadBalancer.getId());
            Assert.assertNotNull(loadBalancer);

            loadBalancer = loadBalancerService.pseudoDelete(loadBalancer);

            Assert.assertTrue(deadLockRetryAspect.getConncurrencyRetryCalls() > 0);
        }
    }
}
