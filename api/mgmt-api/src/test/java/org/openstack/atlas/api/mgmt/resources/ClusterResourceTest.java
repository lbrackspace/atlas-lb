package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LbaasFault;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.ClusterNotEmptyException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.services.ClusterService;
import org.openstack.atlas.service.domain.services.HostService;
import org.rackspace.vtm.client.monitor.Argument;

import javax.ws.rs.core.Response;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ClusterResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenCheckingHealth {
        private ClusterResource resource;
        private OperationResponse response;
        private Configuration configuration;
        private ClusterService clusterService;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        private List<Host> hosts;

        @Before
        public void setUp() {
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            clusterService = mock(ClusterService.class);

            resource = new ClusterResource();
            resource.setMockitoAuth(true);
            resource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            resource.setClusterService(clusterService);
            response = new OperationResponse();
            response.setExecutedOkay(true);
            configuration = mock(Configuration.class);
            resource.setConfiguration(configuration);
            resource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            hosts = new ArrayList<>();
            Host host = new Host();
            host.setId(2);
            hosts.add(host);

            when(clusterService.getHosts(anyInt())).thenReturn(hosts);
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        }

        @Test
        public void addVirtualIpBlocksShouldCallVTMService() throws Exception {
            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any())).thenReturn(new Hostssubnet());

            Response resp = resource.addVirtualIpBlocks(new VirtualIpBlocks());
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(any());
        }

    }

    public static class whenRetrievingAllHostNetworks {
        private ClusterResource resource;
        private OperationResponse response;
        private Configuration configuration;

        @Mock
        private ClusterService clusterService;
        @Mock
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;

        Cluster cluster;
        List<Host> hosts;
        Host failOverHost;
        Host soapEndPointHost;
        Host host;
        Host host2;
        List<String> failOverHostNames;
        List<Host> failoverHosts;
        String logFileLocation;
        Integer loadBalancerId;
        Hostssubnet hostssubnet;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            resource = new ClusterResource();
            resource.setMockitoAuth(true);
            resource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            resource.setClusterService(clusterService);
            response = new OperationResponse();
            response.setExecutedOkay(true);
            configuration = mock(Configuration.class);
            resource.setConfiguration(configuration);
            resource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            hosts = new ArrayList<>();
            host = new Host();
            host2 = new Host();
            cluster = new Cluster();
            soapEndPointHost = new Host();
            failOverHost = new Host();
            failOverHostNames = new ArrayList<>();
            failoverHosts = new ArrayList<>();

            resource.setId(1);

            host.setName("testHost");
            host2.setName("testHost2");
            host.setId(1);
            host.setId(2);
            host.setEndpoint("https://endPointHostTest/soap");
            host2.setEndpoint("https://endPointHostTest2/soap");
            host.setRestEndpoint("https://restEndPointHostTest2/config/active");
            host2.setRestEndpoint("https://restEndPointHostTest/config/active");
            host.setHostStatus(HostStatus.ACTIVE);
            host2.setHostStatus(HostStatus.FAILOVER);
            host.setZone(Zone.A);
            host2.setZone(Zone.A);
            host.setTrafficManagerName("n1.com");
            host2.setTrafficManagerName("n2.com");
            cluster.setId(1);
            cluster.setName("testCluster");
            soapEndPointHost.setId(3);
            soapEndPointHost.setEndpoint("https://soapEndPointHostTest/soap");
            Host failOverHost2 = new Host();
            failOverHost.setEndpoint("failover.com/soap");
            failOverHost.setZone(Zone.A);
            failOverHost.setTrafficManagerName("nf1.com");
            failOverHost2.setZone(Zone.B);
            failOverHost2.setEndpoint("failover.com/soap");
            failOverHost2.setTrafficManagerName("nf2.com");
            loadBalancerId = 1;
            failOverHostNames.add("test.com/soap");
            failoverHosts.add(failOverHost);
            failoverHosts.add(failOverHost2);
            logFileLocation = "test";
            host.setCluster(cluster);
            hosts.add(host);
            hosts.add(host2);

            hostssubnet = new org.openstack.atlas.service.domain.pojos.Hostssubnet();
            org.openstack.atlas.service.domain.pojos.Hostsubnet h1 = new org.openstack.atlas.service.domain.pojos.Hostsubnet();
            h1.setName("h1");
            h1.setNetInterfaces(new ArrayList<>());
            hostssubnet.getHostsubnets().add(h1);

            when(clusterService.getHosts(anyInt())).thenReturn(hosts);
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        }

        @Test
        public void shouldRetrieveHostSubnetsForCluster() throws Exception {
            Hostssubnet hostssubnet2 = new Hostssubnet();
            org.openstack.atlas.service.domain.pojos.Hostsubnet h1 = new org.openstack.atlas.service.domain.pojos.Hostsubnet();
            h1.setName("h2");
            h1.setNetInterfaces(new ArrayList<>());
            hostssubnet2.getHostsubnets().add(h1);
            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(host)).thenReturn(hostssubnet);
            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(host2)).thenReturn(hostssubnet2);

            Response resp = resource.getClusterHostsSubnets();
            Assert.assertEquals(200, resp.getStatus());
            verify(clusterService, times(1)).getHosts(1);
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host2);

            org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet rsnets = resp.readEntity(org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet.class);
            Assert.assertEquals(2, rsnets.getHostsubnets().size());
        }

        @Test
        public void shouldRetrieveSingleHostSubnetsForCluster() throws Exception {
            hosts.remove(host2);
            when(clusterService.getHosts(anyInt())).thenReturn(hosts);
            when(reverseProxyLoadBalancerVTMService.getSubnetMappings(host)).thenReturn(hostssubnet);

            Response resp = resource.getClusterHostsSubnets();
            Assert.assertEquals(200, resp.getStatus());
            verify(clusterService, times(1)).getHosts(1);
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host2);

            org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet rsnets = resp.readEntity(org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet.class);
            Assert.assertEquals(1, rsnets.getHostsubnets().size());
        }

        @Test
        public void shouldFailRetrieveNoHostSubnetsForCluster() throws Exception {
            hosts.remove(host2);
            hosts.remove(host);
            when(clusterService.getHosts(anyInt())).thenReturn(hosts);

            Response resp = resource.getClusterHostsSubnets();
            Assert.assertEquals(400, resp.getStatus());
            verify(clusterService, times(1)).getHosts(1);
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host);
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host2);

            Assert.assertEquals("Could not find any host networks", resp.readEntity(BadRequest.class).getMessage());
        }

        @Test
        public void shouldErrorOutOnFailureRetrieveNoHostSubnetsForCluster() throws Exception {
            when(clusterService.getHosts(anyInt())).thenReturn(new ArrayList<>());

            doThrow(new RemoteException("Error")).when(reverseProxyLoadBalancerVTMService).getSubnetMappings(any());

            Response resp = resource.getClusterHostsSubnets();
            Assert.assertEquals(400, resp.getStatus());
            verify(clusterService, times(1)).getHosts(1);
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host);
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host2);

            Assert.assertEquals("Could not find any host networks", resp.readEntity(LbaasFault.class).getMessage());
        }
    }


    public static class whenRetrievingClusterEndpointHost {
        private ClusterResource resource;
        private OperationResponse response;
        private Configuration configuration;

        @Mock
        private HostService hostService;
        @Mock
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;

        Cluster cluster;
        List<Host> hosts;
        Host failOverHost;
        Host soapEndPointHost;
        Host host;
        Host host2;
        List<String> failOverHostNames;
        List<Host> failoverHosts;
        String logFileLocation;
        Integer loadBalancerId;
        Hostssubnet hostssubnet;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            resource = new ClusterResource();
            resource.setMockitoAuth(true);
            resource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            resource.setHostService(hostService);
            response = new OperationResponse();
            response.setExecutedOkay(true);
            configuration = mock(Configuration.class);
            resource.setConfiguration(configuration);
            resource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            hosts = new ArrayList<>();
            host = new Host();
            host2 = new Host();
            cluster = new Cluster();
            soapEndPointHost = new Host();
            failOverHost = new Host();
            failOverHostNames = new ArrayList<>();
            failoverHosts = new ArrayList<>();

            resource.setId(1);

            host.setName("testHost");
            host2.setName("testHost2");
            host.setId(1);
            host.setId(2);
            host.setEndpoint("https://endPointHostTest/soap");
            host2.setEndpoint("https://endPointHostTest2/soap");
            host.setRestEndpoint("https://restEndPointHostTest/config/active");
            host2.setRestEndpoint("https://restEndPointHostTest2/config/active");
            host.setHostStatus(HostStatus.ACTIVE);
            host2.setHostStatus(HostStatus.REST_API_ENDPOINT);
            host.setZone(Zone.A);
            host2.setZone(Zone.A);
            host.setTrafficManagerName("n1.com");
            host2.setTrafficManagerName("n2.com");
            cluster.setId(1);
            cluster.setName("testCluster");
            soapEndPointHost.setId(3);
            soapEndPointHost.setEndpoint("https://soapEndPointHostTest/soap");
            Host failOverHost2 = new Host();
            failOverHost.setEndpoint("failover.com/soap");
            failOverHost.setZone(Zone.A);
            failOverHost.setTrafficManagerName("nf1.com");
            failOverHost2.setZone(Zone.B);
            failOverHost2.setEndpoint("failover.com/soap");
            failOverHost2.setTrafficManagerName("nf2.com");
            loadBalancerId = 1;
            failOverHostNames.add("test.com/soap");
            failoverHosts.add(failOverHost);
            failoverHosts.add(failOverHost2);
            logFileLocation = "test";
            host.setCluster(cluster);
            hosts.add(host);
            hosts.add(host2);

            hostssubnet = new org.openstack.atlas.service.domain.pojos.Hostssubnet();
            org.openstack.atlas.service.domain.pojos.Hostsubnet h1 = new org.openstack.atlas.service.domain.pojos.Hostsubnet();
            h1.setName("h1");
            h1.setNetInterfaces(new ArrayList<>());
            hostssubnet.getHostsubnets().add(h1);

            when(hostService.getRestEndPointHost(anyInt())).thenReturn(host2);
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
        }

        @Test
        public void shouldRetrieveEndpointHostForCluster() throws Exception {
            Response resp = resource.getClusterEndPointHost();
            Assert.assertEquals(200, resp.getStatus());
            verify(hostService, times(1)).getRestEndPointHost(1);
            verify(hostService, times(0)).getEndPointHost(1);

            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host h = resp.readEntity(org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class);
            Assert.assertEquals(HostStatus.REST_API_ENDPOINT.toString(), h.getStatus().toString());
            Assert.assertEquals("https://restEndPointHostTest2/config/active", h.getManagementRestInterface());
        }

        @Test
        public void shouldReturn500OnFailureRetrivingEndpointHost() {
            when(hostService.getRestEndPointHost(anyInt())).thenThrow(Exception.class);

            Response resp = resource.getClusterEndPointHost();
            Assert.assertEquals(500, resp.getStatus());
            verify(hostService, times(1)).getRestEndPointHost(1);
            verify(hostService, times(0)).getEndPointHost(1);
        }

        @Test
        public void shouldReturnSoapEndpointHostIfNoRest() {
            when(hostService.getRestEndPointHost(anyInt())).thenReturn(null);
            when(hostService.getEndPointHost(anyInt())).thenReturn(host);

            Response resp = resource.getClusterEndPointHost();
            Assert.assertEquals(200, resp.getStatus());
            verify(hostService, times(1)).getRestEndPointHost(1);
            verify(hostService, times(1)).getEndPointHost(1);
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Host h = resp.readEntity(org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class);
            Assert.assertEquals(HostStatus.ACTIVE.toString(), h.getStatus().toString());
            Assert.assertEquals("https://endPointHostTest/soap", h.getManagementSoapInterface());
            Assert.assertEquals("https://restEndPointHostTest/config/active", h.getManagementRestInterface());
        }

        @Test
        public void shouldReturn404OnNullEndpointHost() {
            when(hostService.getRestEndPointHost(anyInt())).thenReturn(null);
            when(hostService.getEndPointHost(anyInt())).thenReturn(null);

            Response resp = resource.getClusterEndPointHost();
            Assert.assertEquals(404, resp.getStatus());
            verify(hostService, times(1)).getRestEndPointHost(1);
            verify(hostService, times(1)).getRestEndPointHost(1);
        }
    }

    public static class whenDeletingCluster {
        @Mock
        private ClusterService clusterService;
        @InjectMocks
        private ClusterResource clusterResource;
        private Cluster cluster;
        private Response response;

        @Before
        public void setup(){
            MockitoAnnotations.initMocks(this);
            clusterResource.setMockitoAuth(true);
        }

        @Test
        public void shouldBeAbleToDeleteTheCluster() throws EntityNotFoundException, ClusterNotEmptyException {
            doNothing().when(clusterService).deleteCluster(ArgumentMatchers.<Cluster>any());
            response = clusterResource.deleteCluster();
            Assert.assertEquals(Response.Status.ACCEPTED, response.getStatusInfo().toEnum());
        }

        @Test
        public void shouldThrowEntityNotFoundExceptionWhenClusterNotFound() throws EntityNotFoundException, ClusterNotEmptyException {
            doThrow(EntityNotFoundException.class).when(clusterService).deleteCluster(ArgumentMatchers.<Cluster>any());
            response = clusterResource.deleteCluster();
            Assert.assertEquals(404, response.getStatus());
        }
    }
}
