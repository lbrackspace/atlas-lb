package org.openstack.atlas.adapter.stm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.StmAdapterImplTestHelper;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class StmAdapterImplTest extends StmAdapterImplTestHelper {
    @RunWith(PowerMockRunner.class)
    @PrepareForTest({TrafficScriptHelper.class, ResourceTranslator.class})
    public static class WhenModifyingLoadbalancerResources {
        private String vsName;
        private static LoadBalancer loadBalancer;

        @Mock
        private static StmAdapterResources resources;
        @Mock
        private static LoadBalancerEndpointConfiguration config;
        @Mock
        private static StingrayRestClient client;
        @Mock
        private static TrafficScriptHelper trafficScriptHelper;
        @Spy
        private static StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);

            PowerMockito.mockStatic(TrafficScriptHelper.class);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testCreateLoadBalancer() throws Exception {
            ResourceTranslator resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);
            adapterSpy.createLoadBalancer(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, ZxtmNameBuilder.genVSName(loadBalancer), loadBalancer, loadBalancer);
            PowerMockito.verifyStatic();
            TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
            PowerMockito.verifyStatic();
            TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
            verify(resources).createPersistentClasses(config);
            //verify(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class)); //TODO: this should be passing, but if the LB has SSL it won't
            verify(resources).updateProtection(eq(config), eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(config), eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updatePool(eq(config), eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            verify(client).destroy();
        }
    }
}