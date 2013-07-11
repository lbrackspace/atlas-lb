package org.openstack.atlas.api.async;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.ArrayList;

@RunWith(Enclosed.class)
public class CreateLoadBalancerListenerTest {

    public static class WhenCreatingLoadBalancer {

        private ArrayList<SnmpUsage> snmpUsages;
        private LoadBalancer lb;
        private SnmpUsage snmpUsage;

        @Before
        public void standUp() throws Exception {
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

//            loadBalancerService = mock(LoadBalancerService.class);
//            notificationService = mock(NotificationService.class);
//            doNothing().when(loadBalancerService).update(Matchers.<LoadBalancer>any());
//            doNothing().when(loadBalancerService).setStatus(Matchers.<LoadBalancer>any(), Matchers.<LoadBalancerStatus>any());
//            doNothing().when(notificationService).saveAlert(Matchers.<Integer>any(), Matchers.<Integer>any(), Matchers.<Exception>any(), Matchers.<String>any(), Matchers.<String>any());
//            doNothing().when(notificationService).saveAlert(Matchers.<Exception>any(), Matchers.<String>any(), Matchers.<String>any());
//            doNothing().when(notificationService).saveSslTerminationEvent(Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any(),
//                    Matchers.<Integer>any(), Matchers.<String>any(), Matchers.<String>any(), Matchers.<EventType>any(), Matchers.<CategoryType>any(), Matchers.<EventSeverity>any());
//            doNothing().when(notificationService).saveLoadBalancerEvent(Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any(), Matchers.<String>any(),
//                    Matchers.<String>any(), Matchers.<EventType>any(), Matchers.<CategoryType>any(), Matchers.<EventSeverity>any());
        }

        @Test
        public void sampleTest() {

        }
    }
}
