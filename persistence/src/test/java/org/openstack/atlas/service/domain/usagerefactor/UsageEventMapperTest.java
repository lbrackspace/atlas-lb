package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.mapper.UsageEventMapper;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Enclosed.class)
public class UsageEventMapperTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingUsageEvents {
        LoadBalancer lb;
        SnmpUsage snmpUsage;
        SnmpUsage snmpUsage1;
        List<SnmpUsage> snmpUsages;
        UsageEventMapper usageEventMapper;


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

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_OFF, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();

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
        public void shouldMapPollTime() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_OFF, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();

            Assert.assertEquals(now, mappedUsage.getPollTime());
        }

        @Test
        public void shouldMapBasicUsageRecordWithNumVips() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_OFF, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();

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
        public void shouldMapBasicUsageRecordWithMulipleNumVips() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            LoadBalancerJoinVip jv2 = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            jvs.add(jv2);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_OFF, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();

            Assert.assertEquals(2, mappedUsage.getNumVips());
        }

        @Test
        public void shouldMapBasicUsageRecordWithMulipleNumVipsAndPollTime() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            LoadBalancerJoinVip jv2 = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            jvs.add(jv2);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_OFF, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();

            Assert.assertEquals(2, mappedUsage.getNumVips());
            Assert.assertEquals(now, mappedUsage.getPollTime());
        }

        @Test
        public void shouldMapBitTagsSSLON() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_ONLY_ON, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());

        }

        @Test
        public void shouldMapBasicUsageRecordWithMulipleNumVipsAndPollTimeAndSSLOn() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            LoadBalancerJoinVip jv2 = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            jvs.add(jv2);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_ONLY_ON, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();

            Assert.assertEquals(2, mappedUsage.getNumVips());
            Assert.assertEquals(now, mappedUsage.getPollTime());
            Assert.assertEquals(BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapCreatLB() {
            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.CREATE_LOADBALANCER, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(0, mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapBitTagsPrevUsageSSLON() {
            LoadBalancerHostUsage usage = new LoadBalancerHostUsage();
            usage.setTagsBitmask(BitTag.SSL.tagValue());

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_ONLY_ON, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapBitTagsPrevUsageSSLMixed() {
            LoadBalancerHostUsage usage = new LoadBalancerHostUsage();
            usage.setTagsBitmask(BitTag.SSL.tagValue());

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.SSL_MIXED_ON, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SSL.tagValue() + BitTag.SSL_MIXED_MODE.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapServiceNeCreatetLB() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, true, snmpUsage, UsageEvent.CREATE_LOADBALANCER, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapServiceNetLBSSLON() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, true, snmpUsage, UsageEvent.SSL_ONLY_ON, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue() + BitTag.SSL.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapServiceNetLBSSLMixed() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, true, snmpUsage, UsageEvent.SSL_MIXED_ON, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue() + BitTag.SSL.tagValue() + +BitTag.SSL_MIXED_MODE.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapDeleteServiceNetLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, true, snmpUsage, UsageEvent.DELETE_LOADBALANCER, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapCreateServiceNetLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            Set<VirtualIp> vips = new HashSet<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setVipType(VirtualIpType.SERVICENET);
            vips.add(vip);

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, true, snmpUsage, UsageEvent.CREATE_LOADBALANCER, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(BitTag.SERVICENET_LB.tagValue(), mappedUsage.getTagsBitmask());
        }

        @Test
        public void shouldMapDeleteLoadBalancer() throws EntityNotFoundException, DeletedStatusException {

            LoadBalancerHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip();
            Set<LoadBalancerJoinVip> jvs = new HashSet<LoadBalancerJoinVip>();
            jvs.add(jv);
            lb.setLoadBalancerJoinVipSet(jvs);

            usageEventMapper = new UsageEventMapper(lb, false, snmpUsage, UsageEvent.DELETE_LOADBALANCER, now);
            mappedUsage = usageEventMapper.mapSnmpUsageToUsageEvent();
            Assert.assertEquals(0, mappedUsage.getTagsBitmask());
        }
    }
}
