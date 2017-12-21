package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.*;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.integration.AsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.AccessListService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class NetworkItemResourceTest {
    static final String mappingFile = "loadbalancing-dozer-mapping.xml";
    public static class WhenDeletingAccessListItem {
        @Mock
        AsyncService asyncService;
        @Mock
        AccessListService accessListService;

        @InjectMocks
        NetworkItemResource networkItemResource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            networkItemResource = new NetworkItemResource();
            networkItemResource.setAsyncService(asyncService);
            networkItemResource.setAccessListService(accessListService);
            networkItemResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            Response response = networkItemResource.deleteNetworkItem();
            Assert.assertEquals(202, response.getStatus());
        }


        @Test
        public void shouldProduceInternalServerErrorWhenAsyncServiceThrowsRuntimeException() throws Exception {
            doThrow(new JMSException("fail")).when(asyncService).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.eq(Operation.APPEND_TO_ACCESS_LIST), ArgumentMatchers.<LoadBalancer>any());
            Response response = networkItemResource.deleteNetworkItem();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
