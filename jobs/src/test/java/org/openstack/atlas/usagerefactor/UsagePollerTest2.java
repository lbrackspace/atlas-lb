package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.text.SimpleDateFormat;
import java.util.*;

/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(Enclosed.class)
public class UsagePollerTest2 {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:context.xml"})
    @TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
    public static class WhenTestingProcessRecordsNoEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;

        
        private final int FIRST_LB_ID = 123;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, List<LoadBalancerHostUsage>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;

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
        public void case1() throws Exception{
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
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

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
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

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
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

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
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

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
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

            //new lb_merged_host_usage records assertions
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
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
    }

    public static class WhenTestingProcessRecordsWithEvents {

        @Autowired
        private UsageRefactorService usageRefactorService;


        private final int FIRST_LB_ID = 123;

        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, List<LoadBalancerHostUsage>> lbHostMap;
        private int numHosts;
        private Calendar pollTime;
        String pollTimeStr;
        private int numLBs;

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

        @Ignore
        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/processrecordswithevents/case1.xml")
        public void case1() throws Exception{
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
            Assert.assertEquals(2, result.getMergedUsages().size());

            //new lb_merged_host_usage records assertions
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTimeStr, result.getMergedUsages().get(1));

            //New lb_host_usage records assertions
        }
    }
}