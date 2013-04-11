package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;

import java.util.Calendar;
import java.util.List;

@RunWith(Enclosed.class)
public class UsageEventProcessorTest {

    class WhenProcessingUsageEvents {
        SnmpUsage snmpUsage;
        List<SnmpUsage> snmpUsages;
        LoadBalancer lb;


        @Before
        public void standUp() {
            snmpUsage = new SnmpUsage();
            snmpUsage.setHostId(1);
            snmpUsage.setLoadbalancerId(123);
            snmpUsage.setBytesIn(1234455);
            snmpUsage.setBytesInSsl(4321);
            snmpUsage.setLoadbalancerId(543221);
            snmpUsage.setBytesOut(987);
            snmpUsage.setBytesOutSsl(986);
            snmpUsage.setConcurrentConnections(1);
            snmpUsage.setConcurrentConnectionsSsl(3);
            snmpUsages.add(snmpUsage);
            lb = new LoadBalancer();
            lb.setId(543221);


        }

        @Test
        public void shouldMapBasicUsageRecord() {
            UsageEventProcessor processor = new UsageEventProcessorImpl();
            LoadBalancerMergedHostUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            mappedUsage = processor.mapSnmpUsage(snmpUsage, lb, now, UsageEvent.SSL_OFF);
//            Assert.assertEquals(1, mappedUsage.get);

        }
    }
}
