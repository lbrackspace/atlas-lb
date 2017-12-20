package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
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
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.SessionPersistenceService;

import javax.jms.JMSException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@Ignore
public class SessionPersistenceResourceTest {
    static final String mappingFile = "loadbalancing-dozer-mapping.xml";
    public static class WhenEnablingSessionPersistence {
        @Mock
        LoadBalancerRepository lbRepo;
        @Mock
        AsyncService asyncService;
        @Mock
        SessionPersistenceService sessionPersistenceService;
        @Mock
        LoadBalancerService loadBalancerService;

        private SessionPersistence sessionPersistence;
        private org.openstack.atlas.service.domain.entities.LoadBalancer dlb;

        @InjectMocks
        SessionPersistenceResource persistenceResource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            persistenceResource = new SessionPersistenceResource();
            persistenceResource.setAsyncService(asyncService);
            persistenceResource.setLbRepository(lbRepo);
            persistenceResource.setSessionPersistenceService(sessionPersistenceService);
            persistenceResource.setLoadBalancerService(loadBalancerService);

            dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dlb.setSessionPersistence(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE);

            persistenceResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Before
        public void setUpSessionPersistenceObject() {
            sessionPersistence = new SessionPersistence();
            sessionPersistence.setPersistenceType(PersistenceType.HTTP_COOKIE);
        }

        @Test
        public void shouldProduceAcceptResponseWhenAsyncResponseIsNormal() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Calendar>any(), ArgumentMatchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.enableSessionPersistence(sessionPersistence);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenAsyncServiceThrowsRuntimeException() throws Exception {
            doThrow(new JMSException("fail")).when(asyncService).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.<Operation>any(), ArgumentMatchers.<LoadBalancer>any());
            Response response = persistenceResource.enableSessionPersistence(sessionPersistence);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenGettingSessionPersistence {
        @Mock
        LoadBalancerRepository lbRepo;
        @Mock
        SessionPersistenceService sessionPersistenceService;
        @Mock
        HttpHeaders requestHeaders;
        @Mock
        LoadBalancerService loadBalancerService;

        org.openstack.atlas.service.domain.entities.LoadBalancer dlb;
        List<String> headers;

        @InjectMocks
        SessionPersistenceResource persistenceResource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            persistenceResource = new SessionPersistenceResource();
            persistenceResource.setLbRepository(lbRepo);
            persistenceResource.setSessionPersistenceService(sessionPersistenceService);
            persistenceResource.setLoadBalancerService(loadBalancerService);
            persistenceResource.setRequestHeaders(requestHeaders);

            headers = new ArrayList<>();
            headers.add("APPLICATION_JSON");
            when(requestHeaders.getRequestHeader(ArgumentMatchers.any())).thenReturn(headers);

            dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dlb.setSessionPersistence(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE);
            persistenceResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldProduceOkResponseWhenAsyncResponseIsNormal() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Calendar>any(), ArgumentMatchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.retrieveSessionPersistence(null);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldProduce410WhenDeletedStatusExceptionIsThrown() throws Exception {
            doThrow(new DeletedStatusException("fail")).when(sessionPersistenceService).get(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any());
            Response response = persistenceResource.retrieveSessionPersistence(null);
            Assert.assertEquals(410, response.getStatus());
        }
    }

    public static class WhenDisablingSessionPersistence {
        @Mock
        LoadBalancerRepository lbRepo;
        @Mock
        AsyncService asyncService;
        @Mock
        SessionPersistenceService sessionPersistenceService;
        @Mock
        LoadBalancerService loadBalancerService;

        private SessionPersistence sessionPersistence;
        private org.openstack.atlas.service.domain.entities.LoadBalancer dlb;

        @InjectMocks
        SessionPersistenceResource persistenceResource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            persistenceResource = new SessionPersistenceResource();
            persistenceResource.setAsyncService(asyncService);
            persistenceResource.setLbRepository(lbRepo);
            persistenceResource.setSessionPersistenceService(sessionPersistenceService);
            persistenceResource.setLoadBalancerService(loadBalancerService);

            dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dlb.setSessionPersistence(org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE);
            persistenceResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldProduceAcceptResponseWhenAsyncResponseIsNormal() throws Exception {
            when(lbRepo.getUsageByAccountIdandLbId(anyInt(), anyInt(), Matchers.<Calendar>any(), Matchers.<Calendar>any())).thenReturn(null);
            Response response = persistenceResource.disableSessionPersistence();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduceInternalServerErrorWhenAsyncServiceThrowsRuntimeException() throws Exception {
            doThrow(new JMSException("fail")).when(asyncService).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.<Operation>any(), ArgumentMatchers.<LoadBalancer>any());
            Response response = persistenceResource.disableSessionPersistence();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
