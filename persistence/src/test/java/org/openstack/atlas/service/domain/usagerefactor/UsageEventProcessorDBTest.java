package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(Enclosed.class)
public class UsageEventProcessorDBTest {

//    @RunWith(MockitoJUnitRunner.class)
    @RunWith(SpringJUnit4ClassRunner.class)

    @ContextConfiguration(locations = {"classpath:context.xml"})
    @Transactional
    public static class WhenTestingProcessRecordsNoEvents {

        @Autowired
        @Qualifier("usageRefactorService")
        public UsageRefactorService usageRefactorService;

        @Autowired
        public UsageEventProcessorImpl usageEventProcessor;

        @Autowired
        public LoadBalancerRepository loadBalancerRepository;

        public SnmpUsage snmpUsage;
        public SnmpUsage snmpUsage2;
        public List<SnmpUsage> snmpUsages;
        public LoadBalancer lb;

        @Before
        public void standUp() throws Exception {
            loadBalancerRepository = mock(LoadBalancerRepository.class);

            lb = new LoadBalancer();
            lb.setId(543221);
            lb.setAccountId(55555);

            snmpUsages = new ArrayList<SnmpUsage>();
            snmpUsage = new SnmpUsage();
            snmpUsage.setHostId(1);
            snmpUsage.setLoadbalancerId(lb.getId());
            snmpUsage.setBytesIn(1234455);
            snmpUsage.setBytesInSsl(4321);
            snmpUsage.setBytesOut(987);
            snmpUsage.setBytesOutSsl(986);
            snmpUsage.setConcurrentConnections(1);
            snmpUsage.setConcurrentConnectionsSsl(3);
            snmpUsages.add(snmpUsage);
        }

        @Test
        public void shouldHaveNoPreviousUsages() {
            Calendar starttime = Calendar.getInstance();
            starttime.roll(Calendar.MONTH, false);

            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(0, oUsages.size());
        }

        @Test
        public void shouldCreateSimpleUsageRecord() {
            Calendar starttime = Calendar.getInstance();
            starttime.roll(Calendar.MONTH, false);

            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.<Integer>any())).thenReturn(true);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));
        }
    }
}