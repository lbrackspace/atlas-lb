package org.openstack.atlas.api.async;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


@Ignore
@RunWith(Enclosed.class)
public class UpdateSslTerminationListenerTest {
    @Test
    public void sampleTest() {

    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:context.xml"})
    @Transactional
    public static class WhenTestingUpdateSslTerminationListener {

        //TODO: update stuff for injections....

        @Autowired
        public UpdateSslTerminationListener updateSslTerminationListener;

        @Autowired
        @Qualifier("usageRefactorService")
        public UsageRefactorService usageRefactorService;

        @Autowired
        @Qualifier("usageEventProcessorImpl")
        public UsageEventProcessorImpl usageEventProcessor;

        @Autowired
        @Qualifier("loadBalancerRepository")
        public LoadBalancerRepository loadBalancerRepository;


        public NotificationService notificationService;

        public LoadBalancerService loadBalancerService;

        @Autowired
        public ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;

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

            loadBalancerService = mock(LoadBalancerService.class);
            notificationService = mock(NotificationService.class);
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
        public void shouldHaveNoPreviousUsagesForTestDB() throws Exception {
            Calendar starttime = Calendar.getInstance();
            starttime.roll(Calendar.MONTH, false);

            ObjectMessage m = mock(ObjectMessage.class);
            MessageDataContainer md = new MessageDataContainer();
            md.setAccountId(lb.getAccountId());
            md.setLoadBalancerId(lb.getId());
            md.setUserName("me");

            ZeusSslTermination zm = new ZeusSslTermination();
            md.setZeusSslTermination(zm);

            when(loadBalancerService.get(Matchers.anyInt(), Matchers.anyInt())).thenReturn(lb);
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            doNothing().when(reverseProxyLoadBalancerService).updateSslTermination(Matchers.<LoadBalancer>any(), Matchers.<ZeusSslTermination>any());

            updateSslTerminationListener.doOnMessage(m);

//            usageEventProcessor.processUsageEvent(snmpUsages, lb, UsageEvent.SSL_ON);
            Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> oUsages = usageRefactorService.getAllLoadBalancerHostUsages();
            Assert.assertNotNull(oUsages);
            Assert.assertEquals(1, oUsages.size());
            Assert.assertEquals(true, oUsages.containsKey(543221));
        }
    }
}

