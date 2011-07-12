package org.openstack.atlas.api.resources;

import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.service.domain.services.ConnectionThrottleService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ConnectionThrottleResourceTest {

    @Ignore
    public static class WhenCreatingThrottles {

        private LoadBalancerRepository lbRepo;
        private AsyncService asyncService;
        private ConnectionThrottleResource resource;
        private OperationResponse operationResponse;
        private ConnectionThrottle ct;
        private Response resp;

        @Before
        public void setUp() {
            lbRepo = mock(LoadBalancerRepository.class);
            asyncService = mock(AsyncService.class);
            resource = new ConnectionThrottleResource();
            resource.setAccountId(31337);
            resource.setLoadBalancerId(32);
            resource.setAsyncService(asyncService);
            resource.setLbRepository(lbRepo);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            ct = new ConnectionThrottle();
            ct.setMaxConnectionRate(1);
            ct.setMaxConnections(2);
            ct.setMinConnections(3);
            ct.setRateInterval(4);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            resource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldreturn202whenESBisNormal() throws Exception {            
            when(lbRepo.getByIdAndAccountId(1,1)).thenReturn(null);
            resp = resource.updateConnectionThrottle(ct);
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldreturn500OnEsbException() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);           
            resp = resource.updateConnectionThrottle(ct);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldreturn200whenESBreturnsNull() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);            
            resp = resource.updateConnectionThrottle(ct);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);
            operationResponse.setExecutedOkay(false);            
            resp = resource.updateConnectionThrottle(ct);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500NonExistentEntryHit() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null).thenThrow(new EntityNotFoundException(""));
        }
    }

    @Ignore
    public static class WhenGettingThrottles {

        private AsyncService asyncService;
        private ConnectionThrottleResource resource;
        private OperationResponse operationResponse;
        private Response resp;

        @Before
        public void setUp() {
            asyncService = mock(AsyncService.class);
            resource = new ConnectionThrottleResource();
            resource.setAccountId(31337);
            resource.setLoadBalancerId(32);
            resource.setAsyncService(asyncService);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Test
        public void shouldreturn200whenESBisNormal() {
            resp = resource.retrieveConnectionThrottle(10);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldreturn500OnEsbException() {            
            resp = resource.retrieveConnectionThrottle(10);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldreturn200whenESBreturnsNull() {
            resp = resource.retrieveConnectionThrottle(10);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() {
            operationResponse.setExecutedOkay(false);            
            resp = resource.retrieveConnectionThrottle(10);
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    @Ignore
    public static class WhenDeleteingThrottles {

        private LoadBalancerRepository lbRepo;
        private AsyncService asyncService;
        private ConnectionThrottleResource resource;
        private OperationResponse operationResponse;
        private Response resp;

        @Before
        public void setUp() {

            lbRepo = mock(LoadBalancerRepository.class);
            asyncService = mock(AsyncService.class);
            resource = new ConnectionThrottleResource();
            resource.setAccountId(31337);
            resource.setLoadBalancerId(32);
            resource.setAsyncService(asyncService);
            resource.setLbRepository(lbRepo);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            resource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shoudReturn500whenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);
            resp = resource.disableConnectionThrottle();
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenEsbException() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);
            resp = resource.disableConnectionThrottle();
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn202WhenNormal() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);            
            resp = resource.disableConnectionThrottle();
            Assert.assertEquals(202, resp.getStatus());
        }

        @Test
        public void shouldReturn500onEsbReturningNull() throws Exception {
            when(lbRepo.getByIdAndAccountId(anyInt(), anyInt())).thenReturn(null);            
            resp = resource.disableConnectionThrottle();
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    public static class WhenUpdatingThrottles {

        private LoadBalancerService lbService;
        private AsyncService asyncService;
        private ConnectionThrottleResource resource;
        private ConnectionThrottleService connectionThrottleService;
        private ConnectionThrottle cl;
        private Response resp;

        @Before
        public void setUp() {
            lbService = mock(LoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            connectionThrottleService = mock(ConnectionThrottleService.class);
            resource = new ConnectionThrottleResource();
            resource.setAccountId(31337);
            resource.setLoadBalancerId(32);
            resource.setAsyncService(asyncService);
            resource.setLoadBalancerService(lbService);
            resource.setConnectionThrottleService(connectionThrottleService);
            cl = new ConnectionThrottle();
            cl.setMaxConnectionRate(1);
            cl.setMaxConnections(2);
            cl.setMinConnections(3);
            cl.setRateInterval(4);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            resource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturn202onSuccess() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            doNothing().when(connectionThrottleService).update(Matchers.<LoadBalancer>any());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONNECTION_THROTTLE), Matchers.<LoadBalancer>any());
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals((String) resp.getEntity(), 202, resp.getStatus());
        }

        @Test
        public void shouldReturn400onValidationFailure() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            cl = new ConnectionThrottle();
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturn500onJmsException() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            doThrow(new JMSException("Exception")).when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONNECTION_THROTTLE), Matchers.<LoadBalancer>any());
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500onRuntimeException() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            doThrow(new RuntimeException("Exception")).when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONNECTION_THROTTLE), Matchers.<LoadBalancer>any());
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn404WhenUpdateThrowsEntityNotFoundException() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            doThrow(new EntityNotFoundException("Exception")).when(connectionThrottleService).update(Matchers.<LoadBalancer>any());
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals(404, resp.getStatus());
        }

        @Test
        public void shouldReturn422WhenUpdateThrowsUnprocessableEntityException() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            doThrow(new UnprocessableEntityException("Exception")).when(connectionThrottleService).update(Matchers.<LoadBalancer>any());
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals(422, resp.getStatus());
        }

        @Test
        public void shouldReturn400WhenUpdateThrowsBadRequestException() throws Exception {
            when(lbService.get(anyInt())).thenReturn(null);
            doThrow(new BadRequestException("Exception")).when(connectionThrottleService).update(Matchers.<LoadBalancer>any());
            resp = resource.updateConnectionThrottle(cl);
            Assert.assertEquals(400, resp.getStatus());
        }
    }
}
