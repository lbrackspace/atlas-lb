package org.openstack.atlas.api.resources;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@Ignore
public class NetworkItemResourceTest {

    public static class WhenDeletingAccessListItem {
        private AsyncService esbService;
        private NetworkItemResource networkItemResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            esbService = mock(AsyncService.class);
            networkItemResource = new NetworkItemResource();
            networkItemResource.setAsyncService(esbService);
            operationResponse = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            networkItemResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            operationResponse.setExecutedOkay(true);           
            Response response = networkItemResource.deleteNetworkItem();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            operationResponse.setExecutedOkay(false);            
            Response response = networkItemResource.deleteNetworkItem();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {            
            Response response = networkItemResource.deleteNetworkItem();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
            doThrow(new Exception()).when(esbService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.DELETE_ACCESS_LIST_ITEM), Matchers.<LoadBalancer>any());
            Response response = networkItemResource.deleteNetworkItem();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
