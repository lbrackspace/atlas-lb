package org.openstack.atlas.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;

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
        private UsagePollerImpl usagePoller;
        private HostService hostService;
        private StingrayUsageClient client;
        private Map<Integer, SnmpUsage>map;
        private List<Host> hosts;

        @Before
        public void standUp() throws Exception {
            usagePoller = new UsagePollerImpl();
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
            usagePoller.setHostService(hostService);
        }

        @Test
        public void getCurrentDataTest() throws Exception {
            Assert.assertNotNull(usagePoller.getCurrentData());
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
}
