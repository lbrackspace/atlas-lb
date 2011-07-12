package org.openstack.atlas.api.resources;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.mapper.dozer.MapperBuilder;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.VirtualIpService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class LoadBalancersResourceTest {

    private static final String publicDozerConfigFile = "loadbalancing-dozer-mapping.xml";

    public static class WhenCreatingALoadBalancer {

        private LoadBalancersResource loadBalancersResource;
        private LoadBalancerService loadBalancerService;
        private AsyncService asyncService;
        private VirtualIpService virtualIpService;
        private LoadBalancer loadBalancer;

        @Before
        public void setUp() {
            asyncService = mock(AsyncService.class);
            virtualIpService = mock(VirtualIpService.class);
            loadBalancerService = mock(LoadBalancerService.class);
            loadBalancersResource = new LoadBalancersResource();
            loadBalancersResource.setAsyncService(asyncService);
            loadBalancersResource.setVirtualIpService(virtualIpService);
            loadBalancersResource.setLoadBalancerService(loadBalancerService);
            loadBalancersResource.setDozerMapper(MapperBuilder.getConfiguredMapper(publicDozerConfigFile));
        }

        @Before
        public void setupLoadBalancerObject() {
            loadBalancer = new LoadBalancer();
            loadBalancer.setName("a-new-loadbalancer");
            loadBalancer.setProtocol("IMAPv4");

            List<VirtualIp> virtualIps = new ArrayList<VirtualIp>();
            VirtualIp vip = new VirtualIp();
            vip.setType(VipType.PUBLIC);
            virtualIps.add(vip);

            loadBalancer.getVirtualIps().addAll(virtualIps);

            Nodes nodes = new Nodes();
            Node node = new Node();
            node.setAddress("10.1.1.1");
            node.setPort(80);
            node.setCondition(NodeCondition.ENABLED);
            nodes.getNodes().add(node);
            loadBalancer.getNodes().addAll(nodes.getNodes());
        }

        @Test
        public void shouldProduce400ResponseWhenFailingValidation() {
            Response response = loadBalancersResource.createLoadBalancer(new LoadBalancer());
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce202ResponseWhenCreateSucceeds() throws Exception {
            doNothing().when(virtualIpService).addAccountRecord(Matchers.<Integer>any());
            when(loadBalancerService.create(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any())).thenReturn(new org.openstack.atlas.service.domain.entities.LoadBalancer());
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.CREATE_LOADBALANCER), Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = loadBalancersResource.createLoadBalancer(loadBalancer);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduce500ResponseWhenCreateThrowsException() throws Exception {
            doThrow(new Exception("Exception")).when(loadBalancerService).create(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = loadBalancersResource.createLoadBalancer(loadBalancer);
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn500onJmsException() throws Exception {
            when(loadBalancerService.create(Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any())).thenReturn(new org.openstack.atlas.service.domain.entities.LoadBalancer());
            doThrow(new JMSException("Exception")).when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.CREATE_LOADBALANCER), Matchers.<org.openstack.atlas.service.domain.entities.LoadBalancer>any());
            Response response = loadBalancersResource.createLoadBalancer(loadBalancer);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenRetrievingResources {

        private LoadBalancersResource loadBalancersResource;

        @Before
        public void setUp() {
            loadBalancersResource = new LoadBalancersResource();
        }

        @Test
        public void shouldSetAccountIdAndLbIdForLoadBalancerResource() {
            LoadBalancerResource mockedLoadBalancerResource = mock(LoadBalancerResource.class);
            loadBalancersResource.setLoadBalancerResource(mockedLoadBalancerResource);
            loadBalancersResource.retrieveLoadBalancerResource(anyInt());
            verify(mockedLoadBalancerResource).setId(anyInt());
            verify(mockedLoadBalancerResource).setAccountId(anyInt());
        }
    }
}
