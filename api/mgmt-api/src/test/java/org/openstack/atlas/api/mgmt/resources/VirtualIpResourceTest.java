package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VirtualIpResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private VirtualIpRepository virtualIpRepository;
        private VirtualIpsResource virtualIpsResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            virtualIpRepository = mock(VirtualIpRepository.class);
            virtualIpsResource.setVipRepository(virtualIpRepository);
            asyncService = mock(ManagementAsyncService.class);
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();
            virtualIpsResource.setDozerMapper( DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test(expected = Exception.class)
        public void shouldProduceExceptionWhenEntitymanagerFails() throws Exception {
            doThrow(Exception.class).when(virtualIpRepository.getEntityManager().createQuery((String) any()));
            Response response = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn200OnSuccessfulRequests() throws Exception {
            Response resp = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }
    }
}