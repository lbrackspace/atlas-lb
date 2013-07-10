package org.openstack.atlas.api.resources;

import net.spy.memcached.MemcachedClient;
import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
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
            verify(accessListResource).setAccountId(anyInt());
            verify(accessListResource).setLoadBalancerId(anyInt());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForHealthMonitorResource() {
            HealthMonitorResource healthMonitorResource = mock(HealthMonitorResource.class);
            loadBalancerResource.setHealthMonitorResource(healthMonitorResource);
            loadBalancerResource.retrieveHealthMonitorResource();
            verify(healthMonitorResource).setAccountId(anyInt());
            verify(healthMonitorResource).setLoadBalancerId(anyInt());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForNodesResource() {
            NodesResource nodesResource = mock(NodesResource.class);
            loadBalancerResource.setNodesResource(nodesResource);
            loadBalancerResource.retrieveNodesResource();
            verify(nodesResource).setAccountId(anyInt());
            verify(nodesResource).setLoadBalancerId(anyInt());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForSessionPersistenceResource() {
            SessionPersistenceResource persistenceResource = mock(SessionPersistenceResource.class);
            loadBalancerResource.setSessionPersistenceResource(persistenceResource);
            loadBalancerResource.retrieveSessionPersistenceResource();
            verify(persistenceResource).setAccountId(anyInt());
            verify(persistenceResource).setLoadBalancerId(anyInt());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForConnectionThrottleResource() {
            ConnectionThrottleResource throttleResource = mock(ConnectionThrottleResource.class);
            loadBalancerResource.setConnectionThrottleResource(throttleResource);
            loadBalancerResource.retrieveConnectionThrottleResource();
            verify(throttleResource).setAccountId(anyInt());
            verify(throttleResource).setLoadBalancerId(anyInt());
        }

        @Test
        public void shouldSetAccountIdAndLbIdForVirtualIpsResource() {
            VirtualIpsResource virtualIpsResource = mock(VirtualIpsResource.class);
            loadBalancerResource.setVirtualIpsResource(virtualIpsResource);
            loadBalancerResource.retrieveVirtualIpsResource();
            verify(virtualIpsResource).setAccountId(anyInt());
            verify(virtualIpsResource).setLoadBalancerId(anyInt());
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
            when(loadBalancerService.get(anyInt())).thenReturn(null);
            doNothing().when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.DELETE_LOADBALANCER), Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals((String) response.getEntity(), 202, response.getStatus());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception {
            when(loadBalancerService.get(anyInt())).thenReturn(null);
            doThrow(new EntityNotFoundException("Exception")).when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldReturnA500WhenDeletionFails() throws Exception {
            when(loadBalancerService.get(anyInt())).thenReturn(null);
            doThrow(new Exception("Exception")).when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            response = loadBalancerResource.deleteLoadBalancer();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn500OnJmsException() throws Exception {
            when(loadBalancerService.get(anyInt())).thenReturn(null);
            doNothing().when(loadBalancerService).prepareForDelete(Matchers.<LoadBalancer>any());
            doThrow(new JMSException("Exception")).when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.DELETE_LOADBALANCER), Matchers.<LoadBalancer>any());
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
        private DozerBeanMapper dozerBeanMapper;
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
            dozerBeanMapper = mock(DozerBeanMapper.class);
            loadBalancerResource = new LoadBalancerResource();
            loadBalancerResource.setId(1);
            loadBalancerResource.setAccountId(1234);
            loadBalancerResource.setLoadBalancerService(loadBalancerService);
            loadBalancerResource.setAsyncService(asyncService);
            loadBalancerResource.setRestApiConfiguration(restApiConfiguration);
            loadBalancerResource.setReverseProxyLoadBalancerService(reverseProxyLoadBalancerService);
            loadBalancerResource.setDozerMapper(dozerBeanMapper);

            stats = new Stats();
            stats.setConnectError(new int[]{90});
            stats.setConnectFailure(new int[]{30});
            stats.setConnectTimeOut(new int[]{40});
            stats.setDataTimedOut(new int[]{500});
            stats.setKeepAliveTimedOut(new int[]{40});
            stats.setMaxConn(new int[]{20});

            lb = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer();
            lb.setStatus("ACTIVE");
        }

        @Ignore
        @Test
        public void shouldReturnOkWhenRetrievingStats() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("true").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.stats);


            when(loadBalancerService.get(anyInt(), anyInt())).thenReturn(null);
            doReturn(stats).when(reverseProxyLoadBalancerService).getLoadBalancerStats(Matchers.anyInt(), Matchers.anyInt());
            response = loadBalancerResource.retrieveLoadBalancerStats();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn400WhenRetrievingStatsWithFalseFlag() throws Exception {
            doReturn(true).when(restApiConfiguration).hasKeys(PublicApiServiceConfigurationKeys.stats);
            doReturn("false").when(restApiConfiguration).getString(PublicApiServiceConfigurationKeys.stats);

            when(loadBalancerService.get(anyInt())).thenReturn(null);
            doReturn(stats).when(reverseProxyLoadBalancerService).getLoadBalancerStats(Matchers.anyInt(), Matchers.anyInt());
            response = loadBalancerResource.retrieveLoadBalancerStats();
            Assert.assertEquals(400, response.getStatus());
        }
    }
}
