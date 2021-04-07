package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncServiceImpl;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostsubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.NetInterface;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LbaasFault;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.entities.Zone;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ClusterNotEmptyException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.ClusterService;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import sun.net.util.IPAddressUtil;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
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

        @Test
        public void shouldThrowClusterNotEmptyExceptionWhenThereIsHostAssociated() throws EntityNotFoundException, ClusterNotEmptyException {
            doThrow(ClusterNotEmptyException.class).when(clusterService).deleteCluster(ArgumentMatchers.<Cluster>any());
            response = clusterResource.deleteCluster();
            Assert.assertEquals(400, response.getStatus());
        }
    }

    public static class whenUpdatingAcLuster {

        ClusterResource clusterResource;

        @Mock
        ClusterService clusterService;

        org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster cluster;

        @Before
        public void setUp() {

            MockitoAnnotations.initMocks(this);
            clusterResource = new ClusterResource();
            clusterResource.setClusterService(clusterService);
            clusterResource.setMockitoAuth(true);
            clusterResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            cluster = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster();

            cluster.setName("dev");
            cluster.setDescription("test");
            

        }

        @Test
        public void shouldReturn200WhenUpdatingACluster() throws Exception {

            Response response = clusterResource.updateCluster(cluster);
            Assert.assertEquals(200, response.getStatus());

        }

        @Test
        public void shouldAcceptWhenClusterNotNamed() throws Exception  {
            cluster.setName(null);
            Response response = clusterResource.updateCluster(cluster);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test public void shouldReturn400WhenRequestHasUtilization() throws BadRequestException {
            cluster.setUtilization("0");
            Response response = clusterResource.updateCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn400WhenClusterHasId() throws Exception  {
            cluster.setId(1);
            Response response = clusterResource.updateCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn200WhenClusterHasDataCenter() throws Exception  {
            cluster.setDataCenter(org.openstack.atlas.docs.loadbalancers.api.management.v1.DataCenter.DFW);
            Response response = clusterResource.updateCluster(cluster);
            Assert.assertEquals(200, response.getStatus());
        }

    }

    public static class whenCreatingAcLuster {

        ClustersResource clustersResource;

        @Mock
        ClusterService clusterService;

        org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster cluster;

        @Before
        public void setUp() {

            MockitoAnnotations.initMocks(this);
            clustersResource = new ClustersResource();
            clustersResource.setClusterService(clusterService);
            clustersResource.setMockitoAuth(true);
            clustersResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            cluster = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster();
            cluster.setName("dev");
            cluster.setDescription("test");
            cluster.setDataCenter(org.openstack.atlas.docs.loadbalancers.api.management.v1.DataCenter.DFW);
            cluster.setClusterIpv6Cidr("2001:4801:79f1:1::/64");
            cluster.setPassword("e2fed4da98a840a40788acb64940469d");
            cluster.setUsername("admin");
            cluster.setStatus(org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus.ACTIVE);

        }

        @Test
        public void shouldReturn200WhenCreatingAValidCluster() {
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldThrowBadRequestWhenCreatingAClusterWithoutStatus() {
            cluster.setStatus(null);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide a status.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenCreatingAClusterWithInvalidDataCenter() {
            cluster.setDataCenter(null);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide a valid Data Center.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenClusterNotNamed() {
            cluster.setName(null);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide a unique cluster name.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWithInvalidUsername() {
            cluster.setUsername(null);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide a valid username.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenNullPassword() {
            cluster.setPassword(null);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide a valid password.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenInvalidUnEncryptedPassword() throws BadRequestException {
            cluster.setPassword("testPassword");
            doThrow(new BadRequestException("Cluster password must be valid and encrypted with proper keys.")).when(
                    clusterService).create(any());
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Cluster password must be valid and encrypted with proper keys.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenRequestHasUtilization() {
            cluster.setUtilization("0");
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Utilization can not be updated in this request.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenRequestNumberOfHostMachines() {
            cluster.setNumberOfHostMachines(1);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Number of host Machines can not be updated in this request.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenRequestNumberOfLoadBalancingConfigurations() {
            cluster.setNumberOfLoadBalancingConfigurations(1);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Number of Load Balancing Configs can not be updated in this request.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenRequestNumberOfUniqueCustomers() {
            cluster.setNumberOfUniqueCustomers(1);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Number of unique customers can not be updated in this request.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldThrowBadRequestWhenClusterHasId()  {
            cluster.setId(1);
            Response response = clustersResource.createCluster(cluster);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must not include ID for this request.",
                    ((BadRequest) response.getEntity()).getValidationErrors().getMessages().get(0));
        }
    }

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
        @InjectMocks
        ClusterResource clusterResource;

        List<Host> hosts;
        long hostConnections = 5;
        int conn = 3;


        @Before
        public void setUp() throws RemoteException, VTMRestClientException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, DecryptException, EntityNotFoundException, MalformedURLException {
        MockitoAnnotations.initMocks(this);
        hosts = new ArrayList<Host>();
        Host h1 = new Host();
        Host h2 = new Host();
        hosts.add(h1);
        hosts.add(h2);
        doReturn(hostRepository).when(managementDependencyProvider).getHostRepository();
        doReturn(hostConnections).when(hostRepository).getHostsConnectionsForCluster(ArgumentMatchers.anyInt());
        doReturn(hosts).when(clusterService).getHosts(ArgumentMatchers.anyInt());

        }

        @Test
        public void shouldReturnUtilization() throws Exception {
            doReturn(conn).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
            String response = clusterResource.getUtilization(5);
            Assert.assertEquals("100.0 %", response);
        }

        @Test
        public void shouldThrowRemoteException() throws Exception {
            doThrow(RemoteException.class).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
            String response = clusterResource.getUtilization(5);
            verify(notificationService, times(2)).saveAlert(ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.anyString());
        }
    }

    public static class whenRetrievingACluster {

        @Mock
        ManagementDependencyProvider managementDependencyProvider;
        @Mock
        ClusterService clusterService;
        @Mock
        HostRepository hostRepository;
        @Mock
        ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        @InjectMocks
        ClusterResource clusterResource;
        Response response;
        List<Host> hosts;
        long hostConnections = 5;
        int conn = 3;
        Cluster domainCl;


        @Before
        public void setUp() throws RemoteException, VTMRestClientException, RollBackException, InsufficientRequestException, VTMRestClientObjectNotFoundException, DecryptException, EntityNotFoundException, MalformedURLException {
            MockitoAnnotations.initMocks(this);
            clusterResource.setMockitoAuth(true);
            domainCl = new Cluster();
            domainCl.setId(1);
            clusterResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            hosts = new ArrayList<Host>();
            Host h1 = new Host();
            Host h2 = new Host();
            hosts.add(h1);
            hosts.add(h2);
            doReturn(domainCl).when(clusterService).get(ArgumentMatchers.anyInt());
            doReturn(hostRepository).when(managementDependencyProvider).getHostRepository();
            doReturn(hostConnections).when(hostRepository).getHostsConnectionsForCluster(ArgumentMatchers.anyInt());
            doReturn(hosts).when(clusterService).getHosts(ArgumentMatchers.anyInt());
            doReturn(conn).when(reverseProxyLoadBalancerVTMService).getTotalCurrentConnectionsForHost(ArgumentMatchers.any());
        }

        @Test
        public void shouldRetrieveAClusterWithStatus200() throws Exception {
            response = clusterResource.retrieveCluster();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundException() throws Exception {
            domainCl.setId(0);
            doThrow(EntityNotFoundException.class).when(clusterService).get(ArgumentMatchers.eq(0));
            response = clusterResource.retrieveCluster();
            Assert.assertEquals(404, response.getStatus());
        }

    }

    public static class WhenUpdatingSubnets {
        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        private ManagementAsyncService asyncService;
        private ClusterResource clusterResource;
        private OperationResponse operationResponse;
        private Hostsubnet hsub;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet hsubs;
        private NetInterface ni;
        private Cidr cidr;
        private Hostsubnet hsub2;
        private org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet hsubs2;
        private NetInterface ni2;
        private Cidr cidr2;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        private ClusterService clusterService;
        private Host host1;
        private Host host2;
        private List<Host> hosts;
        private org.openstack.atlas.service.domain.pojos.Hostssubnet hostssubnet;
        private org.openstack.atlas.service.domain.pojos.NetInterface netInterface;
        private org.openstack.atlas.service.domain.pojos.NetInterface netInterface2;
        private org.openstack.atlas.service.domain.pojos.Cidr cidr3;
        private ManagementDependencyProvider managementDependencyProvider;
        private ManagementAsyncServiceImpl managementAsyncServiceimpl;
        private Session session;

        @Mock
        private RestApiConfiguration config;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            clusterResource = new ClusterResource();
            clusterResource.setMockitoAuth(true);
            managementDependencyProvider = mock(ManagementDependencyProvider.class);
            ClusterRepository crepo = mock(ClusterRepository.class);
            asyncService = mock(ManagementAsyncService.class);
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            clusterService = mock(ClusterService.class);
            clusterResource.setManagementAsyncService(asyncService);
            clusterResource.setClusterService(clusterService);
            clusterResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            clusterResource.setConfiguration(config);

            clusterResource.setId(1);
            clusterResource.setClusterRepository(crepo);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            clusterResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            host1 = new Host();
            host1.setMaxConcurrentConnections(2);
            host1.setHostStatus(HostStatus.ACTIVE);
            host2 = new Host();
            host2.setMaxConcurrentConnections(2);
            host2.setHostStatus(HostStatus.ACTIVE);
            hosts = new ArrayList<Host>();
            hosts.add(host1);
            hosts.add(host2);
            netInterface = new org.openstack.atlas.service.domain.pojos.NetInterface();
            netInterface2 = new org.openstack.atlas.service.domain.pojos.NetInterface();
            cidr3 = new org.openstack.atlas.service.domain.pojos.Cidr();
            cidr3.setBlock("123.78.1.2/27");
            hostssubnet = new org.openstack.atlas.service.domain.pojos.Hostssubnet();
            org.openstack.atlas.service.domain.pojos.Hostsubnet h1 = new org.openstack.atlas.service.domain.pojos.Hostsubnet();
            h1.setName("t1");
            hostssubnet.getHostsubnets().add(h1);

            h1.getNetInterfaces().add(netInterface);
            h1.getNetInterfaces().add(netInterface2);

            when(crepo.getHosts(anyInt())).thenReturn(hosts);
            when(clusterService.getHosts(anyInt())).thenReturn(hosts);
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        }

        @Before
        public void standUpSubnet() {
            hsub = new Hostsubnet();
            hsubs = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet();
            ni = new NetInterface();
            cidr = new Cidr();
            ni.setName("name");

            hsub2 = new Hostsubnet();
            hsubs2 = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet();
            ni2 = new NetInterface();
            cidr2 = new Cidr();
            ni2.setName("name2");
            cidr2.setBlock("123.78.1.1/27");
            ni2.getCidrs().add(cidr2);
            hsub2.getNetInterfaces().add(ni2);
            hsubs2.getHostsubnets().add(hsub2);
        }

        @Test
        public void shouldReturn202WhenESBisNormalWhenAddSubnetWIpv6() throws Exception {
            cidr.setBlock("fe80::200:f8ff:fe21:67cf/16");

            ni.getCidrs().add(cidr);
            hsub.getNetInterfaces().add(ni);
            hsubs.getHostsubnets().add(hsub);
            Response resp = clusterResource.putHostsSubnetMappings(hsubs, false, false);
            Assert.assertEquals(202, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
            verify(asyncService, times(1)).callAsyncLoadBalancingOperation(
                    eq(Operation.SET_HOST_SUBNET_MAPPINGS), any(EsbRequest.class));        }

        @Test
        public void shouldReturn202whenESBisNormalWhenAddSubnetWIpv4() throws Exception {
            cidr.setBlock("192.168.0.1/24");

            ni.getCidrs().add(cidr);
            hsub.getNetInterfaces().add(ni);
            hsubs.getHostsubnets().add(hsub);
            Response resp = clusterResource.putHostsSubnetMappings(hsubs, false, false);
            Assert.assertEquals(202, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
            verify(asyncService, times(1)).callAsyncLoadBalancingOperation(
                    eq(Operation.SET_HOST_SUBNET_MAPPINGS), any(EsbRequest.class));
        }

        @Test
        public void shouldFailWithMultipleSubnets() throws Exception {
            cidr.setBlock("192.168.0.1/24");

            ni.getCidrs().add(cidr);
            hsub.getNetInterfaces().add(ni);
            hsubs.getHostsubnets().add(hsub);
            hsubs.getHostsubnets().add(hsub);
            Response resp = clusterResource.putHostsSubnetMappings(hsubs, false, false);
            Assert.assertEquals(400, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
            verify(asyncService, times(0)).callAsyncLoadBalancingOperation(
                    eq(Operation.SET_HOST_SUBNET_MAPPINGS), any(EsbRequest.class));
        }

        @Test
        public void shouldReturn500WhenAsyncCallThrowsException() throws Exception {
            cidr.setBlock("192.168.0.1/24");
            ni.getCidrs().add(cidr);
            hsub.getNetInterfaces().add(ni);
            hsubs.getHostsubnets().add(hsub);
            doReturn(asyncService).when(managementDependencyProvider).getManagementAsyncService();
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(ArgumentMatchers.any(), ArgumentMatchers.<EsbRequest>any());
            Response resp = clusterResource.putHostsSubnetMappings(hsubs, false, false);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn400BadRequestInCaseOfMultipleSubnets() throws Exception {
            cidr.setBlock("192.168.0.1/24");
            ni.getCidrs().add(cidr);
            hsub.getNetInterfaces().add(ni);
            hsubs.getHostsubnets().add(hsub);
            hsubs.getHostsubnets().add(hsub2);
            Response resp = clusterResource.putHostsSubnetMappings(hsubs, false, false);
            Assert.assertEquals("Invalid request", ((BadRequest)resp.getEntity()).getMessage());
        }

    }
}
