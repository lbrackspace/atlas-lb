package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.ClusterService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ClustersResourceTest {

    public static class whenGettingTheUtilization {

        @Mock
        ManagementDependencyProvider managementDependencyProvider;
        @Mock
        ClusterService clusterService;
        @Mock
        HostRepository hostRepository;
        @Mock
        ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        @Mock
        NotificationService notificationService;
        @Mock
        ClusterRepository clusterRepository;
        @InjectMocks
        ClustersResource clustersResource;

        Cluster cluster;

        List<Host> hosts;
        long hostConnections = 6;
        int conn = 3;

        @Before
        public void setUp() throws RemoteException, VTMRestClientException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, DecryptException, EntityNotFoundException, MalformedURLException {
        MockitoAnnotations.initMocks(this);
        cluster = new Cluster();
        cluster.setId(1);
        hosts = new ArrayList<Host>();
        Host h1 = new Host();
        Host h2 = new Host();
        hosts.add(h1);
        hosts.add(h2);
        doReturn(hostRepository).when(managementDependencyProvider).getHostRepository();
        doReturn(hostConnections).when(hostRepository).getHostsConnectionsForCluster(ArgumentMatchers.anyInt());
        doReturn(clusterRepository).when(managementDependencyProvider).getClusterRepository();
        doReturn(hosts).when(clusterRepository).getHosts(ArgumentMatchers.anyInt());
        doReturn(hosts).when(clusterService).getHosts(ArgumentMatchers.anyInt());
        }

        @Test
        public void shouldReturnUtilization() throws Exception {
            doReturn(conn).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
            String response = clustersResource.getUtilization(cluster);
            Assert.assertEquals("100.0 %", response);
        }

        @Test
        public void shouldThrowRemoteException() throws Exception {
            doThrow(RemoteException.class).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
            String response = clustersResource.getUtilization(cluster);
            verify(notificationService, times(2)).saveAlert(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.anyString());
        }




    }
}
