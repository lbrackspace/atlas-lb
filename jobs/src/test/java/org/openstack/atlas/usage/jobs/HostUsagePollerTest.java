package org.openstack.atlas.usage.jobs;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
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
        private RestApiConfiguration config;
        @InjectMocks
        private HostUsagePoller hostUsagePoller;

        private Host host;
        private Cluster cluster;
        private ArrayList<Host> hosts;

        @Before
        public void standUp() throws VTMRollBackException {
            cluster = new Cluster();
            cluster.setId(1);
            cluster.setUsername("bob");
            cluster.setPassword("d752bc12821edc4d53bd9b30c8d35b6b");
            host = new Host();
            hosts = new ArrayList<>();
            host.setRestEndpoint("https://127.0.0.1:9070/config/thing/7.0/");
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
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getHostBytesIn(any(LoadBalancerEndpointConfiguration.class));
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getHostBytesOut(any(LoadBalancerEndpointConfiguration.class));
            verify(hostUsageRepository).save(any());
        }

        @Test
        public void shouldChooseVTMAdapterToQueryAndRetrievieBytesForRestEndpoint() throws Exception {
            host.setHostStatus(HostStatus.REST_API_ENDPOINT);
            when(hostRepository.getAll()).thenReturn(hosts);

            hostUsagePoller.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getHostBytesIn(any(LoadBalancerEndpointConfiguration.class));
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).getHostBytesOut(any(LoadBalancerEndpointConfiguration.class));
            verify(hostUsageRepository).save(any());
        }
    }
}
