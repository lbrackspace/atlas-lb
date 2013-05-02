package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.impl.UsageRefactorServiceImpl;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;

import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.when;

@Ignore
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
        UsageRefactorService usageService1 = new UsageRefactorServiceImpl();

        @Mock
        UsageRefactorService usageService;

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

    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenProcessingAccountUsageEvents {
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
        UsageRefactorService usageService1 = new UsageRefactorServiceImpl();

        @Mock
        UsageRefactorService usageService;

        @InjectMocks
        UsageEventProcessor processor = new UsageEventProcessorImpl();

        @Before
        public void standUp() {
            lb = new LoadBalancer();
            lb.setId(543221);
            lb.setAccountId(55555);
        }

        @Test
        public void shouldMapBasicAccountUsageRecord() {
            when(loadBalancerRepository.getNumNonDeletedLoadBalancersForAccount(Matchers.<Integer>any())).thenReturn(2);
            when(virtualIpRepository.getNumUniqueVipsForAccount(Matchers.<Integer>any(), Matchers.<VirtualIpType>any())).thenReturn(6);

            AccountUsage mappedUsage;
            Calendar now = Calendar.getInstance();
            mappedUsage = processor.createAccountUsageEntry(lb, now);
            Assert.assertEquals(lb.getAccountId(), mappedUsage.getAccountId());
            Assert.assertEquals(6, (int) mappedUsage.getNumServicenetVips());
            Assert.assertEquals(6, (int) mappedUsage.getNumPublicVips());
            Assert.assertEquals(2, (int) mappedUsage.getNumLoadBalancers());
            Assert.assertEquals(now, mappedUsage.getStartTime());

        }
    }
}
