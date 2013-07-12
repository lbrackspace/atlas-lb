package org.openstack.atlas.api.async;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.usagerefactor.SnmpUsage;

import java.util.ArrayList;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;

@Ignore
@RunWith(Enclosed.class)
public class CreateLoadBalancerListenerTest {

    public static class WhenCreatingLoadBalancer {

        private ArrayList<SnmpUsage> snmpUsages;
        private LoadBalancer lb;
        private SnmpUsage snmpUsage;
        private LoadBalancerService loadBalancerService;
        private NotificationService notificationService;

        @Before
        public void standUp() throws Exception {
            lb = new LoadBalancer();
            lb.setId(543221);
            lb.setAccountId(55555);


            loadBalancerService = mock(LoadBalancerService.class);
            notificationService = mock(NotificationService.class);

            //These wont return anything, successful to start with; verify when failure occurs...
            doNothing().when(loadBalancerService).update(Matchers.<LoadBalancer>any());
            doNothing().when(loadBalancerService).setStatus(Matchers.<LoadBalancer>any(), Matchers.<LoadBalancerStatus>any());
            doNothing().when(notificationService).saveAlert(Matchers.<Integer>any(), Matchers.<Integer>any(), Matchers.<Exception>any(), Matchers.<String>any(), Matchers.<String>any());
            doNothing().when(notificationService).saveAlert(Matchers.<Exception>any(), Matchers.<String>any(), Matchers.<String>any());
            doNothing().when(notificationService).saveSslTerminationEvent(Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any(),
                    Matchers.<Integer>any(), Matchers.<String>any(), Matchers.<String>any(), Matchers.<EventType>any(), Matchers.<CategoryType>any(), Matchers.<EventSeverity>any());
            doNothing().when(notificationService).saveLoadBalancerEvent(Matchers.<String>any(), Matchers.<Integer>any(), Matchers.<Integer>any(), Matchers.<String>any(),
                    Matchers.<String>any(), Matchers.<EventType>any(), Matchers.<CategoryType>any(), Matchers.<EventSeverity>any());
        }

        @Test
        public void shouldSetLBActiveIfSuccessfull() {

        }
    }
}
