package org.openstack.atlas.api.mgmt.resources;


import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.adapter.exceptions.RollBackException;
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
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(Enclosed.class)
public class BackupsResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class whenCreatingHostConfigBackup {
        private BackupsResource backupsResource;
        private Backup backup;
        private ManagementAsyncService asyncService;
        private OperationResponse operationResponse;
        private ReverseProxyLoadBalancerServiceVTMImpl reverseProxyLoadBalancerVTMServiceImpl;
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
            hostService = mock(HostServiceImpl.class);
            host = mock(Host.class);
            backupsResource = new BackupsResource();
            backupsResource.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMServiceImpl);
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

    }
}
