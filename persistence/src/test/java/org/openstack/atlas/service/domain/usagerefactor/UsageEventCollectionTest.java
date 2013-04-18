package org.openstack.atlas.service.domain.usagerefactor;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.services.impl.UsageServiceImpl;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(Enclosed.class)
public class UsageEventCollectionTest {

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(Executors.class)
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

        @Mock
        HostRepository hostRepository;

        @Mock
        ExecutorService executorService;

        @Mock
        Executors executors;

        @InjectMocks
        UsageService usageService1 = new UsageServiceImpl();

        @Mock
        UsageService usageService;

        @InjectMocks
        UsageEventProcessor processor = new UsageEventProcessorImpl();

        @Mock
        UsageEventProcessor processor2;

        @InjectMocks
        UsageEventCollection usageEventCollection = new UsageEventCollection();

        @Before
        public void standUp() {

        }

        @Test
        public void shasdfouldMapDeleteLoadBalancer() throws EntityNotFoundException, DeletedStatusException {
            mockStatic(Executors.class);
            PowerMockito.when(Executors.newFixedThreadPool(1)).thenReturn();

            List<Host> hosts = new ArrayList<Host>();
            Host host = new Host();
            hosts.add(host);

            when(hostRepository.getAllHosts()).thenReturn(hosts);

            usageEventCollection.processUsageRecord(new LoadBalancer(), UsageEvent.SSL_ONLY_ON);
        }
    }
}
