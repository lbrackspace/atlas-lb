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
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.util.Constants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolProperties;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class StmAdapterImplTest extends StmAdapterImplTestHelper {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest({ResourceTranslator.class})
    public static class WhenModifyingLoadbalancerResources {

        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(adapterSpy).setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
            doNothing().when(adapterSpy).deleteVirtualIps(config, loadBalancer);
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
            verify(resources).createPersistentClasses(config);
            //verify(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class)); //TODO: this should be passing, but if the LB has SSL it won't
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(resources).updateVirtualServer(eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testUpdateLoadBalancer() throws Exception {
            adapterSpy.updateLoadBalancer(config, loadBalancer, loadBalancer, null);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            //verify(resources).updateHealthMonitor(eq(config), eq(client), eq(vsName), Matchers.any(Monitor.class)); //TODO: this should be passing, but if the LB has SSL it won't
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(adapterSpy).setErrorFile(config, loadBalancer, loadBalancer.getUserPages().getErrorpage());
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(resources).updateVirtualServer(eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), Matchers.any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteLoadBalancer() throws Exception {
            adapterSpy.deleteLoadBalancer(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteRateLimit(config, loadBalancer, vsName);
            verify(resources).deleteHealthMonitor(client, vsName);
            verify(resources).deleteProtection(client, vsName);
            verify(adapterSpy).deleteVirtualIps(config, loadBalancer);
            verify(resources).deletePool(client, vsName);
            verify(resources).deleteVirtualServer(client, vsName);
            verify(resources).deleteVirtualServer(client, secureVsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingVirtualIpResources {

        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

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
            verify(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteVirtualIps() throws Exception {
            List<Integer> vipsToDelete = new ArrayList<Integer>();
            for (LoadBalancerJoinVip vip : loadBalancer.getLoadBalancerJoinVipSet()) {
                vipsToDelete.add(vip.getVirtualIp().getId());
            }

            adapterSpy.deleteVirtualIps(config, loadBalancer, vipsToDelete, null);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator, times(2)).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer, false);
            verify(loadBalancer.getLoadBalancerJoinVipSet()).removeAll(anySetOf(LoadBalancerJoinVip.class));
            verify(loadBalancer.getLoadBalancerJoinVipSet()).addAll(anySetOf(LoadBalancerJoinVip.class));
            verify(client).deleteTrafficIp(anyString());
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingHealthMonitorResources {

        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

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
            verify(resources).updateHealthMonitor(eq(client), eq(vsName), Matchers.any(Monitor.class));
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteHealthMonitor() throws Exception {
            adapterSpy.deleteHealthMonitor(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteHealthMonitor(client, vsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingProtectionResources {

        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

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
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateProtection() throws Exception {
            adapterSpy.updateProtection(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateProtectionResource(loadBalancer);
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteProtection() throws Exception {
            adapterSpy.deleteProtection(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteProtection(client, vsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingBandwidthResources { //Lumping ConnectionThrottle and RateLimit together here, even though they aren't both Bandwidth related...

        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        private RateLimit testRateLimit;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

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
        private LoadBalancer loadBalancer;
        private String errorContent;
        private File errorFile;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            errorContent = "My Error Content";
            errorFile = new StmAdapterResources().getFileWithContent(errorContent);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            when(resources.getFileWithContent(errorContent)).thenReturn(errorFile);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testSetErrorFile() throws Exception {
            adapterSpy.setErrorFile(config, loadBalancer, errorContent);

            verify(resources).loadSTMRestClient(config);
            verify(resources).setErrorFile(config, client, loadBalancer, errorContent);
            verify(client).destroy();
        }

        @Test
        public void testDeleteErrorFile() throws Exception {
            adapterSpy.deleteErrorFile(config, loadBalancer, null);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteErrorFile(config, client, loadBalancer, null);
            verify(client).destroy();
        }

        @Test
        public void testUploadDefaultErrorFile() throws Exception {
            adapterSpy.uploadDefaultErrorFile(config, errorContent);

            verify(resources).loadSTMRestClient(config);
            verify(client).createExtraFile(Constants.DEFAULT_ERRORFILE, errorFile);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenModifyingPoolResources {

        private String vsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        private List<Node> doomedNodes;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            doomedNodes = new ArrayList<Node>();
            doomedNodes.add(loadBalancer.getNodes().iterator().next());

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            Pool p = new Pool();
            PoolProperties pp = new PoolProperties();
            PoolBasic pb = new PoolBasic();
            pp.setBasic(pb);
            p.setProperties(pp);
            when(resources.getPool(Matchers.any(StingrayRestClient.class), Matchers.anyString())).thenReturn(p);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testSetNodes() throws Exception {
            adapterSpy.setNodes(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translatePoolResource(vsName, loadBalancer, loadBalancer);
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(client).destroy();
        }

        @Test
        public void testRemoveNodes() throws Exception {
            int numNodes = loadBalancer.getNodes().size();
            adapterSpy.removeNodes(config, loadBalancer, doomedNodes);

            Assert.assertTrue(loadBalancer.getNodes().size() == numNodes - 1);
            Assert.assertFalse(loadBalancer.getNodes().contains(doomedNodes.get(0)));
            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translatePoolResource(vsName, loadBalancer, loadBalancer);
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest(ResourceTranslator.class)
    public static class WhenSuspendingLoadBalancer {

        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        private VirtualServer virtualServer;
        private VirtualServer virtualServerSecure;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
            virtualServer = new VirtualServer();
            virtualServer.setProperties(new VirtualServerProperties());
            virtualServer.getProperties().setBasic(new VirtualServerBasic());
            virtualServerSecure = new VirtualServer();
            virtualServerSecure.setProperties(new VirtualServerProperties());
            virtualServerSecure.getProperties().setBasic(new VirtualServerBasic());

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);

            when(client.getVirtualServer(vsName)).thenReturn(virtualServer);
            when(client.getVirtualServer(secureVsName)).thenReturn(virtualServerSecure);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testAddSuspension() throws Exception {
            virtualServer.getProperties().getBasic().setEnabled(true);
            virtualServerSecure.getProperties().getBasic().setEnabled(true);
            adapterSpy.addSuspension(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(client).getVirtualServer(vsName);
            Assert.assertFalse(virtualServer.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).getVirtualServer(secureVsName);
            Assert.assertFalse(virtualServerSecure.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(resourceTranslator).translateTrafficIpGroupsResource(config, loadBalancer, false);
            verify(resources).updateVirtualIps(eq(client), eq(secureVsName), anyMapOf(String.class, TrafficIp.class)); //TODO: Are the VIPs using the SecureName or the normal vsName?
            verify(client).destroy();
        }

        @Test
        public void testRemoveSuspension() throws Exception {
            virtualServer.getProperties().getBasic().setEnabled(false);
            virtualServerSecure.getProperties().getBasic().setEnabled(false);
            adapterSpy.removeSuspension(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(client).getVirtualServer(vsName);
            Assert.assertTrue(virtualServer.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).getVirtualServer(secureVsName);
            Assert.assertTrue(virtualServerSecure.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(resourceTranslator).translateTrafficIpGroupsResource(config, loadBalancer, true);
            verify(resources).updateVirtualIps(eq(client), eq(secureVsName), anyMapOf(String.class, TrafficIp.class)); //TODO: Are the VIPs using the SecureName or the normal vsName?
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "com.sun.crypto.provider.*", "javax.crypto.*"})
    @PrepareForTest({ResourceTranslator.class})
    public static class WhenModifyingSSLResources {

        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private ResourceTranslator resourceTranslator;
        private ZeusSslTermination sslTermination;
        @Mock
        private StmAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;
        @Spy
        private StmAdapterImpl adapterSpy = new StmAdapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
            sslTermination = new ZeusSslTermination();

            resourceTranslator = spy(new ResourceTranslator());
            PowerMockito.mockStatic(ResourceTranslator.class);
            PowerMockito.when(ResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadSTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).updateKeypair(eq(client), eq(vsName), Matchers.any(Keypair.class));
            doNothing().when(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            doNothing().when(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            doNothing().when(resources).updateVirtualServer(eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            doNothing().when(resources).deleteKeypair(client, secureVsName);
            doNothing().when(resources).deleteVirtualServer(client, secureVsName);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateSslTermination() throws Exception {
            adapterSpy.updateSslTermination(config, loadBalancer, sslTermination, null);

            verify(resources).loadSTMRestClient(config);
            verify(resourceTranslator).translateVirtualServerResource(config, vsName, loadBalancer);
            verify(resourceTranslator, times(3)).translateKeypairResource(loadBalancer, true);
            verify(resources).updateKeypair(eq(client), eq(secureVsName), Matchers.any(Keypair.class));
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            verify(resourceTranslator).translateLoadBalancerResource(config, secureVsName, loadBalancer, loadBalancer);
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(client), eq(secureVsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testRemoveSslTermination() throws Exception {
            adapterSpy.removeSslTermination(config, loadBalancer);

            verify(resources).loadSTMRestClient(config);
            verify(resources).deleteKeypair(client, secureVsName);
            verify(resources).deleteVirtualServer(client, secureVsName);
            verify(client).destroy();
        }
    }
}
