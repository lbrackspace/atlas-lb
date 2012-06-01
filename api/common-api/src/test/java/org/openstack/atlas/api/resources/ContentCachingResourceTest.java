package org.openstack.atlas.api.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.services.ContentCachingService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ContentCachingResourceTest {
    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";


    public static class WhenRetrievingResources {
        private ContentCachingResource contentCachingResource;
        private ContentCachingService contentCachingService;
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        private AsyncService asyncService;
        private Response response;


        @Before
        public void setUp() {

            contentCachingService = mock(ContentCachingService.class);
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            contentCachingResource = new ContentCachingResource();
            contentCachingResource.setAccountId(222222);
            contentCachingResource.setLoadBalancerId(1234);
            contentCachingResource.setContentCachingService(contentCachingService);
            contentCachingResource.setAsyncService(asyncService);

            contentCachingResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));
        }

        @Test
        public void shouldUpdateContentCaching() throws EntityNotFoundException, JMSException {
            when(contentCachingService.get(anyInt(), anyInt())).thenReturn(true);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONTENT_CACHING), Matchers.<LoadBalancer>any());

            ContentCaching cc = new ContentCaching();
            cc.setEnabled(true);

            response = contentCachingResource.updateContentCaching(cc);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduce400ResponseWhenUpdateThrowsBadRequestException() throws Exception {
            doThrow(new BadRequestException("Exception")).when(contentCachingService).update(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = contentCachingResource.updateContentCaching(new ContentCaching());
            junit.framework.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce400ResponseWhenUpdateThrowsImmutableEntityException() throws Exception {
            doThrow(new ImmutableEntityException("Exception")).when(contentCachingService).update(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = contentCachingResource.updateContentCaching(new ContentCaching());
            junit.framework.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce400ResponseWhenUpdateThrowsEntityNotFoundException() throws Exception {
            doThrow(new EntityNotFoundException("Exception")).when(contentCachingService).update(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = contentCachingResource.updateContentCaching(new ContentCaching());
            junit.framework.Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce400ResponseWhenUpdateThrowsUnprocessableEntityException() throws Exception {
            doThrow(new UnprocessableEntityException("Exception")).when(contentCachingService).update(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = contentCachingResource.updateContentCaching(new ContentCaching());
            junit.framework.Assert.assertEquals(400, response.getStatus());
        }
    }
}
