package org.openstack.atlas.service.domain.services;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
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
public class AccessListServiceImplIntegrationTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:db-services-test.xml"})
    @Transactional
    public static class WhenAddingAccessLists {

        @Autowired
        private AccessListService accessListService;

        @Autowired
        private LoadBalancerService loadBalancerService;

        @PersistenceContext(unitName = "loadbalancing")
        private EntityManager entityManager;


        private AccessList accessList;

        private LoadBalancer loadBalancer;

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
            userPages.setErrorpage("aError");
            userPages.setLoadbalancer(loadBalancer);
            loadBalancer.setUserPages(userPages);

            loadBalancer = createLoadBalancerInActiveStatus(loadBalancer);

            accessList = new AccessList();
            accessList.setIpAddress("new ip");
            accessList.setType(AccessListType.ALLOW);
        }

        @After
        public void tearDown() {

        }

        @Test
        public void shouldAddNewAccessListLoadBalancerWhenOperationSucceeds() throws Exception {
            List<AccessList> accessListsBefore = accessListService.getAccessListByAccountIdLoadBalancerId(loadBalancer.getAccountId(), loadBalancer.getId());

            LoadBalancer newLoadBalancer = new LoadBalancer();
            newLoadBalancer.setId(loadBalancer.getId());
            newLoadBalancer.setAccountId(loadBalancer.getAccountId());

            accessList.setLoadbalancer(loadBalancer);
            newLoadBalancer.addAccessList(accessList);

            accessListService.updateAccessList(newLoadBalancer);

            List<AccessList> accessListsAfter = accessListService.getAccessListByAccountIdLoadBalancerId(loadBalancer.getAccountId(), loadBalancer.getId());
            Assert.assertEquals(accessListsBefore.size() + 1, accessListsAfter.size());
        }

        @Test(expected = ImmutableEntityException.class)
        public void shouldThrowExceptionWhenLoaBalancerNotActive() throws Exception {
            loadBalancerService.setStatus(loadBalancer, LoadBalancerStatus.ERROR);

            LoadBalancer newLoadBalancer = new LoadBalancer();
            newLoadBalancer.setId(loadBalancer.getId());
            newLoadBalancer.setAccountId(loadBalancer.getAccountId());

            newLoadBalancer.addAccessList(accessList);

            accessListService.updateAccessList(newLoadBalancer);
        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenDuplicateAccessLists() throws Exception {
            LoadBalancer newLoadBalancer = new LoadBalancer();
            newLoadBalancer.setId(loadBalancer.getId());
            newLoadBalancer.setAccountId(loadBalancer.getAccountId());

            accessList.setLoadbalancer(loadBalancer);
            newLoadBalancer.addAccessList(accessList);

            accessListService.updateAccessList(newLoadBalancer);

            loadBalancerService.setStatus(loadBalancer, LoadBalancerStatus.ACTIVE);

            newLoadBalancer.addAccessList(accessList);
            accessListService.updateAccessList(newLoadBalancer);


        }

        @Test(expected = BadRequestException.class)
        public void shouldThrowExceptionWhenAccessListLimitExceeded() throws Exception {
            LoadBalancer newLoadBalancer = new LoadBalancer();
            newLoadBalancer.setId(loadBalancer.getId());
            newLoadBalancer.setAccountId(loadBalancer.getAccountId());

            accessList.setLoadbalancer(loadBalancer);
            for (int i = 0; i < 101; i++) {
                accessList = new AccessList();
                accessList.setIpAddress("new ip " + i);
                accessList.setType(AccessListType.ALLOW);
                newLoadBalancer.addAccessList(accessList);
            }

            accessListService.updateAccessList(newLoadBalancer);
        }

        private LoadBalancer createLoadBalancerInActiveStatus(LoadBalancer loadBalancer) throws Exception {
            LoadBalancer dbLoadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerService.setStatus(dbLoadBalancer, LoadBalancerStatus.ACTIVE);
            return dbLoadBalancer;
        }
    }
}
