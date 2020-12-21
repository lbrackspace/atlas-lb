package org.openstack.atlas.api.resources;

import net.spy.memcached.MemcachedClient;
import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.exceptions.ValidationException;
import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.validators.ConnectionLoggingValidator;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.services.ConnectionLoggingService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.jms.JMSException;
import javax.ws.rs.core.*;

import java.util.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ConnectionLoggingResourceTest {

    public static class WhenRetrievingResources {
        private ConnectionLoggingResource connectionLoggingResource;
        private ConnectionLoggingService connectionLoggingService;
        private Response response;

        @Before
        public void setUp() {
            connectionLoggingResource = new ConnectionLoggingResource();
            connectionLoggingService = mock(ConnectionLoggingService.class);
            connectionLoggingResource.setAccountId(1);
            connectionLoggingResource.setLoadBalancerId(1234);
            connectionLoggingResource.setConnectionLoggingService(connectionLoggingService);
        }

        @Test
        public void shouldRetrieveConnectionLoggingDetail() throws EntityNotFoundException {
            doReturn(true).when(connectionLoggingService).get(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = connectionLoggingResource.retrieveConnectionLogging();
            ConnectionLogging connectionLogging = (ConnectionLogging) response.getEntity();
            Assert.assertEquals(true, connectionLogging.getEnabled());
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn500ErrorCode() throws EntityNotFoundException {
            doThrow(Exception.class).when(connectionLoggingService).get(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = connectionLoggingResource.retrieveConnectionLogging();
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenUpdatingResources {
        @Mock
        private List<String> headerList;
        @InjectMocks
        private ConnectionLoggingResource connectionLoggingResource;
        private HttpHeaders requestHeaders;
        private LoadBalancerService loadBalancerService;
        private AsyncService asyncService;
        private Response response;
        private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";
        private ConnectionLoggingService connectionLoggingService;

        @Before
        public void standUp() {
            loadBalancerService = mock(LoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            connectionLoggingService = mock(ConnectionLoggingService.class);
            connectionLoggingResource = new ConnectionLoggingResource();
            connectionLoggingResource.setAccountId(1);
            connectionLoggingResource.setLoadBalancerId(1234);
            connectionLoggingResource.setLoadBalancerService(loadBalancerService);
            connectionLoggingResource.setAsyncService(asyncService);
            connectionLoggingResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));
            connectionLoggingResource.setConnectionLoggingService(connectionLoggingService);
            MockitoAnnotations.initMocks(this); // Just comment this line to test the ignored test case
        }

        @Test
        public void shouldFailValidationForConnectionLogging() {
            response =  connectionLoggingResource.updateConnectionLogging(new ConnectionLogging());
            Assert.assertEquals(400, response.getStatus());
            BadRequest res = (BadRequest) response.getEntity();
            Assert.assertEquals("Validation Failure", res.getMessage());
        }

        @Test
        public void shouldFailToUpdateConnectionLogging() throws EntityNotFoundException, JMSException {
            org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            ConnectionLogging conLog = new ConnectionLogging();
            conLog.setEnabled(true);
            loadBalancer.setProtocol(LoadBalancerProtocol.DNS_UDP);
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(loadBalancer);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONNECTION_LOGGING), Matchers.<LoadBalancer>any());
            response = connectionLoggingResource.updateConnectionLogging(conLog);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldUpdateConnectionLoggingWhenHttpHeaderIsNull() throws EntityNotFoundException, JMSException {
            ConnectionLogging conLog = new ConnectionLogging();
            org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            conLog.setEnabled(true);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            apiLb.setProtocol("HTTP");
            loadBalancer.setConnectionLogging(false);
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(loadBalancer);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONNECTION_LOGGING), Matchers.<LoadBalancer>any());
            response = connectionLoggingResource.updateConnectionLogging(conLog);
            Assert.assertEquals(202, response.getStatus());
        }
        @Test
        public void shouldUpdateConnectionLoggingWhenHttpHeaderNotNull() throws EntityNotFoundException, JMSException {
            requestHeaders = mock(HttpHeaders.class);
            ConnectionLogging conLog = new ConnectionLogging();
            org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            conLog.setEnabled(true);
            loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
            apiLb.setProtocol("HTTP");
            loadBalancer.setConnectionLogging(false);
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(loadBalancer);
            when(requestHeaders.getRequestHeader(ArgumentMatchers.anyString())).thenReturn(headerList);
            when(headerList.get(0)).thenReturn("abc");
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_CONNECTION_LOGGING), Matchers.<LoadBalancer>any());
            response = connectionLoggingResource.updateConnectionLogging(conLog);
            Assert.assertEquals(202, response.getStatus());
        }
    }
}
