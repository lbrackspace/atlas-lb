package org.openstack.atlas.service.domain.repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.Base;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.DataCenter;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.entities.LimitType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.util.common.CalendarUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Set;

@RunWith(Enclosed.class)
public class LoadBalancerRepositoryITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenGettingLoadBalancersActiveDuringPeriod extends Base {

        private LoadBalancer loadBalancer;
        private Integer accountId = 1234;

        @Before
        public void standUp() throws Exception {
            Cluster testCluster = setupTestCluster();
            Host testHost = setupTestHost(testCluster);

            LimitType loadbalancerLimitType = setupLoadBalancerLimit();
            LimitType nodeLimitType = setupNodeLimit();
            setupAccountLimits(loadbalancerLimitType, nodeLimitType);

            loadBalancerService.setDefaultErrorPage("defaultErrorPage");

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
        }

        @After
        public void tearDown() {
            loadBalancerRepository.delete(loadBalancer);
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
            String created = "2013-04-10 05:00:00";
            loadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            loadBalancer.setCreated(CalendarUtils.stringToCalendar(created));
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer = loadBalancerService.update(loadBalancer);

            Assert.assertEquals(created, CalendarUtils.calendarToString(loadBalancer.getCreated()));

            final Calendar now = Calendar.getInstance();
            final Set<LbIdAccountId> loadBalancersActiveDuringPeriod = loadBalancerRepository.getLoadBalancersActiveDuringPeriod(loadBalancer.getCreated(), now);
            Assert.assertFalse(loadBalancersActiveDuringPeriod.isEmpty());
        }
    }
}
