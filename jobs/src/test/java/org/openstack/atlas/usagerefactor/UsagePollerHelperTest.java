package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.dbunit.FlatXmlLoader;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class UsagePollerHelperTest {

    public static class WhenTestingUsageMappingHelper {

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

    public static class WhenTestingIsReset {

        private UsagePollerHelper usagePollerHelper;

        @Before
        public void standUp() {
            usagePollerHelper = new UsagePollerHelper();
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

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:context.xml"})
    @TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessExistingEvents {

        @Autowired
        @Qualifier("usageRefactorService")
        private UsageRefactorService usageRefactorService;

        private UsagePollerHelper usagePollerHelper;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;

        @Before
        public void standUp() throws Exception {
            usagePollerHelper = new UsagePollerHelper();
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case1.xml")
        public void shouldNotReturnAnyNewRecordsToInsertWhenEventsDidNotTakePlace() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedUsages = usagePollerHelper.processExistingEvents(lbHostMap);
            Assert.assertEquals(0, mergedUsages.size());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case2.xml")
        public void shouldReturnMergedRecordsWithOneEventNoUsage() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedUsages = usagePollerHelper.processExistingEvents(lbHostMap);

            Assert.assertEquals(2, mergedUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:00", mergedUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:02:00", mergedUsages.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case3.xml")
        public void shouldReturnMergedRecordsWithOneEventWithUsage() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedUsages = usagePollerHelper.processExistingEvents(lbHostMap);

            Assert.assertEquals(2, mergedUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 575L, 775L, 175L, 375L, 7, 15, 2, 3,
                    UsageEvent.CREATE_VIRTUAL_IP, "2013-04-10 20:03:00", mergedUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 475L, 675L, 75L, 275L, 3, 11, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:02:00", mergedUsages.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case4.xml")
        public void shouldAggregateUsageForAllHostsWithManyHosts() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedUsages = usagePollerHelper.processExistingEvents(lbHostMap);

            Assert.assertEquals(2, mergedUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 10L, 6L, 18L, 14L, 12, 24, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:00", mergedUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1L, 0L, 6L, 3L, 8, 20, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:02:00", mergedUsages.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case5.xml")
        public void shouldReturnMergedRecordsWithMultipleEventsWithUsage() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedUsages = usagePollerHelper.processExistingEvents(lbHostMap);

            Assert.assertEquals(4, mergedUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 575L, 775L, 175L, 375L, 7, 15, 2, 0,
                    UsageEvent.CREATE_VIRTUAL_IP, "2013-04-10 20:03:00", mergedUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 7, 15, 1, 0,
                    UsageEvent.DELETE_VIRTUAL_IP, "2013-04-10 20:03:30", mergedUsages.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 475L, 675L, 75L, 275L, 3, 11, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:02:00", mergedUsages.get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 250L, 200L, 125L, 150L, 3, 11, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedUsages.get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case6.xml")
        public void shouldNotRecordUsageForHostThatResets() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedUsages = usagePollerHelper.processExistingEvents(lbHostMap);

            Assert.assertEquals(2, mergedUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 150L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:00", mergedUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 10L, 0L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:02:00", mergedUsages.get(1));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:context.xml"})
    @TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessCurrentUsage {

        @Autowired
        @Qualifier("usageRefactorService")
        private UsageRefactorService usageRefactorService;

        private UsagePollerHelper usagePollerHelper;
        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;
        private int numHosts = 2;
        private int numLBs = 2;

        @Before
        public void standUp() throws Exception {
            usagePollerHelper = new UsagePollerHelper();
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Ignore
        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case1.xml")
        public void shouldCalculateUsageNormallyWhenThereAreNoEvents() throws Exception{
            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);
            Assert.assertEquals(2, result.getMergedUsages().size());
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
            usagePollerHelper = new UsagePollerHelper();
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
            usagePollerHelper = new UsagePollerHelper();
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
}