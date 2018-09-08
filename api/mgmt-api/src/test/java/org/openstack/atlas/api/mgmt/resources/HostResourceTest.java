package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostsubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.NetInterface;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import sun.net.util.IPAddressUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
@Ignore
public class HostResourceTest {
    public static class whenRetrievingHostDetails {
        private ManagementAsyncService asyncService;
        private HostResource hostResource;
        private OperationResponse operationResponse;

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
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
            hostResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturn200WhenEsbIsNormalDetails() throws Exception {
            operationResponse.setExecutedOkay(true);            
            Response resp = hostResource.retrieveHosts(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn200WhenEsbIsNormalSubNetMappings() throws Exception {
            operationResponse.setExecutedOkay(true);            
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
            
            when(hostResource.activateHost()).thenReturn(null);
            Response resp = hostResource.activateHost();
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldreturn202whenESBisNormalWhenInactivHost() throws Exception {            
            when(hostResource.disableEndPoint()).thenReturn(null);
            Response resp = hostResource.inactivateHost();
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldreturn202whenESBisNormalWhenDisablEndPointEnablEn() throws Exception {            
            when(hostResource.disableEndPoint()).thenReturn(null);
            Response resp = hostResource.disableEndPoint();
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldreturn202whenESBisNormalWhenEnableEndPointEnablEn() throws Exception {            
            when(hostResource.enableEndPoint()).thenReturn(null);
            Response resp = hostResource.enableEndPoint();
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldreturn202whenESBisNormalWhenDeleteHost() throws Exception {            
            when(hostResource.deleteHost()).thenReturn(null);
            Response resp = hostResource.deleteHost();
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
                List<String> mappingFiles = new ArrayList<String>();
                mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
                hostResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
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

