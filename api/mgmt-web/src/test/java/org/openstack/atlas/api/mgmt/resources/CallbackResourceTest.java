package org.openstack.atlas.api.mgmt.resources;

import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class CallbackResourceTest {
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
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-management-mapping.xml");
            callbackResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturnOK() throws Exception {
            ZeusEvent zeusEvent = new ZeusEvent();
            zeusEvent.setParamLine("paramLine");
            Response response = callbackResource.receiveCallbackMessage(zeusEvent);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test(expected = Exception.class)
        public void shouldReturn500() throws Exception {
            ZeusEvent zeusEvent = new ZeusEvent();
            zeusEvent.setParamLine("paramLine");
            doThrow(Exception.class).when(callbackResource.receiveCallbackMessage(zeusEvent));
        }
    }
}
