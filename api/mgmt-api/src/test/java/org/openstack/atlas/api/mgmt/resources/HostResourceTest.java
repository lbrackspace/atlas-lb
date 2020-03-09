package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.HostService;
import sun.net.util.IPAddressUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@Ignore
public class HostResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class whenRetrievingHostDetails {
        private ManagementAsyncService asyncService;
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
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
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            reverseProxyLoadBalancerVTMService = mock(ReverseProxyLoadBalancerVTMService.class);
            hostService = mock(HostService.class);
            hostResource.setManagementAsyncService(asyncService);
            hostResource.setId(1);
            hostResource.setHostRepository(hrepo);
            hostResource.setHostService(hostService);
            hostResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
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
            verify(reverseProxyLoadBalancerService, times(0)).getTotalCurrentConnectionsForHost(host);
            verify(reverseProxyLoadBalancerVTMService, times(1)).getTotalCurrentConnectionsForHost(host);
        }

        @Test
        public void shouldReturn200WhenRetrievingHostViaSoap() throws Exception {
            when(reverseProxyLoadBalancerService.getTotalCurrentConnectionsForHost(host)).thenReturn(14);
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");

            Response resp = hostResource.getHost();
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getTotalCurrentConnectionsForHost(host);
            verify(reverseProxyLoadBalancerService, times(1)).getTotalCurrentConnectionsForHost(host);
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

            @Before
            public void setUp() {
                hostResource = new HostResource();
                hostResource.setMockitoAuth(true);
                HostRepository hrepo = mock(HostRepository.class);
                asyncService = mock(ManagementAsyncService.class);
                hostResource.setManagementAsyncService(asyncService);
                hostResource.setId(1);
                hostResource.setHostRepository(hrepo);
                operationResponse = new OperationResponse();
                operationResponse.setExecutedOkay(true);
                hostResource.setDozerMapper(DozerBeanMapperBuilder.create()
                        .withMappingFiles(mappingFile)
                        .build());
            }
            @Before
            public void standUpSubnet() {
                hsub = new Hostsubnet();
                hsubs = new Hostssubnet();
                ni = new NetInterface();
                cidr = new Cidr();
                ni.setName("name");
            }

            @Test
            public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv6() throws Exception {
                 cidr.setBlock("fe80::200:f8ff:fe21:67cf/16");

                ni.getCidrs().add(cidr);
                hsub.getNetInterfaces().add(ni);
                hsubs.getHostsubnets().add(hsub);                
                Response resp = hostResource.putHostsSubnetMappings(hsubs);
                Assert.assertEquals(200, resp.getStatus());
            }
            @Test
            public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv4() throws Exception {
                cidr.setBlock("192.168.0.1/24");

                ni.getCidrs().add(cidr);
                hsub.getNetInterfaces().add(ni);
                hsubs.getHostsubnets().add(hsub);                
                Response resp = hostResource.putHostsSubnetMappings(hsubs);
                Assert.assertEquals(200, resp.getStatus());
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

