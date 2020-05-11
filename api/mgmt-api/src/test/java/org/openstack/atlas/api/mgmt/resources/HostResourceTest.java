package org.openstack.atlas.api.mgmt.resources;

import org.apache.commons.configuration2.Configuration;
import org.dozer.DozerBeanMapperBuilder;
import org.dozer.Mapper;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceVTMImpl;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.helpers.MgmtMapperBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
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
import org.openstack.atlas.service.domain.services.impl.HostServiceImpl;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import sun.net.util.IPAddressUtil;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
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
        public void shouldReturn200whenRetrievingHostsSubnetMappingsViaRest() throws Exception {

            Response resp = hostResource.retrieveHostsSubnetMappings();
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(1)).getSubnetMappings(host);
            verify(reverseProxyLoadBalancerService, times(0)).getSubnetMappings(host);
        }

        @Test
        public void shouldReturn200whenRetrievingHostsSubnetMappingsViaSOAP() throws Exception{
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");

            Response resp = hostResource.retrieveHostsSubnetMappings();
            Assert.assertEquals(200, resp.getStatus());
            verify(reverseProxyLoadBalancerVTMService, times(0)).getSubnetMappings(host);
            verify(reverseProxyLoadBalancerService, times(1)).getSubnetMappings(host);

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
            private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
            private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
            private HostService hostService;
            private Host host;
            private org.openstack.atlas.service.domain.pojos.Hostssubnet hostssubnet;
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
                hostResource.setHostService(hostService);
                hostResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
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

                hostssubnet = new org.openstack.atlas.service.domain.pojos.Hostssubnet();
                org.openstack.atlas.service.domain.pojos.Hostsubnet h1 = new org.openstack.atlas.service.domain.pojos.Hostsubnet();
                h1.setName("t1");
                hostssubnet.getHostsubnets().add(h1);

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
                verify(reverseProxyLoadBalancerService, times(0)).setSubnetMappings(any(), any());
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
                verify(reverseProxyLoadBalancerService, times(0)).setSubnetMappings(any(), any());
            }

            @Test
            public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv6Soap() throws Exception {
                when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("notREST");

                cidr.setBlock("fe80::200:f8ff:fe21:67cf/16");

                ni.getCidrs().add(cidr);
                hsub.getNetInterfaces().add(ni);
                hsubs.getHostsubnets().add(hsub);
                Response resp = hostResource.putHostsSubnetMappings(hsubs);
                Assert.assertEquals(200, resp.getStatus());
                verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
                verify(reverseProxyLoadBalancerService, times(1)).setSubnetMappings(any(), any());
            }
            @Test
            public void shouldreturn202whenESBisNormalWhenaddSubnetWIpv4Soap() throws Exception {
                when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("notREST");

                cidr.setBlock("192.168.0.1/24");

                ni.getCidrs().add(cidr);
                hsub.getNetInterfaces().add(ni);
                hsubs.getHostsubnets().add(hsub);
                Response resp = hostResource.putHostsSubnetMappings(hsubs);
                Assert.assertEquals(200, resp.getStatus());
                verify(reverseProxyLoadBalancerVTMService, times(0)).setSubnetMappings(any(), any());
                verify(reverseProxyLoadBalancerService, times(1)).setSubnetMappings(any(), any());
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

        public static class whenCreatingHostConfigBackup {
            private BackupsResource backupsResource;
            private Backup backup;
            private ManagementAsyncService asyncService;
            private OperationResponse operationResponse;
            private ReverseProxyLoadBalancerServiceVTMImpl reverseProxyLoadBalancerVTMServiceImpl;
            private ReverseProxyLoadBalancerServiceImpl reverseProxyLoadBalancerServiceImpl;
            private Host host;
            private org.openstack.atlas.service.domain.entities.Backup domainBackup;
            private HostServiceImpl hostService;
            private RestApiConfiguration config;

            @Before
            public void setUp() throws EntityNotFoundException, ImmutableEntityException {
                backup = new Backup();
                asyncService = mock(ManagementAsyncService.class);
                config = mock(RestApiConfiguration.class);
                domainBackup = mock(org.openstack.atlas.service.domain.entities.Backup.class);
                reverseProxyLoadBalancerVTMServiceImpl =  mock(ReverseProxyLoadBalancerServiceVTMImpl.class);
                reverseProxyLoadBalancerServiceImpl = mock(ReverseProxyLoadBalancerServiceImpl.class);
                hostService = mock(HostServiceImpl.class);
                host = mock(Host.class);
                backupsResource = new BackupsResource();
                backupsResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMServiceImpl);
                backupsResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerServiceImpl);
                backupsResource.setManagementAsyncService(asyncService);
                backupsResource.setHostService(hostService);
                backupsResource.setMockitoAuth(true);
                backupsResource.setConfiguration(config);
                operationResponse = new OperationResponse();
                when(hostService.getById(anyInt())).thenReturn(host);
                when(hostService.isActiveHost(host)).thenReturn(true);
                when(hostService.createBackup(any(Host.class), any(org.openstack.atlas.service.domain.entities.Backup.class))).thenReturn(domainBackup);
                backupsResource.setDozerMapper(DozerBeanMapperBuilder.create()
                        .withMappingFiles(mappingFile)
                        .build());

            }

            @Before
            public void standUp(){
                backup.setName("test");
            }

            @Test
            public void createBackupShouldReturn200ViaRest() throws Exception {
                when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
                Response response = backupsResource.createBackup(backup);
                verify(reverseProxyLoadBalancerVTMServiceImpl, times(1)).createHostBackup(host, backup.getName());
                Assert.assertEquals(200,response.getStatus());
            }

            @Test
            public void createBackupShouldReturn400() throws VTMRestClientException, RemoteException, MalformedURLException, RollBackException, VTMRestClientObjectNotFoundException, DecryptException {
                backup.setName(null);
                when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
                Response response = backupsResource.createBackup(backup);
                Assert.assertEquals(400,response.getStatus());
            }

            @Test
            public void createBackupShouldReturn200ViaSOAP() throws Exception {
                when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");
                Response response = backupsResource.createBackup(backup);
                verify(reverseProxyLoadBalancerServiceImpl, times(1)).createHostBackup(host, backup.getName());
                Assert.assertEquals(200,response.getStatus());
            }

            public static class whenDeletingHostConfigBackup {
                private BackupResource backupResource;
                private Backup backup;
                private org.openstack.atlas.service.domain.entities.Backup domainBackup;

                private ManagementAsyncService asyncService;
                private OperationResponse operationResponse;
                private ReverseProxyLoadBalancerServiceVTMImpl reverseProxyLoadBalancerVTMServiceImpl;
                private ReverseProxyLoadBalancerServiceImpl reverseProxyLoadBalancerServiceImpl;
                private Host host;
                private HostServiceImpl hostService;
                private RestApiConfiguration config;

                @Before
                public void setUp() throws EntityNotFoundException, ImmutableEntityException {
                    backup = new Backup();
                    asyncService = mock(ManagementAsyncService.class);
                    config = mock(RestApiConfiguration.class);
                    domainBackup = mock(org.openstack.atlas.service.domain.entities.Backup.class);
                    reverseProxyLoadBalancerVTMServiceImpl =  mock(ReverseProxyLoadBalancerServiceVTMImpl.class);
                    reverseProxyLoadBalancerServiceImpl = mock(ReverseProxyLoadBalancerServiceImpl.class);
                    hostService = mock(HostServiceImpl.class);
                    host = mock(Host.class);
                    backupResource = new BackupResource();
                    backupResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMServiceImpl);
                    backupResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerServiceImpl);
                    backupResource.setManagementAsyncService(asyncService);
                    backupResource.setHostService(hostService);
                    backupResource.setMockitoAuth(true);
                    backupResource.setConfiguration(config);
                    operationResponse = new OperationResponse();
                    when(hostService.getById(anyInt())).thenReturn(host);
                    when(hostService.isActiveHost(host)).thenReturn(true);
                    when(hostService.getBackupByHostIdAndBackupId(anyInt(), anyInt())).thenReturn(domainBackup);
                    backupResource.setDozerMapper(DozerBeanMapperBuilder.create()
                            .withMappingFiles(mappingFile)
                            .build());

                }

                @Before
                public void standUp(){
                    backup.setName("test");
                    domainBackup.setName("testDelete");
                }

                @Test
                public void deleteBackupShouldReturn200ViaRest() throws Exception {
                    when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
                    Response response = backupResource.deleteBackup();
                    verify(reverseProxyLoadBalancerVTMServiceImpl, times(1)).deleteHostBackup(host, domainBackup.getName());
                    Assert.assertEquals(200,response.getStatus());
                }

                @Test
                public void deleteBackupShouldReturn200ViaSOAP() throws Exception {
                    when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");
                    Response response = backupResource.deleteBackup();
                    verify(reverseProxyLoadBalancerServiceImpl, times(1)).deleteHostBackup(host, domainBackup.getName());
                    Assert.assertEquals(200,response.getStatus());
                }


            }

        }
    }
}

