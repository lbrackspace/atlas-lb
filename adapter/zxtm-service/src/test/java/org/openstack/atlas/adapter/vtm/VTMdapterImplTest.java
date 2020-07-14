package org.openstack.atlas.adapter.vtm;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.VTMAdapterImplTestHelper;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.vtm.client.counters.GlobalCounters;
import org.rackspace.vtm.client.counters.GlobalCountersProperties;
import org.rackspace.vtm.client.counters.GlobalCountersStatistics;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.monitor.Monitor;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolBasic;
import org.rackspace.vtm.client.pool.PoolProperties;
import org.rackspace.vtm.client.protection.Protection;
import org.rackspace.vtm.client.ssl.keypair.Keypair;
import org.rackspace.vtm.client.tm.TrafficManager;
import org.rackspace.vtm.client.tm.TrafficManagerBasic;
import org.rackspace.vtm.client.tm.TrafficManagerProperties;
import org.rackspace.vtm.client.tm.Trafficip;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.virtualserver.*;
import org.rackspace.vtm.client.VTMRestClient;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class VTMdapterImplTest extends VTMAdapterImplTestHelper {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class})
    public static class WhenModifyingLoadbalancerResources {
        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
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

            verify(resources).loadVTMRestClient(config);
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
            adapterSpy.updateLoadBalancer(config, loadBalancer, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator, times(1)).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer, true, true);
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

            verify(resources).loadVTMRestClient(config);
            verify(resources).deleteRateLimit(config, loadBalancer, vsName);
            verify(resources).deleteHealthMonitor(client, vsName);
            verify(resources).deleteProtection(client, vsName);
            verify(adapterSpy).deleteVirtualIps(config, loadBalancer);
            verify(resources).deletePool(client, vsName);
            verify(resources).deleteVirtualServer(client, vsName);
            verify(resources).deleteVirtualServer(client, secureVsName);

            verify(client, times(1)).deleteExtraFile(vsName + "_error.html");
            verify(client, times(1)).deleteExtraFile(vsName + "_S_error.html");
            verify(client).destroy();
        }

        @Test
        public  void testDeleteLoadBalancerWithRedirect() throws Exception {
            loadBalancer.setHttpsRedirect(true);
            adapterSpy.deleteLoadBalancer(config, loadBalancer);
            verify(client, times(0)).deleteExtraFile(vsName + "_error.html");
            verify(client, times(1)).deleteExtraFile(vsName + "_R_error.html");
            verify(client, times(1)).deleteExtraFile(vsName + "_S_error.html");
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class, TrafficScriptHelper.class})
    public static class WhenModifyingVirtualIpResources {
        private String vsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
//            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            PowerMockito.mockStatic(TrafficScriptHelper.class);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateVirtualIps() throws Exception {
            adapterSpy.updateVirtualIps(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            verify(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteVirtualIps() throws Exception {
            List<Integer> vipsToDelete = new ArrayList<Integer>();
            for (LoadBalancerJoinVip vip : loadBalancer.getLoadBalancerJoinVipSet())
                vipsToDelete.add(vip.getVirtualIp().getId());

            adapterSpy.deleteVirtualIps(config, loadBalancer, vipsToDelete);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator, times(2)).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer, false, true);
            verify(loadBalancer.getLoadBalancerJoinVipSet()).removeAll(anySetOf(LoadBalancerJoinVip.class));
            verify(loadBalancer.getLoadBalancerJoinVipSet()).addAll(anySetOf(LoadBalancerJoinVip.class));
            verify(client).deleteTrafficIp(anyString());
            verify(resources).updateAppropriateVirtualServers(config, resourceTranslator, loadBalancer);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest(VTMResourceTranslator.class)
    public static class WhenModifyingHealthMonitorResources {
        private String vsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateHealthMonitor() throws Exception {
            adapterSpy.updateHealthMonitor(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateLoadBalancerResource(config, vsName, loadBalancer, loadBalancer);
            verify(resources).updateHealthMonitor(eq(client), eq(vsName), Matchers.any(Monitor.class));
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteHealthMonitor() throws Exception {
            adapterSpy.deleteHealthMonitor(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resources).deleteHealthMonitor(client, vsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest(VTMResourceTranslator.class)
    public static class WhenModifyingProtectionResources {
        private String vsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateProtection() throws Exception {
            adapterSpy.updateProtection(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateProtectionResource(loadBalancer);
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteProtection() throws Exception {
            adapterSpy.deleteProtection(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resources).deleteProtection(client, vsName);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest(VTMResourceTranslator.class)
    public static class WhenModifyingBandwidthResources { //Lumping ConnectionThrottle and RateLimit together here, even though they aren't both Bandwidth related...
        private String vsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;
        private RateLimit testRateLimit;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            testRateLimit = new RateLimit();

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            doNothing().when(adapterSpy).updateProtection(config, loadBalancer);
            doNothing().when(adapterSpy).deleteProtection(config, loadBalancer);
            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
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

            // verify(adapterSpy).deleteProtection(config, loadBalancer); // matching SOAP for now... :(
            verify(adapterSpy).updateProtection(config, loadBalancer);
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
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            errorContent = "My Error Content";
            errorFile = new VTMAdapterResources().getFileWithContent(errorContent);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            when(resources.getFileWithContent(errorContent)).thenReturn(errorFile);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testSetErrorFile() throws Exception {
            adapterSpy.setErrorFile(config, loadBalancer, errorContent);

            verify(resources).loadVTMRestClient(config);
            verify(resources).setErrorFile(config, client, loadBalancer, errorContent);
            verify(client).destroy();
        }

        @Test
        public void testDeleteErrorFile() throws Exception {
            adapterSpy.deleteErrorFile(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resources).deleteErrorFile(config, client, loadBalancer);
            verify(client).destroy();
        }

        @Test
        public void testUploadDefaultErrorFile() throws Exception {
            adapterSpy.uploadDefaultErrorFile(config, errorContent);

            verify(resources).loadVTMRestClient(config);
            verify(client).createExtraFile(Constants.DEFAULT_ERRORFILE, errorFile);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest(VTMResourceTranslator.class)
    public static class WhenModifyingPoolResources {
        private String vsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;
        private List<Node> doomedNodes;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            doomedNodes = new ArrayList<Node>();
            doomedNodes.add(loadBalancer.getNodes().iterator().next());

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            Pool p = new Pool();
            PoolProperties pp = new PoolProperties();
            PoolBasic pb = new PoolBasic();
            pp.setBasic(pb);
            p.setProperties(pp);
            when(resources.getPool(Matchers.any(VTMRestClient.class), Matchers.anyString())).thenReturn(p);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testSetNodes() throws Exception {
            adapterSpy.setNodes(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
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
            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translatePoolResource(vsName, loadBalancer, loadBalancer);
            verify(resources).updatePool(eq(client), eq(vsName), Matchers.any(Pool.class));
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest(VTMResourceTranslator.class)
    public static class WhenSuspendingLoadBalancer {
        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;
        private VirtualServer virtualServer;
        private VirtualServer virtualServerSecure;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

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

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);

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

            verify(resources).loadVTMRestClient(config);
            verify(client).getVirtualServer(vsName);
            Assert.assertFalse(virtualServer.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).getVirtualServer(secureVsName);
            Assert.assertFalse(virtualServerSecure.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(resourceTranslator).translateTrafficIpGroupsResource(config, loadBalancer, false);
            verify(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class)); //TODO: Are the VIPs using the SecureName or the normal vsName?
            verify(client).destroy();
        }

        @Test
        public void testRemoveSuspension() throws Exception {
            virtualServer.getProperties().getBasic().setEnabled(false);
            virtualServerSecure.getProperties().getBasic().setEnabled(false);
            adapterSpy.removeSuspension(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(client).getVirtualServer(vsName);
            Assert.assertTrue(virtualServer.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).getVirtualServer(secureVsName);
            Assert.assertTrue(virtualServerSecure.getProperties().getBasic().getEnabled());
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(resourceTranslator).translateTrafficIpGroupsResource(config, loadBalancer, true);
            verify(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class)); //TODO: Are the VIPs using the SecureName or the normal vsName?
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class})
    public static class WhenModifyingSSLResources {
        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;
        private ZeusSslTermination sslTermination;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
            sslTermination = new ZeusSslTermination();

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
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
            adapterSpy.updateSslTermination(config, loadBalancer, sslTermination);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator).translateKeypairResource(loadBalancer, true);
            verify(resources).updateKeypair(eq(client), eq(secureVsName), Matchers.any(Keypair.class));
            verify(resourceTranslator).translateLoadBalancerResource(config, secureVsName, loadBalancer, loadBalancer);
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(client), eq(secureVsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            // Ensure XFP is set appropriately
            Assert.assertTrue(resourceTranslator.getcVServer().getProperties().getHttp().getAddXForwardedProto());
            Assert.assertTrue(resourceTranslator.getcVServer().getProperties().getHttp().getAddXForwardedFor());
            Assert.assertEquals(VirtualServerHttp.LocationRewrite.NEVER, resourceTranslator.getcVServer().getProperties().getHttp().getLocationRewrite());
            verify(client).destroy();
        }

        @Test
        public void testUpdateSslTerminationNonHTTP() throws Exception {
            // Verify for that any other potentially allowed non-secure protocols httpHeaders are not set. 
            loadBalancer.setProtocol(LoadBalancerProtocol.TCP);
            adapterSpy.updateSslTermination(config, loadBalancer, sslTermination);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator).translateKeypairResource(loadBalancer, true);
            verify(resources).updateKeypair(eq(client), eq(secureVsName), Matchers.any(Keypair.class));
            verify(resourceTranslator).translateLoadBalancerResource(config, secureVsName, loadBalancer, loadBalancer);
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(client), eq(secureVsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            // Ensure XFP is set appropriately
            Assert.assertNull(resourceTranslator.getcVServer().getProperties().getHttp());
            verify(client).destroy();
        }

        @Test
        public void testRemoveSslTermination() throws Exception {
            adapterSpy.removeSslTermination(config, loadBalancer);

            verify(resources).loadVTMRestClient(config);
            verify(resources).deleteKeypair(client, secureVsName);
            verify(resources).deleteVirtualServer(client, secureVsName);
            verify(client).destroy();
        }

        @Test
        public void testUpdateSslCiphers() throws Exception {
            adapterSpy.updateSslTermination(config, loadBalancer, sslTermination);

            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator).translateKeypairResource(loadBalancer, true);
            verify(resources).updateKeypair(eq(client), eq(secureVsName), Matchers.any(Keypair.class));
            verify(resourceTranslator).translateLoadBalancerResource(config, secureVsName, loadBalancer, loadBalancer);
            verify(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            verify(resources).updateVirtualIps(eq(client), eq(secureVsName), anyMapOf(String.class, TrafficIp.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testEnableDisableTLS_11() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);

            adapterSpy.enableDisableTLS_11(config, loadBalancer, true);

            verify(resources).loadVTMRestClient(config);
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test(expected = StmRollBackException.class)
        public void testEnableDisableTLS_11Exception() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);
            doThrow(StmRollBackException.class).when(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));

            adapterSpy.enableDisableTLS_11(config, loadBalancer, true);

            verify(resources).loadVTMRestClient(config);
        }

        @Test
        public void testEnableDisableTLS_10() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);

            adapterSpy.enableDisableTLS_10(config, loadBalancer, true);

            verify(resources).loadVTMRestClient(config);
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test(expected = StmRollBackException.class)
        public void testEnableDisableTLS_10Exception() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);
            doThrow(StmRollBackException.class).when(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));

            adapterSpy.enableDisableTLS_10(config, loadBalancer, true);

            verify(resources).loadVTMRestClient(config);
        }

        @Test
        public void testRetrieveCipherSuitesByVHost() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setCipherSuites("suites");
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);

            String cs = adapterSpy.getSslCiphersByVhost(config, loadBalancer.getAccountId(), loadBalancer.getId());

            verify(resources).loadVTMRestClient(config);
            verify(client).destroy();
            Assert.assertEquals("suites", cs);
        }

        @Test(expected = EntityNotFoundException.class)
        public void testRetrieveCipherSuitesByVHostException() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setCipherSuites("");
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);

            adapterSpy.getSslCiphersByVhost(config, loadBalancer.getAccountId(), loadBalancer.getId());

            verify(resources).loadVTMRestClient(config);
            verify(client).destroy();
        }

        @Test
        public void testSetCipherSuitesByVHost() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);

            adapterSpy.setSslCiphersByVhost(config, loadBalancer.getAccountId(), loadBalancer.getId(), "suites");

            verify(resources).loadVTMRestClient(config);
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test(expected = StmRollBackException.class)
        public void testSetCipherSuitesByVHostException() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(Matchers.any())).thenReturn(vs);
            doThrow(StmRollBackException.class).when(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));


            adapterSpy.setSslCiphersByVhost(config, loadBalancer.getAccountId(), loadBalancer.getId(), "suites");

            verify(resources).loadVTMRestClient(config);
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class, ZeusUtils.class, CertUtils.class, PemUtils.class})
    public static class WhenModifyingCertificateMappingResources {
        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;
        private CertificateMapping certificateMapping;

        @Mock
        private ZeusUtils zeusUtils;
        @Mock
        private CertUtils certUtils;
        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            secureVsName = ZxtmNameBuilder.genSslVSName(loadBalancer);
            certificateMapping = new CertificateMapping();
            certificateMapping.setCertificate("thing");
            certificateMapping.setPrivateKey("thing2");
            certificateMapping.setIntermediateCertificate("thing3");
            certificateMapping.setHostName("thingHost");
            certificateMapping.setId(2);

            ZeusCrtFile zcf = new ZeusCrtFile();
            zcf.setPrivate_key(certificateMapping.getPrivateKey());
            zcf.setPublic_cert(certificateMapping.getCertificate() + certificateMapping.getIntermediateCertificate());
            PowerMockito.mockStatic(PemUtils.class);
            PowerMockito.mockStatic(CertUtils.class);
            PowerMockito.mockStatic(ZeusUtils.class);
            PowerMockito.when(zeusUtils.buildZeusCrtFileLbassValidation(certificateMapping.getPrivateKey(),
                    certificateMapping.getCertificate(),
                    certificateMapping.getIntermediateCertificate())).thenReturn(zcf);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            zeusUtils = spy(new ZeusUtils());

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            doNothing().when(resources).updateKeypair(eq(client), anyString(), Matchers.any(Keypair.class));
            doNothing().when(resources).updateProtection(eq(client), eq(vsName), Matchers.any(Protection.class));
            doNothing().when(resources).updateVirtualIps(eq(client), eq(vsName), anyMapOf(String.class, TrafficIp.class));
            doNothing().when(resources).updateVirtualServer(eq(client), eq(vsName), Matchers.any(VirtualServer.class));
            doNothing().when(resources).deleteKeypair(eq(client), anyString());
            doNothing().when(resources).deleteVirtualServer(client, secureVsName);
        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testUpdateCertificateMappingNew() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerServerCertHostMapping vshm = new VirtualServerServerCertHostMapping();
            vshm.setHost("thingHost1");
            vshm.setCertificate("cert12");
            List<VirtualServerServerCertHostMapping> vsl = new ArrayList<>();
            vsl.add(vshm);
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setServerCertHostMapping(vsl);
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(anyString())).thenReturn(vs);

            Set<CertificateMapping> cms = new HashSet<>();
            cms.add(certificateMapping);
            loadBalancer.setCertificateMappings(cms);
            adapterSpy.updateCertificateMapping(config, loadBalancer, certificateMapping);

            String cname = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId());
            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator).translateKeypairMappingsResource(loadBalancer, true);
            verify(resources).updateKeypair(eq(client), eq(cname), Matchers.any(Keypair.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client, times(0)).getVirtualServer(vsName);

            verify(client).destroy();
        }

        @Test
        public void testUpdateCertificateMappingExisting() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerServerCertHostMapping vshm = new VirtualServerServerCertHostMapping();
            vshm.setHost("thingHost");
            vshm.setCertificate("cert12");
            List<VirtualServerServerCertHostMapping> vsl = new ArrayList<>();
            vsl.add(vshm);
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setServerCertHostMapping(vsl);
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(anyString())).thenReturn(vs);

            Set<CertificateMapping> cms = new HashSet<>();
            cms.add(certificateMapping);
            loadBalancer.setCertificateMappings(cms);
            adapterSpy.updateCertificateMapping(config, loadBalancer, certificateMapping);

            String cname = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId());
            verify(resources).loadVTMRestClient(config);
            verify(resourceTranslator).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator).translateKeypairMappingsResource(loadBalancer, true);
            verify(resources).deleteKeypair(client, ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId()));
            verify(resources).updateKeypair(eq(client), eq(cname), Matchers.any(Keypair.class));
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client, times(0)).getVirtualServer(vsName);
            verify(client).destroy();
        }

        @Test
        public void testUpdateLoadbalancerWithCertificateMappings() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerServerCertHostMapping vshm = new VirtualServerServerCertHostMapping();
            vshm.setHost("thingHost");
            vshm.setCertificate("cert12");
            List<VirtualServerServerCertHostMapping> vsl = new ArrayList<>();
            vsl.add(vshm);
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setServerCertHostMapping(vsl);
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(anyString())).thenReturn(vs);

            Set<CertificateMapping> cms = new HashSet<>();
            cms.add(certificateMapping);
            loadBalancer.setCertificateMappings(cms);
            adapterSpy.updateLoadBalancer(config, loadBalancer, loadBalancer);

            String cname = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId());
            // Called for each resource
            verify(resources, times(3)).loadVTMRestClient(config);
            verify(resourceTranslator, times(2)).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator, times(2)).translateKeypairMappingsResource(loadBalancer, true);
            verify(resources, times(1)).deleteKeypair(client, ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId()));
            verify(resources, times(1)).updateKeypair(eq(client), eq(cname), Matchers.any(Keypair.class));
            verify(resources, times(1)).updateKeypair(eq(client), eq(secureVsName), Matchers.any(Keypair.class));
            verify(resources, times(2)).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client, times(3)).destroy();
        }

        @Test
        public void testUpdateLoadbalancerWithMultipleCertificateMappings() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerServerCertHostMapping vshm = new VirtualServerServerCertHostMapping();
            VirtualServerServerCertHostMapping vshm2 = new VirtualServerServerCertHostMapping();
            vshm.setHost("thingHost");
            vshm.setCertificate("cert12");
            vshm2.setHost("thingHost2");
            vshm2.setCertificate("cert22");
            List<VirtualServerServerCertHostMapping> vsl = new ArrayList<>();
            vsl.add(vshm);
            vsl.add(vshm2);
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setServerCertHostMapping(vsl);
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(anyString())).thenReturn(vs);

            Set<CertificateMapping> cms = new HashSet<>();
            CertificateMapping cmap = new CertificateMapping();
            cmap.setCertificate("thing3");
            cmap.setPrivateKey("thing23");
            cmap.setIntermediateCertificate("thing33");
            cmap.setHostName("thingHost3");
            cmap.setId(3);
            cms.add(cmap);
            cms.add(certificateMapping);
            loadBalancer.setCertificateMappings(cms);
            adapterSpy.updateLoadBalancer(config, loadBalancer, loadBalancer);

            String cname = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId());
            // Called for each resource
            verify(resources, times(3)).loadVTMRestClient(config);
