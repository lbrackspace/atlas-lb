package org.openstack.atlas.usagerefactor;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.bouncycastle.jce.exception.ExtCertificateEncodingException;
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
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRefactorRepository;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerTestHelper;
import org.openstack.atlas.usagerefactor.junit.AssertLoadBalancerMergedHostUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
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
        private HostUsageRefactorRepository hostUsageRefactorRepository;

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
        private static final int NUM_LBS = 2;
        private static final int FIRST_LB_ID = 123;

        private List<Host> hostList;
        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        List<LoadBalancerHostUsage> repoUsages;
        private Map<Integer, List<LoadBalancerHostUsage>> lbHostMap;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            hostList = UsagePollerGenerator.generateHosts(NUM_HOSTS);
            snmpMap = UsagePollerGenerator.generateSnmpMap(NUM_HOSTS, NUM_LBS);
            when(hostService.getAllHosts()).thenReturn(hostList);
            when(hostRepository.getAllHosts()).thenReturn(hostList);
            when(snmpUsageCollector.getCurrentData()).thenReturn(snmpMap);
            repoUsages = hostUsageRefactorRepository.getAllLoadBalancerHostUsageRecords();
            lbHostMap = UsagePollerTestHelper.groupLBHostUsagesByLoadBalancer(repoUsages);
            when(usageRefactorService.getAllLoadBalancerHostUsages()).thenReturn(lbHostMap);
        }

        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/case1.xml")
        public void case1() throws Exception{
            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(2, mergedHostUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, mergedHostUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 0L, 0L, 0L, 0L, 0, 0, 1, 0,
                    null, mergedHostUsages.get(1));
            Calendar compTime = Calendar.getInstance();
            compTime.setTime(lbHostMap.get(FIRST_LB_ID).get(0).getPollTime().getTime());
            Assert.assertTrue(mergedHostUsages.get(0).getPollTime().compareTo(compTime) > 0);
            Assert.assertTrue(mergedHostUsages.get(1).getPollTime().compareTo(compTime) > 0);
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

            List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePoller.processRecords();
            Assert.assertEquals(2, mergedHostUsages.size());
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 124, 5500L, 6600L, 7700L, 8800L, 0, 0, 1, 0,
                    null, mergedHostUsages.get(0));
            AssertLoadBalancerMergedHostUsage.hasValues(1234, 123, 1100L, 2200L, 3300L, 4400L, 0, 0, 1, 0,
                    null, mergedHostUsages.get(1));
        }
    }
}