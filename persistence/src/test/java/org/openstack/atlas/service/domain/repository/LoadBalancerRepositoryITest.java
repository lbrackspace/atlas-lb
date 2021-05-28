package org.openstack.atlas.service.domain.repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.Base;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerServiceImpl;
import org.openstack.atlas.util.common.CalendarUtils;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.util.debug.Debug;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LoadBalancerRepositoryITest {

    @RunWith(Enclosed.class)
    public static class WhenGettingLoadBalancersActiveDuringPeriod extends Base {

        @Before
        @Override
        public void standUp() throws Exception {
            super.standUp();

            loadBalancer = new LoadBalancer();
            loadBalancer.setName("Test Load Balancer");
            loadBalancer.setAccountId(accountId);
            loadBalancer.setAlgorithm(LoadBalancerAlgorithm.RANDOM);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            loadBalancer.setPort(80);
            loadBalancer.setConnectionLogging(false);
            loadBalancer.setContentCaching(false);
            loadBalancer.setHttpsRedirect(false);
            loadBalancer.setHalfClosed(false);
            loadBalancer.setTimeout(100);

            Node node = new Node();
            node.setIpAddress("10.0.0.1");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            loadBalancer.addNode(node);

            loadBalancer = loadBalancerService.create(loadBalancer);
            Debug.nop();
        }

        @After
        @Override
        public void tearDown() throws Exception {
            loadBalancerRepository.delete(loadBalancer);
            super.tearDown();
        }

        private void setupAccountLimits(LimitType loadbalancerLimitType, LimitType nodeLimitType) throws BadRequestException {
            AccountLimit lbLimitForAccount = new AccountLimit();
            lbLimitForAccount.setAccountId(accountId);
            lbLimitForAccount.setLimitType(loadbalancerLimitType);
            lbLimitForAccount.setLimit(20);

            try {
                accountLimitService.getLimit(accountId, AccountLimitType.LOADBALANCER_LIMIT);
            } catch (EntityNotFoundException e) {
                accountLimitService.save(lbLimitForAccount);
            }

            AccountLimit nodeLimitForAccount = new AccountLimit();
            nodeLimitForAccount.setAccountId(accountId);
            nodeLimitForAccount.setLimitType(nodeLimitType);
            nodeLimitForAccount.setLimit(20);

            try {
                accountLimitService.getLimit(accountId, AccountLimitType.NODE_LIMIT);
            } catch (EntityNotFoundException e) {
                accountLimitService.save(nodeLimitForAccount);
            }
        }

        private LimitType setupNodeLimit() {
            LimitType nodeLimitType = new LimitType();
            nodeLimitType.setName(AccountLimitType.NODE_LIMIT);
            nodeLimitType.setDescription("Node Limit");
            nodeLimitType.setDefaultValue(20);

            if(accountLimitRepository.getAllLimitTypes().size() < 2) {
                accountLimitRepository.save(nodeLimitType);
            }

            return nodeLimitType;
        }

        private LimitType setupLoadBalancerLimit() {
            LimitType loadbalancerLimitType = new LimitType();
            loadbalancerLimitType.setName(AccountLimitType.LOADBALANCER_LIMIT);
            loadbalancerLimitType.setDescription("LB Limit");
            loadbalancerLimitType.setDefaultValue(20);

            if(accountLimitRepository.getAllLimitTypes().size() < 2) {
                accountLimitRepository.save(loadbalancerLimitType);
            }

            return loadbalancerLimitType;
        }

        private Host setupTestHost(Cluster cluster) {
            Host host = new Host();
            host.setCluster(cluster);
            host.setName("test host");
            host.setHostStatus(HostStatus.ACTIVE_TARGET);
            host.setMaxConcurrentConnections(1);
            host.setCoreDeviceId("someId");
            host.setManagementIp("10.0.0.1");
            host.setEndpoint("endpoint");
            host.setRestEndpoint("restEndpoint");
            host.setTrafficManagerName("trafficManagerName");

            if (hostRepository.getAll().isEmpty()) {
                hostRepository.save(host);
            }

            return host;
        }

        private Cluster setupTestCluster() {
            Cluster cluster = new Cluster();
            cluster.setDataCenter(DataCenter.DFW);
            cluster.setDescription("cluster description");
            cluster.setName("testCluster");
            cluster.setPassword("cluster password");
            cluster.setUsername("cluster username");
            cluster.setStatus(org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus.ACTIVE);

            if (clusterRepository.getAll().isEmpty()) {
                clusterRepository.save(cluster);
            }

            return cluster;
        }

        @Test
        public void shouldReturnLoadBalancerWhenTimestampsMatchPeriod() throws Exception {
            String provisioned = "2013-04-10 05:00:00";
            loadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            loadBalancer.setProvisioned(CalendarUtils.stringToCalendar(provisioned));
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer = loadBalancerService.update(loadBalancer);

            Assert.assertEquals(provisioned, CalendarUtils.calendarToString(loadBalancer.getProvisioned()));

            final Calendar now = Calendar.getInstance();
            final Set<LbIdAccountId> loadBalancersActiveDuringPeriod = loadBalancerRepository.getLoadBalancersActiveDuringPeriod(loadBalancer.getProvisioned(), now);
            Assert.assertFalse(loadBalancersActiveDuringPeriod.isEmpty());
        }
    }

    public static class whenGettingLoadBalancersByName{

        @Mock
        private Query query;
        @Mock
        private EntityManager entityManager;
        @InjectMocks
        private LoadBalancerRepository loadBalancerRepository;
        private LoadBalancer loadBalancer1;
        private LoadBalancer loadBalancer2;
        private List<LoadBalancer> loadBalancerList;
        private String qStr = "SELECT lb FROM LoadBalancer lb";


        @Before
        public void setup(){
            MockitoAnnotations.initMocks(this);

            loadBalancer1 = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            loadBalancer1.setName("first-loadBalancer");
            loadBalancer1.setAccountId(1);
            loadBalancer1.setPort(8080);

            loadBalancer2 = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            loadBalancer2.setName("first-loadBalancer");
            loadBalancer2.setAccountId(1);
            loadBalancer2.setPort(8081);

            loadBalancerList = new ArrayList<>();
            loadBalancerList.add(loadBalancer1);
            loadBalancerList.add(loadBalancer2);

            when(entityManager.createQuery(anyString())).thenReturn(query);
            when(query.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(query);
            when(query.getResultList()).thenReturn(loadBalancerList);

        }

        @Test
        public void shouldReturnLoadBalancersListAndReturnStatus200(){
            Integer expected = 2;
            List<LoadBalancer> response = loadBalancerRepository.getLoadbalancersByName("first_loadBalancer", 0, 99);
            Integer actual = response.size();
            Assert.assertEquals(expected, actual);

        }
    }
}
