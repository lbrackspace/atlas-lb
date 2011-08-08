package org.openstack.atlas.usage.jobs;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.repository.HostRepository;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsagePollerJobTest {
    public static class WhenGettingLoadbalancerIdsFromVirtualServerNames {
        private LoadBalancerUsagePoller usagePollerJob;
        private List<String> loadbalancerNames;

        @Before
        public void standUp() {
            usagePollerJob = new LoadBalancerUsagePoller();
            loadbalancerNames = new ArrayList<String>();

            loadbalancerNames.add("1_1");
            loadbalancerNames.add("1234_1234");
            loadbalancerNames.add("1234_4321");
        }

        @Test
        public void shouldReturnAllLoadbalancerIds() {
            Assert.assertEquals(loadbalancerNames.size(), usagePollerJob.stripLbIdAndAccountIdFromName(loadbalancerNames).size());
        }

        @Test
        public void shouldOnlyReturnIdsForProperlyFormattedNames() {
            int expectedSize = loadbalancerNames.size();
            loadbalancerNames.add("1_notvalid");
            Assert.assertEquals(expectedSize, usagePollerJob.stripLbIdAndAccountIdFromName(loadbalancerNames).size());
        }

        @Test
        public void shouldOnlyReturnIdsForProperlyFormattedNamesWhenArrayOutOfBoundsExceptionOccurs() {
            int expectedSize = loadbalancerNames.size();
            loadbalancerNames.add("blahblahblah");
            Assert.assertEquals(expectedSize, usagePollerJob.stripLbIdAndAccountIdFromName(loadbalancerNames).size());
        }
    }

    public static class WhenGettingValidNamesForPolling {
        private LoadBalancerUsagePoller usagePollerJob;
        private Host fakeHost;
        private ReverseProxyLoadBalancerAdapter mockedLoadBalancerAdapter;
        private HostRepository mockedHostRepository;
        List<String> allLoadBalancerNames;
        List<LoadBalancer> loadBalancersForHost;

        @Before
        public void standUp() throws RemoteException {
            fakeHost = new Host();
            fakeHost.setId(1);
            
            mockedLoadBalancerAdapter = mock(ReverseProxyLoadBalancerAdapter.class);
            mockedHostRepository = mock(HostRepository.class);

            allLoadBalancerNames = new ArrayList<String>();
            loadBalancersForHost = new ArrayList<LoadBalancer>();

            when(mockedLoadBalancerAdapter.getStatsSystemLoadBalancerNames(Matchers.<LoadBalancerEndpointConfiguration> anyObject()))
                    .thenReturn(allLoadBalancerNames);
            when(mockedHostRepository.getLoadBalancersWithStatus(Matchers.anyInt(), Matchers.eq(LoadBalancerStatus.ACTIVE)))
                    .thenReturn(loadBalancersForHost);

            usagePollerJob = new LoadBalancerUsagePoller();
            usagePollerJob.setReverseProxyLoadBalancerAdapter(mockedLoadBalancerAdapter);
            usagePollerJob.setHostRepository(mockedHostRepository);
        }

        @Test
        public void shouldReturnNoNamesWhenSetsAreEmpty() throws InsufficientRequestException, RemoteException {
            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertTrue(validNames.isEmpty());
        }

        @Test
        public void shouldReturnNoNamesWhenOneSetIsEmpty() throws InsufficientRequestException, RemoteException {
            allLoadBalancerNames.add("1234_1234");

            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertTrue(validNames.isEmpty());
        }

        @Test
        public void shouldReturnNoNamesWhenSetsHaveNoIntersection_Simple() throws InsufficientRequestException, RemoteException {
            allLoadBalancerNames.add("1234_1234");
            loadBalancersForHost.add(createLoadBalancerWithIds(9999, 9999));

            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertTrue(validNames.isEmpty());
        }

        @Test
        public void shouldReturnNoNamesWhenSetsHaveNoIntersection_Medium() throws InsufficientRequestException, RemoteException {
            allLoadBalancerNames.add("1_1");
            allLoadBalancerNames.add("2_2");
            allLoadBalancerNames.add("3_3");
            loadBalancersForHost.add(createLoadBalancerWithIds(11, 11));
            loadBalancersForHost.add(createLoadBalancerWithIds(22, 22));
            loadBalancersForHost.add(createLoadBalancerWithIds(33, 33));

            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertTrue(validNames.isEmpty());
        }

        @Test
        public void shouldReturnNameWhenSetsHaveSameData_Simple() throws InsufficientRequestException, RemoteException {
            allLoadBalancerNames.add("1234_1234");
            loadBalancersForHost.add(createLoadBalancerWithIds(1234, 1234));

            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertFalse(validNames.isEmpty());
            Assert.assertEquals(1, validNames.size());
            Assert.assertTrue(validNames.contains("1234_1234"));
        }

        @Test
        public void shouldReturnNameWhenSetsHaveSameData_Medium() throws InsufficientRequestException, RemoteException {
            allLoadBalancerNames.add("1_1");
            allLoadBalancerNames.add("2_2");
            allLoadBalancerNames.add("3_3");
            loadBalancersForHost.add(createLoadBalancerWithIds(1, 1));
            loadBalancersForHost.add(createLoadBalancerWithIds(2, 2));
            loadBalancersForHost.add(createLoadBalancerWithIds(3, 3));

            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertFalse(validNames.isEmpty());
            Assert.assertEquals(3, validNames.size());
            Assert.assertTrue(validNames.contains("1_1"));
            Assert.assertTrue(validNames.contains("2_2"));
            Assert.assertTrue(validNames.contains("3_3"));
        }

        @Test
        public void shouldReturnIntersectionWhenSetsHaveMixedData() throws InsufficientRequestException, RemoteException {
            allLoadBalancerNames.add("1_1");
            allLoadBalancerNames.add("2_2");
            allLoadBalancerNames.add("3_3");
            allLoadBalancerNames.add("4_4");
            loadBalancersForHost.add(createLoadBalancerWithIds(11, 11));
            loadBalancersForHost.add(createLoadBalancerWithIds(2, 2));
            loadBalancersForHost.add(createLoadBalancerWithIds(33, 33));
            loadBalancersForHost.add(createLoadBalancerWithIds(4, 4));

            Set<String> validNames = usagePollerJob.getValidNames(fakeHost, null);

            Assert.assertFalse(validNames.isEmpty());
            Assert.assertEquals(2, validNames.size());
            Assert.assertTrue(validNames.contains("2_2"));
            Assert.assertTrue(validNames.contains("4_4"));
        }

        private LoadBalancer createLoadBalancerWithIds(Integer id, Integer accountId) {
            LoadBalancer lb = new LoadBalancer();
            lb.setId(id);
            lb.setAccountId(accountId);
            return lb;
        }
    }
}
