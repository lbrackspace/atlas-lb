package org.openstack.atlas.api.resources;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.operations.OperationResponse;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
@Ignore
public class SslTerminationResourceTest {

    public static class createSsl {

        private AsyncService esbService;
        private SslTerminationResource sslTermResource;
        private OperationResponse operationResponse;
        private SslTermination sslTermination;

        @Before
        public void setUp() {
            sslTermResource = new SslTerminationResource();
            esbService = mock(AsyncService.class);
            sslTermResource.setAsyncService(esbService);
            sslTermResource.setId(12);
            sslTermResource.setAccountId(31337);
            sslTermResource.setLoadBalancerId(32);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);            
            Response resp = sslTermResource.createSsl(null);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn200WhenEsbIsNormal() throws Exception {
            Response resp = sslTermResource.createSsl(null);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {
            Response resp = sslTermResource.createSsl(null);
            Assert.assertEquals(500, resp.getStatus());
        }
    }

}
