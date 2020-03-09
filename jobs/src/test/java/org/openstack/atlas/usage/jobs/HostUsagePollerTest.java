package org.openstack.atlas.usage.jobs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.jobs.HostEndpointPollerJob;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class HostUsagePollerTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenCheckingIfEndpointWorks {

        @Mock
        private HostRepository hostRepository;
        @Mock
        private HostUsageRepository hostUsageRepository;
        @Mock
        private ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter;
        @Mock
        private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
        @InjectMocks
        private HostUsagePoller hostUsagePoller;

        private Host host;
        private Cluster cluster;
        private ArrayList<Host> hosts;

        @Before
        public void standUp() throws StmRollBackException {
            cluster = new Cluster();
            cluster.setId(1);
            cluster.setUsername("bob");
            cluster.setPassword("d752bc12821edc4d53bd9b30c8d35b6b");
            host = new Host();
            hosts = new ArrayList<>();
            host.setRestEndpoint("https://127.0.0.1:9070/config/thing/7.0");
            host.setRestEndpointActive(false);
            host.setEndpoint("https://127.0.0.1:9040/soap");
            host.setTrafficManagerName("t1");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setCluster(cluster);
            hosts.add(host);
            when(hostRepository.getAll()).thenReturn(hosts);

            List<String> fhosts = new ArrayList<>();
            when(hostRepository.getFailoverHostNames(anyInt())).thenReturn(fhosts);
            when(hostRepository.getFailoverHosts(anyInt())).thenReturn(hosts);
        }

        @Test
        public void shouldChooseVTMAdapterToQueryAndRetrievieBytes() throws Exception {
            hostUsagePoller.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getHostBytesIn(any());
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getHostBytesOut(any());
            verify(reverseProxyLoadBalancerAdapter, times(0)).getHostBytesIn(any());
            verify(reverseProxyLoadBalancerAdapter, times(0)).getHostBytesOut(any());
            verify(hostUsageRepository).save(any());
        }

        @Test
        public void shouldChooseSTMAdapterToQueryAndRetrieveBytes() throws Exception {
            List<Host> hosts = new ArrayList<>();
            host.setRestEndpoint("https://127.0.0.1:9070/config/thing/3.4");
            host.setEndpoint("https://127.0.0.1:9040/soap");
            host.setTrafficManagerName("t1");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setRestEndpointActive(false);
            host.setCluster(cluster);
            hosts.add(host);
            when(hostRepository.getAll()).thenReturn(hosts);

            hostUsagePoller.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(0)).getHostBytesIn(any());
            verify(reverseProxyLoadBalancerVTMAdapter, times(0)).getHostBytesOut(any());
            verify(reverseProxyLoadBalancerAdapter, times(1)).getHostBytesIn(any());
            verify(reverseProxyLoadBalancerAdapter, times(1)).getHostBytesOut(any());
            verify(hostUsageRepository).save(any());

        }
    }
}
