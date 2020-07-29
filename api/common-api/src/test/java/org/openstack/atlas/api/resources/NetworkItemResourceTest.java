package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.*;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        LoadBalancer requestLb;

        @Before
        public void setUp() throws ImmutableEntityException, EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            networkItemResource = new NetworkItemResource();
            networkItemResource.setAsyncService(asyncService);
            networkItemResource.setAccessListService(accessListService);
            networkItemResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            networkItemResource.setId(25);
            networkItemResource.setAccountId(23323);
            networkItemResource.setLoadBalancerId(20);

            requestLb = new LoadBalancer();
            Set<AccessList> accessLists = new HashSet<AccessList>();
            AccessList aList = new AccessList();
            aList.setId(25);
            accessLists.add(aList);
            requestLb.setId(20);
            requestLb.setAccountId(23323);
            requestLb.setAccessLists(accessLists);

            when(accessListService.markForDeletionNetworkItem(any())).thenReturn(requestLb);
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            Response response = networkItemResource.deleteNetworkItem();
            verify(asyncService, times(1)).callAsyncLoadBalancingOperation(Operation.DELETE_ACCESS_LIST, requestLb);
            verify(accessListService, times(1)).markForDeletionNetworkItem(any());
            Assert.assertEquals(202, response.getStatus());
        }


        @Test
        public void shouldProduceInternalServerErrorWhenAsyncServiceThrowsRuntimeException() throws Exception {
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.eq(Operation.DELETE_ACCESS_LIST), ArgumentMatchers.<LoadBalancer>any());
            Response response = networkItemResource.deleteNetworkItem();
            verify(asyncService, times(1)).callAsyncLoadBalancingOperation(Operation.DELETE_ACCESS_LIST, requestLb);
            verify(accessListService, times(1)).markForDeletionNetworkItem(any());
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
