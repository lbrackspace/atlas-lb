package org.openstack.atlas.api.resources;

import org.junit.Assert;
import org.dozer.DozerBeanMapperBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.services.VirtualIpService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class VirtualIpResourceTest {
    static final String mappingFile = "loadbalancing-dozer-mapping.xml";
    public static class whenRemovingVirtualIpFromLoadBalancer {

        private VirtualIpResource vipResource;
        private VirtualIpService virtualIpService;
        private AsyncService asyncService;

        @Before
        public void setUp() {
            virtualIpService = mock(VirtualIpService.class);
            asyncService = mock(AsyncService.class);
            vipResource = new VirtualIpResource();
            vipResource.setVirtualIpService(virtualIpService);
            vipResource.setAsyncService(asyncService);
            vipResource.setLoadBalancerId(1234);
            vipResource.setAccountId(80);
            vipResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldReturnA202OnSuccess() throws Exception {
            doNothing().when(virtualIpService).prepareForVirtualIpDeletion(ArgumentMatchers.<LoadBalancer>any(), ArgumentMatchers.<Integer>any());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.DELETE_VIRTUAL_IPS), ArgumentMatchers.<LoadBalancer>any());
            Response response = vipResource.removeVirtualIpFromLoadBalancer();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotExceptionFoundIsThrown() throws Exception {
            doThrow(new EntityNotFoundException("Exception")).when(virtualIpService).prepareForVirtualIpDeletion(ArgumentMatchers.<LoadBalancer>any(), ArgumentMatchers.<Integer>any());
            Response response = vipResource.removeVirtualIpFromLoadBalancer();
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldReturnA422WhenImmutableEntityExceptionIsThrown() throws Exception {
            doThrow(new ImmutableEntityException("Exception")).when(virtualIpService).prepareForVirtualIpDeletion(ArgumentMatchers.<LoadBalancer>any(), ArgumentMatchers.<Integer>any());
            Response response = vipResource.removeVirtualIpFromLoadBalancer();
            Assert.assertEquals(422, response.getStatus());
        }

        @Test
        public void shouldReturnA422WhenUnprocessableEntityExceptionIsThrown() throws Exception {
            doThrow(new UnprocessableEntityException("Exception")).when(virtualIpService).prepareForVirtualIpDeletion(ArgumentMatchers.<LoadBalancer>any(), ArgumentMatchers.<Integer>any());
            Response response = vipResource.removeVirtualIpFromLoadBalancer();
            Assert.assertEquals(422, response.getStatus());
        }

        @Ignore
        @Test
        public void shouldReturn500OnJmsException() throws Exception {
            doNothing().when(virtualIpService).prepareForVirtualIpDeletion(ArgumentMatchers.<LoadBalancer>any(), ArgumentMatchers.<Integer>any());
            doThrow(new JMSException("Exception")).when(asyncService).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.DELETE_VIRTUAL_IPS), ArgumentMatchers.<LoadBalancer>any());
            Response response = vipResource.removeVirtualIpFromLoadBalancer();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn500OnRuntimeException() throws Exception {
            doThrow(new RuntimeException("Exception")).when(virtualIpService).prepareForVirtualIpDeletion(ArgumentMatchers.<LoadBalancer>any(), ArgumentMatchers.<Integer>any());
            Response response = vipResource.removeVirtualIpFromLoadBalancer();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldTruncate() throws Exception {
            String blah = "0123456789";
            String b = blah.substring(0,5);
            Assert.assertEquals(5, b.length());
            Assert.assertEquals("01234", b);
        }
    }
}
