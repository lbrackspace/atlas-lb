package org.openstack.atlas.api.resources;

import net.spy.memcached.MemcachedClient;
import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class LoadBalancerResourceTest {

    public static class WhenRetrievingResources {
        private LoadBalancerResource loadBalancerResource;

        @Before
        public void setUp() {
            loadBalancerResource = new LoadBalancerResource();
        }

        @Test
        public void shouldSetAccountIdAndLbIdForAccessListResource() {
            AccessListResource accessListResource = mock(AccessListResource.class);
            loadBalancerResource.setAccessListResource(accessListResource);
            loadBalancerResource.retrieveAccessListResource();
            verify(accessListResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(accessListResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForHealthMonitorResource() {
            HealthMonitorResource healthMonitorResource = mock(HealthMonitorResource.class);
            loadBalancerResource.setHealthMonitorResource(healthMonitorResource);
            loadBalancerResource.retrieveHealthMonitorResource();
            verify(healthMonitorResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(healthMonitorResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForNodesResource() {
            NodesResource nodesResource = mock(NodesResource.class);
            loadBalancerResource.setNodesResource(nodesResource);
            loadBalancerResource.retrieveNodesResource();
            verify(nodesResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(nodesResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForSessionPersistenceResource() {
            SessionPersistenceResource persistenceResource = mock(SessionPersistenceResource.class);
            loadBalancerResource.setSessionPersistenceResource(persistenceResource);
            loadBalancerResource.retrieveSessionPersistenceResource();
            verify(persistenceResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(persistenceResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForConnectionThrottleResource() {
            ConnectionThrottleResource throttleResource = mock(ConnectionThrottleResource.class);
            loadBalancerResource.setConnectionThrottleResource(throttleResource);
            loadBalancerResource.retrieveConnectionThrottleResource();
            verify(throttleResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(throttleResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForVirtualIpsResource() {
            VirtualIpsResource virtualIpsResource = mock(VirtualIpsResource.class);
            loadBalancerResource.setVirtualIpsResource(virtualIpsResource);
            loadBalancerResource.retrieveVirtualIpsResource();
            verify(virtualIpsResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(virtualIpsResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }
    }

    public static class WhenUpdatingResources {
        private LoadBalancerService loadBalancerService;
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        private AsyncService asyncService;
        private Mapper dozerMapper;
        private LoadBalancerResource loadBalancerResource;
        private Response response;

        @Before
        public void standUp() {
            loadBalancerService = mock(LoadBalancerService.class);
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            dozerMapper = mock(Mapper.class);
            loadBalancerResource = new LoadBalancerResource();
            loadBalancerResource.setId(1);
            loadBalancerResource.setAccountId(1234);
            loadBalancerResource.setLoadBalancerService(loadBalancerService);
            loadBalancerResource.setAsyncService(asyncService);
            loadBalancerResource.setDozerMapper(dozerMapper);
        }

        @Test
        public void shouldFailForUDPConnectionLog() throws Exception {
            org.openstack.atlas.service.domain.entities.LoadBalancer dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer rlb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            dlb.setConnectionLogging(true);
            rlb.setProtocol("UDP");
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(dlb);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_LOADBALANCER), Matchers.<LoadBalancer>any());
            response = loadBalancerResource.updateLoadBalancer(rlb);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldSucceedForUDPNoConnectionLogging() throws Exception {
            org.openstack.atlas.service.domain.entities.LoadBalancer dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer rlb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            dlb.setConnectionLogging(false);
            rlb.setProtocol("UDP");
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(dlb);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_LOADBALANCER), Matchers.<LoadBalancer>any());
            doReturn(dlb).when(dozerMapper).map(rlb, LoadBalancer.class);
            response = loadBalancerResource.updateLoadBalancer(rlb);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldSucceedWithOtherAttrAndNullProto() throws Exception {
            org.openstack.atlas.service.domain.entities.LoadBalancer dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer rlb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            dlb.setConnectionLogging(false);
            rlb.setName("NewName");
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(dlb);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_LOADBALANCER), Matchers.<LoadBalancer>any());
            doReturn(dlb).when(dozerMapper).map(rlb, LoadBalancer.class);
            response = loadBalancerResource.updateLoadBalancer(rlb);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldSucceedWithHttpsRedirectOnly() throws Exception {
            org.openstack.atlas.service.domain.entities.LoadBalancer dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer rlb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            dlb.setConnectionLogging(false);
            rlb.setHttpsRedirect(true);
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(dlb);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_LOADBALANCER), Matchers.<LoadBalancer>any());
            doReturn(dlb).when(dozerMapper).map(rlb, LoadBalancer.class);
            response = loadBalancerResource.updateLoadBalancer(rlb);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldFailWithNoExpectedAttrSupplied() throws Exception {
            org.openstack.atlas.service.domain.entities.LoadBalancer dlb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer rlb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(dlb);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.UPDATE_LOADBALANCER), Matchers.<LoadBalancer>any());
            doReturn(dlb).when(dozerMapper).map(rlb, LoadBalancer.class);
            response = loadBalancerResource.updateLoadBalancer(rlb);
            Assert.assertEquals(400, response.getStatus());
        }

    }

    public static class WhenDeletingALoadBalancer {
        private LoadBalancerService loadBalancerService;
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        private AsyncService asyncService;
        private LoadBalancerResource loadBalancerResource;
        private Response response;

        @Before
        public void standUp() {
            loadBalancerService = mock(LoadBalancerService.class);
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            loadBalancerResource = new LoadBalancerResource();
            loadBalancerResource.setId(1);
            loadBalancerResource.setAccountId(1234);
            loadBalancerResource.setLoadBalancerService(loadBalancerService);
            loadBalancerResource.setAsyncService(asyncService);
        }

        @Test
        public void shouldReturnA202OnSuccess() throws Exception {
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any())).thenReturn(null);
            doNothing().when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.DELETE_LOADBALANCER), Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals((String) response.getEntity(), 202, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception {
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any())).thenReturn(null);
            doThrow(EntityNotFoundException.class   ).when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldReturnA500WhenDeletionFails() throws Exception {
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any())).thenReturn(null);
            doThrow(Exception.class).when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn500OnJmsException() throws Exception {
            when(loadBalancerService.get(ArgumentMatchers.<Integer>any())).thenReturn(null);
            doNothing().when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.DELETE_LOADBALANCER), Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenTestingStats {
        private LoadBalancerService loadBalancerService;
        private ReverseProxyLoadBalancerService reverseProxyLoadBalancerService;
        private AsyncService asyncService;
        private LoadBalancerResource loadBalancerResource;
        private RestApiConfiguration restApiConfiguration;
        private MemcachedClient memcachedClient;
        private Mapper dozerBeanMapper;
        private Stats stats;
        org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer lb;

        private Response response;

        @Before
        public void standUp() {
            loadBalancerService = mock(LoadBalancerService.class);
            restApiConfiguration = mock(RestApiConfiguration.class);
            reverseProxyLoadBalancerService = mock(ReverseProxyLoadBalancerService.class);
            asyncService = mock(AsyncService.class);
            memcachedClient = mock(MemcachedClient.class);
            dozerBeanMapper = mock(Mapper.class);
            loadBalancerResource = new LoadBalancerResource();
            loadBalancerResource.setId(1);
            loadBalancerResource.setAccountId(1234);
            loadBalancerResource.setLoadBalancerService(loadBalancerService);
            loadBalancerResource.setAsyncService(asyncService);
            loadBalancerResource.setRestApiConfiguration(restApiConfiguration);
            loadBalancerResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
            loadBalancerResource.setDozerMapper(dozerBeanMapper);

            stats = new Stats();
            stats.setConnectError(90L);
            stats.setConnectFailure(30L);
            stats.setConnectTimeOut(40L);
            stats.setDataTimedOut(500L);
            stats.setKeepAliveTimedOut(40L);
            stats.setMaxConn(20L);

            lb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            lb.setStatus("ACTIVE");
        }

        @Ignore
        @Test
        public void shouldReturnOkWhenRetrievingStats() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.stats);


            when(loadBalancerService.get(ArgumentMatchers.<Integer>any(), ArgumentMatchers.<Integer>any())).thenReturn(null);
            doReturn(stats).when(reverseProxyLoadBalancerService).getLoadBalancerStats(Matchers.any(LoadBalancer.class));
            response = loadBalancerResource.retrieveLoadBalancerStats();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn400WhenRetrievingStatsWithFalseFlag() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("false").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.stats);

            when(loadBalancerService.get(ArgumentMatchers.<Integer>any())).thenReturn(null);
            doReturn(stats).when(reverseProxyLoadBalancerService).getLoadBalancerStats(Matchers.any(LoadBalancer.class));
            response = loadBalancerResource.retrieveLoadBalancerStats();
            Assert.assertEquals(400, response.getStatus());
        }
    }
}
