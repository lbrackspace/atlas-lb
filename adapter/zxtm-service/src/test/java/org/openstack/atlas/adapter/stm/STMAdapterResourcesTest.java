package org.openstack.atlas.adapter.stm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.STMTestBase;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.persistence.Persistence;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;

import java.io.File;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class STMAdapterResourcesTest extends STMTestBase {

    public static class MiscFunctions {
        private StmAdapterResources adapterResources;

        @Mock
        private LoadBalancerEndpointConfiguration config;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            adapterResources = spy(new StmAdapterResources());
        }

        @Test
        public void testLoadSTMRestClient() throws Exception {
            StingrayRestClient returnedClient = adapterResources.loadSTMRestClient(config);

            Assert.assertNotNull(returnedClient);
            verify(config).getRestEndpoint();
            verify(config).getUsername();
            verify(config).getPassword();
            verifyNoMoreInteractions(config);
        }
    }

    public static class VirtualServerOperations {
        private String vsName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private VirtualServer virtualServer;
        private VirtualServer rollbackVirtualServer;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            virtualServer = resourceTranslator.getcVServer();
            rollbackVirtualServer = new VirtualServer();

            adapterResources = spy(new StmAdapterResources());

            when(client.getVirtualServer(vsName)).thenReturn(rollbackVirtualServer);
        }

        @Test
        public void testUpdateVirtualServer() throws Exception {
            adapterResources.updateVirtualServer(client, vsName, virtualServer);

            verify(client).getVirtualServer(vsName);
            verify(client).updateVirtualServer(vsName, virtualServer);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdateVirtualServerClientFailure() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).updateVirtualServer(vsName, virtualServer);
            try {
                adapterResources.updateVirtualServer(client, vsName, virtualServer);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getVirtualServer(vsName);
            verify(client).updateVirtualServer(vsName, virtualServer);
            verify(client).updateVirtualServer(vsName, rollbackVirtualServer);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteVirtualServer() throws Exception {
            adapterResources.deleteVirtualServer(client, vsName);

            verify(client).getVirtualServer(vsName);
            verify(client).deleteVirtualServer(vsName);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteVirtualServerClientFailure() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).deleteVirtualServer(vsName);
            try {
                adapterResources.deleteVirtualServer(client, vsName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getVirtualServer(vsName);
            verify(client).deleteVirtualServer(vsName);
            verify(client).updateVirtualServer(vsName, rollbackVirtualServer);
            verifyNoMoreInteractions(client);
        }
    }

    public static class KeypairOperations {
        private String vsName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private Keypair keypair;
        private Keypair rollbackKeypair;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            keypair = resourceTranslator.getcKeypair();
            rollbackKeypair = new Keypair();

            adapterResources = spy(new StmAdapterResources());

            when(client.getKeypair(vsName)).thenReturn(rollbackKeypair);
        }

        @Test
        public void testUpdateKeypair() throws Exception {
            adapterResources.updateKeypair(client, vsName, keypair);

            verify(client).getKeypair(vsName);
            verify(client).updateKeypair(vsName, keypair);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdateKeypairClientException() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).updateKeypair(vsName, keypair);
            try {
                adapterResources.updateKeypair(client, vsName, keypair);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getKeypair(vsName);
            verify(client).updateKeypair(vsName, keypair);
            verify(client).updateKeypair(vsName, rollbackKeypair);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteKeypair() throws Exception {
            adapterResources.deleteKeypair(client, vsName);

            verify(client).getKeypair(vsName);
            verify(client).deleteKeypair(vsName);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteKeypairClientException() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).deleteKeypair(vsName);
            try {
                adapterResources.deleteKeypair(client, vsName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getKeypair(vsName);
            verify(client).deleteKeypair(vsName);
            verify(client).updateKeypair(vsName, rollbackKeypair);
            verifyNoMoreInteractions(client);
        }
    }

    public static class PoolOperations {
        private String vsName;
        private String poolName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private Pool pool;
        private Pool rollbackPool;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();
            poolName = poolName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            pool = resourceTranslator.getcPool();
            rollbackPool = new Pool();

            adapterResources = spy(new StmAdapterResources());

            when(client.getPool(poolName)).thenReturn(rollbackPool);
        }

        @Test
        public void testUpdatePool() throws Exception {
            adapterResources.updatePool(client, poolName, pool);

            verify(client).getPool(poolName);
            verify(client).updatePool(poolName, pool);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdatePoolClientException() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).updatePool(vsName, pool);
            try {
                adapterResources.updatePool(client, poolName, pool);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getPool(poolName);
            verify(client).updatePool(poolName, pool);
            verify(client).updatePool(poolName, rollbackPool);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeletePool() throws Exception {
            adapterResources.deletePool(client, poolName);

            verify(client).getPool(poolName);
            verify(client).deletePool(poolName);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeletePoolClientException() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).deletePool(vsName);
            try {
                adapterResources.deletePool(client, poolName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getPool(poolName);
            verify(client).deletePool(poolName);
            verify(client).updatePool(poolName, rollbackPool);
            verifyNoMoreInteractions(client);
        }
    }

    public static class VirtualIpOperations {
        private String vsName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private Map<String, TrafficIp> virtualIps;
        private TrafficIp rollbackIp;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            virtualIps = resourceTranslator.getcTrafficIpGroups();
            rollbackIp = new TrafficIp();

            adapterResources = spy(new StmAdapterResources());

            when(client.getTrafficIp(anyString())).thenReturn(rollbackIp);
        }

        @Test
        public void testUpdateVirtualIps() throws Exception {
            adapterResources.updateVirtualIps(client, vsName, virtualIps);

            verify(client, times(virtualIps.size())).getTrafficIp(anyString());
            verify(client, times(virtualIps.size())).updateTrafficIp(anyString(), any(TrafficIp.class));
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdateVirtualIpsClientError() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).updateTrafficIp(anyString(), any(TrafficIp.class));
            try {
                adapterResources.updateVirtualIps(client, vsName, virtualIps);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getTrafficIp(anyString());
            verify(client).updateTrafficIp(anyString(), eq(virtualIps.values().iterator().next()));
            verify(client).updateTrafficIp(anyString(), eq(rollbackIp));
            verifyNoMoreInteractions(client);
        }
    }

    public static class HealthMonitorOperations {
        private String vsName;
        private String monitorName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private Monitor healthMonitor;
        private Monitor rollbackHealthMonitor;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();
            monitorName = monitorName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            healthMonitor = resourceTranslator.getcMonitor();
            rollbackHealthMonitor = new Monitor();

            adapterResources = spy(new StmAdapterResources());

            when(client.getMonitor(monitorName)).thenReturn(rollbackHealthMonitor);
        }

        @Test
        public void testUpdateHealthMonitor() throws Exception {
            adapterResources.updateHealthMonitor(client, monitorName, healthMonitor);

            verify(client).getMonitor(monitorName);
            verify(client).updateMonitor(monitorName, healthMonitor);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdateHealthMonitorClientError() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).updateMonitor(monitorName, healthMonitor);
            try {
                adapterResources.updateHealthMonitor(client, monitorName, healthMonitor);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getMonitor(monitorName);
            verify(client).updateMonitor(monitorName, healthMonitor);
            verify(client).updateMonitor(monitorName, rollbackHealthMonitor);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteHealthMonitor() throws Exception {
            adapterResources.deleteHealthMonitor(client, monitorName);

            verify(client).getMonitor(monitorName);
            verify(client).deleteMonitor(monitorName);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteHealthMonitorClientError() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).deleteMonitor(monitorName);
            try {
                adapterResources.deleteHealthMonitor(client, monitorName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getMonitor(monitorName);
            verify(client).deleteMonitor(monitorName);
            verify(client).updateMonitor(monitorName, rollbackHealthMonitor);
            verifyNoMoreInteractions(client);
        }
    }

    public static class ProtectionOperations {
        private String vsName;
        private String protectionName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private Protection protection;
        private Protection rollbackProtection;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();
            protectionName = protectionClassName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            protection = resourceTranslator.getcProtection();
            rollbackProtection = new Protection();

            adapterResources = spy(new StmAdapterResources());

            when(client.getProtection(protectionName)).thenReturn(rollbackProtection);
        }

        @Test
        public void testUpdateProtection() throws Exception {
            adapterResources.updateProtection(client, protectionName, protection);

            verify(client).getProtection(protectionName);
            verify(client).updateProtection(protectionName, protection);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdateProtectionClientError() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).updateProtection(protectionName, protection);
            try {
                adapterResources.updateProtection(client, protectionName, protection);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getProtection(protectionName);
            verify(client).updateProtection(protectionName, protection);
            verify(client).updateProtection(protectionName, rollbackProtection);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteProtection() throws Exception {
            adapterResources.deleteProtection(client, protectionName);

            verify(client).getProtection(protectionName);
            verify(client).deleteProtection(protectionName);
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteProtectionClientError() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).deleteProtection(protectionName);
            try {
                adapterResources.deleteProtection(client, protectionName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getProtection(protectionName);
            verify(client).deleteProtection(protectionName);
            verify(client).updateProtection(protectionName, rollbackProtection);
            verifyNoMoreInteractions(client);
        }
    }

    public static class PersistenceOperations {
        private StmAdapterResources adapterResources;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);

            adapterResources = spy(new StmAdapterResources());

            doReturn(client).when(adapterResources).loadSTMRestClient(config);
        }

        @Test
        public void testCreatePersistentClasses() throws Exception {
            adapterResources.createPersistentClasses(config);

            verify(client).getPersistence(StmConstants.HTTP_COOKIE);
            verify(client).getPersistence(StmConstants.SOURCE_IP);
            verify(client).getPersistence(StmConstants.SSL_ID);
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testCreatePersistentClassesNoHttpCookie() throws Exception {
            Exception exception = new StingrayRestClientObjectNotFoundException();
            doThrow(exception).when(client).getPersistence(StmConstants.HTTP_COOKIE);
            adapterResources.createPersistentClasses(config);

            verify(client).getPersistence(StmConstants.HTTP_COOKIE);
            verify(client).createPersistence(eq(StmConstants.HTTP_COOKIE), any(Persistence.class));
            verify(client).getPersistence(StmConstants.SOURCE_IP);
            verify(client).getPersistence(StmConstants.SSL_ID);
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testCreatePersistentClassesNoSourceIp() throws Exception {
            Exception exception = new StingrayRestClientObjectNotFoundException();
            doThrow(exception).when(client).getPersistence(StmConstants.SOURCE_IP);
            adapterResources.createPersistentClasses(config);

            verify(client).getPersistence(StmConstants.HTTP_COOKIE);
            verify(client).getPersistence(StmConstants.SOURCE_IP);
            verify(client).createPersistence(eq(StmConstants.SOURCE_IP), any(Persistence.class));
            verify(client).getPersistence(StmConstants.SSL_ID);
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testCreatePersistentClassesNoSslId() throws Exception {
            Exception exception = new StingrayRestClientObjectNotFoundException();
            doThrow(exception).when(client).getPersistence(StmConstants.SSL_ID);
            adapterResources.createPersistentClasses(config);

            verify(client).getPersistence(StmConstants.HTTP_COOKIE);
            verify(client).getPersistence(StmConstants.SOURCE_IP);
            verify(client).getPersistence(StmConstants.SSL_ID);
            verify(client).createPersistence(eq(StmConstants.SSL_ID), any(Persistence.class));
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }
    }

    public static class RateLimitOperations {
        private String vsName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private Bandwidth bandwidth;
        private Bandwidth rollbackBandwidth;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            bandwidth = resourceTranslator.getcBandwidth();
            rollbackBandwidth = new Bandwidth();

            adapterResources = spy(new StmAdapterResources());
            doNothing().when(adapterResources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            doReturn(client).when(adapterResources).loadSTMRestClient(config);
            doReturn(rollbackBandwidth).when(client).getBandwidth(vsName);
            doReturn(new VirtualServer()).when(client).getVirtualServer(vsName);
        }

        @Test
        public void testSetRateLimit() throws Exception {
            adapterResources.setRateLimit(config, lb, vsName);

            verify(client).createBandwidth(vsName, bandwidth);
            verify(adapterResources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testUpdateRateLimit() throws Exception {
            adapterResources.updateRateLimit(config, lb, vsName);

            verify(client).updateBandwidth(vsName, bandwidth);
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteRateLimit() throws Exception {
            adapterResources.deleteRateLimit(config, lb, vsName);

            verify(client).getBandwidth(vsName);
            verify(client).getVirtualServer(vsName);
            verify(client).deleteBandwidth(vsName);
            verify(client).updateVirtualServer(eq(vsName), any(VirtualServer.class));
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteRateLimitClientErrorBandwidth() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            doThrow(exception).when(client).deleteBandwidth(vsName);
            try {
                adapterResources.deleteRateLimit(config, lb, vsName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getBandwidth(vsName);
            verify(client).getVirtualServer(vsName);
            verify(client).deleteBandwidth(vsName);
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteRateLimitClientErrorServer() throws Exception {
            StmRollBackException rollBackException = null;
            Exception exception = new StingrayRestClientException();
            when(client.updateVirtualServer(eq(vsName), any(VirtualServer.class))).thenThrow(exception).thenReturn(new VirtualServer());
            try {
                adapterResources.deleteRateLimit(config, lb, vsName);
            } catch (StmRollBackException rbe) {
                rollBackException = rbe;
            }

            Assert.assertNotNull(rollBackException);
            verify(client).getBandwidth(vsName);
            verify(client).getVirtualServer(vsName);
            verify(client).deleteBandwidth(vsName);
            verify(client, times(2)).updateVirtualServer(eq(vsName), any(VirtualServer.class));
            verify(client).createBandwidth(vsName, rollbackBandwidth);
            verify(client).destroy();
            verifyNoMoreInteractions(client);
        }
    }

    public static class ErrorFileOperations {
        private String vsName;
        private String sslVsName;
        private String errorFileName;
        private StmAdapterResources adapterResources;
        private ResourceTranslator resourceTranslator;
        private String errorContent;
        private File errorFile;

        @Mock
        private LoadBalancerEndpointConfiguration config;
        @Mock
        private StingrayRestClient client;

        @Before
        public void standUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = loadBalancerName();
            sslVsName = secureLoadBalancerName();

            errorFileName = errorFileName();

            resourceTranslator = new ResourceTranslator();
            resourceTranslator.translateLoadBalancerResource(config, vsName, lb, lb);
            errorContent = "My Errorfile Content";

            adapterResources = spy(new StmAdapterResources());
            errorFile = adapterResources.getFileWithContent(errorContent);

            when(adapterResources.getFileWithContent(errorContent)).thenReturn(errorFile);
            doNothing().when(adapterResources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
        }

        @Test
        public void testSetErrorFile() throws Exception {
            adapterResources.setErrorFile(config, client, lb, errorContent);

            Assert.assertEquals(lb.getUserPages().getErrorpage(), errorContent);
            verify(client).createExtraFile(errorFileName, errorFile);
            verify(adapterResources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verifyNoMoreInteractions(client);
        }

        @Test
        public void testDeleteErrorFile() throws Exception {
            adapterResources.deleteErrorFile(config, client, lb);

            verify(adapterResources).updateVirtualServer(eq(client), eq(vsName), any(VirtualServer.class));
            verify(client).deleteExtraFile(errorFileName);
            verifyNoMoreInteractions(client);
        }
    }
}
