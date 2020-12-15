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
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.util.Constants;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ErrorPageResourceTest {

    public static class WhenRetrievingAnErrorPage {
        private ErrorpageResource errorpageResource;
        private LoadBalancerService loadBalancerService;
        private Errorpage errorpage;
        private Response response;
        private String errorContent = null;

        @Before
        public void setUp() throws BadRequestException, ImmutableEntityException, UnprocessableEntityException, EntityNotFoundException {
            loadBalancerService = mock(LoadBalancerService.class);
            errorpage = mock(Errorpage.class);
            errorpageResource = new ErrorpageResource();
            errorpageResource.setLoadBalancerId(1);
            errorpageResource.setAccountId(1234);
            loadBalancerService.setErrorPage(errorpageResource.getLoadBalancerId(), errorpageResource.getAccountId(),errorpage.getContent());
            errorpageResource.setLoadBalancerService(loadBalancerService);
        }

        @Test
        public void whenErrorContentIsNotNull() throws Exception{
            String errorContent = "errorContent";
            when(loadBalancerService.getErrorPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(errorContent);
            errorpage.setContent(errorContent);
            response = errorpageResource.retrieveErrorpage();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void whenErrorContentIsNull() throws Exception{
            String defaultErrorContent = "defaultErrorContent";
            when(loadBalancerService.getErrorPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(null);
            when(loadBalancerService.getDefaultErrorPage()).thenReturn(defaultErrorContent);
            errorpage.setContent(defaultErrorContent);
            response = errorpageResource.retrieveErrorpage();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception{
            String defaultErrorContent = "defaultErrorContent";
            doThrow(EntityNotFoundException.class).when(loadBalancerService).getErrorPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            errorpage.setContent(defaultErrorContent);
            response = errorpageResource.retrieveErrorpage();
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldReturnA500WhenOperationFails() throws Exception{
            String defaultErrorContent = "defaultErrorContent";
            doThrow(Exception.class).when(loadBalancerService).getErrorPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            errorpage.setContent(defaultErrorContent);
            response = errorpageResource.retrieveErrorpage();
            Assert.assertEquals(500, response.getStatus());
        }
        }

    public static class WhenUpdatingErrorPage {
        private LoadBalancerService loadBalancerService;
        private AsyncService asyncService;
        private ErrorpageResource errorpageResource;
        private MessageDataContainer dataContainer;
        private Response response;
        private Errorpage errorpage;
        private int loadBalancerId;
        private int accountId;

        @Before
        public void standUp() {
            loadBalancerService = mock(LoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            errorpageResource = new ErrorpageResource();
            dataContainer = new MessageDataContainer();
            errorpage = new Errorpage();
            errorpageResource.setLoadBalancerId(1);
            errorpageResource.setAccountId(1234);
            loadBalancerId = errorpageResource.getLoadBalancerId();
            accountId = errorpageResource.getAccountId();
            errorpageResource.setLoadBalancerService(loadBalancerService);
            errorpageResource.setAsyncService(asyncService);
        }

        @Test
        public void shouldFailIfContentIsNull() throws Exception {
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Operation.UPDATE_ERRORFILE, dataContainer);
            response = errorpageResource.setErrorPage(errorpage);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldFailIfContentLengthIsExceedingTheLimit() throws Exception {
            StringBuilder sbContent = new StringBuilder();
            sbContent.setLength(1024*64+1);
            String content = new String(sbContent);
            errorpage.setContent(content);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Operation.UPDATE_ERRORFILE, dataContainer);
            response = errorpageResource.setErrorPage(errorpage);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn202IfContentLengthIslessThanTheLimit() throws Exception {
            StringBuilder sbContent = new StringBuilder();
            sbContent.setLength(1024);
            String content = new String(sbContent);
            errorpage.setContent(content);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Operation.UPDATE_ERRORFILE, dataContainer);
            response = errorpageResource.setErrorPage(errorpage);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldReturn500IfExceptionIsThrown() throws Exception {
            StringBuilder sbContent = new StringBuilder();
            sbContent.setLength(1024);
            String content = new String(sbContent);
            errorpage.setContent(content);
            doThrow(Exception.class).when(loadBalancerService).setErrorPage(loadBalancerId, accountId, content);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Operation.UPDATE_ERRORFILE, dataContainer);
            response = errorpageResource.setErrorPage(errorpage);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn500IfExceptionIsThrownOnAsyncService() throws Exception {
            StringBuilder sbContent = new StringBuilder();
            sbContent.setLength(1024);
            String content = new String(sbContent);
            errorpage.setContent(content);
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.UPDATE_ERRORFILE), ArgumentMatchers.<MessageDataContainer>any());
            response = errorpageResource.setErrorPage(errorpage);
            Assert.assertEquals(500, response.getStatus());
        }


    }

    public static class WhenDeletingAnErrorPage {
        private LoadBalancerService loadBalancerService;
        private ErrorpageResource errorpageResource;
        private Response response;
        private MessageDataContainer container;
        private AsyncService asyncService;
        private int loadBalancerId;
        private int accountId;

        @Before
        public void standUp() {
            loadBalancerService = mock(LoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            errorpageResource = new ErrorpageResource();
            container = new MessageDataContainer();
            errorpageResource.setLoadBalancerId(1);
            errorpageResource.setAccountId(1234);
            loadBalancerId = errorpageResource.getLoadBalancerId();
            accountId = errorpageResource.getAccountId();
            errorpageResource.setLoadBalancerService(loadBalancerService);
            errorpageResource.setAsyncService(asyncService);
        }

        @Test
        public void shouldReturnA202OnSuccess() throws Exception {
            container.setLoadBalancerId(errorpageResource.getLoadBalancerId());
            container.setAccountId(errorpageResource.getAccountId());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.DELETE_ERRORFILE), ArgumentMatchers.<MessageDataContainer>any());
            response = errorpageResource.deleteErrorpage();
            Assert.assertEquals((String) response.getEntity(), 202, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception {
            doThrow(EntityNotFoundException.class).when(loadBalancerService).removeErrorPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = errorpageResource.deleteErrorpage();
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldReturnA422WhenDeletionFails() throws Exception {
            doThrow(UnprocessableEntityException.class).when(loadBalancerService).removeErrorPage(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = errorpageResource.deleteErrorpage();
            Assert.assertEquals(422, response.getStatus());
        }

        @Test
        public void shouldReturn500OnJmsException() throws Exception {
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.DELETE_ERRORFILE), ArgumentMatchers.<MessageDataContainer>any());
            response = errorpageResource.deleteErrorpage();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
