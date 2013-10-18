package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
    public static class WhenTestingProcessingUsageRecords {

        @Autowired
        public UsageRefactorService usageRefactorService;

        @Autowired
        public UsageEventProcessorImpl usageEventProcessor;

        @Autowired
        public LoadBalancerRepository loadBalancerRepository;

        @Autowired
        public LoadBalancerService loadBalancerService;

        @Autowired
        public VirtualIpRepository virtualIpRepository;

        public SnmpUsage snmpUsage;
        public SnmpUsage snmpUsage2;
        public List<SnmpUsage> snmpUsages;
        public LoadBalancer lb;
        public BitTags bitTags = new BitTags();

        @Before
        public void standUp() throws Exception {
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            loadBalancerService = mock(LoadBalancerService.class);
            virtualIpRepository = mock(VirtualIpRepository.class);

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

            usageEventProcessor.setLoadBalancerService(loadBalancerService);
            when(loadBalancerService.getCurrentBitTags(Matchers.anyInt())).thenReturn(bitTags);
            usageEventProcessor.setVirtualIpRepository(virtualIpRepository);
            when(virtualIpRepository.getNumIpv4VipsForLoadBalancer(lb)).thenReturn(1L);
        }

        @Test
        public void shouldHaveNoPreviousUsagesForTestDB() {
            Calendar eventTime = Calendar.getInstance();
            

            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(0, oUsages.size());
        }

        @Test
        public void shouldCreateSimpleUsageRecord() {
            Calendar eventTime = Calendar.getInstance();

            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
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
            Assert.assertEquals(5, lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldCreateSimpleServicenetUsageRecord() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
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
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldCreateServicenetSSLONUsageRecord() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            bitTags.flipTagOn(BitTag.SSL);
            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
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
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue() + BitTag.SSL.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldCreateServicenetSSLMixedUsageRecord() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
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
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue() + BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldCreateSSLONUsageRecord() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            bitTags.flipTagOn(BitTag.SSL);
            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
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
            Assert.assertEquals(BitTag.SSL.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldCreateSSLMixedUsageRecord() {
            Calendar eventTime = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
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
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldProcessWhenSimulateCreateEventSslMixed() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue(), lbusages.get(0).getTagsBitmask());

        }

        @Test
        public void shouldProcessWhenSimulateCreateEventSslOff() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(0, lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldProcessWhenSimulateCreateEventSslOn() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            bitTags.flipTagOn(BitTag.SSL);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldProcessWhenSimulateCreateEventSslMixedServicenet() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue() + BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());

        }

        @Test
        public void shouldProcessWhenSimulateCreateEventSslOnServicenet() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            bitTags.flipTagOn(BitTag.SSL);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Test
        public void shouldProcessWhenSimulateCreateEventSslOffServicenet() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap = oUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages = usagemap.get(1);

            Assert.assertEquals(543221, lbusages.get(0).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages.get(0).getAccountId());
            Assert.assertEquals(1, lbusages.get(0).getHostId());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getIncomingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, lbusages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnections());
            Assert.assertEquals(0, lbusages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());
        }

        @Ignore
        @Test
        public void shouldProcessUsageAndGetPreviousRecordWhenSnmpCollectorFailureSslOff() {
            Calendar eventTime = Calendar.getInstance();

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
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

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            Calendar nextEventTime = Calendar.getInstance();
            nextEventTime.setTime(lbusages.get(0).getPollTime().getTime());
            nextEventTime.add(Calendar.MINUTE, 1);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> allUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(allUsages);
            Assert.assertEquals(1, allUsages.size());
            Assert.assertEquals(true, allUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap2 = allUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages2 = usagemap2.get(1);

            Assert.assertEquals(543221, lbusages2.get(1).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages2.get(1).getAccountId());
            Assert.assertEquals(1, lbusages2.get(1).getHostId());
            Assert.assertEquals(1234455, lbusages2.get(1).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages2.get(1).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages2.get(1).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages2.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages2.get(1).getConcurrentConnections());
            Assert.assertEquals(3, lbusages2.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(0, lbusages2.get(1).getTagsBitmask());
        }

        @Ignore
        @Test
        public void shouldProcessUsageAndGetPreviousRecordWhenSnmpCollectorFailureSslOn() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            bitTags.flipTagOn(BitTag.SSL);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
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
            Assert.assertEquals(BitTag.SSL.tagValue(), lbusages.get(0).getTagsBitmask());

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> allUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(allUsages);
            Assert.assertEquals(1, allUsages.size());
            Assert.assertEquals(true, allUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap2 = allUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages2 = usagemap2.get(1);

            Assert.assertEquals(543221, lbusages2.get(1).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages2.get(1).getAccountId());
            Assert.assertEquals(1, lbusages2.get(1).getHostId());
            Assert.assertEquals(1234455, lbusages2.get(1).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages2.get(1).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages2.get(1).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages2.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages2.get(1).getConcurrentConnections());
            Assert.assertEquals(3, lbusages2.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue(), lbusages2.get(1).getTagsBitmask());
        }

        @Ignore
        @Test
        public void shouldProcessUsageAndGetPreviousRecordWhenSnmpCollectorFailureSslMixed() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.PUBLIC);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);

            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
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
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue(), lbusages.get(0).getTagsBitmask());

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> allUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(allUsages);
            Assert.assertEquals(1, allUsages.size());
            Assert.assertEquals(true, allUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap2 = allUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages2 = usagemap2.get(1);

            Assert.assertEquals(543221, lbusages2.get(1).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages2.get(1).getAccountId());
            Assert.assertEquals(1, lbusages2.get(1).getHostId());
            Assert.assertEquals(1234455, lbusages2.get(1).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages2.get(1).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages2.get(1).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages2.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages2.get(1).getConcurrentConnections());
            Assert.assertEquals(3, lbusages2.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue(), lbusages2.get(1).getTagsBitmask());
        }

        @Ignore
        @Test
        public void shouldProcessUsageAndGetPreviousRecordWhenSnmpCollectorFailureSslOffServicenet() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
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
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_OFF, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> allUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(allUsages);
            Assert.assertEquals(1, allUsages.size());
            Assert.assertEquals(true, allUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap2 = allUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages2 = usagemap2.get(1);

            Assert.assertEquals(543221, lbusages2.get(1).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages2.get(1).getAccountId());
            Assert.assertEquals(1, lbusages2.get(1).getHostId());
            Assert.assertEquals(1234455, lbusages2.get(1).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages2.get(1).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages2.get(1).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages2.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages2.get(1).getConcurrentConnections());
            Assert.assertEquals(3, lbusages2.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), lbusages2.get(1).getTagsBitmask());
        }

        @Ignore
        @Test
        public void shouldProcessUsageAndGetPreviousRecordWhenSnmpCollectorFailureSslOnServicenet() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            bitTags.flipTagOn(BitTag.SSL);
            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
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
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ONLY_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> allUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(allUsages);
            Assert.assertEquals(1, allUsages.size());
            Assert.assertEquals(true, allUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap2 = allUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages2 = usagemap2.get(1);

            Assert.assertEquals(543221, lbusages2.get(1).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages2.get(1).getAccountId());
            Assert.assertEquals(1, lbusages2.get(1).getHostId());
            Assert.assertEquals(1234455, lbusages2.get(1).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages2.get(1).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages2.get(1).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages2.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages2.get(1).getConcurrentConnections());
            Assert.assertEquals(3, lbusages2.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SERVICENET_LB.tagValue(), lbusages2.get(1).getTagsBitmask());
        }

        @Ignore
        @Test
        public void shouldProcessUsageAndGetPreviousRecordWhenSnmpCollectorFailureSslMixedServicenet() {
            Calendar eventTime = Calendar.getInstance();
            

            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            jv.setVirtualIp(vip);
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventProcessor.setLoadBalancerRepository(loadBalancerRepository);
            when(loadBalancerRepository.isServicenetLoadBalancer(Matchers.anyInt())).thenReturn(true);

            bitTags.flipTagOn(BitTag.SERVICENET_LB);
            bitTags.flipTagOn(BitTag.SSL);
            bitTags.flipTagOn(BitTag.SSL_MIXED_MODE);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
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
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue()
                    + BitTag.SERVICENET_LB.tagValue(), lbusages.get(0).getTagsBitmask());

            SnmpUsage usage = new SnmpUsage();
            usage.setBytesOut(0);
            usage.setBytesIn(0);
            usage.setBytesOutSsl(0);
            usage.setBytesInSsl(0);
            usage.setHostId(1);
            snmpUsages.clear();
            snmpUsages.add(usage);
            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_MIXED_ON, eventTime);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> allUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(allUsages);
            Assert.assertEquals(1, allUsages.size());
            Assert.assertEquals(true, allUsages.containsKey(543221));

            Map<Integer, List<LoadBalancerHostUsage>> usagemap2 = allUsages.get(543221);
            List<LoadBalancerHostUsage> lbusages2 = usagemap2.get(1);

            Assert.assertEquals(543221, lbusages2.get(1).getLoadbalancerId());
            Assert.assertEquals(55555, lbusages2.get(1).getAccountId());
            Assert.assertEquals(1, lbusages2.get(1).getHostId());
            Assert.assertEquals(1234455, lbusages2.get(1).getIncomingTransfer());
            Assert.assertEquals(4321, lbusages2.get(1).getIncomingTransferSsl());
            Assert.assertEquals(987, lbusages2.get(1).getOutgoingTransfer());
            Assert.assertEquals(986, lbusages2.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(1, lbusages2.get(1).getConcurrentConnections());
            Assert.assertEquals(3, lbusages2.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(BitTag.SSL.tagValue()
                    + BitTag.SSL_MIXED_MODE.tagValue()
                    + BitTag.SERVICENET_LB.tagValue(), lbusages2.get(1).getTagsBitmask());
        }
    }
}