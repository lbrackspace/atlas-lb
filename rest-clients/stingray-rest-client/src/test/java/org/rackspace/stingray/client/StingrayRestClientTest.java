package org.rackspace.stingray.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.action.ActionScript;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.config.ClientConfigKeys;
import org.rackspace.stingray.client.config.StingrayRestClientConfiguration;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.extra.file.ExtraFile;
import org.rackspace.stingray.client.glb.GlobalLoadBalancing;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.list.Children;
import org.rackspace.stingray.client.location.Location;
import org.rackspace.stingray.client.manager.RequestManager;
import org.rackspace.stingray.client.manager.StingrayRequestManagerTest;
import org.rackspace.stingray.client.manager.StingrayRestClientManager;
import org.rackspace.stingray.client.manager.util.StingrayRequestManagerUtil;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.rate.Rate;
import org.rackspace.stingray.client.settings.GlobalSettings;
import org.rackspace.stingray.client.ssl.client.keypair.ClientKeypair;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.tm.TrafficManager;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.trafficscript.Trafficscript;
import org.rackspace.stingray.client.util.ClientConstants;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class StingrayRestClientTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenGettingAListOfItems {
        @Mock
        private RequestManager requestManager;
        private StingrayRestClientManager stingrayRestClientManager;
        @Mock
        private Response mockedResponse;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;

        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient = new StingrayRestClient();
            stingrayRestClient.setRequestManager(requestManager);
            stingrayRestClientManager = spy(new StingrayRestClient());

            Children children = new Children();
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(children);
            when(requestManager.getList(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(mockedResponse);
        }

        @Test
        public void getItemShouldThrowExceptionWhenPathIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException, NoSuchMethodException {
            Method getItems = StingrayRestClient.class.getDeclaredMethod("getItems", String.class);
            getItems.setAccessible(true);
            try {
                getItems.invoke(stingrayRestClient, "BROKENPATH");
            } catch (Exception vex) {
                if (vex.getCause() instanceof StingrayRestClientException) {
                    Assert.assertTrue(true);
                } else {
                    Assert.assertTrue(false);
                }
            }
        }

        @Test
        public void getPoolsShouldReturnWhenPathValid() throws Exception {
            List<Child> pools = stingrayRestClient.getPools();
            Assert.assertNotNull(pools);
        }

        @Test(expected = NullPointerException.class)
        public void getPoolsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> pools = stingrayRestClient.getPools();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getPoolsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> pools = stingrayRestClient.getPools();
        }

        @Test
        public void getActionScriptsShouldReturnWhenPathValid() throws Exception {
            List<Child> actionScript = stingrayRestClient.getActionScripts();
            Assert.assertNotNull(actionScript);
        }

        @Test(expected = NullPointerException.class)
        public void getActionScriptsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> actionScript = stingrayRestClient.getActionScripts();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getActionScriptsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> actionScript = stingrayRestClient.getActionScripts();
        }

        @Test
        public void getBandwidthsShouldReturnWhenPathValid() throws Exception {
            List<Child> bandwidths = stingrayRestClient.getBandwidths();
            Assert.assertNotNull(bandwidths);
        }

        @Test(expected = NullPointerException.class)
        public void getBandwidthsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> bandwidths = stingrayRestClient.getBandwidths();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getABandwidthshouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> bandwidths = stingrayRestClient.getBandwidths();
        }

        @Test
        public void getExtraFilesShouldReturnWhenPathValid() throws Exception {
            List<Child> extraFiles = stingrayRestClient.getExtraFiles();
            Assert.assertNotNull(extraFiles);
        }

        @Test(expected = NullPointerException.class)
        public void getExtraFilesShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> extraFiles = stingrayRestClient.getExtraFiles();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getAExtraFilesShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> extraFiles = stingrayRestClient.getExtraFiles();
        }

        @Test
        public void getGLBsShouldReturnWhenPathValid() throws Exception {
            List<Child> glbs = stingrayRestClient.getGlbs();
            Assert.assertNotNull(glbs);
        }

        @Test(expected = NullPointerException.class)
        public void getGLBsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> glbs = stingrayRestClient.getGlbs();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getGLBsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> glbs = stingrayRestClient.getGlbs();
        }

        @Test
        public void getLocationsShouldReturnWhenPathValid() throws Exception {
            List<Child> locations = stingrayRestClient.getLocations();
            Assert.assertNotNull(locations);
        }

        @Test(expected = NullPointerException.class)
        public void getLocationsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> locations = stingrayRestClient.getLocations();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getLocationsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> locations = stingrayRestClient.getLocations();
        }

        @Test
        public void getMonitorsShouldReturnWhenPathValid() throws Exception {
            List<Child> monitors = stingrayRestClient.getMonitors();
            Assert.assertNotNull(monitors);
        }

        @Test(expected = NullPointerException.class)
        public void getMonitorsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> monitors = stingrayRestClient.getMonitors();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getMonitorsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> monitors = stingrayRestClient.getMonitors();
        }

        @Test
        public void getMonitorScriptsShouldReturnWhenPathValid() throws Exception {
            List<Child> monitorScripts = stingrayRestClient.getMonitorScripts();
            Assert.assertNotNull(monitorScripts);
        }

        @Test(expected = NullPointerException.class)
        public void getMonitorScriptsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> monitorScripts = stingrayRestClient.getMonitorScripts();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getMonitorScriptsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> monitorScripts = stingrayRestClient.getMonitorScripts();
        }

        @Test
        public void getPersistencesShouldReturnWhenPathValid() throws Exception {
            List<Child> persistences = stingrayRestClient.getPersistences();
            Assert.assertNotNull(persistences);
        }

        @Test(expected = NullPointerException.class)
        public void getPersistencesShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> persistences = stingrayRestClient.getPersistences();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getPersistencesShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> persistences = stingrayRestClient.getPersistences();
        }

        @Test
        public void getProtectionsShouldReturnWhenPathValid() throws Exception {
            List<Child> protections = stingrayRestClient.getProtections();
            Assert.assertNotNull(protections);
        }

        @Test(expected = NullPointerException.class)
        public void getProtectionsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> protections = stingrayRestClient.getProtections();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getProtectionsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> protections = stingrayRestClient.getProtections();
        }

        @Test
        public void getRatesShouldReturnWhenPathValid() throws Exception {
            List<Child> rates = stingrayRestClient.getRates();
            Assert.assertNotNull(rates);
        }

        @Test(expected = NullPointerException.class)
        public void getRatesShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> rates = stingrayRestClient.getRates();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getRatesShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> rates = stingrayRestClient.getRates();
        }

        @Test
        public void getCacrlsShouldReturnWhenPathValid() throws Exception {
            List<Child> carcls = stingrayRestClient.getCacrls();
            Assert.assertNotNull(carcls);
        }

        @Test(expected = NullPointerException.class)
        public void getCacrlsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> carcls = stingrayRestClient.getCacrls();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getCacrlsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> carcls = stingrayRestClient.getCacrls();
        }

        @Test
        public void getClientKeypairsShouldReturnWhenPathValid() throws Exception {
            List<Child> clientKeypairs = stingrayRestClient.getClientKeypairs();
            Assert.assertNotNull(clientKeypairs);
        }

        @Test(expected = NullPointerException.class)
        public void getClientKeypairsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> clientKeypairs = stingrayRestClient.getClientKeypairs();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getClientKeypairsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> clientKeypairs = stingrayRestClient.getClientKeypairs();
        }

        @Test
        public void getKeypairsShouldReturnWhenPathValid() throws Exception {
            List<Child> keypairs = stingrayRestClient.getKeypairs();
            Assert.assertNotNull(keypairs);
        }

        @Test(expected = NullPointerException.class)
        public void getKeypairsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> keypairs = stingrayRestClient.getKeypairs();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getKeypairsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> keypairs = stingrayRestClient.getKeypairs();
        }

        @Test
        public void getTrafficManagersShouldReturnWhenPathValid() throws Exception {
            List<Child> trafficManagers = stingrayRestClient.getTrafficManagers();
            Assert.assertNotNull(trafficManagers);
        }

        @Test(expected = NullPointerException.class)
        public void getTrafficManagersShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> trafficManagers = stingrayRestClient.getTrafficManagers();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getTrafficManagersShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> trafficManagers = stingrayRestClient.getTrafficManagers();
        }

        @Test
        public void getTrafficScriptsShouldReturnWhenPathValid() throws Exception {
            List<Child> trafficScripts = stingrayRestClient.getActionScripts();
            Assert.assertNotNull(trafficScripts);
        }

        @Test(expected = NullPointerException.class)
        public void getTrafficScriptsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> trafficScripts = stingrayRestClient.getActionScripts();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getTrafficScriptsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> trafficScripts = stingrayRestClient.getActionScripts();
        }

        @Test
        public void getTrafficIpsShouldReturnWhenPathValid() throws Exception {
            List<Child> trafficIps = stingrayRestClient.getTrafficIps();
            Assert.assertNotNull(trafficIps);
        }

        @Test(expected = NullPointerException.class)
        public void getTrafficIpsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> trafficIps = stingrayRestClient.getTrafficIps();

        }

        @Test(expected = StingrayRestClientException.class)
        public void getTrafficIpsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(StingrayRestClientException.class);
            List<Child> trafficIps = stingrayRestClient.getTrafficIps();
        }

    }

    @RunWith(MockitoJUnitRunner.class)
    public static class whenGettingAnItem {
        @Mock
        private RequestManager requestManager;
        private StingrayRestClientManager stingrayRestClientManager;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;
        private Pool pool;
        private VirtualServer virtualServer;
        private File file;
        private GlobalLoadBalancing glb;
        private Location location;
        private Monitor monitor;
        private Persistence persistence;
        private Protection protection;
        private Rate rate;
        private ClientKeypair clientKeypair;
        private Keypair keypair;
        private TrafficManager trafficManager;
        private TrafficIp trafficIp;
        private GlobalSettings globalSettings;




        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
            stingrayRestClientManager = spy(new StingrayRestClient());
            pool = new Pool();
            virtualServer = new VirtualServer();
            file = new File("12345_1234", "test");
            glb = new GlobalLoadBalancing();
            location = new Location();
            monitor = new Monitor();
            persistence = new Persistence();
            protection = new Protection();
            rate = new Rate();
            clientKeypair = new ClientKeypair();
            keypair = new Keypair();
            trafficManager = new TrafficManager();
            trafficIp = new TrafficIp();
            stingrayRestClientManager = spy(new StingrayRestClient());
            vsName = "12345_1234";
            globalSettings = new GlobalSettings();

            when(mockedResponse.readEntity(GlobalSettings.class)).thenReturn(globalSettings);
            when(mockedResponse.readEntity(TrafficIp.class)).thenReturn(trafficIp);
            when(mockedResponse.readEntity(TrafficManager.class)).thenReturn(trafficManager);
            when(mockedResponse.readEntity(Keypair.class)).thenReturn(keypair);
            when(mockedResponse.readEntity(ClientKeypair.class)).thenReturn(clientKeypair);
            when(mockedResponse.readEntity(Rate.class)).thenReturn(rate);
            when(mockedResponse.readEntity(Protection.class)).thenReturn(protection);
            when(mockedResponse.readEntity(Persistence.class)).thenReturn(persistence);
            when(mockedResponse.readEntity(Monitor.class)).thenReturn(monitor);
            when(mockedResponse.readEntity(Location.class)).thenReturn(location);
            when(mockedResponse.readEntity(GlobalLoadBalancing.class)).thenReturn(glb);
            when(mockedResponse.readEntity(File.class)).thenReturn(file);
            when(mockedResponse.readEntity(VirtualServer.class)).thenReturn(virtualServer);
            when(mockedResponse.readEntity(Pool.class)).thenReturn(pool);
            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(bandwidth);
            when(requestManager.getItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
        }

        @Test
        public void getItemShouldThrowExceptionWhenPathIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException, NoSuchMethodException {
            Method getItem = StingrayRestClient.class.getDeclaredMethod("getItem", String.class, Class.class, String.class);
            getItem.setAccessible(true);
            try {
                getItem.invoke(stingrayRestClient, "vsname", Bandwidth.class, "BROKENPATH");
            } catch (Exception vex) {
                if (vex.getCause() instanceof StingrayRestClientException) {
                    Assert.assertTrue(true);
                } else {
                    Assert.assertTrue(false);
                }
            }
        }

        @Test
        public void getBandwidthShouldReturnABandwidth() throws Exception {
            Bandwidth bandwidth = stingrayRestClient.getBandwidth(vsName);
            Assert.assertNotNull(bandwidth);
        }

        @Test
        public void getBandWidthShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(null);
            Bandwidth bandwidthThree = stingrayRestClient.getBandwidth(vsName);
            Assert.assertNull(bandwidthThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getBandwidthShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Bandwidth bandwidthThree = stingrayRestClient.getBandwidth(vsName);
        }
        @Test
        public void getVirtualServerShouldReturnAVirtualServer() throws Exception {
            VirtualServer virtualServer = stingrayRestClient.getVirtualServer(vsName);
            Assert.assertNotNull(virtualServer);
        }

        @Test
        public void getVirtualServerShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(VirtualServer.class)).thenReturn(null);
            VirtualServer virtualServerThree = stingrayRestClient.getVirtualServer(vsName);
            Assert.assertNull(virtualServerThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getVirtualServerShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            VirtualServer virtualServerThree = stingrayRestClient.getVirtualServer(vsName);
        }

        @Test
        public void getPoolShouldReturnAPool() throws Exception {
            Pool pool = stingrayRestClient.getPool(vsName);
            Assert.assertNotNull(pool);
        }

        @Test
        public void getPoolShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Pool.class)).thenReturn(null);
            Pool pool = stingrayRestClient.getPool(vsName);
            Assert.assertNull(pool);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getPoolShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Pool pool = stingrayRestClient.getPool(vsName);
        }

        @Test
        public void getActionScriptShouldReturnAnActionScript() throws Exception {
            File actionScript = stingrayRestClient.getActionScript(vsName);
            Assert.assertNotNull(actionScript);
        }

        @Test
        public void getActionScriptShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File actionScript = stingrayRestClient.getActionScript(vsName);
            Assert.assertNull(actionScript);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getActionScriptShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            File actionScript = stingrayRestClient.getActionScript(vsName);
        }

        @Test
        public void getExtraFileShouldReturnAnExtraFile() throws Exception {
            File extraFile = stingrayRestClient.getExtraFile(vsName);
            Assert.assertNotNull(extraFile);
        }

        @Test
        public void getExtraFileShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File extraFile = stingrayRestClient.getExtraFile(vsName);
            Assert.assertNull(extraFile);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getExtraFileShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            File extraFile = stingrayRestClient.getExtraFile(vsName);
        }

        @Test
        public void getGLBShouldReturnAGLB() throws Exception {
            GlobalLoadBalancing glb = stingrayRestClient.getGlb(vsName);
            Assert.assertNotNull(glb);
        }

        @Test
        public void getGLBShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(GlobalLoadBalancing.class)).thenReturn(null);
            GlobalLoadBalancing glb = stingrayRestClient.getGlb(vsName);
            Assert.assertNull(glb);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getGLBShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            GlobalLoadBalancing glb = stingrayRestClient.getGlb(vsName);
        }

        @Test
        public void getLocationShouldReturnALocation() throws Exception {
            Location location = stingrayRestClient.getLocation(vsName);
            Assert.assertNotNull(location);
        }

        @Test
        public void getLocationShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Location.class)).thenReturn(null);
            Location location = stingrayRestClient.getLocation(vsName);
            Assert.assertNull(location);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getLocationShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Location location = stingrayRestClient.getLocation(vsName);
        }

        @Test
        public void getMonitorShouldReturnAMonitor() throws Exception {
            Monitor monitor = stingrayRestClient.getMonitor(vsName);
            Assert.assertNotNull(monitor);
        }

        @Test
        public void getMonitorShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Monitor.class)).thenReturn(null);
            Monitor monitor = stingrayRestClient.getMonitor(vsName);
            Assert.assertNull(monitor);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getMonitorShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Monitor monitor = stingrayRestClient.getMonitor(vsName);
        }

        @Test
        public void getMonitorScriptShouldReturnAMonitorScript() throws Exception {
            File monitorScript = stingrayRestClient.getMonitorScript(vsName);
            Assert.assertNotNull(monitorScript);
        }

        @Test
        public void getMonitorScriptShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File monitorScript = stingrayRestClient.getMonitorScript(vsName);
            Assert.assertNull(monitorScript);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getMonitorScriptShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            File monitorScript = stingrayRestClient.getMonitorScript(vsName);
        }

        @Test
        public void getPersistenceShouldReturnAPersistence() throws Exception {
            Persistence persistence = stingrayRestClient.getPersistence(vsName);
            Assert.assertNotNull(persistence);
        }

        @Test
        public void getPersistenceShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Persistence.class)).thenReturn(null);
            Persistence persistence = stingrayRestClient.getPersistence(vsName);
            Assert.assertNull(persistence);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getPersistenceShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Persistence persistence = stingrayRestClient.getPersistence(vsName);
        }

        @Test
        public void getProtectionShouldReturnAProtection() throws Exception {
            Protection protection = stingrayRestClient.getProtection(vsName);
            Assert.assertNotNull(protection);
        }

        @Test
        public void getProtectionShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Protection.class)).thenReturn(null);
            Protection protection = stingrayRestClient.getProtection(vsName);
            Assert.assertNull(protection);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getProtectionShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Protection protection = stingrayRestClient.getProtection(vsName);
        }

        @Test
        public void getRateShouldReturnARate() throws Exception {
            Rate rate = stingrayRestClient.getRate(vsName);
            Assert.assertNotNull(rate);
        }

        @Test
        public void getRateShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Rate.class)).thenReturn(null);
            Rate rate = stingrayRestClient.getRate(vsName);
            Assert.assertNull(rate);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getRateShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Rate rate = stingrayRestClient.getRate(vsName);
        }

        @Test
        public void getCacrlShouldReturnAFile() throws Exception {
            File cacrl = stingrayRestClient.getCacrl(vsName);
            Assert.assertNotNull(cacrl);
        }

        @Test
        public void getCacrlShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File cacrl = stingrayRestClient.getCacrl(vsName);
            Assert.assertNull(cacrl);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getCacrlShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            File cacrl = stingrayRestClient.getCacrl(vsName);
        }

        @Test
        public void getClientKeyPairShouldReturnAClientKeypair() throws Exception {
            ClientKeypair clientKeypair = stingrayRestClient.getClientKeypair(vsName);
            Assert.assertNotNull(clientKeypair);
        }

        @Test
        public void getClientKeyPairShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(ClientKeypair.class)).thenReturn(null);
            ClientKeypair clientKeypair = stingrayRestClient.getClientKeypair(vsName);
            Assert.assertNull(clientKeypair);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getClientKeyPairShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            ClientKeypair clientKeypair = stingrayRestClient.getClientKeypair(vsName);
        }

        @Test
        public void getKeypairShouldReturnAKeypair() throws Exception {
            Keypair keypair = stingrayRestClient.getKeypair(vsName);
            Assert.assertNotNull(keypair);
        }

        @Test
        public void getKeypairShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Keypair.class)).thenReturn(null);
            Keypair keypair = stingrayRestClient.getKeypair(vsName);
            Assert.assertNull(keypair);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getKeypairShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            Keypair keypair = stingrayRestClient.getKeypair(vsName);
        }

        @Test
        public void getTrafficManagerShouldReturnATrafficManager() throws Exception {
            TrafficManager trafficManager = stingrayRestClient.getTrafficManager(vsName);
            Assert.assertNotNull(trafficManager);
        }

        @Test
        public void getTrafficManagerShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(TrafficManager.class)).thenReturn(null);
            TrafficManager trafficManager = stingrayRestClient.getTrafficManager(vsName);
            Assert.assertNull(trafficManager);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getTrafficManagerShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            TrafficManager trafficManager = stingrayRestClient.getTrafficManager(vsName);
        }

        @Test
        public void getTrafficScriptShouldReturnAFile() throws Exception {
            File trafficScript = stingrayRestClient.getTraffiscript(vsName);
            Assert.assertNotNull(trafficScript);
        }

        @Test
        public void getTrafficScriptShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File trafficScript = stingrayRestClient.getTraffiscript(vsName);
            Assert.assertNull(trafficScript);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getTrafficScriptShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            File trafficScript = stingrayRestClient.getTraffiscript(vsName);
        }

        @Test
        public void getTrafficIpShouldReturnATrafficIp() throws Exception {
            TrafficIp trafficIp = stingrayRestClient.getTrafficIp(vsName);
            Assert.assertNotNull(trafficIp);
        }

        @Test
        public void getTrafficIpShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(TrafficIp.class)).thenReturn(null);
            TrafficIp trafficIp = stingrayRestClient.getTrafficIp(vsName);
            Assert.assertNull(trafficIp);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getTrafficIpShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            TrafficIp trafficIp = stingrayRestClient.getTrafficIp(vsName);
        }

        @Test
        public void getGlobalSettingsShouldReturnGlobalSettings() throws Exception {
            GlobalSettings globalSettings = stingrayRestClient.getGlobalSettings();
            Assert.assertNotNull(globalSettings);
        }

        @Test
        public void getGlobalSettingsShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(GlobalSettings.class)).thenReturn(null);
            GlobalSettings globalSettings = stingrayRestClient.getGlobalSettings();
            Assert.assertNull(globalSettings);
        }

        @Test(expected = StingrayRestClientException.class)
        public void getGlobalSettingsShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            GlobalSettings globalSettings = stingrayRestClient.getGlobalSettings();
        }

    }

    @RunWith(MockitoJUnitRunner.class)
    public static class whenUpdatingAnItem {
        @Mock
        private RequestManager requestManager;
        @Mock
        private StingrayRequestManagerUtil stingrayRequestManagerUtil;
        private StingrayRestClientManager stingrayRestClientManager;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;
        private Pool pool;
        private VirtualServer virtualServer;
        private File file;
        private GlobalLoadBalancing glb;
        private Location location;
        private Monitor monitor;
        private Persistence persistence;
        private Protection protection;
        private Rate rate;
        private ClientKeypair clientKeypair;
        private Keypair keypair;
        private TrafficManager trafficManager;
        private TrafficIp trafficIp;



        @Before
        public void standUp() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException, URISyntaxException {
            vsName = "12345_1234";
            stingrayRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
            pool = new Pool();
            virtualServer = new VirtualServer();
            file = new File("12345_1234", "test");
            glb = new GlobalLoadBalancing();
            location = new Location();
            monitor = new Monitor();
            persistence = new Persistence();
            protection = new Protection();
            rate = new Rate();
            clientKeypair = new ClientKeypair();
            keypair = new Keypair();
            trafficManager = new TrafficManager();
            trafficIp = new TrafficIp();
            stingrayRestClientManager = spy(new StingrayRestClient());


            when(mockedResponse.readEntity(TrafficIp.class)).thenReturn(trafficIp);
            when(mockedResponse.readEntity(TrafficManager.class)).thenReturn(trafficManager);
            when(mockedResponse.readEntity(Keypair.class)).thenReturn(keypair);
            when(mockedResponse.readEntity(ClientKeypair.class)).thenReturn(clientKeypair);
            when(mockedResponse.readEntity(Rate.class)).thenReturn(rate);
            when(mockedResponse.readEntity(Protection.class)).thenReturn(protection);
            when(mockedResponse.readEntity(Persistence.class)).thenReturn(persistence);
            when(mockedResponse.readEntity(Monitor.class)).thenReturn(monitor);
            when(mockedResponse.readEntity(Location.class)).thenReturn(location);
            when(mockedResponse.readEntity(GlobalLoadBalancing.class)).thenReturn(glb);
            when(mockedResponse.readEntity(File.class)).thenReturn(file);
            when(mockedResponse.readEntity(VirtualServer.class)).thenReturn(virtualServer);
            when(mockedResponse.readEntity(Pool.class)).thenReturn(pool);
            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(bandwidth);
        }

        @Test
        public void updateItemShouldThrowExceptionWhenPathIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException, NoSuchMethodException {
            Method updateItem = StingrayRestClient.class.getDeclaredMethod("updateItem", String.class, Class.class, String.class, Object.class);
            updateItem.setAccessible(true);
            try {
                updateItem.invoke(stingrayRestClient, "vsname", Bandwidth.class, "BROKENPATH", bandwidth);
            } catch (Exception vex) {
                if (vex.getCause() instanceof StingrayRestClientException) {
                    Assert.assertTrue(true);
                } else {
                    Assert.assertTrue(false);
                }
            }
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateItemShouldThrowExceptionWhenResponseIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(stingrayRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(StingrayRestClientException.class);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Bandwidth bandwidthThree = stingrayRestClient.updateBandwidth(vsName, bandwidth);
        }

        @Test
        public void updateBandwidthShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Bandwidth bandwidthThree = stingrayRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNull(bandwidthThree);
        }

        @Test
        public void updateBandwidthShouldNotReturnNull() throws Exception {
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Bandwidth bandwidthTwo = stingrayRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNotNull(bandwidthTwo);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateBandwidthShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            bandwidth.setProperties(null);
            Bandwidth bandwidthThree = stingrayRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNull(bandwidthThree);
        }

        @Test
        public void updatePoolShouldNotReturnNull()throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Pool poolTwo = stingrayRestClient.updatePool(vsName, pool);
            Assert.assertNotNull(poolTwo);
        }

        @Test
        public void updatePoolShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Pool.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Pool poolThree = stingrayRestClient.updatePool(vsName, pool);
            Assert.assertNull(poolThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updatePoolShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            pool.setProperties(null);
            Pool poolThree = stingrayRestClient.updatePool(vsName, pool);
            Assert.assertNull(poolThree);
        }

        @Test
        public void updateVirtualServerShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            VirtualServer virtualServerTwo = stingrayRestClient.updateVirtualServer(vsName, virtualServer);
            Assert.assertNotNull(virtualServerTwo);
        }

        @Test
        public void updateVirtualServerShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(VirtualServer.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            VirtualServer virtualServerThree = stingrayRestClient.updateVirtualServer(vsName, virtualServer);
            Assert.assertNull(virtualServerThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateVirtualServerShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            virtualServer.setProperties(null);
            VirtualServer virtualServerThree = stingrayRestClient.updateVirtualServer(vsName, virtualServer);
            Assert.assertNull(virtualServerThree);
        }

        @Test
        public void  updateActionScriptShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File actionsScriptTwo = stingrayRestClient.updateActionScript(vsName, file);
            Assert.assertNotNull(actionsScriptTwo);
        }

        @Test
        public void updateActionScriptShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File actionScript = stingrayRestClient.updateActionScript(vsName, file);
            Assert.assertNull(actionScript);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateActionScriptShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            vsName = null;
            File actionScriptThree = stingrayRestClient.updateActionScript(vsName, file);
            Assert.assertNull(actionScriptThree);
        }

        @Test
        public void updateExtraFileShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File updateExtraFile = stingrayRestClient.updateExtraFile(vsName, file);
            Assert.assertNotNull(updateExtraFile);
        }

        @Test
        public void updateExtraFileShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File extraFile = stingrayRestClient.updateExtraFile(vsName, file);
            Assert.assertNull(extraFile);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateExtraFileShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            vsName = null;
            File extraFileThree = stingrayRestClient.updateExtraFile(vsName, file);
            Assert.assertNull(extraFileThree);
        }

        @Test
        public void updateGLBShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            GlobalLoadBalancing glbTwo = stingrayRestClient.updateGlb(vsName, glb);
            Assert.assertNotNull(glbTwo);
        }

        @Test
        public void updateGLBShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(GlobalLoadBalancing.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            GlobalLoadBalancing glbThree = stingrayRestClient.updateGlb(vsName, glb);
            Assert.assertNull(glbThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateGLBShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            glb.setProperties(null);
            GlobalLoadBalancing glbThree = stingrayRestClient.updateGlb(vsName, glb);
            Assert.assertNull(glbThree);
        }

        @Test
        public void updateLocationShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Location locationTwo = stingrayRestClient.updateLocation(vsName, location);
            Assert.assertNotNull(locationTwo);
        }

        @Test
        public void updateLocationShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Location.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Location locationThree = stingrayRestClient.updateLocation(vsName, location);
            Assert.assertNull(locationThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateLocationShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            location.setProperties(null);
            Location locationThree = stingrayRestClient.updateLocation(vsName, location);
            Assert.assertNull(locationThree);
        }

        @Test
        public void updateMonitorShouldNotReturnNull() throws  Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Monitor monitorTwo = stingrayRestClient.updateMonitor(vsName, monitor);
            Assert.assertNotNull(monitorTwo);
        }

        @Test
        public void updateMonitorShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Monitor.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Monitor monitorThree = stingrayRestClient.updateMonitor(vsName, monitor);
            Assert.assertNull(monitorThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateMonitorShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            monitor.setProperties(null);
            Monitor monitorThree = stingrayRestClient.updateMonitor(vsName, monitor);
            Assert.assertNull(monitorThree);
        }

        @Test
        public void updatePersistenceShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Persistence persistenceTwo = stingrayRestClient.updatePersistence(vsName, persistence);
            Assert.assertNotNull(persistenceTwo);
        }

        @Test
        public void updatePersistenceShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Persistence.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Persistence persistenceThree = stingrayRestClient.updatePersistence(vsName, persistence);
            Assert.assertNull(persistenceThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updatePersistenceShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            persistence.setProperties(null);
            Persistence persistenceThree = stingrayRestClient.updatePersistence(vsName, persistence);
            Assert.assertNull(persistenceThree);
        }

        @Test
        public void updateProtectionShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Protection protectionTwo = stingrayRestClient.updateProtection(vsName, protection);
            Assert.assertNotNull(protectionTwo);
        }

        @Test
        public void updateProtectionShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Protection.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Protection protectionThree = stingrayRestClient.updateProtection(vsName, protection);
            Assert.assertNull(protectionThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateProtectionShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            protection.setProperties(null);
            Protection protectionThree = stingrayRestClient.updateProtection(vsName, protection);
            Assert.assertNull(protectionThree);
        }

        @Test
        public void updateRateShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Rate rateTwo = stingrayRestClient.updateRate(vsName, rate);
            Assert.assertNotNull(rateTwo);
        }

        @Test
        public void updateRateShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Rate.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Rate rateThree = stingrayRestClient.updateRate(vsName, rate);
            Assert.assertNull(rateThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateRateShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            rate.setProperties(null);
            Rate rateThree = stingrayRestClient.updateRate(vsName, rate);
            Assert.assertNull(rateThree);
        }

        @Test
        public void updateCacrlShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File cacrl = stingrayRestClient.updateCacrl(vsName, file);
            Assert.assertNotNull(cacrl);
        }

        @Test
        public void updateCacrlthShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File cacrl = stingrayRestClient.updateCacrl(vsName, file);
            Assert.assertNull(cacrl);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateCarclShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            vsName = null;
            File carclThree = stingrayRestClient.updateCacrl(vsName, file);
            Assert.assertNull(carclThree);
        }

        @Test
        public void updateClientKeypairShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            ClientKeypair clientKeypairTwo = stingrayRestClient.updateClientKeypair(vsName, clientKeypair);
            Assert.assertNotNull(clientKeypairTwo);
        }

        @Test
        public void updateClientKeypairShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(ClientKeypair.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            ClientKeypair clientKeypairThree = stingrayRestClient.updateClientKeypair(vsName, clientKeypair);
            Assert.assertNull(clientKeypairThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateClientKeypairShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            clientKeypair.setProperties(null);
            ClientKeypair clientKeypairThree = stingrayRestClient.updateClientKeypair(vsName, clientKeypair);
            Assert.assertNull(clientKeypairThree);
        }

        @Test
        public void updateKeypairShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Keypair keypairTwo = stingrayRestClient.updateKeypair(vsName, keypair);
            Assert.assertNotNull(keypairTwo);
        }

        @Test
        public void updateKeypairShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Keypair.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Keypair keypairThree = stingrayRestClient.updateKeypair(vsName, keypair);
            Assert.assertNull(keypairThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateKeypairShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            keypair.setProperties(null);
            Keypair keypairThree = stingrayRestClient.updateKeypair(vsName, keypair);
            Assert.assertNull(keypairThree);
        }

        @Test
        public void updateTrafficManagerShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficManager trafficManagerTwo = stingrayRestClient.updateTrafficManager(vsName, trafficManager);
            Assert.assertNotNull(trafficManagerTwo);
        }

        @Test
        public void updateTrafficManagerShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(TrafficManager.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficManager trafficManagerThree = stingrayRestClient.updateTrafficManager(vsName, trafficManager);
            Assert.assertNull(trafficManagerThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateTrafficManagerShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            trafficManager.setProperties(null);
            TrafficManager trafficManagerThree = stingrayRestClient.updateTrafficManager(vsName, trafficManager);
            Assert.assertNull(trafficManagerThree);
        }

        @Test
        public void updateTrafficscriptShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File trafficscriptTwo = stingrayRestClient.updateTrafficScript(vsName, file);
            Assert.assertNotNull(trafficscriptTwo);
        }

        @Test
        public void updateTrafficScriptShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File trafficScript = stingrayRestClient.updateTrafficScript(vsName, file);
            Assert.assertNull(trafficScript);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateTrafficscriptShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            vsName = null;
            File trafficScriptThree = stingrayRestClient.updateTrafficScript(vsName, file);
            Assert.assertNull(trafficScriptThree);
        }

        @Test
        public void updateTrafficIpShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficIp trafficIpTwo = stingrayRestClient.updateTrafficIp(vsName, trafficIp);
            Assert.assertNotNull(vsName, trafficIpTwo);
        }

        @Test
        public void updateTrafficIpShouldReturnNull() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(TrafficIp.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficIp trafficIpThree = stingrayRestClient.updateTrafficIp(vsName, trafficIp);
            Assert.assertNull(trafficIpThree);
        }

        @Test(expected = StingrayRestClientException.class)
        public void updateTrafficIpShouldThrowException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            trafficIp.setProperties(null);
            TrafficIp trafficIpThree = stingrayRestClient.updateTrafficIp(vsName, trafficIp);
            Assert.assertNull(trafficIpThree);
        }

    }



    @RunWith(MockitoJUnitRunner.class)
    public static class whenDeletingAnItem {
        @Mock
        private RequestManager requestManager;
        @InjectMocks
        private StingrayRestClient stingrayRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            stingrayRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
        }

        @Test
        public void deleteItemShouldThrowExceptionWhenPathIsInvalid() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException, NoSuchMethodException {
            Method deleteItem = StingrayRestClient.class.getDeclaredMethod("deleteItem", String.class, String.class);
            deleteItem.setAccessible(true);
            try {
                deleteItem.invoke(stingrayRestClient, "vsname", "BROKENPATH");
            } catch (Exception vex) {
                if (vex.getCause() instanceof StingrayRestClientException) {
                    Assert.assertTrue(true);
                } else {
                    Assert.assertTrue(false);
                }
            }
        }


        @Test
        public void deletePoolShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deletePool(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deletePoolShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deletePool(vsName);

        }

        @Test
        public void deleteBandwidthShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteBandwidth(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteBandwidthShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteBandwidth(vsName);

        }

        @Test
        public void deleteVirtualServerShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteVirtualServer(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteVirtualServerShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteVirtualServer(vsName);

        }

        @Test
        public void deleteActionScriptShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteActionScript(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteActionScriptShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteActionScript(vsName);

        }

        @Test
        public void deleteExtraFileShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteExtraFile(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteExtraFileShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteExtraFile(vsName);

        }

        @Test
        public void deleteGlbShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteGlb(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteGlbShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteGlb(vsName);

        }

        @Test
        public void deleteLocationShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteLocation(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteLocationShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteLocation(vsName);

        }

        @Test
        public void deleteMonitorShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteMonitor(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteMonitorShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteMonitor(vsName);

        }

        @Test
        public void deleteMonitorScriptShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteMonitorScript(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteMonitorScriptShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteMonitorScript(vsName);

        }

        @Test
        public void deletePersistenceShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deletePersistence(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deletePersistenceShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deletePersistence(vsName);

        }

        @Test
        public void deleteProtectionShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteProtection(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteProtectionShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteProtection(vsName);

        }

        @Test
        public void deleteRateShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteRate(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteRateShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteRate(vsName);

        }

        @Test
        public void deleteCacrlShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteCacrl(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteCacrlShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteCacrl(vsName);

        }

        @Test
        public void deleteClientKeypairShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteClientKeypair(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteClientKeypairShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteClientKeypair(vsName);

        }

        @Test
        public void deleteKeypairShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteKeypair(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteKeypairShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteKeypair(vsName);

        }

        @Test
        public void deleteTrafficManagerShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteTrafficManager(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteTrafficManagerShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteTrafficManager(vsName);

        }

        @Test
        public void deleteTrafficscriptShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteTrafficscript(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteTrafficscriptShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteTrafficscript(vsName);

        }

        @Test
        public void deleteTrafficIpShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteTrafficIp(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = StingrayRestClientException.class)
        public void deleteTrafficIpShouldReturnStingrayRestClientException() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(StingrayRestClientException.class);
            vsName = "12345_1234";
            Response response = stingrayRestClient.deleteTrafficIp(vsName);

        }

    }



}