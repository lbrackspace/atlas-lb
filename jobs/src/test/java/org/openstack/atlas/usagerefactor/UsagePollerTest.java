package org.openstack.atlas.usagerefactor;

import org.apache.camel.processor.loadbalancer.LoadBalancer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.impl.HostServiceImpl;
import org.openstack.atlas.service.domain.services.impl.UsageRefactorServiceImpl;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsagePollerTest {

    public static class WhenLBHostUsageTableIsEmpty {

        private int accountId = 5806065;
        private int lbId = 1234;

        private List<LoadBalancerHostUsage> lbHostUsages;
        private UsagePoller usagePoller;
        private Calendar initialPollTime;
        private Calendar hourToProcess;

        @Before
        public void standUp() {
            usagePoller = new UsagePollerImpl();
            initialPollTime = new GregorianCalendar(2013, Calendar.MARCH, 20, 10, 0, 0);
        }

        @Test
        public void placementTest() {
        }
    }

    public static class WhenTestingBasicRequests {
        private SnmpUsageCollectorImpl snmpUsageCollector;
        private HostService hostService;
        private StingrayUsageClient client;
        private Map<Integer, SnmpUsage>map;
        private List<Host> hosts;

        @Before
        public void standUp() throws Exception {
            snmpUsageCollector = new SnmpUsageCollectorImpl();
            hostService = mock(HostService.class);
            client = mock(StingrayUsageClientImpl.class);
            hosts = new ArrayList<Host>();
            Host host1 = new Host();
            host1.setId(1);
            host1.setName("TestHost1");
            hosts.add(host1);
            Host host2 = new Host();
            host2.setId(2);
            host1.setName("TestHost2");
            hosts.add(host2);
            map = new HashMap<Integer, SnmpUsage>();
            SnmpUsage usage1 = new SnmpUsage();
            usage1.setBytesIn(10);
            usage1.setBytesOut(10);
            usage1.setBytesInSsl(5);
            usage1.setBytesOutSsl(5);
            usage1.setConcurrentConnections(2);
            usage1.setConcurrentConnectionsSsl(2);
            usage1.setHostId(host1.getId());
            SnmpUsage usage2 = new SnmpUsage();
            usage2.setBytesIn(10);
            usage2.setBytesOut(10);
            usage2.setBytesInSsl(5);
            usage2.setBytesOutSsl(5);
            usage2.setConcurrentConnections(2);
            usage2.setConcurrentConnectionsSsl(2);
            usage2.setHostId(host2.getId());
            map.put(host1.getId(), usage1);
            map.put(host2.getId(), usage2);
            when(hostService.getAllHosts()).thenReturn(hosts);
            when(client.getHostUsage(Matchers.<Host>any())).thenReturn(map);
            snmpUsageCollector.setHostService(hostService);
        }

        @Test
        public void getCurrentDataTest() throws Exception {
            Assert.assertNotNull(snmpUsageCollector.getCurrentData());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenTestingProcessRecordsNoEvents {

        @MockitoAnnotations.Mock
        private HostService hostService;

        @MockitoAnnotations.Mock
        private SnmpUsageCollector snmpUsageCollector;

        @InjectMocks
        private UsagePoller usagePoller = new UsagePollerImpl();

        @MockitoAnnotations.Mock
        private UsageRefactorService usageRefactorService;

        @MockitoAnnotations.Mock
        private HostRepository hostRepository;

        private static final int NUM_HOSTS = 2;
        private static final int NUM_LBS = 3;
        private static final int FIRST_LB_ID = 123;
        private Calendar firstPollTime = new GregorianCalendar(2013, 4, 13, 11, 1, 0);;

        private List<Host> hostList;
        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, List<LoadBalancerHostUsage>> lbHostMap;

        @Before
        public void standUp() throws Exception {
            hostList = UsagePollerGenerator.generateHosts(NUM_HOSTS);
            snmpMap = UsagePollerGenerator.generateSnmpMap(NUM_HOSTS, NUM_LBS);
            lbHostMap = UsagePollerGenerator.generateLoadBalancerHostUsageMap(NUM_HOSTS,
                            NUM_LBS, 1, firstPollTime, FIRST_LB_ID);
            when(hostService.getAllHosts()).thenReturn(hostList);
            when(hostRepository.getAllHosts()).thenReturn(hostList);
            when(snmpUsageCollector.getCurrentData()).thenReturn(snmpMap);
            when(usageRefactorService.getAllLoadBalancerHostUsages()).thenReturn(lbHostMap);
        }

        @Test
        public void shouldCreateNMergedRecordsForNLoadBalancers(){
            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 1, mergedHostUsages.get(0).getLoadbalancerId());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 2, mergedHostUsages.get(1).getLoadbalancerId());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 3, mergedHostUsages.get(2).getLoadbalancerId());
        }

        @Test
        public void shouldStoreCorrectPollTimeOnMergedRecords(){
            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(firstPollTime.getTime());
            Assert.assertEquals(compTime, mergedHostUsages.get(0).getPollTime());
            Assert.assertEquals(compTime, mergedHostUsages.get(1).getPollTime());
            Assert.assertEquals(compTime, mergedHostUsages.get(2).getPollTime());
        }

        @Test
        public void shouldStoreCorrectBandwidthNoResets(){
            lbHostMap.get(123).get(0).setOutgoingTransfer(1000);
            lbHostMap.get(123).get(1).setOutgoingTransfer(100);
            lbHostMap.get(123).get(0).setIncomingTransfer(1200);
            lbHostMap.get(123).get(1).setIncomingTransfer(200);
            lbHostMap.get(123).get(0).setOutgoingTransferSsl(1000);
            lbHostMap.get(123).get(1).setOutgoingTransferSsl(100);
            lbHostMap.get(123).get(0).setIncomingTransferSsl(1200);
            lbHostMap.get(123).get(1).setIncomingTransferSsl(200);

            lbHostMap.get(124).get(0).setOutgoingTransfer(3000);
            lbHostMap.get(124).get(1).setOutgoingTransfer(1200);
            lbHostMap.get(124).get(0).setIncomingTransfer(1500);
            lbHostMap.get(124).get(1).setIncomingTransfer(250);
            lbHostMap.get(124).get(0).setOutgoingTransferSsl(3000);
            lbHostMap.get(124).get(1).setOutgoingTransferSsl(1200);
            lbHostMap.get(124).get(0).setIncomingTransferSsl(1500);
            lbHostMap.get(124).get(1).setIncomingTransferSsl(250);

            lbHostMap.get(125).get(0).setOutgoingTransfer(1700);
            lbHostMap.get(125).get(1).setOutgoingTransfer(50);
            lbHostMap.get(125).get(0).setIncomingTransfer(2000);
            lbHostMap.get(125).get(1).setIncomingTransfer(300);
            lbHostMap.get(125).get(0).setOutgoingTransferSsl(1700);
            lbHostMap.get(125).get(1).setOutgoingTransferSsl(50);
            lbHostMap.get(125).get(0).setIncomingTransferSsl(2000);
            lbHostMap.get(125).get(1).setIncomingTransferSsl(300);

            snmpMap.get(1).get(123).setBytesOut(1100);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesIn(1300);
            snmpMap.get(2).get(123).setBytesIn(300);
            snmpMap.get(1).get(123).setBytesOutSsl(1200);
            snmpMap.get(2).get(123).setBytesOutSsl(400);
            snmpMap.get(1).get(123).setBytesInSsl(1350);
            snmpMap.get(2).get(123).setBytesInSsl(350);

            snmpMap.get(1).get(124).setBytesOut(4000);
            snmpMap.get(2).get(124).setBytesOut(2000);
            snmpMap.get(1).get(124).setBytesIn(2000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesOutSsl(3500);
            snmpMap.get(2).get(124).setBytesOutSsl(1700);
            snmpMap.get(1).get(124).setBytesInSsl(2200);
            snmpMap.get(2).get(124).setBytesInSsl(800);

            snmpMap.get(1).get(125).setBytesOut(1700);
            snmpMap.get(2).get(125).setBytesOut(51);
            snmpMap.get(1).get(125).setBytesIn(2001);
            snmpMap.get(2).get(125).setBytesIn(301);
            snmpMap.get(1).get(125).setBytesOutSsl(1701);
            snmpMap.get(2).get(125).setBytesOutSsl(51);
            snmpMap.get(1).get(125).setBytesInSsl(2002);
            snmpMap.get(2).get(125).setBytesInSsl(302);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(300, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(200, mergedHostUsage.getIncomingTransfer());
                        Assert.assertEquals(500, mergedHostUsage.getOutgoingTransferSsl());
                        Assert.assertEquals(300, mergedHostUsage.getIncomingTransferSsl());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(1800, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(750, mergedHostUsage.getIncomingTransfer());
                        Assert.assertEquals(1000, mergedHostUsage.getOutgoingTransferSsl());
                        Assert.assertEquals(1250, mergedHostUsage.getIncomingTransferSsl());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(2, mergedHostUsage.getIncomingTransfer());
                        Assert.assertEquals(2, mergedHostUsage.getOutgoingTransferSsl());
                        Assert.assertEquals(4, mergedHostUsage.getIncomingTransferSsl());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }

        @Test
        public void shouldStoreCorrectBandwidthWithResets(){
            lbHostMap.get(123).get(0).setOutgoingTransfer(1000);
            lbHostMap.get(123).get(1).setOutgoingTransfer(100);
            lbHostMap.get(123).get(0).setIncomingTransfer(1200);
            lbHostMap.get(123).get(1).setIncomingTransfer(200);
            lbHostMap.get(123).get(0).setOutgoingTransferSsl(1000);
            lbHostMap.get(123).get(1).setOutgoingTransferSsl(100);
            lbHostMap.get(123).get(0).setIncomingTransferSsl(1200);
            lbHostMap.get(123).get(1).setIncomingTransferSsl(200);

            lbHostMap.get(124).get(0).setOutgoingTransfer(3000);
            lbHostMap.get(124).get(1).setOutgoingTransfer(1200);
            lbHostMap.get(124).get(0).setIncomingTransfer(1500);
            lbHostMap.get(124).get(1).setIncomingTransfer(250);
            lbHostMap.get(124).get(0).setOutgoingTransferSsl(3000);
            lbHostMap.get(124).get(1).setOutgoingTransferSsl(1200);
            lbHostMap.get(124).get(0).setIncomingTransferSsl(1500);
            lbHostMap.get(124).get(1).setIncomingTransferSsl(250);

            lbHostMap.get(125).get(0).setOutgoingTransfer(1700);
            lbHostMap.get(125).get(1).setOutgoingTransfer(50);
            lbHostMap.get(125).get(0).setIncomingTransfer(2000);
            lbHostMap.get(125).get(1).setIncomingTransfer(300);
            lbHostMap.get(125).get(0).setOutgoingTransferSsl(1700);
            lbHostMap.get(125).get(1).setOutgoingTransferSsl(50);
            lbHostMap.get(125).get(0).setIncomingTransferSsl(2000);
            lbHostMap.get(125).get(1).setIncomingTransferSsl(300);

            snmpMap.get(1).get(123).setBytesOut(999);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesIn(1300);
            snmpMap.get(2).get(123).setBytesIn(199);
            snmpMap.get(1).get(123).setBytesOutSsl(1200);
            snmpMap.get(2).get(123).setBytesOutSsl(400);
            snmpMap.get(1).get(123).setBytesInSsl(1350);
            snmpMap.get(2).get(123).setBytesInSsl(350);

            snmpMap.get(1).get(124).setBytesOut(1500);
            snmpMap.get(2).get(124).setBytesOut(2000);
            snmpMap.get(1).get(124).setBytesIn(2000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesOutSsl(2900);
            snmpMap.get(2).get(124).setBytesOutSsl(2500);
            snmpMap.get(1).get(124).setBytesInSsl(2100);
            snmpMap.get(2).get(124).setBytesInSsl(400);

            snmpMap.get(1).get(125).setBytesOut(1700);
            snmpMap.get(2).get(125).setBytesOut(51);
            snmpMap.get(1).get(125).setBytesIn(2001);
            snmpMap.get(2).get(125).setBytesIn(301);
            snmpMap.get(1).get(125).setBytesOutSsl(1699);
            snmpMap.get(2).get(125).setBytesOutSsl(50);
            snmpMap.get(1).get(125).setBytesInSsl(2000);
            snmpMap.get(2).get(125).setBytesInSsl(299);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(0, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(0, mergedHostUsage.getIncomingTransfer());
                        Assert.assertEquals(500, mergedHostUsage.getOutgoingTransferSsl());
                        Assert.assertEquals(300, mergedHostUsage.getIncomingTransferSsl());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(800, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(250, mergedHostUsage.getIncomingTransfer());
                        Assert.assertEquals(1300, mergedHostUsage.getOutgoingTransferSsl());
                        Assert.assertEquals(150, mergedHostUsage.getIncomingTransferSsl());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(2, mergedHostUsage.getIncomingTransfer());
                        Assert.assertEquals(0, mergedHostUsage.getOutgoingTransferSsl());
                        Assert.assertEquals(0, mergedHostUsage.getIncomingTransferSsl());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }

        @Test
        public void shouldStoreCorrectConcurrentConnectionsNoResets(){
            lbHostMap.get(123).get(0).setConcurrentConnections(1000);
            lbHostMap.get(123).get(1).setConcurrentConnections(100);
            lbHostMap.get(123).get(0).setConcurrentConnectionsSsl(1200);
            lbHostMap.get(123).get(1).setConcurrentConnectionsSsl(200);

            lbHostMap.get(124).get(0).setConcurrentConnections(3000);
            lbHostMap.get(124).get(1).setConcurrentConnections(1200);
            lbHostMap.get(124).get(0).setConcurrentConnectionsSsl(1500);
            lbHostMap.get(124).get(1).setConcurrentConnectionsSsl(250);

            lbHostMap.get(125).get(0).setConcurrentConnections(1700);
            lbHostMap.get(125).get(1).setConcurrentConnections(50);
            lbHostMap.get(125).get(0).setConcurrentConnectionsSsl(2000);
            lbHostMap.get(125).get(1).setConcurrentConnectionsSsl(300);

            snmpMap.get(1).get(123).setConcurrentConnections(999);
            snmpMap.get(2).get(123).setConcurrentConnections(300);
            snmpMap.get(1).get(123).setConcurrentConnectionsSsl(1300);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(199);

            snmpMap.get(1).get(124).setConcurrentConnections(1500);
            snmpMap.get(2).get(124).setConcurrentConnections(2000);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(2000);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(500);

            snmpMap.get(1).get(125).setConcurrentConnections(1700);
            snmpMap.get(2).get(125).setConcurrentConnections(51);
            snmpMap.get(1).get(125).setConcurrentConnectionsSsl(2001);
            snmpMap.get(2).get(125).setConcurrentConnectionsSsl(301);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(1299, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(1499, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(3500, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2500, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1751, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2302, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }

        @Test
        public void shouldStoreCorrectConcurrentConnectionsWithResets(){
            lbHostMap.get(123).get(0).setOutgoingTransfer(100);
            lbHostMap.get(123).get(1).setOutgoingTransfer(100);
            lbHostMap.get(123).get(0).setConcurrentConnections(1000);
            lbHostMap.get(123).get(1).setConcurrentConnections(100);
            lbHostMap.get(123).get(0).setConcurrentConnectionsSsl(1200);
            lbHostMap.get(123).get(1).setConcurrentConnectionsSsl(200);

            lbHostMap.get(124).get(0).setOutgoingTransfer(100);
            lbHostMap.get(124).get(0).setConcurrentConnections(3000);
            lbHostMap.get(124).get(1).setConcurrentConnections(1200);
            lbHostMap.get(124).get(0).setConcurrentConnectionsSsl(1500);
            lbHostMap.get(124).get(1).setConcurrentConnectionsSsl(250);

            lbHostMap.get(125).get(0).setConcurrentConnections(1700);
            lbHostMap.get(125).get(1).setConcurrentConnections(50);
            lbHostMap.get(125).get(0).setConcurrentConnectionsSsl(2000);
            lbHostMap.get(125).get(1).setConcurrentConnectionsSsl(300);

            snmpMap.get(1).get(123).setConcurrentConnections(999);
            snmpMap.get(2).get(123).setConcurrentConnections(300);
            snmpMap.get(1).get(123).setConcurrentConnectionsSsl(1300);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(199);

            snmpMap.get(1).get(124).setConcurrentConnections(1500);
            snmpMap.get(2).get(124).setConcurrentConnections(2000);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(2000);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(500);

            snmpMap.get(1).get(125).setConcurrentConnections(1700);
            snmpMap.get(2).get(125).setConcurrentConnections(51);
            snmpMap.get(1).get(125).setConcurrentConnectionsSsl(2001);
            snmpMap.get(2).get(125).setConcurrentConnectionsSsl(301);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(1299, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(1499, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(3500, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2500, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1751, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2302, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenTestingProcessRecordsWithEvents {

        @MockitoAnnotations.Mock
        private HostService hostService;

        @MockitoAnnotations.Mock
        private SnmpUsageCollector snmpUsageCollector;

        @InjectMocks
        private UsagePoller usagePoller = new UsagePollerImpl();

        @MockitoAnnotations.Mock
        private UsageRefactorService usageRefactorService;

        @MockitoAnnotations.Mock
        private HostRepository hostRepository;

        private static final int NUM_HOSTS = 2;
        private static final int NUM_LBS = 3;
        private static final int FIRST_LB_ID = 123;
        private Calendar firstPollTime = new GregorianCalendar(2013, 4, 13, 11, 1, 0);

        private List<Host> hostList;
        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, List<LoadBalancerHostUsage>> lbHostMap;

        @Before
        public void standUp() throws Exception {
            hostList = UsagePollerGenerator.generateHosts(NUM_HOSTS);
            snmpMap = UsagePollerGenerator.generateSnmpMap(NUM_HOSTS, NUM_LBS);
            lbHostMap = UsagePollerGenerator.generateLoadBalancerHostUsageMap(NUM_HOSTS,
                            NUM_LBS, 3, firstPollTime, FIRST_LB_ID);
            lbHostMap.get(123).get(2).setEventType(UsageEvent.SSL_MIXED_ON);
            lbHostMap.get(123).get(3).setEventType(UsageEvent.SSL_MIXED_ON);
            lbHostMap.get(123).get(4).setEventType(UsageEvent.SSL_OFF);
            lbHostMap.get(123).get(5).setEventType(UsageEvent.SSL_OFF);
            lbHostMap.get(124).get(2).setEventType(UsageEvent.SSL_ONLY_ON);
            lbHostMap.get(124).get(3).setEventType(UsageEvent.SSL_ONLY_ON);
            lbHostMap.get(124).get(4).setEventType(UsageEvent.SSL_MIXED_ON);
            lbHostMap.get(124).get(5).setEventType(UsageEvent.SSL_MIXED_ON);
            lbHostMap.get(125).get(2).setEventType(UsageEvent.CREATE_VIRTUAL_IP);
            lbHostMap.get(125).get(3).setEventType(UsageEvent.CREATE_VIRTUAL_IP);
            lbHostMap.get(125).get(4).setEventType(UsageEvent.DELETE_VIRTUAL_IP);
            lbHostMap.get(125).get(5).setEventType(UsageEvent.DELETE_VIRTUAL_IP);
            when(hostService.getAllHosts()).thenReturn(hostList);
            when(hostRepository.getAllHosts()).thenReturn(hostList);
            when(snmpUsageCollector.getCurrentData()).thenReturn(snmpMap);
            when(usageRefactorService.getAllLoadBalancerHostUsages()).thenReturn(lbHostMap);
        }

        @Test
        public void shouldCreateCreateCorrectAmountOfMergedRecordsForNLoadBalancers(){
            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(9, mergedHostUsages.size());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 1, mergedHostUsages.get(0).getLoadbalancerId());
            Assert.assertEquals(UsageEvent.CREATE_VIRTUAL_IP, mergedHostUsages.get(0).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 1, mergedHostUsages.get(1).getLoadbalancerId());
            Assert.assertEquals(UsageEvent.DELETE_VIRTUAL_IP, mergedHostUsages.get(1).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 1, mergedHostUsages.get(2).getLoadbalancerId());
            Assert.assertNull(mergedHostUsages.get(2).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 2, mergedHostUsages.get(3).getLoadbalancerId());
            Assert.assertEquals(UsageEvent.SSL_ONLY_ON, mergedHostUsages.get(3).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 2, mergedHostUsages.get(4).getLoadbalancerId());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedHostUsages.get(4).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 2, mergedHostUsages.get(5).getLoadbalancerId());
            Assert.assertNull(mergedHostUsages.get(5).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 3, mergedHostUsages.get(6).getLoadbalancerId());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedHostUsages.get(6).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 3, mergedHostUsages.get(7).getLoadbalancerId());
            Assert.assertEquals(UsageEvent.SSL_OFF, mergedHostUsages.get(7).getEventType());
            Assert.assertEquals(FIRST_LB_ID + NUM_LBS - 3, mergedHostUsages.get(8).getLoadbalancerId());
            Assert.assertNull(mergedHostUsages.get(8).getEventType());
        }

        @Test
        public void shouldStoreCorrectPollTimeOnMergedRecords(){
            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(firstPollTime.getTime());
            Assert.assertEquals(compTime, mergedHostUsages.get(0).getPollTime());
            Assert.assertEquals(compTime, mergedHostUsages.get(1).getPollTime());
            Assert.assertEquals(compTime, mergedHostUsages.get(2).getPollTime());
        }

        @Test
        public void shouldStoreCorrectBandwidthNoResets(){
            lbHostMap.get(123).get(0).setOutgoingTransfer(1000);
            lbHostMap.get(123).get(1).setOutgoingTransfer(100);
            lbHostMap.get(123).get(0).setIncomingTransfer(1200);
            lbHostMap.get(123).get(1).setIncomingTransfer(200);

            lbHostMap.get(124).get(0).setOutgoingTransfer(3000);
            lbHostMap.get(124).get(1).setOutgoingTransfer(1200);
            lbHostMap.get(124).get(0).setIncomingTransfer(1500);
            lbHostMap.get(124).get(1).setIncomingTransfer(250);

            lbHostMap.get(125).get(0).setOutgoingTransfer(1700);
            lbHostMap.get(125).get(1).setOutgoingTransfer(50);
            lbHostMap.get(125).get(0).setIncomingTransfer(2000);
            lbHostMap.get(125).get(1).setIncomingTransfer(300);

            snmpMap.get(1).get(123).setBytesOut(1100);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesIn(1300);
            snmpMap.get(2).get(123).setBytesIn(300);

            snmpMap.get(1).get(124).setBytesOut(4000);
            snmpMap.get(2).get(124).setBytesOut(2000);
            snmpMap.get(1).get(124).setBytesIn(2000);
            snmpMap.get(2).get(124).setBytesIn(500);

            snmpMap.get(1).get(125).setBytesOut(1700);
            snmpMap.get(2).get(125).setBytesOut(51);
            snmpMap.get(1).get(125).setBytesIn(2001);
            snmpMap.get(2).get(125).setBytesIn(301);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(300, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(200, mergedHostUsage.getIncomingTransfer());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(1800, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(750, mergedHostUsage.getIncomingTransfer());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(2, mergedHostUsage.getIncomingTransfer());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }

        @Test
        public void shouldStoreCorrectBandwidthWithResets(){
            lbHostMap.get(123).get(0).setOutgoingTransfer(1000);
            lbHostMap.get(123).get(1).setOutgoingTransfer(100);
            lbHostMap.get(123).get(0).setIncomingTransfer(1200);
            lbHostMap.get(123).get(1).setIncomingTransfer(200);

            lbHostMap.get(124).get(0).setOutgoingTransfer(3000);
            lbHostMap.get(124).get(1).setOutgoingTransfer(1200);
            lbHostMap.get(124).get(0).setIncomingTransfer(1500);
            lbHostMap.get(124).get(1).setIncomingTransfer(250);

            lbHostMap.get(125).get(0).setOutgoingTransfer(1700);
            lbHostMap.get(125).get(1).setOutgoingTransfer(50);
            lbHostMap.get(125).get(0).setIncomingTransfer(2000);
            lbHostMap.get(125).get(1).setIncomingTransfer(300);

            snmpMap.get(1).get(123).setBytesOut(999);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesIn(1300);
            snmpMap.get(2).get(123).setBytesIn(199);

            snmpMap.get(1).get(124).setBytesOut(1500);
            snmpMap.get(2).get(124).setBytesOut(2000);
            snmpMap.get(1).get(124).setBytesIn(2000);
            snmpMap.get(2).get(124).setBytesIn(500);

            snmpMap.get(1).get(125).setBytesOut(1700);
            snmpMap.get(2).get(125).setBytesOut(51);
            snmpMap.get(1).get(125).setBytesIn(2001);
            snmpMap.get(2).get(125).setBytesIn(301);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(0, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(0, mergedHostUsage.getIncomingTransfer());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(800, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(250, mergedHostUsage.getIncomingTransfer());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1, mergedHostUsage.getOutgoingTransfer());
                        Assert.assertEquals(2, mergedHostUsage.getIncomingTransfer());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }

        @Test
        public void shouldStoreCorrectConcurrentConnectionsNoResets(){
            lbHostMap.get(123).get(0).setConcurrentConnections(1000);
            lbHostMap.get(123).get(1).setConcurrentConnections(100);
            lbHostMap.get(123).get(0).setConcurrentConnectionsSsl(1200);
            lbHostMap.get(123).get(1).setConcurrentConnectionsSsl(200);

            lbHostMap.get(124).get(0).setConcurrentConnections(3000);
            lbHostMap.get(124).get(1).setConcurrentConnections(1200);
            lbHostMap.get(124).get(0).setConcurrentConnectionsSsl(1500);
            lbHostMap.get(124).get(1).setConcurrentConnectionsSsl(250);

            lbHostMap.get(125).get(0).setConcurrentConnections(1700);
            lbHostMap.get(125).get(1).setConcurrentConnections(50);
            lbHostMap.get(125).get(0).setConcurrentConnectionsSsl(2000);
            lbHostMap.get(125).get(1).setConcurrentConnectionsSsl(300);

            snmpMap.get(1).get(123).setConcurrentConnections(999);
            snmpMap.get(2).get(123).setConcurrentConnections(300);
            snmpMap.get(1).get(123).setConcurrentConnectionsSsl(1300);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(199);

            snmpMap.get(1).get(124).setConcurrentConnections(1500);
            snmpMap.get(2).get(124).setConcurrentConnections(2000);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(2000);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(500);

            snmpMap.get(1).get(125).setConcurrentConnections(1700);
            snmpMap.get(2).get(125).setConcurrentConnections(51);
            snmpMap.get(1).get(125).setConcurrentConnectionsSsl(2001);
            snmpMap.get(2).get(125).setConcurrentConnectionsSsl(301);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(1299, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(1499, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(3500, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2500, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1751, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2302, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }

        @Test
        public void shouldStoreCorrectConcurrentConnectionsWithResets(){
            lbHostMap.get(123).get(0).setOutgoingTransfer(100);
            lbHostMap.get(123).get(1).setOutgoingTransfer(100);
            lbHostMap.get(123).get(0).setConcurrentConnections(1000);
            lbHostMap.get(123).get(1).setConcurrentConnections(100);
            lbHostMap.get(123).get(0).setConcurrentConnectionsSsl(1200);
            lbHostMap.get(123).get(1).setConcurrentConnectionsSsl(200);

            lbHostMap.get(124).get(0).setOutgoingTransfer(100);
            lbHostMap.get(124).get(0).setConcurrentConnections(3000);
            lbHostMap.get(124).get(1).setConcurrentConnections(1200);
            lbHostMap.get(124).get(0).setConcurrentConnectionsSsl(1500);
            lbHostMap.get(124).get(1).setConcurrentConnectionsSsl(250);

            lbHostMap.get(125).get(0).setConcurrentConnections(1700);
            lbHostMap.get(125).get(1).setConcurrentConnections(50);
            lbHostMap.get(125).get(0).setConcurrentConnectionsSsl(2000);
            lbHostMap.get(125).get(1).setConcurrentConnectionsSsl(300);

            snmpMap.get(1).get(123).setConcurrentConnections(999);
            snmpMap.get(2).get(123).setConcurrentConnections(300);
            snmpMap.get(1).get(123).setConcurrentConnectionsSsl(1300);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(199);

            snmpMap.get(1).get(124).setConcurrentConnections(1500);
            snmpMap.get(2).get(124).setConcurrentConnections(2000);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(2000);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(500);

            snmpMap.get(1).get(125).setConcurrentConnections(1700);
            snmpMap.get(2).get(125).setConcurrentConnections(51);
            snmpMap.get(1).get(125).setConcurrentConnectionsSsl(2001);
            snmpMap.get(2).get(125).setConcurrentConnectionsSsl(301);

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(3, mergedHostUsages.size());
            for (LoadBalancerMergedHostUsage mergedHostUsage : mergedHostUsages) {
                switch(mergedHostUsage.getLoadbalancerId()) {
                    case FIRST_LB_ID:
                        Assert.assertEquals(1299, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(1499, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 1:
                        Assert.assertEquals(3500, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2500, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    case FIRST_LB_ID + 2:
                        Assert.assertEquals(1751, mergedHostUsage.getConcurrentConnections());
                        Assert.assertEquals(2302, mergedHostUsage.getConcurrentConnectionsSsl());
                        break;
                    default:
                        Assert.assertTrue("There was a load balancer id that was not handled.", false);
                }
            }
        }
    }
}
