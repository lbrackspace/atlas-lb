package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
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
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@Ignore
public class SessionPersistenceResourceTest {

    public static class WhenEnablingSessionPersistence {
        private LoadBalancerRepository lbRepo;
        private AsyncService esbService;
        private SessionPersistenceResource persistenceResource;
        private OperationResponse operationResponse;
        private SessionPersistence sessionPersistence;
        private org.openstack.atlas.service.domain.entities.LoadBalancer dlb;

        @Before
        public void setUp() {
            lbRepo = mock(LoadBalancerRepository.class);
            esbService = mock(AsyncService.class);
            persistenceResource = new SessionPersistenceResource();
            persistenceResource.setAsyncService(esbService);
            persistenceResource.setLbRepository(lbRepo);
            operationResponse = new OperationResponse();
            dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dlb.setSessionPersistence(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE);
            operationResponse.setEntity(dlb);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            persistenceResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Before
        public void setUpSessionPersistenceObject() {
            sessionPersistence = new SessionPersistence();
            sessionPersistence.setPersistenceType(PersistenceType.HTTP_COOKIE);
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            operationResponse.setExecutedOkay(true);
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.enableSessionPersistence(sessionPersistence);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceA400WhenPassingInAnInvalidaccessListObject() throws EntityNotFoundException, DeletedStatusException {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.enableSessionPersistence(new SessionPersistence());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            operationResponse.setExecutedOkay(false);
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.enableSessionPersistence(sessionPersistence);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.enableSessionPersistence(sessionPersistence);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.enableSessionPersistence(sessionPersistence);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenGettingSessionPersistence {
        private LoadBalancerRepository lbRepo;
        private AsyncService esbService;
        private SessionPersistenceResource persistenceResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            lbRepo = mock(LoadBalancerRepository.class);
            esbService = mock(AsyncService.class);
            persistenceResource = new SessionPersistenceResource();
            persistenceResource.setAsyncService(esbService);
            persistenceResource.setLbRepository(lbRepo);
            operationResponse = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            persistenceResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldProduceOkResponseWhenEsbResponseIsNormal() throws Exception {
            operationResponse.setExecutedOkay(true);
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.retrieveSessionPersistence(null);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            operationResponse.setExecutedOkay(false);
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.retrieveSessionPersistence(null);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.retrieveSessionPersistence(null);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.retrieveSessionPersistence(null);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenDisablingSessionPersistence {
        private LoadBalancerRepository lbRepo;
        private AsyncService esbService;
        private SessionPersistenceResource persistenceResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            lbRepo = mock(LoadBalancerRepository.class);
            esbService = mock(AsyncService.class);
            persistenceResource = new SessionPersistenceResource();
            persistenceResource.setAsyncService(esbService);
            persistenceResource.setLbRepository(lbRepo);
            operationResponse = new OperationResponse();
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            persistenceResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldProduceAcceptResponseWhenEsbResponseIsNormal() throws Exception {
            operationResponse.setExecutedOkay(true);
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.disableSessionPersistence();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseHasError() throws Exception {
            operationResponse.setExecutedOkay(false);
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.disableSessionPersistence();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbResponseIsNull() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.disableSessionPersistence();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenEsbServiceThrowsRuntimeException() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.disableSessionPersistence();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
