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
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.repository.AccountUsageRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.UsageService;
import org.openstack.atlas.service.domain.services.impl.UsageServiceImpl;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.snmp.StingrayUsageClient;
import org.openstack.atlas.usagerefactor.collection.SnmpVSCollector;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.openstack.atlas.usagerefactor.processor.impl.UsageEventProcessorImpl;
import org.openstack.atlas.util.snmp.exceptions.StingraySnmpGeneralException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(Enclosed.class)
public class UsageEventCollectionTest {

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(Executors.class)
    public static class WhenCollectingEventData {
        LoadBalancer lb;
        SnmpUsage snmpUsage;
        SnmpUsage snmpUsage1;
        List<SnmpUsage> snmpUsages;
        Calendar eventTime;

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
        UsageEventCollection usageEventCollection;

        public WhenCollectingEventData() throws UsageEventCollectionException {
            usageEventCollection = new UsageEventCollection();
        }

        @Before
        public void standUp() {
            eventTime = Calendar.getInstance();
        }

        @Ignore
        @Test
        public void shouldNotFailWhenCollectingUsageRecords() throws EntityNotFoundException, DeletedStatusException, InterruptedException, UsageEventCollectionException {
            mock(ExecutorService.class);
            PowerMockito.when(executorService.invokeAll(Matchers.anyCollection())).thenReturn(new ArrayList<java.util.concurrent.Future<Object>>());

            List<Host> hosts = new ArrayList<Host>();
            Host host = new Host();
            hosts.add(host);

            when(hostRepository.getAll()).thenReturn(hosts);
            List<Future<SnmpUsage>> futures = usageEventCollection.collectUsageRecords(executorService, new UsageEventProcessorImpl(), hosts, new LoadBalancer());

            Assert.assertNotNull(futures);
            usageEventCollection.collectUsageAndProcessUsageRecords(new LoadBalancer(), UsageEvent.SSL_ONLY_ON, eventTime);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(Executors.class)
    public static class WhenProcessingSingleUsageEvent {
        SnmpUsage snmpUsage;
        SnmpUsage snmpUsage1;
        List<SnmpUsage> snmpUsages;
        Calendar eventTime;

        @Mock
        LoadBalancer lb;

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
        UsageEventCollection usageEventCollection;

        public WhenProcessingSingleUsageEvent() throws UsageEventCollectionException {
            usageEventCollection = new UsageEventCollection();
        }

        @Before
        public void standUp() {
            eventTime = Calendar.getInstance();
        }

        @Test(expected = UsageEventCollectionException.class)
        public void shouldFailWhenNoHost() throws EntityNotFoundException, DeletedStatusException, InterruptedException, UsageEventCollectionException {
            when(hostRepository.getOnlineHostsByLoadBalancerHostCluster(lb)).thenReturn(null);
            usageEventCollection.processZeroUsageEvent(lb, UsageEvent.CREATE_LOADBALANCER, eventTime);
        }

        @Test
        public void shouldProcessSnmpUsageCreateEvent() throws EntityNotFoundException, DeletedStatusException, InterruptedException, UsageEventCollectionException {
            List<Host> hosts = new ArrayList<Host>();
            Host host = new Host();
            host.setId(7);
            hosts.add(host);

            when(hostRepository.getOnlineHostsByLoadBalancerHostCluster(lb)).thenReturn(hosts);
            usageEventCollection.processZeroUsageEvent(lb, UsageEvent.CREATE_LOADBALANCER, eventTime);
        }

        @Test
        public void shouldProcessSnmpUsageCreateEventVerifyHosts() throws EntityNotFoundException, DeletedStatusException, InterruptedException, UsageEventCollectionException {
            List<Host> hosts = new ArrayList<Host>();
            Host host = new Host();
            host.setId(7);
            hosts.add(host);

            when(hostRepository.getOnlineHostsByLoadBalancerHostCluster(lb)).thenReturn(hosts);
            usageEventCollection.processZeroUsageEvent(lb, UsageEvent.CREATE_LOADBALANCER, eventTime);
            //Assert.assertEquals(host, usageEventCollection.getHosts().get(0));
        }
    }

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(Executors.class)
    public static class WhenSnmpVSCollectorRuns {
        LoadBalancer lb;
        SnmpUsage snmpUsage;
        SnmpUsage snmpUsage1;
        List<SnmpUsage> snmpUsages;
        Host host;
        LoadBalancer loadBalancer;

        @Mock
        StingrayUsageClient stingrayUsageClient;

        @InjectMocks
        SnmpVSCollector vsCollector = new SnmpVSCollector();

        @Before
        public void standUp() {
            host = new Host();
            host.setHostStatus(HostStatus.ACTIVE);
            host.setSoapEndpointActive(true);
            host.setId(7);

            Cluster cluster = new Cluster();
            cluster.setId(1);
            host.setCluster(cluster);
            host.setEndpoint("http://my.endpoint.com");
            host.setManagementIp("192.168.1.1");

            loadBalancer = new LoadBalancer();
            loadBalancer.setAccountId(54321);
            loadBalancer.setId(9);
            loadBalancer.setHost(host);
        }

        @Test
        public void shouldReturnSnmpUsage() throws UsageEventCollectionException, StingraySnmpGeneralException {
            vsCollector.setHost(host);
            vsCollector.setLoadbalancer(loadBalancer);
            when(stingrayUsageClient.getVirtualServerUsage(Matchers.<Host>any(), Matchers.<LoadBalancer>any())).thenReturn(new SnmpUsage());
            SnmpUsage usage = vsCollector.call();
            Assert.assertNotNull(usage);
        }
    }
}
