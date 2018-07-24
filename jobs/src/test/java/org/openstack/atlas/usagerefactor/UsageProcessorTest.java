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
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.dbunit.FlatXmlLoader;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.generator.UsagePollerGenerator;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/*
    To see what each case is testing please refer to their respective xml
    file for more information.
 */
@RunWith(Enclosed.class)
public class UsageProcessorTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    public static class WhenTestingProcessExistingEvents {

        @Mock
        private UsagePollerHelper usagePollerHelper;
        @InjectMocks
        private UsageProcessor usageProcessor = new UsageProcessor();

        private List<LoadBalancerMergedHostUsage> mergedExistingUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        private List<LoadBalancerMergedHostUsage> mergedCurrentUsages = new ArrayList<LoadBalancerMergedHostUsage>();
        private List<LoadBalancerHostUsage> newLBHostUsages = new ArrayList<LoadBalancerHostUsage>();
        private UsageProcessorResult processorResult;

        private Calendar pollTime;
        String pollTimeStr;

        @Before
        public void standUp() throws Exception {
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        @Test
        public void shouldAddExistingUsageMergedRecordsAndCurrentUsageMergedRecordsAndReturnNewHostUsages() {
            LoadBalancerMergedHostUsage merged1 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged2 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged3 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged4 = mock(LoadBalancerMergedHostUsage.class);
            mergedExistingUsages.add(merged1);
            mergedExistingUsages.add(merged2);
            mergedExistingUsages.add(merged3);
            mergedExistingUsages.add(merged4);

            LoadBalancerMergedHostUsage merged5 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged6 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged7 = mock(LoadBalancerMergedHostUsage.class);
            LoadBalancerMergedHostUsage merged8 = mock(LoadBalancerMergedHostUsage.class);
            mergedCurrentUsages.add(merged5);
            mergedCurrentUsages.add(merged6);
            mergedCurrentUsages.add(merged7);
            mergedCurrentUsages.add(merged8);

            LoadBalancerHostUsage hostUsage1 = mock(LoadBalancerHostUsage.class);
            LoadBalancerHostUsage hostUsage2 = mock(LoadBalancerHostUsage.class);
            LoadBalancerHostUsage hostUsage3 = mock(LoadBalancerHostUsage.class);
            LoadBalancerHostUsage hostUsage4 = mock(LoadBalancerHostUsage.class);
            newLBHostUsages.add(hostUsage1);
            newLBHostUsages.add(hostUsage2);
            newLBHostUsages.add(hostUsage3);
            newLBHostUsages.add(hostUsage4);

            initMocks(this);
            when(usagePollerHelper.processExistingEvents((Map)any())).thenReturn(mergedExistingUsages);
            processorResult = new UsageProcessorResult(mergedCurrentUsages, newLBHostUsages);
            when(usagePollerHelper.processCurrentUsage((Map)any(), (Map)any(), anyCalendar())).thenReturn(processorResult);

            int mergedExistingUsagesSize = mergedExistingUsages.size();
            int mergedCurrentUsagesSize = mergedCurrentUsages.size();
            UsageProcessorResult result = usageProcessor.mergeRecords(null, null, pollTime);
            Assert.assertEquals(mergedExistingUsagesSize + mergedCurrentUsagesSize, result.getMergedUsages().size());
            Assert.assertEquals(newLBHostUsages.size(), result.getLbHostUsages().size());
            Assert.assertEquals(mergedExistingUsages.get(0), result.getMergedUsages().get(0));
            Assert.assertEquals(mergedExistingUsages.get(1), result.getMergedUsages().get(1));
            Assert.assertEquals(mergedExistingUsages.get(2), result.getMergedUsages().get(2));
            Assert.assertEquals(mergedExistingUsages.get(3), result.getMergedUsages().get(3));
            Assert.assertEquals(mergedCurrentUsages.get(0), result.getMergedUsages().get(4));
            Assert.assertEquals(mergedCurrentUsages.get(1), result.getMergedUsages().get(5));
            Assert.assertEquals(mergedCurrentUsages.get(2), result.getMergedUsages().get(6));
            Assert.assertEquals(mergedCurrentUsages.get(3), result.getMergedUsages().get(7));
            Assert.assertEquals(newLBHostUsages.get(0), result.getLbHostUsages().get(0));
            Assert.assertEquals(newLBHostUsages.get(1), result.getLbHostUsages().get(1));
            Assert.assertEquals(newLBHostUsages.get(2), result.getLbHostUsages().get(2));
            Assert.assertEquals(newLBHostUsages.get(3), result.getLbHostUsages().get(3));
        }

        private Calendar anyCalendar(){
            ArgumentMatcher<Calendar> t = o -> o instanceof Calendar;
            return argThat(t);
        }

    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath:dbunit-context.xml"})
    @TestExecutionListeners({
            DependencyInjectionTestExecutionListener.class,
            DbUnitTestExecutionListener.class})
    @DbUnitConfiguration(dataSetLoader = FlatXmlLoader.class)
    public static class WhenProcessingExistingEventsAndCurrentUsage {

        @Autowired
        private UsageRefactorService usageRefactorService;

        @Mock
        private HostRepository hostRepository;

        @InjectMocks
        private UsageProcessor usageProcessor = new UsageProcessor();

        @InjectMocks
        private UsagePollerHelper usagePollerHelper = new UsagePollerHelper();
        private List<Host> hosts = new ArrayList<Host>();
        private Map<Integer, Map<Integer, SnmpUsage>> snmpMap;
        private Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> lbHostMap;
        private Calendar pollTime;
        String pollTimeStr;
        private int numHosts = 2;
        private int numLBs = 2;

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

            //snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);
            //snmpMap = MapUtil.swapKeys(snmpMap);
            lbHostMap = usageRefactorService.getAllLoadBalancerHostUsages();
            pollTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pollTimeStr = sdf.format(pollTime.getTime());
        }

        /**
         * Verifies UsageProcessor.mergeRecords() method.
         * usagepoller/usagepoller_mergerecords.xml contains existing LbHostUsages for loadbalancers 123, 124 and hosts 1, 2.
         * snmpMap data is generated for the two load balancers with the 2 hosts.
         *
         * UsageProcessorResult will have 4 LoadBalancerHostUsages generated from the current snmp usage(2 lbs X 2 hosts)
         * UsageProcessorResult will have 4 LoadBalancerMergedHostUsages as follow:
         * 2 LoadBalancerMergedHostUsages from usagePollerHelper.processExistingEvents():
         *      one for lb 123 with event type SSL_MIXED_ON;
         *      one for lb 124 with event type SSL_ONLY_ON;
         * 2 LoadBalancerMergedHostUsages from usagePollerHelper.processCurrentUsage()
         */
        @Test
        @DatabaseSetup("classpath:org/openstack/atlas/usagerefactor/usagepoller/usagepoller_mergerecords.xml")
        public void testUsageProcessor_MergeRecords() throws Exception{
            snmpMap = UsagePollerGenerator.generateSnmpMap(numHosts, numLBs);//snmpMap as Map<hostId,Map<LBId, SnmpUsage>
            snmpMap.get(1).get(123).setBytesIn(40);
            snmpMap.get(2).get(123).setBytesIn(21);
            snmpMap.get(1).get(123).setBytesInSsl(40);
            snmpMap.get(2).get(123).setBytesInSsl(40);
            snmpMap.get(1).get(123).setBytesOut(60);
            snmpMap.get(2).get(123).setBytesOut(80);
            snmpMap.get(1).get(123).setBytesOutSsl(50);
            snmpMap.get(2).get(123).setBytesOutSsl(60);
            snmpMap.get(1).get(123).setConcurrentConnections(11);
            snmpMap.get(2).get(123).setConcurrentConnections(15);
            snmpMap.get(1).get(123).setConcurrentConnectionsSsl(20);
            snmpMap.get(2).get(123).setConcurrentConnectionsSsl(25);

            snmpMap.get(1).get(124).setBytesIn(35);
            snmpMap.get(2).get(124).setBytesIn(45);
            snmpMap.get(1).get(124).setBytesInSsl(41);
            snmpMap.get(2).get(124).setBytesInSsl(51);
            snmpMap.get(1).get(124).setBytesOut(100);
            snmpMap.get(2).get(124).setBytesOut(110);
            snmpMap.get(1).get(124).setBytesOutSsl(70);
            snmpMap.get(2).get(124).setBytesOutSsl(90);
            snmpMap.get(1).get(124).setConcurrentConnections(1);
            snmpMap.get(2).get(124).setConcurrentConnections(5);
            snmpMap.get(1).get(124).setConcurrentConnectionsSsl(0);
            snmpMap.get(2).get(124).setConcurrentConnectionsSsl(5);

            usageProcessor.setUsagePollerHelper(usagePollerHelper);
            UsageProcessorResult result = usageProcessor.mergeRecords(lbHostMap, snmpMap, pollTime);
            System.out.println("LbHostUsages: "+result.getLbHostUsages());
            System.out.println("MergedUsages: ");
            for(LoadBalancerMergedHostUsage mergedHostUsage:result.getMergedUsages()){
                System.out.print(toString(mergedHostUsage));
            }
            Assert.assertEquals(4, result.getMergedUsages().size());
            //TODO verify the data manually
            AssertLoadBalancerMergedHostUsage.containsValuesByEventType(1234, 123, 2L, 2L, 2L, 2L, 13, 11, 2, 5,
                    UsageEvent.SSL_MIXED_ON, pollTimeStr, result.getMergedUsages());
            AssertLoadBalancerMergedHostUsage.containsValuesByEventType(1234, 123, 29L, 28L, 68L, 18L, 26, 45, 2, 5,
                    null, pollTimeStr, result.getMergedUsages());

            AssertLoadBalancerMergedHostUsage.containsValuesByEventType(1234, 124, 2L, 2L, 2L, 2L, 15, 11, 3, 3,
                    UsageEvent.SSL_ONLY_ON, pollTimeStr, result.getMergedUsages());
            AssertLoadBalancerMergedHostUsage.containsValuesByEventType(1234, 124, 8L, 0L, 98L, 28L, 6, 5, 3, 3,
                    null, pollTimeStr, result.getMergedUsages());

        }

        public String toString(LoadBalancerMergedHostUsage mergedHostUsage){
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb/*.append("account_id: ").append(mergedHostUsage.getAccountId())*/.append(", loadbalancer_id: ").append(mergedHostUsage.getLoadbalancerId());
            sb.append(", bandwidth_out: ").append(mergedHostUsage.getOutgoingTransfer()).append(", bandwidth_in: ").append(mergedHostUsage.getIncomingTransfer()).append(", bandwidth_in_ssl: ");
            sb.append(mergedHostUsage.getIncomingTransferSsl()).append(", bandwdith_out_ssl: ").append(mergedHostUsage.getOutgoingTransferSsl()).append(", concurrent_connections: ");
            sb.append(mergedHostUsage.getConcurrentConnections()).append(", concurrent_connections_ssl: ").append(mergedHostUsage.getConcurrentConnectionsSsl()).append(", poll_time: ");
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String pollTimeStr = formatter.format(pollTime.getTime());
            sb.append(pollTimeStr).append(", tags_bitmask: ").append(mergedHostUsage.getTagsBitmask()).append(", num_vips: ").append(mergedHostUsage.getNumVips());
            sb.append(", event_type: ").append(mergedHostUsage.getEventType());
            sb.append(" }\n");
            return sb.toString();
        }

    }

}