//            verify(zeusUtils, times(2)).buildZeusCrtFileLbassValidation(anyString(), anyString(), anyString());
            verify(resourceTranslator, times(2)).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator, times(2)).translateKeypairMappingsResource(loadBalancer, true);
            verify(resources, times(1)).deleteKeypair(client, ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId()));
            verify(resources, times(1)).deleteKeypair(client, ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), cmap.getId()));
            verify(resources, times(1)).updateKeypair(eq(client), eq(cname), Matchers.any(Keypair.class));
            verify(resources, times(1)).updateKeypair(eq(client), eq(secureVsName), Matchers.any(Keypair.class));
            verify(resources, times(2)).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client, times(3)).destroy();
        }

        @Test
        public void testDeleteCertificateMapping() throws Exception {
            adapterSpy.deleteCertificateMapping(config, loadBalancer, certificateMapping);
            String cn = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId());
            verify(resources).loadVTMRestClient(config);
            verify(resources).deleteKeypair(client, cn);
            verify(resources).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client).destroy();
        }

        @Test
        public void testDeleteWithMultipleCertificateMappings() throws Exception {
            VirtualServer vs = new VirtualServer();
            VirtualServerServerCertHostMapping vshm = new VirtualServerServerCertHostMapping();
            VirtualServerServerCertHostMapping vshm2 = new VirtualServerServerCertHostMapping();
            vshm.setHost("thingHost");
            vshm.setCertificate("cert12");
            vshm2.setHost("thingHost2");
            vshm2.setCertificate("cert22");
            List<VirtualServerServerCertHostMapping> vsl = new ArrayList<>();
            vsl.add(vshm);
            vsl.add(vshm2);
            VirtualServerProperties vsp = new VirtualServerProperties();
            VirtualServerSsl vsssl = new VirtualServerSsl();
            vsssl.setServerCertHostMapping(vsl);
            vsp.setSsl(vsssl);
            vs.setProperties(vsp);
            when(client.getVirtualServer(anyString())).thenReturn(vs);

            Set<CertificateMapping> cms = new HashSet<>();
            CertificateMapping cmap = new CertificateMapping();
            cmap.setCertificate("thing3");
            cmap.setPrivateKey("thing23");
            cmap.setIntermediateCertificate("thing33");
            cmap.setHostName("thingHost3");
            cmap.setId(3);
            cms.add(cmap);
            cms.add(certificateMapping);
            loadBalancer.setCertificateMappings(cms);
            adapterSpy.deleteCertificateMapping(config, loadBalancer, cmap);

            String cname = ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), certificateMapping.getId());
            // Called for each resource
            verify(resources, times(1)).loadVTMRestClient(config);
