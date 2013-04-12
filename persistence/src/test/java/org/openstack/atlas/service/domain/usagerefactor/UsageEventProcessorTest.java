package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Enclosed.class)
public class UsageEventProcessorTest {

    public static class WhenProcessingUsageEvents {
        SnmpUsage snmpUsage;
        SnmpUsage snmpUsage1;
        List<SnmpUsage> snmpUsages;
        LoadBalancer lb;


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
            UsageEventProcessor processor = new UsageEventProcessorImpl();
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
            UsageEventProcessor processor = new UsageEventProcessorImpl();
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
    }
}
