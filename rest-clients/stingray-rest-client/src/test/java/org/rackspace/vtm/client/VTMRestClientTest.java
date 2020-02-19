package org.rackspace.vtm.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.glb.GlobalLoadBalancing;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.list.Children;
import org.rackspace.vtm.client.location.Location;
import org.rackspace.vtm.client.manager.VTMRequestManager;
import org.rackspace.vtm.client.manager.VTMRestClientManager;
import org.rackspace.vtm.client.manager.util.VTMRequestManagerUtil;
import org.rackspace.vtm.client.monitor.Monitor;
import org.rackspace.vtm.client.persistence.Persistence;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.protection.Protection;
import org.rackspace.vtm.client.rate.Rate;
import org.rackspace.vtm.client.settings.GlobalSettings;
import org.rackspace.vtm.client.ssl.client.keypair.ClientKeypair;
import org.rackspace.vtm.client.ssl.keypair.Keypair;
import org.rackspace.vtm.client.tm.TrafficManager;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.util.ClientConstants;
import org.rackspace.vtm.client.virtualserver.VirtualServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class VTMRestClientTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenGettingAListOfItems {
        @Mock
        private VTMRequestManager requestManager;
        @Mock
        private VTMRequestManagerUtil mockedVtmRequestManagerUtil;
        private VTMRestClientManager vtimRestClientManager;
        @Mock
        private Response mockedResponse;
        @InjectMocks
        private VTMRestClient vtimRestClient;

        @Before
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtimRestClient = new VTMRestClient();
            vtimRestClient.setRequestManager(requestManager);
            mockedVtmRequestManagerUtil = new VTMRequestManagerUtil();
            vtimRestClientManager = spy(new VTMRestClient());

            Children children = new Children();
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(children);
            when(requestManager.getList(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(mockedResponse);
        }

        @Test(expected = VTMRestClientException.class)
        public void getItemShouldThrowExceptionWhenPathIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtimRestClient.getItem("itemName", Pool.class, "BROKENPATH");
        }

        @Test
        public void getPoolsShouldReturnWhenPathValid() throws Exception {
            List<Child> pools = vtimRestClient.getPools();
            Assert.assertNotNull(pools);
        }

        @Test(expected = VTMRestClientException.class)
        public void getPoolsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);

            List<Child> pools = vtimRestClient.getPools();
        }

        @Test
        public void getActionScriptsShouldReturnWhenPathValid() throws Exception {
            List<Child> actionScript = vtimRestClient.getActionScripts();
            Assert.assertNotNull(actionScript);
        }

        @Test(expected = NullPointerException.class)
        public void getActionScriptsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> actionScript = vtimRestClient.getActionScripts();

        }

        @Test(expected = VTMRestClientException.class)
        public void getActionScriptsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(VTMRestClientException.class);
            List<Child> actionScript = vtimRestClient.getActionScripts();
        }

        @Test
        public void getBandwidthsShouldReturnWhenPathValid() throws Exception {
            List<Child> bandwidths = vtimRestClient.getBandwidths();
            Assert.assertNotNull(bandwidths);
        }

        @Test(expected = NullPointerException.class)
        public void getBandwidthsShouldThrowNullPointerException() throws Exception {
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenReturn(null);
            List<Child> bandwidths = vtimRestClient.getBandwidths();

        }

        @Test(expected = VTMRestClientException.class)
        public void getABandwidthshouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(VTMRestClientException.class);
            List<Child> bandwidths = vtimRestClient.getBandwidths();
        }

        @Test
        public void getExtraFilesShouldReturnWhenPathValid() throws Exception {
            List<Child> extraFiles = vtimRestClient.getExtraFiles();
            Assert.assertNotNull(extraFiles);
        }

        @Test(expected = VTMRestClientException.class)
        public void getAExtraFilesShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> extraFiles = vtimRestClient.getExtraFiles();
        }

        @Test
        public void getGLBsShouldReturnWhenPathValid() throws Exception {
            List<Child> glbs = vtimRestClient.getGlbs();
            Assert.assertNotNull(glbs);
        }

        @Test(expected = VTMRestClientException.class)
        public void getGLBsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> glbs = vtimRestClient.getGlbs();
        }

        @Test
        public void getLocationsShouldReturnWhenPathValid() throws Exception {
            List<Child> locations = vtimRestClient.getLocations();
            Assert.assertNotNull(locations);
        }

        @Test(expected = VTMRestClientException.class)
        public void getLocationsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> locations = vtimRestClient.getLocations();
        }

        @Test
        public void getMonitorsShouldReturnWhenPathValid() throws Exception {
            List<Child> monitors = vtimRestClient.getMonitors();
            Assert.assertNotNull(monitors);
        }

        @Test(expected = VTMRestClientException.class)
        public void getMonitorsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> monitors = vtimRestClient.getMonitors();
        }

        @Test
        public void getMonitorScriptsShouldReturnWhenPathValid() throws Exception {
            List<Child> monitorScripts = vtimRestClient.getMonitorScripts();
            Assert.assertNotNull(monitorScripts);
        }

        @Test(expected = VTMRestClientException.class)
        public void getMonitorScriptsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);            List<Child> monitorScripts = vtimRestClient.getMonitorScripts();
        }

        @Test
        public void getPersistencesShouldReturnWhenPathValid() throws Exception {
            List<Child> persistences = vtimRestClient.getPersistences();
            Assert.assertNotNull(persistences);
        }

        @Test(expected = VTMRestClientException.class)
        public void getPersistencesShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> persistences = vtimRestClient.getPersistences();
        }

        @Test
        public void getProtectionsShouldReturnWhenPathValid() throws Exception {
            List<Child> protections = vtimRestClient.getProtections();
            Assert.assertNotNull(protections);
        }

        @Test(expected = VTMRestClientException.class)
        public void getProtectionsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> protections = vtimRestClient.getProtections();
        }

        @Test
        public void getRatesShouldReturnWhenPathValid() throws Exception {
            List<Child> rates = vtimRestClient.getRates();
            Assert.assertNotNull(rates);
        }

        @Test(expected = VTMRestClientException.class)
        public void getRatesShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> rates = vtimRestClient.getRates();
        }

        @Test
        public void getCacrlsShouldReturnWhenPathValid() throws Exception {
            List<Child> carcls = vtimRestClient.getCacrls();
            Assert.assertNotNull(carcls);
        }

        @Test(expected = VTMRestClientException.class)
        public void getCacrlsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> carcls = vtimRestClient.getCacrls();
        }

        @Test
        public void getClientKeypairsShouldReturnWhenPathValid() throws Exception {
            List<Child> clientKeypairs = vtimRestClient.getClientKeypairs();
            Assert.assertNotNull(clientKeypairs);
        }

        @Test(expected = VTMRestClientException.class)
        public void getClientKeypairsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> clientKeypairs = vtimRestClient.getClientKeypairs();
        }

        @Test
        public void getKeypairsShouldReturnWhenPathValid() throws Exception {
            List<Child> keypairs = vtimRestClient.getKeypairs();
            Assert.assertNotNull(keypairs);
        }

        @Test(expected = VTMRestClientException.class)
        public void getKeypairsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> keypairs = vtimRestClient.getKeypairs();
        }

        @Test
        public void getTrafficManagersShouldReturnWhenPathValid() throws Exception {
            List<Child> trafficManagers = vtimRestClient.getTrafficManagers();
            Assert.assertNotNull(trafficManagers);
        }

        @Test(expected = VTMRestClientException.class)
        public void getTrafficManagersShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> trafficManagers = vtimRestClient.getTrafficManagers();
        }

        @Test
        public void getTrafficScriptsShouldReturnWhenPathValid() throws Exception {
            List<Child> trafficScripts = vtimRestClient.getActionScripts();
            Assert.assertNotNull(trafficScripts);
        }

        @Test(expected = VTMRestClientException.class)
        public void getTrafficScriptsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> trafficScripts = vtimRestClient.getActionScripts();
        }

        @Test
        public void getTrafficIpsShouldReturnWhenPathValid() throws Exception {
            List<Child> trafficIps = vtimRestClient.getTrafficIps();
            Assert.assertNotNull(trafficIps);
        }

        @Test(expected = VTMRestClientException.class)
        public void getTrafficIpsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            List<Child> trafficIps = vtimRestClient.getTrafficIps();
        }

    }

    @RunWith(MockitoJUnitRunner.class)
    public static class whenGettingAnItem {
        @Mock
        private VTMRequestManager requestManager;
        private VTMRestClientManager vtimRestClientManager;
        @InjectMocks
        private VTMRestClient vtimRestClient;
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
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtimRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
            vtimRestClientManager = spy(new VTMRestClient());
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
            vtimRestClientManager = spy(new VTMRestClient());
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
        public void getBandwidthShouldReturnABandwidth() throws Exception {
            Bandwidth bandwidth = vtimRestClient.getBandwidth(vsName);
            Assert.assertNotNull(bandwidth);
        }

        @Test
        public void getBandWidthShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(null);
            Bandwidth bandwidthThree = vtimRestClient.getBandwidth(vsName);
            Assert.assertNull(bandwidthThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void getBandwidthShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Bandwidth bandwidthThree = vtimRestClient.getBandwidth(vsName);
        }
        @Test
        public void getVirtualServerShouldReturnAVirtualServer() throws Exception {
            VirtualServer virtualServer = vtimRestClient.getVirtualServer(vsName);
            Assert.assertNotNull(virtualServer);
        }

        @Test
        public void getVirtualServerShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(VirtualServer.class)).thenReturn(null);
            VirtualServer virtualServerThree = vtimRestClient.getVirtualServer(vsName);
            Assert.assertNull(virtualServerThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void getVirtualServerShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            VirtualServer virtualServerThree = vtimRestClient.getVirtualServer(vsName);
        }

        @Test
        public void getPoolShouldReturnAPool() throws Exception {
            Pool pool = vtimRestClient.getPool(vsName);
            Assert.assertNotNull(pool);
        }

        @Test
        public void getPoolShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Pool.class)).thenReturn(null);
            Pool pool = vtimRestClient.getPool(vsName);
            Assert.assertNull(pool);
        }

        @Test(expected = VTMRestClientException.class)
        public void getPoolShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Pool pool = vtimRestClient.getPool(vsName);
        }

        @Test
        public void getActionScriptShouldReturnAnActionScript() throws Exception {
            File actionScript = vtimRestClient.getActionScript(vsName);
            Assert.assertNotNull(actionScript);
        }

        @Test
        public void getActionScriptShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File actionScript = vtimRestClient.getActionScript(vsName);
            Assert.assertNull(actionScript);
        }

        @Test(expected = VTMRestClientException.class)
        public void getActionScriptShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            File actionScript = vtimRestClient.getActionScript(vsName);
        }

        @Test
        public void getExtraFileShouldReturnAnExtraFile() throws Exception {
            File extraFile = vtimRestClient.getExtraFile(vsName);
            Assert.assertNotNull(extraFile);
        }

        @Test
        public void getExtraFileShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File extraFile = vtimRestClient.getExtraFile(vsName);
            Assert.assertNull(extraFile);
        }

        @Test(expected = VTMRestClientException.class)
        public void getExtraFileShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            File extraFile = vtimRestClient.getExtraFile(vsName);
        }

        @Test
        public void getGLBShouldReturnAGLB() throws Exception {
            GlobalLoadBalancing glb = vtimRestClient.getGlb(vsName);
            Assert.assertNotNull(glb);
        }

        @Test
        public void getGLBShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(GlobalLoadBalancing.class)).thenReturn(null);
            GlobalLoadBalancing glb = vtimRestClient.getGlb(vsName);
            Assert.assertNull(glb);
        }

        @Test(expected = VTMRestClientException.class)
        public void getGLBShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            GlobalLoadBalancing glb = vtimRestClient.getGlb(vsName);
        }

        @Test
        public void getLocationShouldReturnALocation() throws Exception {
            Location location = vtimRestClient.getLocation(vsName);
            Assert.assertNotNull(location);
        }

        @Test
        public void getLocationShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Location.class)).thenReturn(null);
            Location location = vtimRestClient.getLocation(vsName);
            Assert.assertNull(location);
        }

        @Test(expected = VTMRestClientException.class)
        public void getLocationShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Location location = vtimRestClient.getLocation(vsName);
        }

        @Test
        public void getMonitorShouldReturnAMonitor() throws Exception {
            Monitor monitor = vtimRestClient.getMonitor(vsName);
            Assert.assertNotNull(monitor);
        }

        @Test
        public void getMonitorShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Monitor.class)).thenReturn(null);
            Monitor monitor = vtimRestClient.getMonitor(vsName);
            Assert.assertNull(monitor);
        }

        @Test(expected = VTMRestClientException.class)
        public void getMonitorShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Monitor monitor = vtimRestClient.getMonitor(vsName);
        }

        @Test
        public void getMonitorScriptShouldReturnAMonitorScript() throws Exception {
            File monitorScript = vtimRestClient.getMonitorScript(vsName);
            Assert.assertNotNull(monitorScript);
        }

        @Test
        public void getMonitorScriptShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File monitorScript = vtimRestClient.getMonitorScript(vsName);
            Assert.assertNull(monitorScript);
        }

        @Test(expected = VTMRestClientException.class)
        public void getMonitorScriptShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            File monitorScript = vtimRestClient.getMonitorScript(vsName);
        }

        @Test
        public void getPersistenceShouldReturnAPersistence() throws Exception {
            Persistence persistence = vtimRestClient.getPersistence(vsName);
            Assert.assertNotNull(persistence);
        }

        @Test
        public void getPersistenceShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Persistence.class)).thenReturn(null);
            Persistence persistence = vtimRestClient.getPersistence(vsName);
            Assert.assertNull(persistence);
        }

        @Test(expected = VTMRestClientException.class)
        public void getPersistenceShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Persistence persistence = vtimRestClient.getPersistence(vsName);
        }

        @Test
        public void getProtectionShouldReturnAProtection() throws Exception {
            Protection protection = vtimRestClient.getProtection(vsName);
            Assert.assertNotNull(protection);
        }

        @Test
        public void getProtectionShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Protection.class)).thenReturn(null);
            Protection protection = vtimRestClient.getProtection(vsName);
            Assert.assertNull(protection);
        }

        @Test(expected = VTMRestClientException.class)
        public void getProtectionShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Protection protection = vtimRestClient.getProtection(vsName);
        }

        @Test
        public void getRateShouldReturnARate() throws Exception {
            Rate rate = vtimRestClient.getRate(vsName);
            Assert.assertNotNull(rate);
        }

        @Test
        public void getRateShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Rate.class)).thenReturn(null);
            Rate rate = vtimRestClient.getRate(vsName);
            Assert.assertNull(rate);
        }

        @Test(expected = VTMRestClientException.class)
        public void getRateShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Rate rate = vtimRestClient.getRate(vsName);
        }

        @Test
        public void getCacrlShouldReturnAFile() throws Exception {
            File cacrl = vtimRestClient.getCacrl(vsName);
            Assert.assertNotNull(cacrl);
        }

        @Test
        public void getCacrlShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File cacrl = vtimRestClient.getCacrl(vsName);
            Assert.assertNull(cacrl);
        }

        @Test(expected = VTMRestClientException.class)
        public void getCacrlShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            File cacrl = vtimRestClient.getCacrl(vsName);
        }

        @Test
        public void getClientKeyPairShouldReturnAClientKeypair() throws Exception {
            ClientKeypair clientKeypair = vtimRestClient.getClientKeypair(vsName);
            Assert.assertNotNull(clientKeypair);
        }

        @Test
        public void getClientKeyPairShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(ClientKeypair.class)).thenReturn(null);
            ClientKeypair clientKeypair = vtimRestClient.getClientKeypair(vsName);
            Assert.assertNull(clientKeypair);
        }

        @Test(expected = VTMRestClientException.class)
        public void getClientKeyPairShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            ClientKeypair clientKeypair = vtimRestClient.getClientKeypair(vsName);
        }

        @Test
        public void getKeypairShouldReturnAKeypair() throws Exception {
            Keypair keypair = vtimRestClient.getKeypair(vsName);
            Assert.assertNotNull(keypair);
        }

        @Test
        public void getKeypairShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(Keypair.class)).thenReturn(null);
            Keypair keypair = vtimRestClient.getKeypair(vsName);
            Assert.assertNull(keypair);
        }

        @Test(expected = VTMRestClientException.class)
        public void getKeypairShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            Keypair keypair = vtimRestClient.getKeypair(vsName);
        }

        @Test
        public void getTrafficManagerShouldReturnATrafficManager() throws Exception {
            TrafficManager trafficManager = vtimRestClient.getTrafficManager(vsName);
            Assert.assertNotNull(trafficManager);
        }

        @Test
        public void getTrafficManagerShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(TrafficManager.class)).thenReturn(null);
            TrafficManager trafficManager = vtimRestClient.getTrafficManager(vsName);
            Assert.assertNull(trafficManager);
        }

        @Test(expected = VTMRestClientException.class)
        public void getTrafficManagerShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            TrafficManager trafficManager = vtimRestClient.getTrafficManager(vsName);
        }

        @Test
        public void getTrafficScriptShouldReturnAFile() throws Exception {
            File trafficScript = vtimRestClient.getTraffiscript(vsName);
            Assert.assertNotNull(trafficScript);
        }

        @Test
        public void getTrafficScriptShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            File trafficScript = vtimRestClient.getTraffiscript(vsName);
            Assert.assertNull(trafficScript);
        }

        @Test(expected = VTMRestClientException.class)
        public void getTrafficScriptShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            File trafficScript = vtimRestClient.getTraffiscript(vsName);
        }

        @Test
        public void getTrafficIpShouldReturnATrafficIp() throws Exception {
            TrafficIp trafficIp = vtimRestClient.getTrafficIp(vsName);
            Assert.assertNotNull(trafficIp);
        }

        @Test
        public void getTrafficIpShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(TrafficIp.class)).thenReturn(null);
            TrafficIp trafficIp = vtimRestClient.getTrafficIp(vsName);
            Assert.assertNull(trafficIp);
        }

        @Test(expected = VTMRestClientException.class)
        public void getTrafficIpShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            TrafficIp trafficIp = vtimRestClient.getTrafficIp(vsName);
        }

        @Test
        public void getGlobalSettingsShouldReturnGlobalSettings() throws Exception {
            GlobalSettings globalSettings = vtimRestClient.getGlobalSettings();
            Assert.assertNotNull(globalSettings);
        }

        @Test
        public void getGlobalSettingsShouldReturnNull() throws Exception{
            when(mockedResponse.readEntity(GlobalSettings.class)).thenReturn(null);
            GlobalSettings globalSettings = vtimRestClient.getGlobalSettings();
            Assert.assertNull(globalSettings);
        }

        @Test(expected = VTMRestClientException.class)
        public void getGlobalSettingsShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity((Class<Object>) Matchers.any())).thenThrow(Exception.class);
            when(mockedResponse.getStatus()).thenReturn(ClientConstants.BAD_REQUEST);
            GlobalSettings globalSettings = vtimRestClient.getGlobalSettings();
        }

    }

    @RunWith(MockitoJUnitRunner.class)
    public static class whenUpdatingAnItem {
        @Mock
        private VTMRequestManager requestManager;
        @Mock
        private VTMRequestManagerUtil vtmRequestManagerUtil;
        private VTMRestClientManager vtimRestClientManager;
        @InjectMocks
        private VTMRestClient vtimRestClient;
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
        public void standUp() throws VTMRestClientException, VTMRestClientObjectNotFoundException, URISyntaxException {
            vsName = "12345_1234";
            vtimRestClient.setRequestManager(requestManager);
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
            vtimRestClientManager = spy(new VTMRestClient());


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

        @Test(expected = VTMRestClientException.class)
        public void updateItemShouldThrowExceptionWhenPathIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtimRestClient.updateItem("itemName", Pool.class, "BROKENPATH", new Pool());
        }

        @Test(expected = VTMRestClientException.class)
        public void updateItemShouldThrowExceptionWhenResponseIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(vtimRestClientManager.interpretResponse(anyObject(), anyObject())).thenThrow(VTMRestClientException.class);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Bandwidth bandwidthThree = vtimRestClient.updateBandwidth(vsName, bandwidth);
        }

        @Test
        public void updateBandwidthShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Bandwidth.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Bandwidth bandwidthThree = vtimRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNull(bandwidthThree);
        }

        @Test
        public void updateBandwidthShouldNotReturnNull() throws Exception {
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Bandwidth bandwidthTwo = vtimRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNotNull(bandwidthTwo);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateBandwidthShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            bandwidth.setProperties(null);
            Bandwidth bandwidthThree = vtimRestClient.updateBandwidth(vsName, bandwidth);
            Assert.assertNull(bandwidthThree);
        }

        @Test
        public void updatePoolShouldNotReturnNull()throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Pool poolTwo = vtimRestClient.updatePool(vsName, pool);
            Assert.assertNotNull(poolTwo);
        }

        @Test
        public void updatePoolShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Pool.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Pool poolThree = vtimRestClient.updatePool(vsName, pool);
            Assert.assertNull(poolThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updatePoolShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            pool.setProperties(null);
            Pool poolThree = vtimRestClient.updatePool(vsName, pool);
            Assert.assertNull(poolThree);
        }

        @Test
        public void updateVirtualServerShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            VirtualServer virtualServerTwo = vtimRestClient.updateVirtualServer(vsName, virtualServer);
            Assert.assertNotNull(virtualServerTwo);
        }

        @Test
        public void updateVirtualServerShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(VirtualServer.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            VirtualServer virtualServerThree = vtimRestClient.updateVirtualServer(vsName, virtualServer);
            Assert.assertNull(virtualServerThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateVirtualServerShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            virtualServer.setProperties(null);
            VirtualServer virtualServerThree = vtimRestClient.updateVirtualServer(vsName, virtualServer);
            Assert.assertNull(virtualServerThree);
        }

        @Test
        public void  updateActionScriptShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File actionsScriptTwo = vtimRestClient.updateActionScript(vsName, file);
            Assert.assertNotNull(actionsScriptTwo);
        }

        @Test
        public void updateActionScriptShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File actionScript = vtimRestClient.updateActionScript(vsName, file);
            Assert.assertNull(actionScript);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateActionScriptShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vsName = null;
            File actionScriptThree = vtimRestClient.updateActionScript(vsName, file);
            Assert.assertNull(actionScriptThree);
        }

        @Test
        public void updateExtraFileShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File updateExtraFile = vtimRestClient.updateExtraFile(vsName, file);
            Assert.assertNotNull(updateExtraFile);
        }

        @Test
        public void updateExtraFileShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File extraFile = vtimRestClient.updateExtraFile(vsName, file);
            Assert.assertNull(extraFile);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateExtraFileShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            vsName = null;
            File extraFileThree = vtimRestClient.updateExtraFile(vsName, file);
            Assert.assertNull(extraFileThree);
        }

        @Test
        public void updateGLBShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            GlobalLoadBalancing glbTwo = vtimRestClient.updateGlb(vsName, glb);
            Assert.assertNotNull(glbTwo);
        }

        @Test
        public void updateGLBShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(GlobalLoadBalancing.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            GlobalLoadBalancing glbThree = vtimRestClient.updateGlb(vsName, glb);
            Assert.assertNull(glbThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateGLBShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            glb.setProperties(null);
            GlobalLoadBalancing glbThree = vtimRestClient.updateGlb(vsName, glb);
            Assert.assertNull(glbThree);
        }

        @Test
        public void updateLocationShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Location locationTwo = vtimRestClient.updateLocation(vsName, location);
            Assert.assertNotNull(locationTwo);
        }

        @Test
        public void updateLocationShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Location.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Location locationThree = vtimRestClient.updateLocation(vsName, location);
            Assert.assertNull(locationThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateLocationShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            location.setProperties(null);
            Location locationThree = vtimRestClient.updateLocation(vsName, location);
            Assert.assertNull(locationThree);
        }

        @Test
        public void updateMonitorShouldNotReturnNull() throws  Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Monitor monitorTwo = vtimRestClient.updateMonitor(vsName, monitor);
            Assert.assertNotNull(monitorTwo);
        }

        @Test
        public void updateMonitorShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Monitor.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Monitor monitorThree = vtimRestClient.updateMonitor(vsName, monitor);
            Assert.assertNull(monitorThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateMonitorShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            monitor.setProperties(null);
            Monitor monitorThree = vtimRestClient.updateMonitor(vsName, monitor);
            Assert.assertNull(monitorThree);
        }

        @Test
        public void updatePersistenceShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Persistence persistenceTwo = vtimRestClient.updatePersistence(vsName, persistence);
            Assert.assertNotNull(persistenceTwo);
        }

        @Test
        public void updatePersistenceShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Persistence.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Persistence persistenceThree = vtimRestClient.updatePersistence(vsName, persistence);
            Assert.assertNull(persistenceThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updatePersistenceShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            persistence.setProperties(null);
            Persistence persistenceThree = vtimRestClient.updatePersistence(vsName, persistence);
            Assert.assertNull(persistenceThree);
        }

        @Test
        public void updateProtectionShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Protection protectionTwo = vtimRestClient.updateProtection(vsName, protection);
            Assert.assertNotNull(protectionTwo);
        }

        @Test
        public void updateProtectionShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Protection.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Protection protectionThree = vtimRestClient.updateProtection(vsName, protection);
            Assert.assertNull(protectionThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateProtectionShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            protection.setProperties(null);
            Protection protectionThree = vtimRestClient.updateProtection(vsName, protection);
            Assert.assertNull(protectionThree);
        }

        @Test
        public void updateRateShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Rate rateTwo = vtimRestClient.updateRate(vsName, rate);
            Assert.assertNotNull(rateTwo);
        }

        @Test
        public void updateRateShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Rate.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Rate rateThree = vtimRestClient.updateRate(vsName, rate);
            Assert.assertNull(rateThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateRateShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            rate.setProperties(null);
            Rate rateThree = vtimRestClient.updateRate(vsName, rate);
            Assert.assertNull(rateThree);
        }

        @Test
        public void updateCacrlShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File cacrl = vtimRestClient.updateCacrl(vsName, file);
            Assert.assertNotNull(cacrl);
        }

        @Test
        public void updateCacrlthShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File cacrl = vtimRestClient.updateCacrl(vsName, file);
            Assert.assertNull(cacrl);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateCarclShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            vsName = null;
            File carclThree = vtimRestClient.updateCacrl(vsName, file);
            Assert.assertNull(carclThree);
        }

        @Test
        public void updateClientKeypairShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            ClientKeypair clientKeypairTwo = vtimRestClient.updateClientKeypair(vsName, clientKeypair);
            Assert.assertNotNull(clientKeypairTwo);
        }

        @Test
        public void updateClientKeypairShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(ClientKeypair.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            ClientKeypair clientKeypairThree = vtimRestClient.updateClientKeypair(vsName, clientKeypair);
            Assert.assertNull(clientKeypairThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateClientKeypairShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            clientKeypair.setProperties(null);
            ClientKeypair clientKeypairThree = vtimRestClient.updateClientKeypair(vsName, clientKeypair);
            Assert.assertNull(clientKeypairThree);
        }

        @Test
        public void updateKeypairShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Keypair keypairTwo = vtimRestClient.updateKeypair(vsName, keypair);
            Assert.assertNotNull(keypairTwo);
        }

        @Test
        public void updateKeypairShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(Keypair.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            Keypair keypairThree = vtimRestClient.updateKeypair(vsName, keypair);
            Assert.assertNull(keypairThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateKeypairShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            keypair.setProperties(null);
            Keypair keypairThree = vtimRestClient.updateKeypair(vsName, keypair);
            Assert.assertNull(keypairThree);
        }

        @Test
        public void updateTrafficManagerShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficManager trafficManagerTwo = vtimRestClient.updateTrafficManager(vsName, trafficManager);
            Assert.assertNotNull(trafficManagerTwo);
        }

        @Test
        public void updateTrafficManagerShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(TrafficManager.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficManager trafficManagerThree = vtimRestClient.updateTrafficManager(vsName, trafficManager);
            Assert.assertNull(trafficManagerThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateTrafficManagerShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            trafficManager.setProperties(null);
            TrafficManager trafficManagerThree = vtimRestClient.updateTrafficManager(vsName, trafficManager);
            Assert.assertNull(trafficManagerThree);
        }

        @Test
        public void updateTrafficscriptShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File trafficscriptTwo = vtimRestClient.updateTrafficScript(vsName, file);
            Assert.assertNotNull(trafficscriptTwo);
        }

        @Test
        public void updateTrafficScriptShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(File.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            File trafficScript = vtimRestClient.updateTrafficScript(vsName, file);
            Assert.assertNull(trafficScript);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateTrafficscriptShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            vsName = null;
            File trafficScriptThree = vtimRestClient.updateTrafficScript(vsName, file);
            Assert.assertNull(trafficScriptThree);
        }

        @Test
        public void updateTrafficIpShouldNotReturnNull() throws Exception{
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficIp trafficIpTwo = vtimRestClient.updateTrafficIp(vsName, trafficIp);
            Assert.assertNotNull(vsName, trafficIpTwo);
        }

        @Test
        public void updateTrafficIpShouldReturnNull() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(mockedResponse.readEntity(TrafficIp.class)).thenReturn(null);
            when(requestManager.updateItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any(), Matchers.<Bandwidth>any(), Matchers.<MediaType>any())).thenReturn(mockedResponse);
            TrafficIp trafficIpThree = vtimRestClient.updateTrafficIp(vsName, trafficIp);
            Assert.assertNull(trafficIpThree);
        }

        @Test(expected = VTMRestClientException.class)
        public void updateTrafficIpShouldThrowException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            trafficIp.setProperties(null);
            TrafficIp trafficIpThree = vtimRestClient.updateTrafficIp(vsName, trafficIp);
            Assert.assertNull(trafficIpThree);
        }

    }



    @RunWith(MockitoJUnitRunner.class)
    public static class whenDeletingAnItem {
        @Mock
        private VTMRequestManager requestManager;
        @InjectMocks
        private VTMRestClient vtimRestClient;
        @Mock
        private Response mockedResponse;
        private Bandwidth bandwidth;
        private String vsName;


        @Before
        public void standUp()  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtimRestClient.setRequestManager(requestManager);
            bandwidth = new Bandwidth();
        }

        @Test(expected = VTMRestClientException.class)
        public void deleteItemShouldThrowExceptionWhenPathIsInvalid() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
            vtimRestClient.deleteItem("itemName","BROKENPATH");
        }


        @Test
        public void deletePoolShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deletePool(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deletePoolShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deletePool(vsName);

        }

        @Test
        public void deleteBandwidthShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteBandwidth(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteBandwidthShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteBandwidth(vsName);

        }

        @Test
        public void deleteVirtualServerShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteVirtualServer(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteVirtualServerShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteVirtualServer(vsName);

        }

        @Test
        public void deleteActionScriptShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteActionScript(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteActionScriptShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteActionScript(vsName);

        }

        @Test
        public void deleteExtraFileShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteExtraFile(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteExtraFileShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteExtraFile(vsName);

        }

        @Test
        public void deleteGlbShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteGlb(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteGlbShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteGlb(vsName);

        }

        @Test
        public void deleteLocationShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteLocation(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteLocationShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteLocation(vsName);

        }

        @Test
        public void deleteMonitorShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteMonitor(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteMonitorShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteMonitor(vsName);

        }

        @Test
        public void deleteMonitorScriptShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteMonitorScript(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteMonitorScriptShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteMonitorScript(vsName);

        }

        @Test
        public void deletePersistenceShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deletePersistence(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deletePersistenceShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deletePersistence(vsName);

        }

        @Test
        public void deleteProtectionShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteProtection(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteProtectionShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteProtection(vsName);

        }

        @Test
        public void deleteRateShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteRate(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteRateShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteRate(vsName);

        }

        @Test
        public void deleteCacrlShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteCacrl(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteCacrlShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteCacrl(vsName);

        }

        @Test
        public void deleteClientKeypairShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteClientKeypair(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteClientKeypairShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteClientKeypair(vsName);

        }

        @Test
        public void deleteKeypairShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteKeypair(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteKeypairShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteKeypair(vsName);

        }

        @Test
        public void deleteTrafficManagerShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteTrafficManager(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteTrafficManagerShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteTrafficManager(vsName);

        }

        @Test
        public void deleteTrafficscriptShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteTrafficscript(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteTrafficscriptShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteTrafficscript(vsName);

        }

        @Test
        public void deleteTrafficIpShouldReturnANoContentResponse() throws Exception {
            Response rnc = Response.noContent().build();
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenReturn(rnc);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteTrafficIp(vsName);
            Assert.assertEquals(204, response.getStatus());

        }

        @Test(expected = VTMRestClientException.class)
        public void deleteTrafficIpShouldReturnVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException{
            when(requestManager.deleteItem(Matchers.<URI>any(), Matchers.<Client>any(), Matchers.<String>any())).thenThrow(VTMRestClientException.class);
            vsName = "12345_1234";
            Response response = vtimRestClient.deleteTrafficIp(vsName);

        }

    }



}