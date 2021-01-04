package org.openstack.atlas.api.resources;

import net.spy.memcached.MemcachedClient;
import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.*;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.OutOfVipsException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ThrowResourceTest {

    public static class WhenRetrievingResources {
        private ThrowResource throwResource;
        private Response response;

        @Before
        public void setUp() {
            throwResource = new ThrowResource();
        }

        @Test
        public void shouldThrowBadRequestAndReturn400StatusCode() {
            response = throwResource.getBadRequest();
            Assert.assertEquals(400,response.getStatus());
            BadRequest badRequest = (BadRequest)response.getEntity();
            Assert.assertEquals("Validation fault",badRequest.getMessage());
        }

        @Test
        public void shouldThrowLoadBalancerFaultAndReturn500StatusCode() {
            response = throwResource.getLoadBalancerFault();
            Assert.assertEquals(500,response.getStatus());
            LoadBalancerFault loadBalancerFault = (LoadBalancerFault) response.getEntity();
            Assert.assertEquals("An unknown exception has occurred. Please contact support.",loadBalancerFault.getMessage());
        }

        @Test
        public void shouldThrowItemNotFoundAndReturn404StatusCode() {
            response = throwResource.getItemNotFound();
            Assert.assertEquals(404,response.getStatus());
            ItemNotFound itemNotFound = (ItemNotFound) response.getEntity();
            Assert.assertEquals("Object not Found",itemNotFound.getMessage());
        }

        @Test
        public void shouldThrowOverLimitAndReturn413StatusCode() {
            response = throwResource.getOverLimit();
            Assert.assertEquals(413,response.getStatus());
            OverLimit overlimit = (OverLimit) response.getEntity();
            Assert.assertEquals("Your account is currently over the limit so your request could not be processed.",overlimit.getMessage());
        }

        @Test
        public void shouldThrowUnauthorizedAndReturn404StatusCode() {
            response = throwResource.getUnauthorized();
            Assert.assertEquals(404,response.getStatus());
            Unauthorized unauthorized = (Unauthorized) response.getEntity();
            Assert.assertEquals("You are not authorized to execute this operation.",unauthorized.getMessage());
        }

        @Test
        public void shouldThrowOutOfVirtualIPsAndReturn500StatusCode() {
            response = throwResource.getOutOfVirtualIps();
            Assert.assertEquals(500,response.getStatus());
            OutOfVirtualIps outOfVipsException = (OutOfVirtualIps) response.getEntity();
            Assert.assertEquals("Out of virtual IPs. Please contact support so they can allocate more virtual IPs.",outOfVipsException.getMessage());
        }

        @Test
        public void shouldThrowImmutableEntityAndReturn422StatusCode() {
            response = throwResource.getImmutableEntity();
            Assert.assertEquals(422,response.getStatus());
            ImmutableEntity immutableEntity = (ImmutableEntity) response.getEntity();
            Assert.assertEquals("The object at the specified URI is immutable and can not be overwritten.",immutableEntity.getMessage());
        }

        @Test
        public void shouldThrowUnprocessableEntityAndReturn422StatusCode() {
            response = throwResource.getUnprocessableEntity();
            Assert.assertEquals(422,response.getStatus());
            UnProcessableEntity unProcessableEntity = (UnProcessableEntity) response.getEntity();
            Assert.assertEquals("The Object at the specified URI is unprocessable.",unProcessableEntity.getMessage());
        }

        @Test
        public void shouldThrowServiceUnavailableAndReturn500StatusCode() {
            response = throwResource.getServiceUnavailable();
            Assert.assertEquals(500,response.getStatus());
            ServiceUnavailable serviceUnavailable = (ServiceUnavailable) response.getEntity();
            Assert.assertEquals("The LoadBalancing API is currently not available",serviceUnavailable.getMessage());
        }
    }
}
