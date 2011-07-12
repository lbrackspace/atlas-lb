package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
@Ignore
public class HealthMonitorResourceTest {

    public static class WhenCreatingAConnectHealthMonitor {
        private AsyncService es;
        private HealthMonitorResource hmr;
        private OperationResponse or;
        private HealthMonitor chm;
        private LoadBalancerRepository lbr;

        @Before
        public void setUp() throws EntityNotFoundException {
            es = mock(AsyncService.class);
            lbr = mock(LoadBalancerRepository.class);
            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            or = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            hmr.setDozerMapper(new DozerBeanMapper(mappingFiles));
            when(lbr.getByIdAndAccountId(anyInt(),anyInt())).thenReturn(new LoadBalancer());
        }

        @Before
        public void setUpValidConnectHealthMonitor() {
            chm = new HealthMonitor();
            chm.setType(HealthMonitorType.CONNECT);
            chm.setDelay(50);
            chm.setTimeout(60);
            chm.setAttemptsBeforeDeactivation(5);
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            or.setExecutedOkay(true);
//            when(es.callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject()).thenReturn(or);
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInANullObject() {
            Response response = hmr.updateHealthMonitor(null);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInAnInvalidaccessListObject() {
            Response response = hmr.updateHealthMonitor(new HealthMonitor());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            or.setExecutedOkay(false);
//            when(es.callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
//            when(es.callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(null);
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
//            when(es.callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenThrow(new Exception());
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenCreatingAHttpHealthMonitor {
        private AsyncService es;
        private HealthMonitorResource hmr;
        private OperationResponse or;
        private HealthMonitor hm;
        private LoadBalancerRepository lbr;

        @Before
        public void setUp() throws EntityNotFoundException {
            lbr = mock(LoadBalancerRepository.class);
            es = mock(AsyncService.class);
            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            or = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            hmr.setDozerMapper(new DozerBeanMapper(mappingFiles));
            when(lbr.getByIdAndAccountId(anyInt(),anyInt())).thenReturn(new LoadBalancer());
        }

        @Before
        public void setUpValidHttpHealthMonitor() {
            hm = new HealthMonitor();
            hm.setType(HealthMonitorType.HTTP);
            hm.setDelay(50);
            hm.setTimeout(60);
            hm.setAttemptsBeforeDeactivation(5);
            hm.setPath("/mnt/pfft");
            hm.setStatusRegex(".*");
            hm.setBodyRegex(".*");
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            or.setExecutedOkay(true);
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInANullObject() {
            Response response = hmr.updateHealthMonitor(null);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInAnInvalidaccessListObject() {
            Response response = hmr.updateHealthMonitor(new HealthMonitor());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            or.setExecutedOkay(false);
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
          //  when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(null);
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
          //  when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenThrow(new Exception());
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenUpdatingAConnectHealthMonitor {
        private AsyncService es;
        private HealthMonitorResource hmr;
        private OperationResponse or;
        private HealthMonitor chm;
        private LoadBalancerRepository lbr;

        @Before
        public void setUp() throws EntityNotFoundException {
            lbr = mock(LoadBalancerRepository.class);
            es = mock(AsyncService.class);
            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            or = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            hmr.setDozerMapper(new DozerBeanMapper(mappingFiles));
            when(lbr.getByIdAndAccountId(anyInt(),anyInt())).thenReturn(new LoadBalancer());
        }

        @Before
        public void setUpValidConnectHealthMonitor() {
            chm = new HealthMonitor();
            chm.setType(HealthMonitorType.CONNECT);
            chm.setDelay(50);
            chm.setTimeout(60);
            chm.setAttemptsBeforeDeactivation(5);
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            or.setExecutedOkay(true);
          //  when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInANullObject() {
            Response response = hmr.updateHealthMonitor(null);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInAnInvalidaccessListObject() {
            Response response = hmr.updateHealthMonitor(new HealthMonitor());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            or.setExecutedOkay(false);
          //  when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(null);
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenThrow(new Exception());
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenUpdatingAHttpHealthMonitor {
        private AsyncService es;
        private HealthMonitorResource hmr;
        private OperationResponse or;
        private HealthMonitor hm;
        private LoadBalancerRepository lbr;

        @Before
        public void setUp() throws EntityNotFoundException {
            lbr = mock(LoadBalancerRepository.class);
            es = mock(AsyncService.class);
            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            or = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            hmr.setDozerMapper(new DozerBeanMapper(mappingFiles));
            when(lbr.getByIdAndAccountId(anyInt(),anyInt())).thenReturn(new LoadBalancer());
        }

        @Before
        public void setUpValidHttpHealthMonitor() {
            hm = new HealthMonitor();
            hm.setType(HealthMonitorType.HTTP);
            hm.setDelay(50);
            hm.setTimeout(60);
            hm.setAttemptsBeforeDeactivation(5);
            hm.setPath("/mnt/pfft");
            hm.setStatusRegex(".*");
            hm.setBodyRegex(".*");
        }
  /*
        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            or.setExecutedOkay(true);
            when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInANullObject() {
            Response response = hmr.updateHealthMonitor(null);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInAnInvalidaccessListObject() {
            Response response = hmr.updateHealthMonitor(new HealthMonitor());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            or.setExecutedOkay(false);
            when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
            when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(null);
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
            when(es.callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject());).thenThrow(new Exception());
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }  */
    }

    public static class WhenDeletingAHealthMonitor {
        private AsyncService es;
        private HealthMonitorResource hmr;
        private OperationResponse or;
        private LoadBalancerRepository lbr;

        @Before
        public void setUp() throws EntityNotFoundException {
            lbr = mock(LoadBalancerRepository.class);
            es = mock(AsyncService.class);
            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            or = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            hmr.setDozerMapper(new DozerBeanMapper(mappingFiles));
            when(lbr.getByIdAndAccountId(anyInt(),anyInt())).thenReturn(new LoadBalancer());
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
         //   or.setExecutedOkay(true);
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.DELETE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.deleteHealthMonitor();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            or.setExecutedOkay(false);
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.DELETE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
            Response response = hmr.deleteHealthMonitor();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.DELETE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(null);
            Response response = hmr.deleteHealthMonitor();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
         //   when(es.callLoadBalancingOperation(Matchers.eq(Operation.DELETE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenThrow(new Exception());
            Response response = hmr.deleteHealthMonitor();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
