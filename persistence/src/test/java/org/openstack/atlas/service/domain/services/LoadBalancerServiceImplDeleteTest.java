package org.openstack.atlas.service.domain.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.Base;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class LoadBalancerServiceImplDeleteTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenPseudoDeletingALoadBalancer extends Base {
        private LoadBalancer lb;

        @Before
        public void standUp() throws Exception {
            super.standUp();

            lb = new LoadBalancer();
            lb.setAccountId(1234);
            lb.setName("Pseudo Delete Test");
            lb.setPort(80);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            lb.setUserName("Rackspace Cloud");

            Set<LoadBalancerJoinVip6> lbJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 lbJoinVip6 = new LoadBalancerJoinVip6(80, lb, new VirtualIpv6());
            lbJoinVip6Set.add(lbJoinVip6);
            lb.setLoadBalancerJoinVip6Set(lbJoinVip6Set);

            Set<Node> nodes = new HashSet<Node>();
            Node node = new Node();
            node.setIpAddress("198.9.23.94");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            node.setType(NodeType.PRIMARY);
            nodes.add(node);
            lb.setNodes(nodes);

            lb = loadBalancerService.create(lb);
        }

        @After
        public void tearDown() throws Exception {
            lb = loadBalancerService.pseudoDelete(lb);
            loadBalancerRepository.delete(lb);

            super.tearDown();
        }

        @Test
        public void shouldExist() throws EntityNotFoundException {
            LoadBalancer loadBalancer = loadBalancerService.get(lb.getId());
            Assert.assertNotNull(loadBalancer);
        }
    }
}
