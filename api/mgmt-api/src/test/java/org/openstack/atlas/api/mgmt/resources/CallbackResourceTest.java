package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.dozer.DozerBeanMapperBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.CallbackService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class CallbackResourceTest {
    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";
    public static class whenRetrievingAccountDetails {
        private ManagementAsyncService asyncService;
        private CallbackResource callbackResource;
        private CallbackService callbackService;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            callbackResource = new CallbackResource();
            callbackResource.setMockitoAuth(true);

            asyncService = mock(ManagementAsyncService.class);
            callbackResource.setManagementAsyncService(asyncService);
            callbackService = mock(CallbackService.class);
            callbackResource.setCallbackService(callbackService);

            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            callbackResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldReturnOK() throws Exception {
            ZeusEvent zeusEvent = new ZeusEvent();
            zeusEvent.setParamLine("paramLine");
            zeusEvent.setCallbackHost("hostname");
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test()
        public void shouldReturn500() throws Exception {
            ZeusEvent zeusEvent = new ZeusEvent();
            zeusEvent.setParamLine("paramLine");
            zeusEvent.setCallbackHost("hostname");
            doThrow(Exception.class).when(callbackService).handleZeusEvent(any(), any());
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
