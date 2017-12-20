package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.HealthMonitorService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class HealthMonitorResourceTest {
    static final String mappingFile = "loadbalancing-dozer-mapping.xml";
    public static class WhenCreatingAConnectHealthMonitor {
        @Mock
        AsyncService es;
        @Mock
        LoadBalancerRepository lbr;
        @Mock
        LoadBalancerService lbs;
        @Mock
        HealthMonitorService hms;

        private HealthMonitor chm;

        @InjectMocks
        HealthMonitorResource hmr;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);

            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            hmr.setLoadBalancerService(lbs);
            hmr.setHealthMonitorService(hms);
            hmr.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(lbr.getByIdAndAccountId(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(new LoadBalancer());
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
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
            doThrow(new JMSException("fail")).when(es).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.eq(Operation.UPDATE_HEALTH_MONITOR), ArgumentMatchers.<LoadBalancer>any());
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenCreatingAHttpHealthMonitor {
        @Mock
        AsyncService es;
        @Mock
        LoadBalancerRepository lbr;
        @Mock
        LoadBalancerService lbs;
        @Mock
        HealthMonitorService hms;

        private HealthMonitor hm;

        @InjectMocks
        HealthMonitorResource hmr;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);

            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            hmr.setLoadBalancerService(lbs);
            hmr.setHealthMonitorService(hms);
            hmr.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(lbr.getByIdAndAccountId(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(new LoadBalancer());
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
        public void shouldProduceInternalServerErrorWhenAsyncServiceThrowsRuntimeException() throws Exception {
            doThrow(new JMSException("fail")).when(es).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.eq(Operation.UPDATE_HEALTH_MONITOR), ArgumentMatchers.<LoadBalancer>any());
            Response response = hmr.updateHealthMonitor(hm);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenUpdatingAConnectHealthMonitor {
        @Mock
        AsyncService es;
        @Mock
        LoadBalancerRepository lbr;
        @Mock
        LoadBalancerService lbs;
        @Mock
        HealthMonitorService hms;

        private HealthMonitor chm;

        @InjectMocks
        HealthMonitorResource hmr;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);

            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLbRepository(lbr);
            hmr.setLoadBalancerService(lbs);
            hmr.setHealthMonitorService(hms);
            hmr.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(lbr.getByIdAndAccountId(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(new LoadBalancer());
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
        public void shouldProduceInternalServerErrorWhenAsyncServiceThrowsRuntimeException() throws Exception {
            doThrow(new JMSException("fail")).when(es).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.UPDATE_HEALTH_MONITOR), ArgumentMatchers.<LoadBalancer>any());
            Response response = hmr.updateHealthMonitor(chm);
            Assert.assertEquals(500, response.getStatus());
        }
    }

//    public static class WhenUpdatingAHttpHealthMonitor {
//        private AsyncService es;
//        private HealthMonitorResource hmr;
//        private OperationResponse or;
//        private HealthMonitor hm;
//        private LoadBalancerRepository lbr;
//
//        @Before
//        public void setUp() throws EntityNotFoundException {
//            lbr = mock(LoadBalancerRepository.class);
//            es = mock(AsyncService.class);
//            hmr = new HealthMonitorResource();
//            hmr.setAsyncService(es);
//            hmr.setLbRepository(lbr);
//            or = new OperationResponse();
//            List<String> mappingFiles = new ArrayList<String>();
//            mappingFiles.add("loadbalancing-dozer-mapping.xml");
//            hmr.setDozerMapper(DozerBeanMapperBuilder.create()                    .withMappingFiles(mappingFile)                    .build());
//            when(lbr.getByIdAndAccountId(anyInt(),anyInt())).thenReturn(new LoadBalancer());
//        }
//
//        @Before
//        public void setUpValidHttpHealthMonitor() {
//            hm = new HealthMonitor();
//            hm.setType(HealthMonitorType.HTTP);
//            hm.setDelay(50);
//            hm.setTimeout(60);
//            hm.setAttemptsBeforeDeactivation(5);
//            hm.setPath("/mnt/pfft");
//            hm.setStatusRegex(".*");
//            hm.setBodyRegex(".*");
//        }
//  /*
//        @Test
//        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
//            or.setExecutedOkay(true);
//            when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
//            Response response = hmr.updateHealthMonitor(hm);
//            Assert.assertEquals(202, response.getStatus());
//        }
//
//        @Test
//        public void shouldProduceA400WhenPassingInANullObject() {
//            Response response = hmr.updateHealthMonitor(null);
//            Assert.assertEquals(400, response.getStatus());
//        }
//
//        @Test
//        public void shouldProduceA400WhenPassingInAnInvalidaccessListObject() {
//            Response response = hmr.updateHealthMonitor(new HealthMonitor());
//            Assert.assertEquals(400, response.getStatus());
//        }
//
//        @Test
//        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
//            or.setExecutedOkay(false);
//            when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(or);
//            Response response = hmr.updateHealthMonitor(hm);
//            Assert.assertEquals(500, response.getStatus());
//        }
//
//        @Test
//        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
//            when(es.callLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject())).thenReturn(null);
//            Response response = hmr.updateHealthMonitor(hm);
//            Assert.assertEquals(500, response.getStatus());
//        }
//
//        @Test
//        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
//            when(es.callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_HEALTH_MONITOR), Matchers.<LoadBalancer>anyObject());).thenThrow(new Exception());
//            Response response = hmr.updateHealthMonitor(hm);
//            Assert.assertEquals(500, response.getStatus());
//        }  */
//    }

    public static class WhenDeletingAHealthMonitor {
        @Mock
        AsyncService es;
        @Mock
        LoadBalancerRepository lbr;
        @Mock
        LoadBalancerService lbs;
        @Mock
        HealthMonitorService hms;

        OperationResponse or;

        @InjectMocks
        HealthMonitorResource hmr;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);

            hmr = new HealthMonitorResource();
            hmr.setAsyncService(es);
            hmr.setLoadBalancerService(lbs);
            hmr.setHealthMonitorService(hms);
            hmr.setLbRepository(lbr);
            hmr.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            when(lbr.getByIdAndAccountId(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(new LoadBalancer());
        }

        @Test
        public void shouldProduceAcceptResponseWhenAsynResponseIsNormal() throws Exception {
            // Tests asyncservice response normal
            Response response = hmr.deleteHealthMonitor();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenAsyncResponseHasError() throws Exception {
            // Verify async returns exception
            doThrow(new JMSException("fail")).when(es).callAsyncLoadBalancingOperation(ArgumentMatchers.eq(Operation.DELETE_HEALTH_MONITOR), ArgumentMatchers.<LoadBalancer>any());
            Response response = hmr.deleteHealthMonitor();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
