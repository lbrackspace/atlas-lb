package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerTestHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        private int numLBs;

        @Before
        public void standUp() throws Exception {
            numHosts = 2;
            numLBs = 2;
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/case1.xml")
        public void case1() throws Exception{
            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTime, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, pollTime, result.getMergedUsages().get(1));
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(lbHostMap.get(FIRST_LB_ID).get(0).getPollTime().getTime());
            Assert.assertTrue(result.getMergedUsages().get(0).getPollTime().compareTo(compTime) > 0);
            Assert.assertTrue(result.getMergedUsages().get(1).getPollTime().compareTo(compTime) > 0);
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/case2.xml")
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

            UsageProcessorResult result = UsageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime, numHosts);
            Assert.assertEquals(2, result.getMergedUsages().size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, pollTime, result.getMergedUsages().get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, pollTime, result.getMergedUsages().get(1));
        }
    }
}