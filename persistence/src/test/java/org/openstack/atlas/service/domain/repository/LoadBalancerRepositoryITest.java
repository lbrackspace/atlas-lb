package org.openstack.atlas.service.domain.repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.Base;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.util.common.CalendarUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.Set;

@RunWith(Enclosed.class)
public class LoadBalancerRepositoryITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenGettingLoadBalancersActiveDuringPeriod extends Base {

        @Before
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
        }

        @After
        public void tearDown() throws Exception {
            loadBalancerRepository.delete(loadBalancer);
            super.tearDown();
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
}
