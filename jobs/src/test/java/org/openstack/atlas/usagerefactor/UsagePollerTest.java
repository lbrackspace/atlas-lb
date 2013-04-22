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

    public static class WhenTestingUsageMappingHelper {
        private UsagePoller usagePoller;

        @Before
        public void standUp() {

        }

        @Test
        public void shouldTransformUsagesGroupedByHostsToGroupedByLoadBalancers() {
            SnmpUsage host1lb1 = new SnmpUsage();
            host1lb1.setLoadbalancerId(1);
            host1lb1.setHostId(1);
            SnmpUsage host1lb2 = new SnmpUsage();
            host1lb2.setLoadbalancerId(2);
            host1lb2.setHostId(1);
            SnmpUsage host1lb3 = new SnmpUsage();
            host1lb3.setLoadbalancerId(3);
            host1lb3.setHostId(1);
            SnmpUsage host2lb1 = new SnmpUsage();
            host2lb1.setLoadbalancerId(1);
            host2lb1.setHostId(2);
            SnmpUsage host2lb2 = new SnmpUsage();
            host2lb2.setLoadbalancerId(2);
            host2lb2.setHostId(2);
            SnmpUsage host2lb3 = new SnmpUsage();
            host2lb3.setLoadbalancerId(3);
            host2lb3.setHostId(2);
            SnmpUsage host3lb1 = new SnmpUsage();
            host3lb1.setLoadbalancerId(1);
            host3lb1.setHostId(3);
            SnmpUsage host3lb2 = new SnmpUsage();
            host3lb2.setLoadbalancerId(2);
            host3lb2.setHostId(3);
            SnmpUsage host3lb3 = new SnmpUsage();
            host3lb3.setLoadbalancerId(3);
            host3lb3.setHostId(3);
            Map<Integer, Map<Integer, SnmpUsage>> groupedByHosts = new HashMap<Integer, Map<Integer, SnmpUsage>>();
            Map<Integer, SnmpUsage> host1Map = new HashMap<Integer, SnmpUsage>();
            host1Map.put(1, host1lb1);
            host1Map.put(2, host1lb2);
            host1Map.put(3, host1lb3);
            groupedByHosts.put(1, host1Map);
            Map<Integer, SnmpUsage> host2Map = new HashMap<Integer, SnmpUsage>();
            host2Map.put(1, host2lb1);
            host2Map.put(2, host2lb2);
            host2Map.put(3, host2lb3);
            groupedByHosts.put(2, host2Map);
            Map<Integer, SnmpUsage> host3Map = new HashMap<Integer, SnmpUsage>();
            host3Map.put(1, host3lb1);
            host3Map.put(2, host3lb2);
            host3Map.put(3, host3lb3);
            groupedByHosts.put(3, host3Map);
            Map<Integer, Map<Integer, SnmpUsage>> lbMap = UsageMappingHelper.swapKeyGrouping(groupedByHosts);
            Assert.assertEquals(host1lb1.getHostId(), lbMap.get(1).get(1).getHostId());
            Assert.assertEquals(host1lb1.getLoadbalancerId(), lbMap.get(1).get(1).getLoadbalancerId());
            Assert.assertEquals(host2lb1.getHostId(), lbMap.get(1).get(2).getHostId());
            Assert.assertEquals(host2lb1.getLoadbalancerId(), lbMap.get(1).get(2).getLoadbalancerId());
            Assert.assertEquals(host3lb1.getHostId(), lbMap.get(1).get(3).getHostId());
            Assert.assertEquals(host3lb1.getLoadbalancerId(), lbMap.get(1).get(3).getLoadbalancerId());

            Assert.assertEquals(host1lb2.getHostId(), lbMap.get(2).get(1).getHostId());
            Assert.assertEquals(host1lb2.getLoadbalancerId(), lbMap.get(2).get(1).getLoadbalancerId());
            Assert.assertEquals(host2lb2.getHostId(), lbMap.get(2).get(2).getHostId());
            Assert.assertEquals(host2lb2.getLoadbalancerId(), lbMap.get(2).get(2).getLoadbalancerId());
            Assert.assertEquals(host3lb2.getHostId(), lbMap.get(2).get(3).getHostId());
            Assert.assertEquals(host3lb2.getLoadbalancerId(), lbMap.get(2).get(3).getLoadbalancerId());

            Assert.assertEquals(host1lb3.getHostId(), lbMap.get(3).get(1).getHostId());
            Assert.assertEquals(host1lb3.getLoadbalancerId(), lbMap.get(3).get(1).getLoadbalancerId());
            Assert.assertEquals(host2lb3.getHostId(), lbMap.get(3).get(2).getHostId());
            Assert.assertEquals(host2lb3.getLoadbalancerId(), lbMap.get(3).get(2).getLoadbalancerId());
            Assert.assertEquals(host3lb3.getHostId(), lbMap.get(3).get(3).getHostId());
            Assert.assertEquals(host3lb3.getLoadbalancerId(), lbMap.get(3).get(3).getLoadbalancerId());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenTestingProcessExistingEvents {

        int accountId = 5806065;
        int lbId1 = 1234;
        int lbId2 = 4321;
        long defOutgoing = 1100;
        long defIncoming = 245;
        long defOutgoingSsl = 500;
        long defIncomingSsl = 700;
        int defConns = 10;
        int defConnsSsl = 20;
        int defVips = 1;
        int defTags = 0;
        Calendar firstPollTime;
        UsageEvent defaultEvent = null;
        long bandwidthIncrease = 1200;
        long bandwidthDecrease = 1;
        int ccIncrease = 12;
        int numHosts = 2;

        List<LoadBalancerHostUsage> existingRecordsLB1;
        List<LoadBalancerHostUsage> existingRecordsLB2;
        Map <Integer, List<LoadBalancerHostUsage>> existingRecordsMap;
        List<LoadBalancerMergedHostUsage> mergedUsages;
        UsagePollerHelper usagePollerHelper;

        HostService hostService;

        @Before
        public void standUp(){
            mock(HostService.class);
            firstPollTime = new GregorianCalendar(2013, 1, 1, 1, 1, 1);
            existingRecordsLB1 = new ArrayList<LoadBalancerHostUsage>();
            existingRecordsLB2 = new ArrayList<LoadBalancerHostUsage>();
            existingRecordsMap = new HashMap<Integer, List<LoadBalancerHostUsage>>();
            usagePollerHelper = new UsagePollerHelper(numHosts);
        }

        //TODO: Test for when there are no existing records?
        //TODO: Test for when hosts count varies?

        @Test
        public void shouldNotReturnAnyNewRecordsToInsertWhenEventsDidNotTakePlace(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(0, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
        }

        @Test
        public void shouldReturnMergedRecordsWithOneEventNoUsage(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns*2, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl*2, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());
        }

        @Test
        public void shouldReturnMergedRecordsWithOneEventWithUsage(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());
        }

        @Test
        public void shouldAggregateUsageForAllHosts(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease, defIncomingSsl + bandwidthIncrease,
                    defConns, defConnsSsl, defVips, defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(bandwidthIncrease * 2, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease * 2, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(bandwidthIncrease * 2, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease * 2, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());
        }

        @Test
        public void shouldAggregateUsageForAllHostsWithManyHosts(){
            usagePollerHelper.setNumHosts(8);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 3, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 4, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 5, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 6, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 7, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 8, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 3, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 4, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 5, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 6, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 7, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 8, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(8, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(bandwidthIncrease * 8, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease * 8, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(bandwidthIncrease * 8, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease * 8, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals((defConns + ccIncrease) * 4 + defConns * 4, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals((defConnsSsl + ccIncrease) * 4 + defConnsSsl * 4, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());
        }

        @Test
        public void shouldReturnMergedRecordsWithMultipleEventsWithUsage(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar secondPollTime = Calendar.getInstance();
            secondPollTime.setTime(firstPollTime.getTime());
            secondPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, secondPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, secondPollTime,
                    UsageEvent.SSL_MIXED_ON));
            Calendar thirdPollTime = Calendar.getInstance();
            thirdPollTime.setTime(firstPollTime.getTime());
            thirdPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing + bandwidthIncrease * 2,
                    defIncoming + bandwidthIncrease * 2, defOutgoingSsl + bandwidthIncrease * 2,
                    defIncomingSsl + bandwidthIncrease * 2, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, thirdPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, thirdPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(2, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(secondPollTime, mergedUsages.get(0).getPollTime());

            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(1).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(1).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(defConns * 2 + ccIncrease, mergedUsages.get(1).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl * 2 + ccIncrease, mergedUsages.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(1).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(1).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(1).getTagsBitmask());
            Assert.assertEquals(thirdPollTime, mergedUsages.get(1).getPollTime());
        }

        @Test
        public void shouldNotRecordUsageForHostThatResets(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing - bandwidthDecrease,
                    defIncoming, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());
        }

        @Test
        public void shouldRecordUsageForHostThatDidNotResetWhileAnotherDidReset(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing - bandwidthDecrease,
                    defIncoming, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl,
                    defVips, defTags, nextPollTime, UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, mergedUsages.size());
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(bandwidthIncrease, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl * 2 + ccIncrease, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());
        }

        @Test
        public void shouldPreserveMostRecentSetOfRecords(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 5);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing + bandwidthIncrease,
                    defIncoming + bandwidthIncrease, defOutgoingSsl + bandwidthIncrease,
                    defIncomingSsl + bandwidthIncrease, defConns + ccIncrease, defConnsSsl + ccIncrease, defVips,
                    defTags, nextPollTime, UsageEvent.SSL_OFF));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_OFF));
            existingRecordsMap.put(lbId1, existingRecordsLB1);
            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(1, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(UsageEvent.SSL_OFF, existingRecordsMap.get(lbId1).get(0).getEventType());
            Assert.assertEquals(UsageEvent.SSL_OFF, existingRecordsMap.get(lbId1).get(1).getEventType());
            Assert.assertEquals(nextPollTime, existingRecordsMap.get(lbId1).get(0).getPollTime());
            Assert.assertEquals(nextPollTime, existingRecordsMap.get(lbId1).get(1).getPollTime());
            Assert.assertEquals(defOutgoing + bandwidthIncrease,
                                existingRecordsMap.get(lbId1).get(0).getOutgoingTransfer());
            Assert.assertEquals(defIncoming + bandwidthIncrease,
                                existingRecordsMap.get(lbId1).get(0).getIncomingTransfer());
            Assert.assertEquals(defOutgoingSsl + bandwidthIncrease,
                                existingRecordsMap.get(lbId1).get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defIncomingSsl + bandwidthIncrease,
                                existingRecordsMap.get(lbId1).get(0).getIncomingTransferSsl());
            Assert.assertEquals(defConns + ccIncrease, existingRecordsMap.get(lbId1).get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl + ccIncrease,
                                existingRecordsMap.get(lbId1).get(0).getConcurrentConnectionsSsl());

            Assert.assertEquals(defOutgoing, existingRecordsMap.get(lbId1).get(1).getOutgoingTransfer());
            Assert.assertEquals(defIncoming, existingRecordsMap.get(lbId1).get(1).getIncomingTransfer());
            Assert.assertEquals(defOutgoingSsl, existingRecordsMap.get(lbId1).get(1).getOutgoingTransferSsl());
            Assert.assertEquals(defIncomingSsl, existingRecordsMap.get(lbId1).get(1).getIncomingTransferSsl());
            Assert.assertEquals(defConns, existingRecordsMap.get(lbId1).get(1).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl, existingRecordsMap.get(lbId1).get(1).getConcurrentConnectionsSsl());
        }

        @Test
        public void shouldReturnTwoMergedRecordsForDifferentLoadBalancers(){
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime = Calendar.getInstance();
            nextPollTime.setTime(firstPollTime.getTime());
            nextPollTime.add(Calendar.MINUTE, 2);
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsLB1.add(new LoadBalancerHostUsage(accountId, lbId1, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime,
                    UsageEvent.SSL_MIXED_ON));
            existingRecordsMap.put(lbId1, existingRecordsLB1);

            existingRecordsLB2.add(new LoadBalancerHostUsage(accountId, lbId2, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            existingRecordsLB2.add(new LoadBalancerHostUsage(accountId, lbId2, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, firstPollTime,
                    defaultEvent));
            Calendar nextPollTime2 = Calendar.getInstance();
            nextPollTime2.setTime(firstPollTime.getTime());
            nextPollTime2.add(Calendar.MINUTE, 3);
            existingRecordsLB2.add(new LoadBalancerHostUsage(accountId, lbId2, 1, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime2,
                    UsageEvent.SSL_OFF));
            existingRecordsLB2.add(new LoadBalancerHostUsage(accountId, lbId2, 2, defOutgoing, defIncoming,
                    defOutgoingSsl, defIncomingSsl, defConns, defConnsSsl, defVips, defTags, nextPollTime2,
                    UsageEvent.SSL_OFF));
            existingRecordsMap.put(lbId2, existingRecordsLB2);

            mergedUsages = usagePollerHelper.processExistingEvents(existingRecordsMap);
            Assert.assertEquals(2, mergedUsages.size());
            Assert.assertEquals(2, existingRecordsMap.size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId1).size());
            Assert.assertEquals(2, existingRecordsMap.get(lbId2).size());

            Assert.assertEquals(accountId, mergedUsages.get(0).getAccountId());
            Assert.assertEquals(lbId1, mergedUsages.get(0).getLoadbalancerId());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransfer());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(0, mergedUsages.get(0).getIncomingTransfer());
            Assert.assertEquals(0, mergedUsages.get(0).getOutgoingTransferSsl());
            Assert.assertEquals(defConns*2, mergedUsages.get(0).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl*2, mergedUsages.get(0).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_MIXED_ON, mergedUsages.get(0).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(0).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(0).getTagsBitmask());
            Assert.assertEquals(nextPollTime, mergedUsages.get(0).getPollTime());

            Assert.assertEquals(accountId, mergedUsages.get(1).getAccountId());
            Assert.assertEquals(lbId2, mergedUsages.get(1).getLoadbalancerId());
            Assert.assertEquals(0, mergedUsages.get(1).getOutgoingTransfer());
            Assert.assertEquals(0, mergedUsages.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(0, mergedUsages.get(1).getIncomingTransfer());
            Assert.assertEquals(0, mergedUsages.get(1).getOutgoingTransferSsl());
            Assert.assertEquals(defConns*2, mergedUsages.get(1).getConcurrentConnections());
            Assert.assertEquals(defConnsSsl*2, mergedUsages.get(1).getConcurrentConnectionsSsl());
            Assert.assertEquals(UsageEvent.SSL_OFF, mergedUsages.get(1).getEventType());
            Assert.assertEquals(defVips, mergedUsages.get(1).getNumVips());
            Assert.assertEquals(defTags, mergedUsages.get(1).getTagsBitmask());
            Assert.assertEquals(nextPollTime2, mergedUsages.get(1).getPollTime());
        }
    }

    public static class WhenTestingIsReset {

        private UsagePollerHelper usagePollerHelper;

        @Before
        public void standUp() {
            usagePollerHelper = new UsagePollerHelper(2);
        }

        @Test
        public void shouldBeResetIfCurrentBandwidthIsLessThanPrevious(){
            long currentBandwidth = 1000;
            long previousBandwidth = 1001;
            Assert.assertTrue(usagePollerHelper.isReset(currentBandwidth, previousBandwidth));
        }

        @Test
        public void shouldNotBeResetIfCurrentBandwidthIsEqualToPrevious(){
            long currentBandwidth = 1000;
            long previousBandwidth = 1000;
            Assert.assertFalse(usagePollerHelper.isReset(currentBandwidth, previousBandwidth));
        }

        @Test
        public void shouldNotBeResetIfCurrentBandwidthIsGreaterThanPrevious(){
            long currentBandwidth = 1000;
            long previousBandwidth = 999;
            Assert.assertFalse(usagePollerHelper.isReset(currentBandwidth, previousBandwidth));
        }

    }
    public static class WhenTestingCalculateCurrentUsage {

        private UsagePollerHelper usagePollerHelper;

        private SnmpUsage currentRecord;
        private LoadBalancerHostUsage previousRecord;
        private LoadBalancerMergedHostUsage newMergedRecord;
        private Calendar previousTime;
        private Calendar currentTime;
        @Before
        public void standUp() {
            usagePollerHelper = new UsagePollerHelper(2);
            previousTime = new GregorianCalendar(2013, 4, 10, 11, 1, 0);
            currentTime = new GregorianCalendar(2013, 4, 10, 11, 4, 0);
            currentRecord = new SnmpUsage();
            currentRecord.setBytesIn(0);
            currentRecord.setBytesInSsl(0);
            currentRecord.setBytesOut(0);
            currentRecord.setBytesOutSsl(0);
            currentRecord.setConcurrentConnections(0);
            currentRecord.setConcurrentConnectionsSsl(0);
            currentRecord.setHostId(1);
            currentRecord.setLoadbalancerId(111);
            previousRecord = new LoadBalancerHostUsage(111, 111, 1, 0, 0, 0, 0, 0, 0, 1, 0, previousTime, null);
            newMergedRecord = new LoadBalancerMergedHostUsage(111, 111, 0, 0, 0, 0, 0, 0, 1, 0, currentTime, null);
        }

        @Test
        public void shouldStoreDifferenceOfIncomingTransferToNewMergedRecord(){
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setBytesIn(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getIncomingTransfer());
        }

        @Test
        public void shouldStoreDifferenceOfIncomingTransferSslToNewMergedRecord() {
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setBytesInSsl(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getIncomingTransferSsl());
        }

        @Test
        public void shouldStoreDifferenceOfOutgoingTransferToNewMergedRecord() {
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setBytesOut(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getOutgoingTransfer());
        }

        @Test
        public void shouldStoreDifferenceOfOutgoingTransferSslToNewMergedRecord() {
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setBytesOutSsl(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStoreCurrentRecordsConcurrentConnectionsToNewMergedRecord() {
            previousRecord.setConcurrentConnections(10);
            currentRecord.setConcurrentConnections(15);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(15, newMergedRecord.getConcurrentConnections());
        }

        @Test
        public void shouldStoreCurrentRecordsConcurrentConnectionsSslToNewMergedRecord() {
            previousRecord.setConcurrentConnectionsSsl(10);
            currentRecord.setConcurrentConnectionsSsl(15);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(15, newMergedRecord.getConcurrentConnectionsSsl());
        }

        @Test
        public void shouldStoreNoBandwidthIfResetHappenedOnNormalVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setBytesIn(999);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setBytesOut(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
        }

        @Test
        public void shouldStoreNoBandwidthIfResetHappenedOnSslVirtualServer() {
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setBytesInSsl(999);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setBytesOutSsl(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStillStoreNormalBandwidthIfResetHappenedOnSslVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setBytesIn(1050);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setBytesOut(1100);
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setBytesInSsl(999);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setBytesOutSsl(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(50, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(100, newMergedRecord.getOutgoingTransfer());
            Assert.assertEquals(0, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStillStoreSslBandwidthIfResetHappenedOnNormalVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setBytesIn(999);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setBytesOut(1100);
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setBytesInSsl(1050);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setBytesOutSsl(1100);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
            Assert.assertEquals(50, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(100, newMergedRecord.getOutgoingTransferSsl());
        }
    }

    public static class WhenTestingCalculateExistingUsage {

        private UsagePollerHelper usagePollerHelper;

        private LoadBalancerHostUsage currentRecord;
        private LoadBalancerHostUsage previousRecord;
        private LoadBalancerMergedHostUsage newMergedRecord;
        private Calendar previousTime;
        private Calendar currentTime;

        @Before
        public void standUp() {
            usagePollerHelper = new UsagePollerHelper(2);
            previousTime = new GregorianCalendar(2013, 4, 10, 11, 1, 0);
            currentTime = new GregorianCalendar(2013, 4, 10, 11, 4, 0);
            currentRecord = new LoadBalancerHostUsage(111, 111, 1, 0, 0, 0, 0, 0, 0, 1, 0, currentTime, null);
            previousRecord = new LoadBalancerHostUsage(111, 111, 1, 0, 0, 0, 0, 0, 0, 1, 0, previousTime, null);
            newMergedRecord = new LoadBalancerMergedHostUsage(111, 111, 0, 0, 0, 0, 0, 0, 1, 0, currentTime, null);
        }

        @Test
        public void shouldStoreDifferenceOfIncomingTransferToNewMergedRecord(){
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setIncomingTransfer(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getIncomingTransfer());
        }

        @Test
        public void shouldStoreDifferenceOfIncomingTransferSslToNewMergedRecord() {
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setIncomingTransferSsl(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getIncomingTransferSsl());
        }

        @Test
        public void shouldStoreDifferenceOfOutgoingTransferToNewMergedRecord() {
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setOutgoingTransfer(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getOutgoingTransfer());
        }

        @Test
        public void shouldStoreDifferenceOfOutgoingTransferSslToNewMergedRecord() {
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setOutgoingTransferSsl(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(200, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStoreCurrentRecordsConcurrentConnectionsToNewMergedRecord() {
            previousRecord.setConcurrentConnections(10);
            currentRecord.setConcurrentConnections(15);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(15, newMergedRecord.getConcurrentConnections());
        }

        @Test
        public void shouldStoreCurrentRecordsConcurrentConnectionsSslToNewMergedRecord() {
            previousRecord.setConcurrentConnectionsSsl(10);
            currentRecord.setConcurrentConnectionsSsl(15);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(15, newMergedRecord.getConcurrentConnectionsSsl());
        }

        @Test
        public void shouldStoreNoBandwidthIfResetHappenedOnNormalVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setIncomingTransfer(999);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setOutgoingTransfer(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
        }

        @Test
        public void shouldStoreNoBandwidthIfResetHappenedOnSslVirtualServer() {
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setIncomingTransferSsl(999);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setOutgoingTransferSsl(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStillStoreNormalBandwidthIfResetHappenedOnSslVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setIncomingTransfer(1050);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setOutgoingTransfer(1100);
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setIncomingTransferSsl(999);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setOutgoingTransferSsl(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(50, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(100, newMergedRecord.getOutgoingTransfer());
            Assert.assertEquals(0, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStillStoreSslBandwidthIfResetHappenedOnNormalVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setIncomingTransfer(999);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setOutgoingTransfer(1100);
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setIncomingTransferSsl(1050);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setOutgoingTransferSsl(1100);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
            Assert.assertEquals(50, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(100, newMergedRecord.getOutgoingTransferSsl());
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