//            verify(zeusUtils, times(2)).buildZeusCrtFileLbassValidation(anyString(), anyString(), anyString());
            verify(resourceTranslator, times(1)).translateVirtualServerResource(config, secureVsName, loadBalancer);
            verify(resourceTranslator, times(0)).translateKeypairMappingsResource(loadBalancer, true);
            verify(resources, times(1)).deleteKeypair(client, ZxtmNameBuilder.generateCertificateName(loadBalancer.getId(),
                    loadBalancer.getAccountId(), cmap.getId()));
            verify(resources, times(0)).updateKeypair(eq(client), eq(cname), Matchers.any(Keypair.class));
            verify(resources, times(1)).updateVirtualServer(eq(client), eq(secureVsName), any(VirtualServer.class));
            verify(client, times(0)).getVirtualServer(vsName);
            verify(client, times(1)).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class})
    public static class whenRetrievingHostCounters {

        private String vsName;
        private String host;
        private GlobalCounters globalCounters;
        private VTMResourceTranslator resourceTranslator;

        @Mock
        VTMRestClient client;
        @Mock
        LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMAdapterResources resources;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            globalCounters = new GlobalCounters();
            GlobalCountersStatistics globalCountersStatistics = new GlobalCountersStatistics();
            globalCountersStatistics.setTotalBytesIn(10L);
            globalCountersStatistics.setTotalBytesOut(20L);
            globalCountersStatistics.setTotalCurrentConn(2);
            GlobalCountersProperties globalCountersProperties = new GlobalCountersProperties();
            globalCountersProperties.setStatistics(globalCountersStatistics);
            globalCounters.setProperties(globalCountersProperties);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            when(client.getGlobalCounters(any())).thenReturn(globalCounters);

        }

        @Test
        public void testGetTotalCurrentConnectionsForHost() throws Exception {
            int totalCurrentConnectionsForHost = adapterSpy.getTotalCurrentConnectionsForHost(config);
            Assert.assertEquals(2, totalCurrentConnectionsForHost);
            verify(client, times(1)).getGlobalCounters(any());
            verify(client).destroy();

        }

        @Test(expected = VTMRestClientException.class)
        public void testGetTotalCurrentConnectionsForHostShouldHandleException() throws Exception {
            when(client.getGlobalCounters(any())).thenThrow(URISyntaxException.class);

            adapterSpy.getTotalCurrentConnectionsForHost(config);
            verify(client, times(1)).getTrafficManager(host);
            verify(client).destroy();

        }

        @Test
        public void testGetBytesIn() throws Exception {
            long bytesIn = adapterSpy.getHostBytesIn(config);
            Assert.assertEquals(10, bytesIn);
            verify(client, times(1)).getGlobalCounters(any());
            verify(client).destroy();

        }

        @Test(expected = VTMRestClientException.class)
        public void testGetBytesInShouldHandleException() throws Exception {
            when(client.getGlobalCounters(any())).thenThrow(URISyntaxException.class);

            adapterSpy.getHostBytesIn(config);
            verify(client, times(1)).getTrafficManager(host);
            verify(client).destroy();

        }

        @Test
        public void testGetBytesOut() throws Exception {
            long bytesOut = adapterSpy.getHostBytesOut(config);
            Assert.assertEquals(20, bytesOut);
            verify(client, times(1)).getGlobalCounters(any());
            verify(client).destroy();

        }

        @Test(expected = VTMRestClientException.class)
        public void testGetBytesOutShouldHandleException() throws Exception {
            when(client.getGlobalCounters(any())).thenThrow(URISyntaxException.class);

            adapterSpy.getHostBytesOut(config);
            verify(client, times(1)).getTrafficManager(host);
            verify(client).destroy();

        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class})
    public static class whenGettingSubnetMappings {

        private String vsName;
        private String host;
        private Set<String> networks = new LinkedHashSet<>();
        private List<Trafficip> trafficIps;
        private TrafficManager trafficManager;
        private Trafficip trafficip;
        private LoadBalancer loadBalancer;
        private VTMResourceTranslator resourceTranslator;

        @Mock
        VTMRestClient client;
        @Mock
        LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMAdapterResources resources;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            trafficip = new Trafficip();
            trafficip.setName("testTrafficIp");
            networks.add("1.23.4.5");
            networks.add("6.7.8.9.10");
            trafficip.setNetworks(networks);
            trafficIps = new ArrayList<>();
            trafficIps.add(trafficip);
            ArrayList<NetInterface> netInterfaces = new ArrayList<>();
            NetInterface ni1 = new NetInterface();
            ni1.setCidrs(new ArrayList<>());
            netInterfaces.add(ni1);
            host = "Test";
            trafficManager = new TrafficManager();
            TrafficManagerProperties trafficManagerProperties = new TrafficManagerProperties();
            TrafficManagerBasic trafficManagerBasic = new TrafficManagerBasic();
            trafficManagerBasic.setTrafficip(trafficIps);
            trafficManagerProperties.setBasic(trafficManagerBasic);
            trafficManager.setProperties(trafficManagerProperties);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            when(client.getTrafficManager(any())).thenReturn(trafficManager);

        }

        @Test
        public void testGetSubnetMappingsShouldReturnHostssubnet() throws Exception {
            Hostssubnet hostssubnet = adapterSpy.getSubnetMappings(config, host);
            Assert.assertEquals("1.23.4.5", hostssubnet.getHostsubnets().get(0).getNetInterfaces().get(0).getCidrs().get(0).getBlock());
            Assert.assertEquals("testTrafficIp", hostssubnet.getHostsubnets().get(0).getNetInterfaces().get(0).getName());
            verify(client, times(1)).getTrafficManager(host);
            verify(client).destroy();

        }

        @Test(expected = StmRollBackException.class)
        public void testGetHostSubnetShouldRollback() throws Exception {
            when(client.getTrafficManager(host)).thenThrow(VTMRestClientException.class);

            adapterSpy.getSubnetMappings(config, host);
            verify(client, times(1)).getTrafficManager(host);
            verify(client).destroy();

        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class})
    public static class WhenModifyingHostSubnetResources {
        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private ArrayList<Hostsubnet> hostssubnetList;
        private Hostssubnet hostssubnet;
        private Hostsubnet hostsubnet;
        private ArrayList<NetInterface> netInterfaces;
        private TrafficManager trafficManager;
        private VTMResourceTranslator resourceTranslator;
        private ZeusSslTermination sslTermination;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            hostsubnet = new Hostsubnet();
            hostssubnet = new Hostssubnet();
            hostssubnetList = new ArrayList<>();
            netInterfaces = new ArrayList<>();
            NetInterface ni1 = new NetInterface();
            ni1.setCidrs(new ArrayList<>());
            netInterfaces.add(ni1);
            hostsubnet.setNetInterfaces(netInterfaces);
            hostssubnetList.add(hostsubnet);
            hostssubnet.setHostsubnets(hostssubnetList);

            trafficManager = new TrafficManager();
            TrafficManagerProperties trafficManagerProperties = new TrafficManagerProperties();
            TrafficManagerBasic trafficManagerBasic = new TrafficManagerBasic();
            trafficManagerProperties.setBasic(trafficManagerBasic);
            trafficManager.setProperties(trafficManagerProperties);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            doReturn(trafficManager).when(client).updateTrafficManager(eq(hostsubnet.getName()), Matchers.any(TrafficManager.class));

        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testSetHostSubnet() throws Exception {
            when(client.getTrafficManager(hostsubnet.getName())).thenReturn(trafficManager);
            adapterSpy.setSubnetMappings(config, hostssubnet);

            verify(client, times(1)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
            Assert.assertEquals(1, trafficManager.getProperties().getBasic().getTrafficip().size());
        }

        @Test
        public void testSetHostSubnetMultipleHosts() throws Exception {
            NetInterface ni2 = new NetInterface();
            Cidr c2 = new Cidr();
            ArrayList<Cidr> c2list = new ArrayList<>();
            ni2.setName("t2");
            c2.setBlock("b2");
            c2list.add(c2);
            ni2.setCidrs(c2list);
            netInterfaces.add(ni2);
            hostsubnet.setNetInterfaces(netInterfaces);
            hostssubnetList.add(hostsubnet);
            hostssubnet.setHostsubnets(hostssubnetList);

            when(client.getTrafficManager(hostsubnet.getName())).thenReturn(trafficManager);
            adapterSpy.setSubnetMappings(config, hostssubnet);

            verify(client, times(2)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
            Assert.assertEquals(2, trafficManager.getProperties().getBasic().getTrafficip().size());
        }

        @Test(expected = StmRollBackException.class)
        public void testSetHostSubnetShouldRollback() throws Exception {
            when(client.getTrafficManager(hostsubnet.getName())).thenThrow(VTMRestClientException.class);
            adapterSpy.setSubnetMappings(config, hostssubnet);

            verify(client, times(1)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"org.bouncycastle.*", "javax.management.*"})
    @PrepareForTest({VTMResourceTranslator.class})
    public static class WhenRemovingHostSubnetResources {
        private String vsName;
        private String secureVsName;
        private LoadBalancer loadBalancer;
        private ArrayList<Hostsubnet> hostssubnetList;
        private Hostssubnet hostssubnet;
        private Hostsubnet hostsubnet;
        private ArrayList<NetInterface> netInterfaces;
        private TrafficManager trafficManager;
        private VTMResourceTranslator resourceTranslator;
        private ZeusSslTermination sslTermination;

        @Mock
        private VTMAdapterResources resources;
        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private VTMRestClient client;
        @Spy
        private VTMadapterImpl adapterSpy = new VTMadapterImpl();

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            loadBalancer = generateLoadBalancer();
            vsName = ZxtmNameBuilder.genVSName(loadBalancer);
            hostsubnet = new Hostsubnet();
            hostssubnet = new Hostssubnet();
            hostssubnetList = new ArrayList<>();
            netInterfaces = new ArrayList<>();
            NetInterface ni1 = new NetInterface();
            ni1.setCidrs(new ArrayList<>());
            ni1.setName("ni1");
            netInterfaces.add(ni1);
            hostsubnet.setName("h1");
            hostsubnet.setNetInterfaces(netInterfaces);
            hostssubnetList.add(hostsubnet);
            hostssubnet.setHostsubnets(hostssubnetList);

            trafficManager = new TrafficManager();
            TrafficManagerProperties trafficManagerProperties = new TrafficManagerProperties();
            TrafficManagerBasic trafficManagerBasic = new TrafficManagerBasic();
            trafficManagerProperties.setBasic(trafficManagerBasic);
            trafficManager.setProperties(trafficManagerProperties);

            resourceTranslator = spy(new VTMResourceTranslator());
            PowerMockito.mockStatic(VTMResourceTranslator.class);
            PowerMockito.when(VTMResourceTranslator.getNewResourceTranslator()).thenReturn(resourceTranslator);

            when(adapterSpy.getResources()).thenReturn(resources);
            when(resources.loadVTMRestClient(config)).thenReturn(client);
            doReturn(trafficManager).when(client).updateTrafficManager(eq(hostsubnet.getName()), Matchers.any(TrafficManager.class));

        }

        @After
        public void tearDown() {
            //TODO figure out if I need to do any actual cleanup
        }

        @Test
        public void testDeleteHostSubnet() throws Exception {
            when(client.getTrafficManager(hostsubnet.getName())).thenReturn(trafficManager);
            adapterSpy.deleteSubnetMappings(config, hostssubnet);

            verify(client, times(1)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
            Assert.assertEquals(0, trafficManager.getProperties().getBasic().getTrafficip().size());

        }

        @Test
        public void testDeleteHostSubnetMultipleHosts() throws Exception {
            NetInterface ni2 = new NetInterface();
            Cidr c2 = new Cidr();
            ArrayList<Cidr> c2list = new ArrayList<>();
            ni2.setName("t2");
            c2.setBlock("b2");
            c2list.add(c2);
            ni2.setCidrs(c2list);
            netInterfaces.add(ni2);
            hostsubnet.setNetInterfaces(netInterfaces);
            hostssubnetList.add(hostsubnet);
            hostssubnet.setHostsubnets(hostssubnetList);

            when(client.getTrafficManager(hostsubnet.getName())).thenReturn(trafficManager);
            adapterSpy.deleteSubnetMappings(config, hostssubnet);

            verify(client, times(2)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
            Assert.assertEquals(0, trafficManager.getProperties().getBasic().getTrafficip().size());
        }

        @Test
        public void testDeleteHostSubnetMultipleCidrs() throws Exception {
            NetInterface ni2 = new NetInterface();
            Cidr c2 = new Cidr();
            ArrayList<Cidr> c2list = new ArrayList<>();
            ni2.setName("ni2");
            c2.setBlock("b2");
            c2list.add(c2);
            ni2.setCidrs(c2list);
            netInterfaces.add(ni2);
            hostsubnet.setNetInterfaces(netInterfaces);
            hostssubnetList = new ArrayList<>();
            hostssubnetList.add(hostsubnet);
            hostssubnet.setHostsubnets(hostssubnetList);

            Set<String> nets = new HashSet<>();
            nets.add("b1");
            Trafficip tip = new Trafficip();
            tip.setNetworks(nets);
            tip.setName("ni1");
            Set<String> nets2 = new HashSet<>();
            nets.add("b2");
            Trafficip tip2 = new Trafficip();
            tip2.setNetworks(nets2);
            tip.setName("ni2");
            ArrayList<Trafficip> tips = new ArrayList<>();
            tips.add(tip);
            trafficManager.getProperties().getBasic().setTrafficip(tips);

            when(client.getTrafficManager(hostsubnet.getName())).thenReturn(trafficManager);
            Assert.assertEquals(2, trafficManager.getProperties().getBasic().getTrafficip().get(0).getNetworks().size());

            adapterSpy.deleteSubnetMappings(config, hostssubnet);

            verify(client, times(1)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
            Assert.assertEquals(1, trafficManager.getProperties().getBasic().getTrafficip().get(0).getNetworks().size());
        }

        @Test(expected = StmRollBackException.class)
        public void testDeleteHostSubnetShouldRollback() throws Exception {
            when(client.getTrafficManager(hostsubnet.getName())).thenThrow(VTMRestClientException.class);
            adapterSpy.deleteSubnetMappings(config, hostssubnet);

            verify(client, times(1)).updateTrafficManager(hostsubnet.getName(), trafficManager);
            verify(client).destroy();
        }
    }
}