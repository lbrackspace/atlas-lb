package org.openstack.atlas.api.resources;

import org.dozer.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.SslTerminationService;

import javax.ws.rs.core.Response;

import java.rmi.RemoteException;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class SslTerminationResourceTest {

    public static class createSsl {

        @Mock
        AsyncService asyncService;
        @Mock
        ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        @Mock
        ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;
        @Mock
        SslTerminationService sslTerminationService;
        @Mock
        Mapper dozerBeanMapper;
        @Mock
        RestApiConfiguration restApiConfiguration;

        private Response response;

        @InjectMocks
        SslTerminationResource sslTermResource;

        @Before
        public void standUp() {
            MockitoAnnotations.initMocks(this);

            dozerBeanMapper = mock(Mapper.class);
            restApiConfiguration = mock(RestApiConfiguration.class);

            sslTermResource = new SslTerminationResource();
            sslTermResource.setId(1);
            sslTermResource.setAccountId(1234);
            sslTermResource.setSslTerminationService(sslTerminationService);
            sslTermResource.setAsyncService(asyncService);
            sslTermResource.setDozerMapper(dozerBeanMapper);
            sslTermResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
            sslTermResource.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
            sslTermResource.setRestApiConfiguration(restApiConfiguration);

        }

        @Test
        public void shouldReturnA200OnSuccess() throws Exception {
            when(sslTerminationService.getSslTermination(anyInt(), anyInt())).thenReturn(null);
            response = sslTermResource.getSsl();
            org.junit.Assert.assertEquals((String) response.getEntity(), 200, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception {
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(null);
            doThrow(EntityNotFoundException.class).when(sslTerminationService).getSslTermination(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any());
            response = sslTermResource.getSsl();
            org.junit.Assert.assertEquals(404, response.getStatus());
        }

        // Ciphers Tests

        @Test
        public void shouldReturnA200WhenReturningEmptyCiphersList() throws Exception {
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(null);
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA200WhenReturningDefaultCiphersList() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("soap").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.stats);
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);
            doReturn("a,b,c,d,3des").when(reverseProxyLoadBalancerService).getSsl3Ciphers();
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA200WhenReturningDefaultCiphersListREST() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("REST").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.stats);
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(ArgumentMatchers.<Integer>any(),
                    ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);
            doReturn("a,b,c,d,3des").when(reverseProxyLoadBalancerStmService).getSsl3Ciphers();
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA200WhenReturningDefinedCiphersList() throws Exception {
            SslTermination sslTerm = new SslTermination();
            sslTerm.setCipherList("a,b,c,d,3des");
            when(sslTerminationService.getSslTermination(anyInt(), anyInt())).thenReturn(sslTerm);
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA500WhenReturningDefaultCiphersListFails() throws Exception {
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(sslTerm);
            doThrow(RemoteException.class).when(reverseProxyLoadBalancerService).getSsl3Ciphers();
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenReturningDefaultCiphersListFails() throws Exception {
            SslTermination sslTerm = new SslTermination();
            when(sslTerminationService.getSslTermination(anyInt(), anyInt())).thenReturn(sslTerm);
            doThrow(EntityNotFoundException.class).when(sslTerminationService).getSslTermination(
                    ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any());
            response = sslTermResource.retrieveSupportedCiphers();
            org.junit.Assert.assertEquals(404, response.getStatus());
        }

        //TODO: Moar Tests
    }
}

