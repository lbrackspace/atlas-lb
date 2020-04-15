package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VirtualIpsResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class WhenGettingALoadBalancerVirtualIps {

        private ManagementAsyncService asyncService;
        private VirtualIpsResource virtualIpsResource;
        private OperationResponse operationResponse;
        private VirtualIpRepository vpRepository;
        private List<org.openstack.atlas.service.domain.entities.VirtualIp> vips;

        @Before
        public void setUp() {
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            asyncService = mock(ManagementAsyncService.class);
            vpRepository = mock(VirtualIpRepository.class);
            virtualIpsResource.setVipRepository(vpRepository);
            
            vips = new ArrayList<org.openstack.atlas.service.domain.entities.VirtualIp>();

            virtualIpsResource.setManagementAsyncService(asyncService);
            virtualIpsResource.setId(12);
            operationResponse = new OperationResponse();

            virtualIpsResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }


        @Test
        public void shouldProduceInternalServerErrorWhenNullPointerFoundOrSomething() throws NullPointerException {
            doThrow(NullPointerException.class).when(vpRepository).getAll(any(), any());
            Response response = virtualIpsResource.retrieveAllVirtualIps(0,0);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test        
        public void shouldReturn200OnSuccessfulRequests() throws Exception {
            when(vpRepository.getAll(any(),any())).thenReturn(vips);
            Response resp = virtualIpsResource.retrieveAllVirtualIps(0, 0);
            Assert.assertEquals(200, resp.getStatus());
        }
    }
}
