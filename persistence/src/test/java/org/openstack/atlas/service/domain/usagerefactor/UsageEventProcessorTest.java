package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.services.impl.UsageServiceImpl;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageEventProcessorTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingUsageEvents {
        LoadBalancer lb;
        SnmpUsage snmpUsage;
        SnmpUsage snmpUsage1;
        List<SnmpUsage> snmpUsages;

        @Mock
        VirtualIpRepository virtualIpRepository;

        @Mock
        UsageRefactorService usageRefactorService;

        @Mock
        AccountUsageRepository accountUsageRepository;

        @Mock
        LoadBalancerRepository loadBalancerRepository;

        @Mock
        HostUsageRefactorRepository hostUsageRefactorRepository;

        @InjectMocks
        UsageService usageService1 = new UsageServiceImpl();

        @Mock
        UsageService usageService;

        @InjectMocks
        UsageEventProcessor processor = new UsageEventProcessorImpl();

        @Before
        public void standUp() {
            lb = new LoadBalancer();
            lb.setId(543221);
            lb.setAccountId(55555);
            snmpUsage = new SnmpUsage();
            snmpUsage.setHostId(1);
            snmpUsage.setLoadbalancerId(lb.getId());
            snmpUsage.setBytesIn(1234455);
            snmpUsage.setBytesInSsl(4321);
            snmpUsage.setBytesOut(987);
            snmpUsage.setBytesOutSsl(986);
            snmpUsage.setConcurrentConnections(1);
            snmpUsage.setConcurrentConnectionsSsl(3);
        }

        @Test
        public void shouldMapBasicUsageRecord() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_OFF);
            Assert.assertEquals(1, mappedUsage.getHostId());
            Assert.assertEquals(543221, mappedUsage.getLoadbalancerId());
            Assert.assertEquals(1234455, mappedUsage.getIncomingTransfer());
            Assert.assertEquals(4321, mappedUsage.getIncomingTransferSsl());
            Assert.assertEquals(987, mappedUsage.getOutgoingTransfer());
            Assert.assertEquals(986, mappedUsage.getOutgoingTransferSsl());
            Assert.assertEquals(1, mappedUsage.getConcurrentConnections());
            Assert.assertEquals(3, mappedUsage.getConcurrentConnectionsSsl());
        }

        @Test
        public void shouldMapBasicUsageRecordWithNumVips() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);
            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_OFF);
            Assert.assertEquals(1, mappedUsage.getHostId());
            Assert.assertEquals(543221, mappedUsage.getLoadbalancerId());
            Assert.assertEquals(1234455, mappedUsage.getIncomingTransfer());
            Assert.assertEquals(4321, mappedUsage.getIncomingTransferSsl());
            Assert.assertEquals(987, mappedUsage.getOutgoingTransfer());
            Assert.assertEquals(986, mappedUsage.getOutgoingTransferSsl());
            Assert.assertEquals(1, mappedUsage.getConcurrentConnections());
            Assert.assertEquals(3, mappedUsage.getConcurrentConnectionsSsl());
            Assert.assertEquals(1, mappedUsage.getNumVips());
        }

        @Test
        public void shouldMapBitTagsSSLON() {
            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_ONLY_ON);
            Assert.assertEquals(BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());

        }

        @Test
        public void shouldMapBitTagsCreatLB() {
            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(null);
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.CREATE_LOADBALANCER);
            Assert.assertEquals(0, mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapBitTagsPrevUsageSSLON() {
            LoadBalancerHostUsage usage = new LoadBalancerHostUsage();
            usage.setTagsBitmask(BitTag.SSL.tagValue());

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(usage);
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_ONLY_ON);
            Assert.assertEquals(BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapBitTagsPrevUsageSSLMixed() {
            LoadBalancerHostUsage usage = new LoadBalancerHostUsage();
            usage.setTagsBitmask(BitTag.SSL.tagValue());

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(usage);
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_MIXED_ON);
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapServiceNetLB() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.anyInt(), Matchers.anyInt())).thenReturn(vips);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.CREATE_LOADBALANCER);
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapServiceNetLBSSLON() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.anyInt(), Matchers.anyInt())).thenReturn(vips);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_ONLY_ON);
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue() + BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapServiceNetLBSSLMixed() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.anyInt(), Matchers.anyInt())).thenReturn(vips);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_MIXED_ON);
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue() + BitTag.SSL.tagValue() + +BitTag.SSL_MIXED_MODE.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapDeleteServiceNetLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.anyInt(), Matchers.anyInt())).thenReturn(vips);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.DELETE_LOADBALANCER);
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapCreateServiceNetLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());
            when(loadBalancerRepository.getVipsByAccountIdLoadBalancerId(Matchers.anyInt(), Matchers.anyInt())).thenReturn(vips);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.CREATE_LOADBALANCER);
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapDeleteLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            when(usageService1.getRecentHostUsageRecord(Matchers.anyInt())).thenReturn(new LoadBalancerHostUsage());

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.DELETE_LOADBALANCER);
            Assert.assertEquals(0, mappedUsage.getTagsBitmask());
        }
        @Test
        public void shasdfouldMapDeleteLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            UsageEventCollection c = new UsageEventCollection();
            c.processUsageRecord(null, null);
        }
    }
}
