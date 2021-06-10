package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.dozer.DozerBeanMapperBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.LoadBalancerFault;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.CallbackService;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class CallbackResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class whenRetrievingAccountDetails {
        private ManagementAsyncService asyncService;
        private CallbackResource callbackResource;
        private CallbackService callbackService;
        private OperationResponse operationResponse;
        ZeusEvent zeusEvent = new ZeusEvent();
        @Mock
        RequestStateContainer requestStateContainer;
        List<String> hdr;
        @Mock
        HttpHeaders httpHeaders;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            callbackResource = new CallbackResource();
            callbackResource.setMockitoAuth(true);
            hdr = new ArrayList<>();
            asyncService = mock(ManagementAsyncService.class);
            callbackResource.setManagementAsyncService(asyncService);
            callbackResource.setRequestStateContainer(requestStateContainer);
            callbackService = mock(CallbackService.class);
            callbackResource.setCallbackService(callbackService);
            zeusEvent.setParamLine("paramLine");
            zeusEvent.setCallbackHost("hostname");
            hdr.add("test");
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            callbackResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(requestStateContainer.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader(anyString())).thenReturn(hdr);
        }

        @Test
        public void shouldReturnOK() throws BadRequestException {
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(200, response.getStatus());
            verify(callbackService, times(1)).handleZeusEvent(any(), eq("test"));
        }

        @Test
        public void shouldReturn500() throws Exception {
            doThrow(Exception.class).when(callbackService).handleZeusEvent(any(), any());
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(500, response.getStatus());
            Assert.assertEquals("An unknown exception has occurred. Please contact support.", ((LoadBalancerFault) response.getEntity()).getMessage());
        }

        @Test
        public void shouldReturn400WithNullParamLine() throws Exception {
            zeusEvent.setParamLine(null);
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Validation fault", ((BadRequest) response.getEntity()).getMessage());
        }

        @Test
        public void shouldReturn400WithNullCallBackHost() throws Exception {
            zeusEvent.setCallbackHost(null);
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Validation fault", ((BadRequest) response.getEntity()).getMessage());
        }

        @Test
        public void shouldReturn200WithNullHeader() {
            hdr.clear();
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(200, response.getStatus());

        }

        @Test
        public void shouldReturn400WithNullZuesEvent() {
            Response response = callbackResource.receiveCallbackMessage(null);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Validation fault", ((BadRequest) response.getEntity()).getMessage());
        }

    }
}
