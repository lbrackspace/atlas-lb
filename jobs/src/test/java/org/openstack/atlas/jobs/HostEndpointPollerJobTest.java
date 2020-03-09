package org.openstack.atlas.jobs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerStmAdapter;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerVTMAdapter;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class HostEndpointPollerJobTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenCheckingIfEndpointWorks {

        @Mock
        private HostRepository hostRepository;
        @Mock
        private AlertRepository alertRepository;
        @Mock
        private ReverseProxyLoadBalancerVTMAdapter reverseProxyLoadBalancerVTMAdapter;
        @Mock
        private ReverseProxyLoadBalancerStmAdapter reverseProxyLoadBalancerStmAdapter;
        @InjectMocks
        private HostEndpointPollerJob hostEndpointPollerJob;

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

            when(reverseProxyLoadBalancerVTMAdapter.isEndPointWorking(any())).thenReturn(true);
            when(reverseProxyLoadBalancerStmAdapter.isEndPointWorking(any())).thenReturn(true);

        }

        @Test
        public void shouldChooseVTMAdapterToQueryAndSetActive() throws Exception {
            hostEndpointPollerJob.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).isEndPointWorking(any());
            verify(reverseProxyLoadBalancerStmAdapter, times(0)).isEndPointWorking(any());
            verify(hostRepository).update(host);
            Assert.assertEquals(true, hosts.get(0).getRestEndpointActive());
        }

        @Test
        public void shouldChooseSTMAdapterToQueryAndSetActive() throws Exception {
            List<Host> hosts = new ArrayList<>();
            host.setRestEndpoint("https://127.0.0.1:9070/config/thing/3.4");
            host.setEndpoint("https://127.0.0.1:9040/soap");
            host.setTrafficManagerName("t1");
            host.setHostStatus(HostStatus.ACTIVE);
            host.setRestEndpointActive(false);
            host.setCluster(cluster);
            hosts.add(host);
            when(hostRepository.getAll()).thenReturn(hosts);

            hostEndpointPollerJob.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(0)).isEndPointWorking(any());
            verify(reverseProxyLoadBalancerStmAdapter, times(1)).isEndPointWorking(any());
            verify(hostRepository).update(host);
            Assert.assertEquals(true, hosts.get(0).getRestEndpointActive());

        }

        @Test
        public void shouldChooseVTMAdapterToQueryAndSetInActive() throws Exception {
            hosts = new ArrayList<>();
            host.setRestEndpointActive(true);
            hosts.add(host);
            when(hostRepository.getAll()).thenReturn(hosts);
            when(reverseProxyLoadBalancerVTMAdapter.isEndPointWorking(any())).thenReturn(false);

            hostEndpointPollerJob.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(1)).isEndPointWorking(any());
            verify(reverseProxyLoadBalancerStmAdapter, times(0)).isEndPointWorking(any());
            verify(hostRepository).update(host);
            Assert.assertEquals(false, hosts.get(0).getRestEndpointActive());
        }

        @Test
        public void shouldChooseSTMAdapterToQueryAndSetInActive() throws Exception {
            hosts = new ArrayList<>();
            host.setRestEndpoint("https://127.0.0.1:9070/config/thing/3.4");
            host.setRestEndpointActive(true);
            hosts.add(host);
            when(hostRepository.getAll()).thenReturn(hosts);
            when(reverseProxyLoadBalancerStmAdapter.isEndPointWorking(any())).thenReturn(false);

            hostEndpointPollerJob.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(0)).isEndPointWorking(any());
            verify(reverseProxyLoadBalancerStmAdapter, times(1)).isEndPointWorking(any());
            verify(hostRepository).update(host);
            Assert.assertEquals(false, hosts.get(0).getRestEndpointActive());

        }

        @Test(expected = Exception.class)
        public void shouldSaveAlertOnAdapterException() throws Exception {
            when(reverseProxyLoadBalancerVTMAdapter.isEndPointWorking(any())).thenThrow(Exception.class);

            hostEndpointPollerJob.run();
            verify(reverseProxyLoadBalancerVTMAdapter, times(0)).isEndPointWorking(any());
            verify(reverseProxyLoadBalancerStmAdapter, times(0)).isEndPointWorking(any());
            verify(hostRepository, times(0)).update(host);
            verify(alertRepository, times(1)).save(any());
        }
    }
}
