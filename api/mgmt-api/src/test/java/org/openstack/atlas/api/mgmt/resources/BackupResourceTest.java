package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceVTMImpl;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.impl.HostServiceImpl;
import javax.ws.rs.core.Response;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class BackupResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
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

    public static class whenRestoringBackup {
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
        public void restoreBackupShouldReturn200ViaRest() throws Exception {
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
            Response response = backupResource.restoreBackup();
            verify(reverseProxyLoadBalancerVTMServiceImpl, times(1)).restoreHostBackup(host, domainBackup.getName());
            Assert.assertEquals(200,response.getStatus());
        }

        @Test
        public void deleteBackupShouldReturn200ViaSOAP() throws Exception {
            when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("NOTREST");
            Response response = backupResource.restoreBackup();
            verify(reverseProxyLoadBalancerServiceImpl, times(1)).restoreHostBackup(host, domainBackup.getName());
            Assert.assertEquals(200,response.getStatus());
        }




    }




}
