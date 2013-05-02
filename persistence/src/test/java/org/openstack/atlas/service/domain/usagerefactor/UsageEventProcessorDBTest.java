package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
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

import java.util.*;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith(Enclosed.class)
public class UsageEventProcessorDBTest {

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
        public void shouldHaveNoPreviousUsagesForTestDB() {
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

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ON);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(1234455, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(3, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(0, lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldCreateSimpleServicenetUsageRecord() {
            Calendar starttime = Calendar.getInstance();
            starttime.roll(Calendar.MONTH, false);

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(1234455, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(3, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(2, lbusages.get(0).getTagsBitmask());
        }
    }
}