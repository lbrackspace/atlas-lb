package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.services.impl.UsageRefactorServiceImpl;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.openstack.atlas.util.common.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(Enclosed.class)
public class UsagePollerHelperTest {

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
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessExistingEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;

        @Mock
        private HostRepository hostRepository;
        @InjectMocks
        private UsagePollerHelper usagePollerHelper = new UsagePollerHelper();
        private List<Host> hosts = new ArrayList<Host>();
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;

        @Before
        public void standUp() throws Exception {
            initMocks(this);
            Host h1 = new Host();
            Host h2 = new Host();
            h1.setId(1);
            h2.setId(2);
            hosts.add(h1);
            hosts.add(h2);
            when(hostRepository.getAll()).thenReturn(hosts);
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


        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case7.xml")
        public void case7() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 2, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-08-27 21:56:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 30L, 70L, 110L, 150L, 3, 7, 2, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-08-27 21:57:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case8.xml")
        public void case8() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-08-27 21:56:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processexistingevents/case9.xml")
        public void case9() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-08-27 21:55:58", mergedRecords.get(0));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessRecordsWithEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;
        @Autowired
        private UsagePollerHelper usagePollerHelper;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        private int numHosts;

        @Before
        public void standUp() throws Exception {
            numLBs = 2;
            numHosts = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case1.xml")
        public void case1() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case2.xml")
        public void case2() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case3.xml")
        public void case3() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 700L, 400L, 300L, 300L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 400L, 450L, 200L, 400L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case4.xml")
        public void case4() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 200L, 100L, 200L, 100L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 300L, 50L, 100L, 200L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case5.xml")
        public void case5() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 700L, 400L, 300L, 300L, 28, 16, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 400L, 450L, 200L, 400L, 9, 22, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case6.xml")
        public void case6() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 200L, 100L, 200L, 100L, 28, 16, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 300L, 50L, 100L, 200L, 9, 22, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case7.xml")
        public void case7() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 200L, 400L, 700L, 200L, 28, 16, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 100L, 50L, 50L, 200L, 9, 10, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case8.xml")
        public void case8() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case9.xml")
        public void shouldReturnZeroBandwidthWhenPreviousPollHasNegativeBandwidth() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case10.xml")
        public void shouldReturnZeroBandwidthWhenFirstEventHasNegativeBandwidth() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case11.xml")
        public void shouldReturnZeroBandwidthWhenPreviousPollAndFirstEventHaveNegativeBandwidth() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case12.xml")
        public void shouldReturnZeroBandwidthWhenThePreviousPollAndLastEventHaveNegativeBandwidth() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:04:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case13.xml")
        public void shouldReturnZeroBandwidthWhenOnlyTheFirstEventHasNegativeBandwidth() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 25L, 0L, 50L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:04:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case14.xml")
        public void shouldReturnZeroConcurrentConnectionsWhenConcurrentConnectionsAreNegative() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 30, 70, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:04:00", mergedRecords.get(1));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessCurrentUsage {

        @Autowired
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
            snmpMap = MapUtil.swapKeys(snmpMap);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case1.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasNoEventsZeroUsage() throws Exception{
            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case2.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasNoEventsWithUsage() throws Exception{
            snmpMap.get(123).get(1).setBytesIn(40);
            snmpMap.get(123).get(2).setBytesIn(20);
            snmpMap.get(123).get(1).setBytesInSsl(40);
            snmpMap.get(123).get(2).setBytesInSsl(40);
            snmpMap.get(123).get(1).setBytesOut(60);
            snmpMap.get(123).get(2).setBytesOut(80);
            snmpMap.get(123).get(1).setBytesOutSsl(50);
            snmpMap.get(123).get(2).setBytesOutSsl(60);
            snmpMap.get(123).get(1).setConcurrentConnections(11);
            snmpMap.get(123).get(2).setConcurrentConnections(15);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(20);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(25);

            snmpMap.get(124).get(1).setBytesIn(35);
            snmpMap.get(124).get(2).setBytesIn(45);
            snmpMap.get(124).get(1).setBytesInSsl(40);
            snmpMap.get(124).get(2).setBytesInSsl(50);
            snmpMap.get(124).get(1).setBytesOut(100);
            snmpMap.get(124).get(2).setBytesOut(110);
            snmpMap.get(124).get(1).setBytesOutSsl(70);
            snmpMap.get(124).get(2).setBytesOutSsl(90);
            snmpMap.get(124).get(1).setConcurrentConnections(1);
            snmpMap.get(124).get(2).setConcurrentConnections(5);
            snmpMap.get(124).get(1).setConcurrentConnectionsSsl(0);
            snmpMap.get(124).get(2).setConcurrentConnectionsSsl(5);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 10L, 0L, 100L, 30L, 6, 5, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 30L, 30L, 70L, 20L, 26, 45, 3, 3,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 35L, 40L, 100L, 70L, 1, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 45L, 50L, 110L, 90L, 5, 5, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 40L, 40L, 60L, 50L, 11, 20, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 20L, 40L, 80L, 60L, 15, 25, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case3.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasNoEventsWithUsageWithResets() throws Exception{
            snmpMap.get(123).get(1).setBytesIn(5);
            snmpMap.get(123).get(2).setBytesIn(20);
            snmpMap.get(123).get(1).setBytesInSsl(40);
            snmpMap.get(123).get(2).setBytesInSsl(10);
            snmpMap.get(123).get(1).setBytesOut(60);
            snmpMap.get(123).get(2).setBytesOut(80);
            snmpMap.get(123).get(1).setBytesOutSsl(50);
            snmpMap.get(123).get(2).setBytesOutSsl(49);
            snmpMap.get(123).get(1).setConcurrentConnections(11);
            snmpMap.get(123).get(2).setConcurrentConnections(15);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(20);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(25);

            snmpMap.get(124).get(1).setBytesIn(35);
            snmpMap.get(124).get(2).setBytesIn(35);
            snmpMap.get(124).get(1).setBytesInSsl(40);
            snmpMap.get(124).get(2).setBytesInSsl(50);
            snmpMap.get(124).get(1).setBytesOut(10);
            snmpMap.get(124).get(2).setBytesOut(110);
            snmpMap.get(124).get(1).setBytesOutSsl(70);
            snmpMap.get(124).get(2).setBytesOutSsl(90);
            snmpMap.get(124).get(2).setConcurrentConnections(5);
            snmpMap.get(124).get(1).setConcurrentConnectionsSsl(0);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 30L, 5, 0, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 20L, 40L, 10L, 26, 45, 3, 3,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 35L, 40L, 10L, 70L, 0, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 35L, 50L, 110L, 90L, 5, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5L, 40L, 60L, 50L, 11, 20, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 20L, 10L, 80L, 49L, 15, 25, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case4.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasEventsZeroUsage() throws Exception{
            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 3, 3,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case5.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasWithEventsWithUsage() throws Exception{
            snmpMap.get(123).get(1).setBytesIn(40);
            snmpMap.get(123).get(2).setBytesIn(21);
            snmpMap.get(123).get(1).setBytesInSsl(40);
            snmpMap.get(123).get(2).setBytesInSsl(40);
            snmpMap.get(123).get(1).setBytesOut(60);
            snmpMap.get(123).get(2).setBytesOut(80);
            snmpMap.get(123).get(1).setBytesOutSsl(50);
            snmpMap.get(123).get(2).setBytesOutSsl(60);
            snmpMap.get(123).get(1).setConcurrentConnections(11);
            snmpMap.get(123).get(2).setConcurrentConnections(15);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(20);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(25);

            snmpMap.get(124).get(1).setBytesIn(35);
            snmpMap.get(124).get(2).setBytesIn(45);
            snmpMap.get(124).get(1).setBytesInSsl(41);
            snmpMap.get(124).get(2).setBytesInSsl(51);
            snmpMap.get(124).get(1).setBytesOut(100);
            snmpMap.get(124).get(2).setBytesOut(110);
            snmpMap.get(124).get(1).setBytesOutSsl(70);
            snmpMap.get(124).get(2).setBytesOutSsl(90);
            snmpMap.get(124).get(1).setConcurrentConnections(1);
            snmpMap.get(124).get(2).setConcurrentConnections(5);
            snmpMap.get(124).get(1).setConcurrentConnectionsSsl(0);
            snmpMap.get(124).get(2).setConcurrentConnectionsSsl(5);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 8L, 0L, 98L, 28L, 6, 5, 3, 3,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 29L, 28L, 68L, 18L, 26, 45, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 35L, 41L, 100L, 70L, 1, 0, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 45L, 51L, 110L, 90L, 5, 5, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 40L, 40L, 60L, 50L, 11, 20, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 21L, 40L, 80L, 60L, 15, 25, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case6.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasEventsWithUsageWithResets() throws Exception{
            snmpMap.get(123).get(1).setBytesIn(5);
            snmpMap.get(123).get(2).setBytesIn(20);
            snmpMap.get(123).get(1).setBytesInSsl(40);
            snmpMap.get(123).get(2).setBytesInSsl(10);
            snmpMap.get(123).get(1).setBytesOut(60);
            snmpMap.get(123).get(2).setBytesOut(80);
            snmpMap.get(123).get(1).setBytesOutSsl(50);
            snmpMap.get(123).get(2).setBytesOutSsl(49);
            snmpMap.get(123).get(1).setConcurrentConnections(11);
            snmpMap.get(123).get(2).setConcurrentConnections(15);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(20);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(25);

            snmpMap.get(124).get(1).setBytesIn(35);
            snmpMap.get(124).get(2).setBytesIn(35);
            snmpMap.get(124).get(1).setBytesInSsl(40);
            snmpMap.get(124).get(2).setBytesInSsl(50);
            snmpMap.get(124).get(1).setBytesOut(10);
            snmpMap.get(124).get(2).setBytesOut(110);
            snmpMap.get(124).get(1).setBytesOutSsl(70);
            snmpMap.get(124).get(2).setBytesOutSsl(90);
            snmpMap.get(124).get(2).setConcurrentConnections(5);
            snmpMap.get(124).get(1).setConcurrentConnectionsSsl(0);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 30L, 5, 0, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 20L, 40L, 10L, 26, 45, 3, 3,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 35L, 40L, 10L, 70L, 0, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 35L, 50L, 110L, 90L, 5, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5L, 40L, 60L, 50L, 11, 20, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 20L, 10L, 80L, 49L, 15, 25, 3, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case7.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasWithManyEventsWithUsage() throws Exception{
            snmpMap.get(123).get(1).setBytesIn(40);
            snmpMap.get(123).get(2).setBytesIn(22);
            snmpMap.get(123).get(1).setBytesInSsl(40);
            snmpMap.get(123).get(2).setBytesInSsl(40);
            snmpMap.get(123).get(1).setBytesOut(60);
            snmpMap.get(123).get(2).setBytesOut(80);
            snmpMap.get(123).get(1).setBytesOutSsl(50);
            snmpMap.get(123).get(2).setBytesOutSsl(60);
            snmpMap.get(123).get(1).setConcurrentConnections(11);
            snmpMap.get(123).get(2).setConcurrentConnections(15);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(20);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(25);

            snmpMap.get(124).get(1).setBytesIn(35);
            snmpMap.get(124).get(2).setBytesIn(45);
            snmpMap.get(124).get(1).setBytesInSsl(42);
            snmpMap.get(124).get(2).setBytesInSsl(52);
            snmpMap.get(124).get(1).setBytesOut(100);
            snmpMap.get(124).get(2).setBytesOut(110);
            snmpMap.get(124).get(1).setBytesOutSsl(70);
            snmpMap.get(124).get(2).setBytesOutSsl(90);
            snmpMap.get(124).get(1).setConcurrentConnections(1);
            snmpMap.get(124).get(2).setConcurrentConnections(5);
            snmpMap.get(124).get(1).setConcurrentConnectionsSsl(0);
            snmpMap.get(124).get(2).setConcurrentConnectionsSsl(5);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 6L, 0L, 96L, 26L, 6, 5, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 28L, 26L, 66L, 16L, 26, 45, 2, 3,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 35L, 42L, 100L, 70L, 1, 0, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 45L, 52L, 110L, 90L, 5, 5, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 40L, 40L, 60L, 50L, 11, 20, 2, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 22L, 40L, 80L, 60L, 15, 25, 2, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case8.xml")
        public void shouldReturnCorrectDataWhenCurrentUsageHasNoEventsZeroUsageMissingHostRecord() throws Exception{
            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 3,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 1,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 3, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case9.xml")
        public void shouldReturnCorrectDataWhenExistingUsageHasEventAndPreviousPollSameTimestamp() throws Exception{
            snmpMap.remove(124);
            snmpMap.get(123).get(1).setBytesIn(40);
            snmpMap.get(123).get(2).setBytesIn(22);
            snmpMap.get(123).get(1).setBytesInSsl(40);
            snmpMap.get(123).get(2).setBytesInSsl(40);
            snmpMap.get(123).get(1).setBytesOut(60);
            snmpMap.get(123).get(2).setBytesOut(80);
            snmpMap.get(123).get(1).setBytesOutSsl(90);
            snmpMap.get(123).get(2).setBytesOutSsl(100);
            snmpMap.get(123).get(1).setConcurrentConnections(11);
            snmpMap.get(123).get(2).setConcurrentConnections(15);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(20);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(25);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 32L, 0L, 30L, 40L, 26, 45, 2, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 40L, 40L, 60L, 90L, 11, 20, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 22L, 40L, 80L, 100L, 15, 25, 2, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case10.xml")
        public void shouldReturnCorrectDataWhenCurrentConcurrentConnsIsNegative() throws Exception{
            snmpMap.remove(124);
            snmpMap.get(123).get(1).setBytesIn(100);
            snmpMap.get(123).get(2).setBytesIn(0);
            snmpMap.get(123).get(1).setBytesInSsl(200);
            snmpMap.get(123).get(2).setBytesInSsl(0);
            snmpMap.get(123).get(1).setBytesOut(300);
            snmpMap.get(123).get(2).setBytesOut(0);
            snmpMap.get(123).get(1).setBytesOutSsl(400);
            snmpMap.get(123).get(2).setBytesOutSsl(0);
            snmpMap.get(123).get(1).setConcurrentConnections(-1);
            snmpMap.get(123).get(2).setConcurrentConnections(0);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(-1);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(0);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 50L, 125L, 200L, 275L, 0, 0, 1, 1,
                    null, pollTimeStr, result.getMergedUsages().get(0));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 100L, 200L, 300L, 400L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case10.xml")
        public void shouldReturnCorrectDataWhenCurrentBandwidthIsNegative() throws Exception{
            snmpMap.remove(124);
            snmpMap.get(123).get(1).setBytesIn(-1);
            snmpMap.get(123).get(2).setBytesIn(0);
            snmpMap.get(123).get(1).setBytesInSsl(-1);
            snmpMap.get(123).get(2).setBytesInSsl(0);
            snmpMap.get(123).get(1).setBytesOut(-1);
            snmpMap.get(123).get(2).setBytesOut(0);
            snmpMap.get(123).get(1).setBytesOutSsl(-1);
            snmpMap.get(123).get(2).setBytesOutSsl(0);
            snmpMap.get(123).get(1).setConcurrentConnections(0);
            snmpMap.get(123).get(2).setConcurrentConnections(0);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(0);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(0);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 1,
                    null, pollTimeStr, result.getMergedUsages().get(0));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, -1L, -1L, -1L, -1L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case11.xml")
        public void shouldReturnCorrectDataWhenPreviousBandwidthIsNegative() throws Exception{
            snmpMap.remove(124);
            snmpMap.get(123).get(1).setBytesIn(100);
            snmpMap.get(123).get(2).setBytesIn(0);
            snmpMap.get(123).get(1).setBytesInSsl(200);
            snmpMap.get(123).get(2).setBytesInSsl(0);
            snmpMap.get(123).get(1).setBytesOut(300);
            snmpMap.get(123).get(2).setBytesOut(0);
            snmpMap.get(123).get(1).setBytesOutSsl(400);
            snmpMap.get(123).get(2).setBytesOutSsl(0);
            snmpMap.get(123).get(1).setConcurrentConnections(0);
            snmpMap.get(123).get(2).setConcurrentConnections(0);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(0);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(0);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 150L, 0L, 300L, 0, 0, 1, 1,
                    null, pollTimeStr, result.getMergedUsages().get(0));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 100L, 200L, 300L, 400L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepollerhelper/processcurrentusage/case11.xml")
        public void shouldReturnCorrectDataWhenPreviousAndCurrentBandwidthIsNegative() throws Exception{
            snmpMap.remove(124);
            snmpMap.get(123).get(1).setBytesIn(-1);
            snmpMap.get(123).get(2).setBytesIn(0);
            snmpMap.get(123).get(1).setBytesInSsl(200);
            snmpMap.get(123).get(2).setBytesInSsl(0);
            snmpMap.get(123).get(1).setBytesOut(-1);
            snmpMap.get(123).get(2).setBytesOut(0);
            snmpMap.get(123).get(1).setBytesOutSsl(400);
            snmpMap.get(123).get(2).setBytesOutSsl(0);
            snmpMap.get(123).get(1).setConcurrentConnections(0);
            snmpMap.get(123).get(2).setConcurrentConnections(0);
            snmpMap.get(123).get(1).setConcurrentConnectionsSsl(0);
            snmpMap.get(123).get(2).setConcurrentConnectionsSsl(0);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 150L, 0L, 300L, 0, 0, 1, 1,
                    null, pollTimeStr, result.getMergedUsages().get(0));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, -1L, 200L, -1L, 400L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 1, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessRecordsNoEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        @Autowired
        private UsagePollerHelper usagePollerHelper;

        @Before
        public void standUp() throws Exception {
            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case1.xml")
        public void case1() throws Exception {
            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case2.xml")
        public void case2() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(1000);
            snmpMap.get(2).get(123).setBytesIn(100);
            snmpMap.get(1).get(123).setBytesInSsl(2000);
            snmpMap.get(2).get(123).setBytesInSsl(200);
            snmpMap.get(1).get(123).setBytesOut(3000);
            snmpMap.get(2).get(123).setBytesOut(300);
            snmpMap.get(1).get(123).setBytesOutSsl(4000);
            snmpMap.get(2).get(123).setBytesOutSsl(400);

            snmpMap.get(1).get(124).setBytesIn(5000);
            snmpMap.get(2).get(124).setBytesIn(500);
            snmpMap.get(1).get(124).setBytesInSsl(6000);
            snmpMap.get(2).get(124).setBytesInSsl(600);
            snmpMap.get(1).get(124).setBytesOut(7000);
            snmpMap.get(2).get(124).setBytesOut(700);
            snmpMap.get(1).get(124).setBytesOutSsl(8000);
            snmpMap.get(2).get(124).setBytesOutSsl(800);

            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 5000L, 6000L, 7000L, 8000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 500L, 600L, 700L, 800L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 1000L, 2000L, 3000L, 4000L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 100L, 200L, 300L, 400L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case3.xml")
        public void case3() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(5000);
            snmpMap.get(2).get(123).setBytesIn(5500);
            snmpMap.get(1).get(123).setBytesInSsl(7000);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);

            snmpMap.get(1).get(124).setBytesIn(6000);
            snmpMap.get(2).get(124).setBytesIn(6600);
            snmpMap.get(1).get(124).setBytesInSsl(8000);
            snmpMap.get(2).get(124).setBytesInSsl(8800);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(2200);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);

            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 6000L, 8000L, 2000L, 4000L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 5000L, 7000L, 1000L, 3000L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 6000L, 8000L, 2000L, 4000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6600L, 8800L, 2200L, 4400L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5000L, 7000L, 1000L, 3000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5500L, 7700L, 1100L, 3300L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case4.xml")
        public void case4() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(499);
            snmpMap.get(2).get(123).setBytesIn(4999);
            snmpMap.get(1).get(123).setBytesInSsl(699);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);

            snmpMap.get(1).get(124).setBytesIn(601);
            snmpMap.get(2).get(124).setBytesIn(6001);
            snmpMap.get(1).get(124).setBytesInSsl(10);
            snmpMap.get(2).get(124).setBytesInSsl(1000);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(1999);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);

            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 1L, 0L, 1800L, 0L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 700L, 0L, 300L, 0, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 601L, 10L, 2000L, 4000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6001L, 1000L, 1999L, 4400L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 499L, 699L, 1000L, 3000L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 4999L, 7700L, 1100L, 3300L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case5.xml")
        public void case5() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(5000);
            snmpMap.get(2).get(123).setBytesIn(5500);
            snmpMap.get(1).get(123).setBytesInSsl(7000);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);
            snmpMap.get(1).get(123).setConcurrentConnections(10);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(7);

            snmpMap.get(1).get(124).setBytesIn(6000);
            snmpMap.get(2).get(124).setBytesIn(6600);
            snmpMap.get(1).get(124).setBytesInSsl(8000);
            snmpMap.get(2).get(124).setBytesInSsl(8800);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(2200);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 6000L, 8000L, 2000L, 4000L, 23, 11, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 5000L, 7000L, 1000L, 3000L, 10, 7, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 6000L, 8000L, 2000L, 4000L, 12, 8, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6600L, 8800L, 2200L, 4400L, 11, 3, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 5000L, 7000L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 5500L, 7700L, 1100L, 3300L, 0, 7, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case6.xml")
        public void case6() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(499);
            snmpMap.get(2).get(123).setBytesIn(4999);
            snmpMap.get(1).get(123).setBytesInSsl(699);
            snmpMap.get(2).get(123).setBytesInSsl(7700);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(2).get(123).setBytesOut(1100);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(2).get(123).setBytesOutSsl(3300);
            snmpMap.get(1).get(123).setConcurrentConnections(10);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(7);

            snmpMap.get(1).get(124).setBytesIn(601);
            snmpMap.get(2).get(124).setBytesIn(6001);
            snmpMap.get(1).get(124).setBytesInSsl(10);
            snmpMap.get(2).get(124).setBytesInSsl(1000);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(1999);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 1L, 0L, 1800L, 0L, 23, 11, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 700L, 0L, 300L, 10, 7, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 601L, 10L, 2000L, 4000L, 12, 8, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6001L, 1000L, 1999L, 4400L, 11, 3, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 499L, 699L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 4999L, 7700L, 1100L, 3300L, 0, 7, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordsnoevents/case7.xml")
        public void case7() throws Exception {
            snmpMap.get(1).get(123).setBytesIn(499);
            snmpMap.get(1).get(123).setBytesInSsl(699);
            snmpMap.get(1).get(123).setBytesOut(1000);
            snmpMap.get(1).get(123).setBytesOutSsl(3000);
            snmpMap.get(1).get(123).setConcurrentConnections(10);

            snmpMap.get(1).get(124).setBytesIn(601);
            snmpMap.get(2).get(124).setBytesIn(6001);
            snmpMap.get(1).get(124).setBytesInSsl(10);
            snmpMap.get(2).get(124).setBytesInSsl(1000);
            snmpMap.get(1).get(124).setBytesOut(2000);
            snmpMap.get(2).get(124).setBytesOut(1999);
            snmpMap.get(1).get(124).setBytesOutSsl(4000);
            snmpMap.get(2).get(124).setBytesOutSsl(4400);
            snmpMap.get(1).get(124).setConcurrentConnections(12);
            snmpMap.get(2).get(124).setConcurrentConnections(11);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(8);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(3);

            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 1L, 0L, 1800L, 0L, 23, 11, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 10, 0, 1, 5,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 601L, 10L, 2000L, 4000L, 12, 8, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 6001L, 1000L, 1999L, 4400L, 11, 3, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 499L, 699L, 1000L, 3000L, 10, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 5, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
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
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(200, newMergedRecord.getIncomingTransfer());
        }

        @Test
        public void shouldStoreDifferenceOfIncomingTransferSslToNewMergedRecord() {
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setBytesInSsl(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(200, newMergedRecord.getIncomingTransferSsl());
        }

        @Test
        public void shouldStoreDifferenceOfOutgoingTransferToNewMergedRecord() {
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setBytesOut(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(200, newMergedRecord.getOutgoingTransfer());
        }

        @Test
        public void shouldStoreDifferenceOfOutgoingTransferSslToNewMergedRecord() {
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setBytesOutSsl(1200);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(200, newMergedRecord.getOutgoingTransferSsl());
        }

        @Test
        public void shouldStoreCurrentRecordsConcurrentConnectionsToNewMergedRecord() {
            previousRecord.setConcurrentConnections(10);
            currentRecord.setConcurrentConnections(15);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(15, newMergedRecord.getConcurrentConnections());
        }

        @Test
        public void shouldStoreCurrentRecordsConcurrentConnectionsSslToNewMergedRecord() {
            previousRecord.setConcurrentConnectionsSsl(10);
            currentRecord.setConcurrentConnectionsSsl(15);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(15, newMergedRecord.getConcurrentConnectionsSsl());
        }

        @Test
        public void shouldStoreNoBandwidthIfResetHappenedOnNormalVirtualServer() {
            previousRecord.setIncomingTransfer(1000);
            currentRecord.setBytesIn(999);
            previousRecord.setOutgoingTransfer(1000);
            currentRecord.setBytesOut(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
        }

        @Test
        public void shouldStoreNoBandwidthIfResetHappenedOnSslVirtualServer() {
            previousRecord.setIncomingTransferSsl(1000);
            currentRecord.setBytesInSsl(999);
            previousRecord.setOutgoingTransferSsl(1000);
            currentRecord.setBytesOutSsl(1001);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
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
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
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
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord, currentTime);
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

        @Test
        public void shouldHaveZeroBandwidthWhenMaximumThresholdIsReachedOrExceeded(){
            previousRecord.setOutgoingTransfer(0);
            currentRecord.setOutgoingTransfer(UsagePollerHelper.MAX_BANDWIDTH_BYTES_THRESHHOLD);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(0, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
            Assert.assertEquals(0, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransferSsl());

            previousRecord.setOutgoingTransfer(1000L);
            currentRecord.setOutgoingTransfer(UsagePollerHelper.MAX_BANDWIDTH_BYTES_THRESHHOLD + 1001L);
            previousRecord.setIncomingTransfer(0);
            currentRecord.setIncomingTransfer(100);
            previousRecord.setOutgoingTransferSsl(0L);
            currentRecord.setOutgoingTransferSsl(500L);
            previousRecord.setIncomingTransferSsl(5000L);
            currentRecord.setIncomingTransferSsl(UsagePollerHelper.MAX_BANDWIDTH_BYTES_THRESHHOLD + 5001L);
            usagePollerHelper.calculateUsage(currentRecord, previousRecord, newMergedRecord);
            Assert.assertEquals(100, newMergedRecord.getIncomingTransfer());
            Assert.assertEquals(0, newMergedRecord.getOutgoingTransfer());
            Assert.assertEquals(0, newMergedRecord.getIncomingTransferSsl());
            Assert.assertEquals(500, newMergedRecord.getOutgoingTransferSsl());
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessExistingEventsWithCreateLBEvent {
        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;
        @Mock
        private HostRepository hostRepository;
        @InjectMocks
        private UsagePollerHelper usagePollerHelper = new UsagePollerHelper();
        private List<Host> hosts = new ArrayList<Host>();

        @Before
        public void standUp() throws Exception {
            initMocks(this);
            Host h1 = new Host();
            Host h2 = new Host();
            h1.setId(1);
            h2.setId(2);
            hosts.add(h1);
            hosts.add(h2);
            when(hostRepository.getAll()).thenReturn(hosts);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case1.xml")
        public void case1() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case2.xml")
        public void case2() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case3.xml")
        public void case3() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(3, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(2));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case4.xml")
        public void case4() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(3, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 30L, 0L, 150L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 40L, 110L, 40L, 230L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(2));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithcreatelbevent/case5.xml")
        public void case5() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.CREATE_LOADBALANCER, "2013-06-24 12:30:54", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.DELETE_LOADBALANCER, "2013-06-24 12:31:00", mergedRecords.get(1));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class ProcessingCurrentUsageWhenHostsAreDown {

        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;
        private LoadBalancer loadBalancer = new LoadBalancer();
        private LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage();
        private Usage usage = new Usage();

        @Mock
        private LoadBalancerMergedHostUsageRepository mergedHostUsageRepository;
        @Mock
        private UsageRepository usageRepository;
        @Mock
        private LoadBalancerService loadBalancerService;
        @Mock
        private VirtualIpRepository virtualIpRepository;
        @Mock
        private LoadBalancerRepository loadBalancerRepository;
        @Mock
        private HostRepository hostRepository;

        @InjectMocks
        private UsagePollerHelper usagePollerHelper = new UsagePollerHelper();


        @Before
        public void standUp() throws Exception {
            initMocks(this);
            loadBalancerMergedHostUsage.setTagsBitmask(0);
            loadBalancerMergedHostUsage.setNumVips(1);
            loadBalancerMergedHostUsage.setAccountId(1234);
            when(mergedHostUsageRepository.getMostRecentRecordForLoadBalancer(anyInt())).thenReturn(loadBalancerMergedHostUsage);
            usage.setTags(0);
            usage.setNumVips(1);
            usage.setAccountId(1234);
            when(usageRepository.getMostRecentUsageForLoadBalancer(anyInt())).thenReturn(usage);
            loadBalancer.setAccountId(1234);
            loadBalancer.setId(123);
            when(loadBalancerService.get(anyInt())).thenReturn(loadBalancer);
            when(loadBalancerService.getCurrentBitTags(anyInt())).thenReturn(new BitTags());
            when(virtualIpRepository.getNumIpv4VipsForLoadBalancer(loadBalancer)).thenReturn(1L);

            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/case1.xml")
        public void case1() throws Exception {
            snmpMap.put(2, new HashMap<Integer, SnmpUsage>());
            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result = usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/case2.xml")
        public void case2() throws Exception {
            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/case3.xml")
        public void case3() throws Exception {
            snmpMap = MapUtil.swapKeys(snmpMap);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
            Assert.assertEquals(4, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 124, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(2));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/case4.xml")
        public void case4() throws Exception {
            snmpMap = MapUtil.swapKeys(snmpMap);
            snmpMap.remove(124);

            UsageProcessorResult result =  usagePollerHelper.processCurrentUsage(lbHostMap, snmpMap, pollTime);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));

            //New lb_host_usage records assertions
            Assert.assertEquals(2, result.getLbHostUsages().size());
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 1, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(0));
            AssertLoadBalancerHostUsage.hasValues(1234, 123, 2, 0L, 0L, 0L, 0L, 0, 0, 1, 0, null, pollTimeStr,
                    result.getLbHostUsages().get(1));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class ProcessingExistingUsageWhenHostsAreDown {
        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;
        @Mock
        private HostRepository hostRepository;
        @InjectMocks
        private UsagePollerHelper usagePollerHelper = new UsagePollerHelper();
        private List<Host> hosts = new ArrayList<Host>();

        @Before
        public void standUp() throws Exception {
            initMocks(this);
            Host h1 = new Host();
            Host h2 = new Host();
            h1.setId(1);
            h2.setId(2);
            hosts.add(h1);
            hosts.add(h2);
            when(hostRepository.getAll()).thenReturn(hosts);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/onevents/case1.xml")
        public void case1() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 1,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:04:01", mergedRecords.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 1,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/onevents/case2.xml")
        public void case2() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(4, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 1,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:03:01", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:04:01", mergedRecords.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 200L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(2));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 300L, 0L, 0, 0, 1, 1,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(3));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/onevents/case3.xml")
        public void case3() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 1,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(1));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/whenhostsaredown/onevents/case4.xml")
        public void case4() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 200L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 250L, 0L, 0, 0, 1, 1,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(1));
        }

    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenTestingProcessExistingEventsWithUnsuspendEvent {
        @Autowired
        private UsageRefactorService usageRefactorService;

        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;
        @Mock
        private HostRepository hostRepository;
        @InjectMocks
        private UsagePollerHelper usagePollerHelper = new UsagePollerHelper();
        private List<Host> hosts = new ArrayList<Host>();

        @Before
        public void standUp() throws Exception {
            initMocks(this);
            Host h1 = new Host();
            Host h2 = new Host();
            h1.setId(1);
            h2.setId(2);
            hosts.add(h1);
            hosts.add(h2);
            when(hostRepository.getAll()).thenReturn(hosts);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithunsuspendevent/case1.xml")
        public void case1() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.UNSUSPEND_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithunsuspendevent/case2.xml")
        public void case2() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(1, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.UNSUSPEND_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithunsuspendevent/case3.xml")
        public void case3() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(3, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.UNSUSPEND_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(2));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithunsuspendevent/case4.xml")
        public void case4() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(3, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.UNSUSPEND_LOADBALANCER, "2013-04-10 20:02:00", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 30L, 0L, 150L, 0L, 0, 0, 1, 5,
                    UsageEvent.SSL_MIXED_ON, "2013-04-10 20:03:00", mergedRecords.get(1));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 40L, 110L, 40L, 230L, 0, 0, 1, 3,
                    UsageEvent.SSL_ONLY_ON, "2013-04-10 20:04:00", mergedRecords.get(2));
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithunsuspendevent/case5.xml")
        public void case5() throws Exception {

            List<LoadBalancerMergedHostUsage> mergedRecords = usagePollerHelper.processExistingEvents(lbHostMap);

            //new lb_merged_host_usage records assertions
            Assert.assertEquals(2, mergedRecords.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.UNSUSPEND_LOADBALANCER, "2013-06-24 12:30:54", mergedRecords.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    UsageEvent.DELETE_LOADBALANCER, "2013-06-24 12:31:00", mergedRecords.get(1));
        }
    }

}