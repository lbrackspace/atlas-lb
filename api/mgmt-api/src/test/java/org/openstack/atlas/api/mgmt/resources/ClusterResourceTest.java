package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LbaasFault;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.services.ClusterService;
import org.openstack.atlas.service.domain.services.HostService;

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
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        private List<Host> hosts;

        @Before
        public void setUp() {
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            clusterService = mock(ClusterService.class);

            resource = new ClusterResource();
            resource.setMockitoAuth(true);
            resource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
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
            verify(reverseProxyLoadBalancerService, times(0)).getSubnetMappings(any());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(any());
        }

        @Test
        public void addVirtualIpBlocksShouldCallSoapService() throws Exception {
            when(configuration.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");

            when(reverseProxyLoadBalancerService.getSubnetMappings(any())).thenReturn(new Hostssubnet());

            Response resp = resource.addVirtualIpBlocks(new VirtualIpBlocks());
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerService, times(1)).getSubnetMappings(any());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(any());
        }
    }

    public static class whenRetrievingAllHostNetworks {
        private ClusterResource resource;
        private OperationResponse response;
        private Configuration configuration;

        @Mock
        private ClusterService clusterService;
        @Mock
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
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
            resource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
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
}
