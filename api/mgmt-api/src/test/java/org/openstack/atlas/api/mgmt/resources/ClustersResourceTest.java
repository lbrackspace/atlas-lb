package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
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

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ClustersResourceTest {

    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

//    public static class whenGettingTheUtilization {
//
//        @Mock
//        ManagementDependencyProvider managementDependencyProvider;
//        @Mock
//        ClusterService clusterService;
//        @Mock
//        HostRepository hostRepository;
//        @Mock
//        ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
//        @Mock
//        NotificationService notificationService;
//        @Mock
//        ClusterRepository clusterRepository;
//        @InjectMocks
//        ClustersResource clustersResource;
//
//        Cluster cluster;
//
//        List<Host> hosts;
//        long hostConnections = 6;
//        int conn = 3;
//
//        @Before
//        public void setUp() throws RemoteException, VTMRestClientException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, DecryptException, EntityNotFoundException, MalformedURLException {
//        MockitoAnnotations.initMocks(this);
//        cluster = new Cluster();
//        cluster.setId(1);
//        hosts = new ArrayList<Host>();
//        Host h1 = new Host();
//        Host h2 = new Host();
//        hosts.add(h1);
//        hosts.add(h2);
//        doReturn(hostRepository).when(managementDependencyProvider).getHostRepository();
//        doReturn(hostConnections).when(hostRepository).getHostsConnectionsForCluster(ArgumentMatchers.anyInt());
//        doReturn(clusterRepository).when(managementDependencyProvider).getClusterRepository();
//        doReturn(hosts).when(clusterRepository).getHosts(ArgumentMatchers.anyInt());
//        doReturn(hosts).when(clusterService).getHosts(ArgumentMatchers.anyInt());
//        }
//
//        @Test
//        public void shouldReturnUtilization() throws Exception {
//            doReturn(conn).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
//            String response = clustersResource.getUtilization(cluster);
//            Assert.assertEquals("100.0 %", response);
//        }
//
//        @Test
//        public void shouldThrowRemoteException() throws Exception {
//            doThrow(RemoteException.class).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
//            String response = clustersResource.getUtilization(cluster);
//            verify(notificationService, times(2)).saveAlert(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.anyString());
//        }
//    }

    public static class whenRetrievingAllClusters {

        @Mock
        ManagementDependencyProvider managementDependencyProvider;
        @Mock
        ClusterService clusterService;
        @Mock
        HostRepository hostRepository;
        @Mock
        ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        @Mock
        ClusterRepository clusterRepository;
        @InjectMocks
        ClustersResource clustersResource;
        Response response;
        List<Host> hosts;
        long hostConnections = 5;
        int conn = 3;
        List<org.openstack.atlas.service.domain.entities.Cluster> domainCls;
        org.openstack.atlas.service.domain.entities.Cluster domainCl1;
        org.openstack.atlas.service.domain.entities.Cluster domainCl2;


        @Before
        public void setUp() throws RemoteException, VTMRestClientException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, DecryptException, EntityNotFoundException, MalformedURLException {
            MockitoAnnotations.initMocks(this);
            clustersResource.setMockitoAuth(true);
            domainCl1 = new org.openstack.atlas.service.domain.entities.Cluster();
            domainCl1.setId(1);
            domainCl2 = new org.openstack.atlas.service.domain.entities.Cluster();
            domainCl2.setId(2);
            domainCls = new ArrayList<org.openstack.atlas.service.domain.entities.Cluster>();
            domainCls.add(domainCl1);
            domainCls.add(domainCl2);
            clustersResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            hosts = new ArrayList<Host>();
            Host h1 = new Host();
            Host h2 = new Host();
            hosts.add(h1);
            hosts.add(h2);
            doReturn(clusterRepository).when(managementDependencyProvider).getClusterRepository();
            doReturn(domainCls).when(clusterRepository).getAll(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            doReturn(hostRepository).when(managementDependencyProvider).getHostRepository();
            doReturn(hostConnections).when(hostRepository).getHostsConnectionsForCluster(ArgumentMatchers.anyInt());
            doReturn(hosts).when(clusterService).getHosts(ArgumentMatchers.anyInt());
            doReturn(conn).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
        }

        @Test
        public void shouldRetrieveClustersWithStatus200() throws Exception {
            response = clustersResource.retrieveAllClusters(1,100);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundException() throws Exception {
            doThrow(EntityNotFoundException.class).when(clusterRepository).getAll(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = clustersResource.retrieveAllClusters(1,100);
            Assert.assertEquals(404, response.getStatus());
        }

    }
}
