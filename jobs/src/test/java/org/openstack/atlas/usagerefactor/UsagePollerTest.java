package org.openstack.atlas.usagerefactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsagePollerTest {

    @RunWith(MockitoJUnitRunner.class)
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
            //when(usagePoller.getLoadBalancerHostUsageRecords()).thenReturn(new Map<Integer, LoadBalancerHostUsage>());
        }

        @Test
        public void placementTest() {
        }
    }

    public static class WhenTestingBasicRequests {
        private UsagePoller usagePoller;
        private HostRepository hostRepository;
        List<Host> hosts;

        @Before
        public void standUp() {
            usagePoller = new UsagePollerImpl();
            hostRepository = mock(HostRepository.class);
        }

        @Before
        public void createHosts() {
            hosts = new ArrayList<Host>();
            Host host1 = new Host();
            host1.setId(1);
            host1.setName("TestHost1");
            hosts.add(host1);
            Host host2 = new Host();
            host2.setId(2);
            host1.setName("TestHost2");
            hosts.add(host2);
        }

        @Ignore
        @Test
        public void getCurrentDataTest() throws Exception {
            when(hostRepository.getAll()).thenReturn(hosts);
            assertNotNull(usagePoller.getCurrentData());
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
}
