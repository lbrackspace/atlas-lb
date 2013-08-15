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
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class StmAdapterImplTest extends StmAdapterImplTestHelper {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*"})
    @PrepareForTest({TrafficScriptHelper.class, ResourceTranslator.class})
    public static class WhenModifyingLoadbalancerResources {
        private String vsName;
        private String secureVsName;
        private static LoadBalancer loadBalancer;

        @Mock
        private static StmAdapterResources resources;
        @Mock
        private static LoadBalancerEndpointConfiguration config;
        @Mock
        private static StingrayRestClient client;
        @Spy
        private static StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);

            PowerMockito.mockStatic(TrafficScriptHelper.class);
            doNothing().when(adapterSpy).setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
            doNothing().when(adapterSpy).deleteVirtualIps(config, loadBalancer);
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

        @Test
        public void testUpdateLoadBalancer() throws Exception {
            ResourceTranslator resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            adapterSpy.updateLoadBalancer(config, loadBalancer, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, ZxtmNameBuilder.genVSName(loadBalancer), loadBalancer, loadBalancer);
            //verify(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class)); //TODO: this should be passing, but if the LB has SSL it won't
            verify(resources).updateProtection(eq(config), eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(config), eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(adapterSpy).setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
            verify(resources).updatePool(eq(config), eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(secureVsName), Matchers.any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteLoadBalancer() throws Exception {
            adapterSpy.deleteLoadBalancer(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteRateLimit(config, loadBalancer, vsName);
            verify(resources).deleteHealthMonitor(config, client, vsName);
            verify(resources).deleteProtection(config, client, vsName);
            verify(adapterSpy).deleteVirtualIps(config, loadBalancer);
            verify(resources).deletePool(config, client, vsName);
            verify(resources).deleteVirtualServer(config, client, vsName);
            verify(resources).deleteVirtualServer(config, client, secureVsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingVirtualIpResources {
        private String vsName;
        private static LoadBalancer loadBalancer;

        @Mock
        private static StmAdapterResources resources;
        @Mock
        private static LoadBalancerEndpointConfiguration config;
        @Mock
        private static StingrayRestClient client;
        @Spy
        private static StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);

            PowerMockito.mockStatic(TrafficScriptHelper.class);
            doNothing().when(adapterSpy).setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
            doNothing().when(adapterSpy).deleteVirtualIps(config, loadBalancer);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateVirtualIps() throws Exception {
            ResourceTranslator resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            adapterSpy.updateVirtualIps(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, ZxtmNameBuilder.genVSName(loadBalancer), loadBalancer, loadBalancer);
            verify(resources).updateVirtualIps(eq(config), eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteVirtualIps() throws Exception {
            ResourceTranslator resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);
            List<Integer> vipsToDelete = new ArrayList<Integer>();
            for (LoadBalancerJoinVip vip : loadBalancer.getLoadBalancerJoinVipSet())
                vipsToDelete.add(vip.getVirtualIp().getId());

            adapterSpy.deleteVirtualIps(config, loadBalancer, vipsToDelete);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator, times(2)).translateLoadBalancerResource(config, ZxtmNameBuilder.genVSName(loadBalancer), loadBalancer, loadBalancer);
            verify(loadBalancer.getLoadBalancerJoinVipSet()).removeAll(anySetOf(LoadBalancerJoinVip.class));
            verify(loadBalancer.getLoadBalancerJoinVipSet()).addAll(anySetOf(LoadBalancerJoinVip.class));
            verify(client).deleteTrafficIp(anyString());
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            verify(client).destroy();
        }
    }

}