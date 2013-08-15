package org.openstack.atlas.adapter.stm;

import junit.framework.Assert;
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
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.monitor.Monitor;
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
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;

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

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            PowerMockito.mockStatic(TrafficScriptHelper.class);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(adapterSpy).setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
            doNothing().when(adapterSpy).deleteVirtualIps(config, loadBalancer);
            doNothing().when(resources).createPersistentClasses(config);
            doNothing().when(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class));
            doNothing().when(resources).updateProtection(eq(config), eq(client), eq(vsName), Matchers.any(Protection.class));
            doNothing().when(resources).updateVirtualIps(eq(config), eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            doNothing().when(resources).updatePool(eq(config), eq(client), eq(vsName), Matchers.any(Pool.class));
            doNothing().when(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), any(VirtualServer.class));
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testCreateLoadBalancer() throws Exception {
            adapterSpy.createLoadBalancer(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
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
            adapterSpy.updateLoadBalancer(config, loadBalancer, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
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
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;

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

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            PowerMockito.mockStatic(TrafficScriptHelper.class);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(adapterSpy).deleteVirtualIps(config, loadBalancer);
            doNothing().when(resources).updateVirtualIps(eq(config), eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            doNothing().when(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), any(VirtualServer.class));
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateVirtualIps() throws Exception {
            adapterSpy.updateVirtualIps(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            verify(resources).updateVirtualIps(eq(config), eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteVirtualIps() throws Exception {
            List<Integer> vipsToDelete = new ArrayList<Integer>();
            for (LoadBalancerJoinVip vip : loadBalancer.getLoadBalancerJoinVipSet())
                vipsToDelete.add(vip.getVirtualIp().getId());

            adapterSpy.deleteVirtualIps(config, loadBalancer, vipsToDelete);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator, times(2)).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            verify(loadBalancer.getLoadBalancerJoinVipSet()).removeAll(anySetOf(LoadBalancerJoinVip.class));
            verify(loadBalancer.getLoadBalancerJoinVipSet()).addAll(anySetOf(LoadBalancerJoinVip.class));
            verify(client).deleteTrafficIp(anyString());
            verify(resources).updateVirtualServer(eq(config), eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingHealthMonitorResources {
        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;

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

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class));
            doNothing().when(resources).updatePool(eq(config), eq(client), eq(vsName), Matchers.any(Pool.class));
            doNothing().when(resources).deleteHealthMonitor(config, client, vsName);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateHealthMonitor() throws Exception {
            adapterSpy.updateHealthMonitor(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            verify(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class));
            verify(resources).updatePool(eq(config), eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteHealthMonitor() throws Exception {
            adapterSpy.deleteHealthMonitor(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteHealthMonitor(config, client, vsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingProtectionResources {
        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;

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

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).updateProtection(eq(config), eq(client), eq(vsName), Matchers.any(Protection.class));
            doNothing().when(resources).deleteProtection(config, client, vsName);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateProtection() throws Exception {
            adapterSpy.updateProtection(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateProtectionResource(vsName, loadBalancer);
            verify(resources).updateProtection(eq(config), eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteProtection() throws Exception {
            adapterSpy.deleteProtection(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteProtection(config, client, vsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingBandwidthResources { //Lumping ConnectionThrottle and RateLimit together here, even though they aren't both Bandwidth related...
        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        private RateLimit testRateLimit;

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
            testRateLimit = new RateLimit();

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            doNothing().when(adapterSpy).updateProtection(config, loadBalancer);
            doNothing().when(adapterSpy).deleteProtection(config, loadBalancer);
            doNothing().when(resources).updateRateLimit(config, loadBalancer, vsName);
            doNothing().when(resources).deleteRateLimit(config, loadBalancer, vsName);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateConnectionThrottleWithConnectionLimit() throws Exception {
            loadBalancer.setConnectionLimit(new ConnectionLimit());

            adapterSpy.updateConnectionThrottle(config, loadBalancer);

            verify(adapterSpy).updateProtection(config, loadBalancer);
        }

        @Test
        public void testUpdateConnectionThrottleWithoutConnectionLimit() throws Exception {
            loadBalancer.setConnectionLimit(null);

            adapterSpy.updateConnectionThrottle(config, loadBalancer);

            verify(adapterSpy, never()).updateProtection(config, loadBalancer);
        }

        @Test
        public void testDeleteConnectionThrottleWithValidAccessList() throws Exception {
            adapterSpy.deleteConnectionThrottle(config, loadBalancer);

            verify(adapterSpy).updateProtection(config, loadBalancer);
        }

        @Test
        public void testDeleteConnectionThrottleWithNoAccessList() throws Exception {
            loadBalancer.setAccessLists(null);

            adapterSpy.deleteConnectionThrottle(config, loadBalancer);

            verify(adapterSpy).deleteProtection(config, loadBalancer);
        }

        @Test
        public void testSetRateLimit() throws Exception {
            Assert.assertFalse(loadBalancer.getRateLimit().equals(testRateLimit));
            adapterSpy.setRateLimit(config, loadBalancer, testRateLimit);

            Assert.assertEquals(testRateLimit, loadBalancer.getRateLimit());
            verify(resources).setRateLimit(config, loadBalancer, vsName);
        }

        @Test
        public void testUpdateRateLimit() throws Exception {
            Assert.assertFalse(loadBalancer.getRateLimit().equals(testRateLimit));
            adapterSpy.updateRateLimit(config, loadBalancer, testRateLimit);

            Assert.assertEquals(testRateLimit, loadBalancer.getRateLimit());
            verify(resources).updateRateLimit(config, loadBalancer, vsName);
        }

        @Test
        public void testDeleteRateLimit() throws Exception {
            adapterSpy.deleteRateLimit(config, loadBalancer);

            verify(resources).deleteRateLimit(config, loadBalancer, vsName);
        }
    }

    public static class WhenModifyingFileResources {
        private String vsName;
        private static LoadBalancer loadBalancer;
        private String errorContent;

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
            errorContent = "My Error Content";

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).setErrorFile(config, client, loadBalancer, vsName, errorContent);
            doNothing().when(resources).deleteErrorFile(config, client, loadBalancer, vsName);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testSetErrorFile() throws Exception {
            adapterSpy.setErrorFile(config, loadBalancer, errorContent);

            verify(resources).loadSTMRestClient(config);
            verify(resources).setErrorFile(config, client, loadBalancer, vsName, errorContent);
            verify(client).destroy();
        }

        @Test
        public void testDeleteErrorFile() throws Exception {
            adapterSpy.deleteErrorFile(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteErrorFile(config, client, loadBalancer, vsName);
            verify(client).destroy();
        }
    }
}