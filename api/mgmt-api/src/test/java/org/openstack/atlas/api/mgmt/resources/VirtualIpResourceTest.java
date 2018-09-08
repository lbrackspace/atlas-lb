package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class VirtualIpResourceTest {

    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private VirtualIpsResource virtualIpsResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
            virtualIpsResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {            
            Response resp = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseFailed() throws Exception {
            operationResponse.setExecutedOkay(false);
            Response response = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        @Ignore
        public void shouldReturn200OnSuccessfulRequests() throws Exception {
            operationResponse.setExecutedOkay(true);
            Response resp = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }
    }
}