package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostsubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.NetInterface;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.HostService;
import sun.net.util.IPAddressUtil;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class HostResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class whenRetrievingHostDetails {
        private ManagementAsyncService asyncService;
        private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
        private HostResource hostResource;
        private HostService hostService;
        private OperationResponse operationResponse;
        private Host host;
        // TODO: Refactor rest for annotations
        @Mock
        private RestApiConfiguration config;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            hostResource = new HostResource();
            hostResource.setMockitoAuth(true);
            HostRepository hrepo = mock(HostRepository.class);
            asyncService = mock(ManagementAsyncService.class);
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            hostService = mock(HostService.class);
            hostResource.setManagementAsyncService(asyncService);
            hostResource.setId(1);
            hostResource.setHostRepository(hrepo);
            hostResource.setHostService(hostService);
            hostResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
            hostResource.setConfiguration(config);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            hostResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            host = new Host();
            host.setMaxConcurrentConnections(2);
            host.setHostStatus(HostStatus.ACTIVE);

            when(hrepo.getById(anyInt())).thenReturn(host);
            when(hostService.getById(anyInt())).thenReturn(host);
            when(hrepo.getNumberOfUniqueAccountsForHost(anyInt())).thenReturn(3);
            when(hrepo.getActiveLoadBalancerForHost(anyInt())).thenReturn((long) 3);
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        }

        @Test
        public void shouldReturn200WhenRetrievingHost() throws Exception {
            when(reverseProxyLoadBalancerVTMService.getTotalCurrentConnectionsForHost(host)).thenReturn(14);
            Response resp = hostResource.getHost();
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getTotalCurrentConnectionsForHost(host);
        }

        @Test
        public void shouldReturn200whenRetrievingHostsSubnetMappingsViaRest() throws Exception {

            Response resp = hostResource.retrieveHostsSubnetMappings();
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
        }

        @Test
        public void shouldReturn200WhenEsbIsNormalDetails() throws Exception {
            Response resp = hostResource.retrieveHosts(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn200WhenEsbIsNormalSubNetMappings() throws Exception {
            Response resp = hostResource.retrieveHostsSubnetMappings();
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn200WhenEsbIsNormalCustCount() throws Exception {
            operationResponse.setExecutedOkay(true);            
            Response resp = hostResource.getCustomersCounts();
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn200WhenEsbIsNormalBackUps() throws Exception {
            operationResponse.setExecutedOkay(true);            
            Response resp = hostResource.getHostCapacityReports();
            Assert.assertEquals(200, resp.getStatus());
        }

        //Other operations, could be decomposed to new static class...
        @Test
        public void shouldreturn202whenESBisNormal() throws Exception {
            Response resp = hostResource.activateHost();
            Assert.assertEquals(202, resp.getStatus());
        }



        public static class whenAddingSubnets {
            private ManagementAsyncService asyncService;
            private HostResource hostResource;
            private OperationResponse operationResponse;
            private Hostsubnet hsub;
            private Hostssubnet hsubs;
            private NetInterface ni;
            private Cidr cidr;
            private Hostsubnet hsub2;
            private Hostssubnet hsubs2;
            private NetInterface ni2;
            private Cidr cidr2;
            private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
            private HostService hostService;
            private Host host;
            private org.openstack.atlas.service.domain.pojos.Hostssubnet hostssubnet;
            private org.openstack.atlas.service.domain.pojos.NetInterface netInterface;
            private org.openstack.atlas.service.domain.pojos.NetInterface netInterface2;
            private org.openstack.atlas.service.domain.pojos.Cidr cidr3;
            // TODO: Refactor rest for annotations
            @Mock
            private RestApiConfiguration config;

            @Before
            public void setUp() throws EntityNotFoundException {
                MockitoAnnotations.initMocks(this);
                hostResource = new HostResource();
                hostResource.setMockitoAuth(true);
                HostRepository hrepo = mock(HostRepository.class);
                asyncService = mock(ManagementAsyncService.class);
                reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
                hostService = mock(HostService.class);
                hostResource.setManagementAsyncService(asyncService);
                hostResource.setHostService(hostService);
                hostResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
                hostResource.setConfiguration(config);

                hostResource.setId(1);
                hostResource.setHostRepository(hrepo);
                operationResponse = new OperationResponse();
                operationResponse.setExecutedOkay(true);
                hostResource.setDozerMapper(DozerBeanMapperBuilder.create()
                        .withMappingFiles(mappingFile)
                        .build());
                host = new Host();
                host.setMaxConcurrentConnections(2);
                host.setHostStatus(HostStatus.ACTIVE);
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

                when(hrepo.getById(anyInt())).thenReturn(host);
                when(hostService.getById(anyInt())).thenReturn(host);
                when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

            }

            @Before
            public void standUpSubnet() {
                hsub = new Hostsubnet();
                hsubs = new Hostssubnet();
                ni = new NetInterface();
                cidr = new Cidr();
                ni.setName("name");

                hsub2 = new Hostsubnet();
                hsubs2 = new Hostssubnet();
                ni2 = new NetInterface();
                cidr2 = new Cidr();
                ni2.setName("name2");
                cidr2.setBlock("123.78.1.1/27");
                ni2.getCidrs().add(cidr2);
                hsub2.getNetInterfaces().add(ni2);
                hsubs2.getHostsubnets().add(hsub2);
            }

            @Test
            public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv6() throws Exception {
                 cidr.setBlock("fe80::200:f8ff:fe21:67cf/16");

                ni.getCidrs().add(cidr);
                hsub.getNetInterfaces().add(ni);
                hsubs.getHostsubnets().add(hsub);                
                Response resp = hostResource.putHostsSubnetMappings(hsubs);
                Assert.assertEquals(200, resp.getStatus());
                verify(reverseProxyLoadBalancerVTMService, times(1)).setSubnetMappings(any(), any());
            }

            @Test
            public void shouldReturn404WhenNoMatchingSubnetMappingsFound() throws Exception {
                netInterface.setName("name3");
                netInterface2.setName("name4");
                when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);

                Response response = hostResource.delHostsSubnetMappings(hsubs2);
                Assert.assertEquals(404, response.getStatus());
            }
            @Test
            public void shouldReturn200WhenMatchingSubnetMappingsFoundInList() throws Exception {
                netInterface.setName("name2");
                netInterface2.setName("name");
                hsub2.getNetInterfaces().add(ni);
                when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);

                Response response = hostResource.delHostsSubnetMappings(hsubs2);
                Assert.assertEquals(200, response.getStatus());
            }

            @Test
            public void shouldReturn404IfOneNetinterfaceDoesNotMatch() throws Exception {
                netInterface.setName("name2");
                netInterface2.setName("brokenName");
                hsub2.getNetInterfaces().add(ni);
                when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);

                Response response = hostResource.delHostsSubnetMappings(hsubs2);
                Assert.assertEquals(404, response.getStatus());
            }

            @Test
            public void shouldReturn200WhenMatchingSubnetMappingsFound() throws Exception {
                netInterface.setName("name2");
                when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);

                Response response = hostResource.delHostsSubnetMappings(hsubs2);
                Assert.assertEquals(200, response.getStatus());
            }
            @Test
            public void shouldReturn404WhenNoSubnetMappingsFound() throws Exception {
                when(reverseProxyLoadBalancerVTMService.getSubnetMappings(any(Host.class))).thenReturn(hostssubnet);

                Response response = hostResource.delHostsSubnetMappings(hsubs2);
                Assert.assertEquals(404, response.getStatus());
            }


            @Test
            public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv4() throws Exception {
                cidr.setBlock("192.168.0.1/24");

                ni.getCidrs().add(cidr);
                hsub.getNetInterfaces().add(ni);
                hsubs.getHostsubnets().add(hsub);                
                Response resp = hostResource.putHostsSubnetMappings(hsubs);
                Assert.assertEquals(200, resp.getStatus());
                verify(reverseProxyLoadBalancerVTMService, times(1)).setSubnetMappings(any(), any());
            }

            @Test
            public void shouldIpv6Pass() {
                Assert.assertTrue(IPAddressUtil.isIPv6LiteralAddress("fe80::200:f8ff:fe21:67cf"));
                Assert.assertTrue(IPAddressUtil.isIPv6LiteralAddress("3ffe:1900:4545:3:200:f8ff:fe21:67cf"));
                Assert.assertTrue(IPAddressUtil.isIPv6LiteralAddress("FE80:0000:0000:0000:0202:B3FF:FE1E:8329"));

            }
            @Test
            public void shouldIpv6Fail() {
                Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("fe80::200:f8ff:fe21:67cfffffffalfaldf"));
                Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("000fe80::200:f8ff:fe21:67cff"));
                Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("fe80::200:::f8ff:fe21:67cff"));
                Assert.assertFalse(IPAddressUtil.isIPv6LiteralAddress("fe80::200:f8ff:fe21:67cff/166666699909"));
            }
            @Test
            public void shouldIpv4Pass() {
                Assert.assertTrue(IPAddressUtil.isIPv4LiteralAddress("192.168.0.0"));
            }
            @Test
            public void shouldIpv4Fail() {
                Assert.assertFalse(IPAddressUtil.isIPv4LiteralAddress("192.168.1.1.111"));
            }
        }

    }
}

