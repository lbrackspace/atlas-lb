package org.rackspace.vtm.client.manager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.vtm.client.counters.GlobalCountersStatistics;
import org.rackspace.vtm.client.counters.VirtualServerStatsProperties;
import org.rackspace.vtm.client.counters.VirtualServerStatsStatistics;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.manager.impl.VTMRequestManagerImpl;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolHttp;
import org.rackspace.vtm.client.pool.PoolProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;

@RunWith(Enclosed.class)
public class VTMRequestManagerTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenRetrievingAnItem {

        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);


        private VTMRequestManager requestManager;
        private String vsName;
        private Pool pool;

        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new VTMRequestManagerImpl();
            vsName = "12345_1234";
            pool = createPool();

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
//
            Mockito.when(this.mockBuilder.get(Response.class)).thenReturn(this.mockResponse);
//
            WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
            Mockito.when(mockWebTarget.request((MediaType) any())).thenReturn(mockBuilder);
//
            Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(mockWebTarget);
        }

        private Pool createPool() {
            PoolProperties poolProperties = new PoolProperties();
            PoolHttp poolHttp = new PoolHttp();
            poolHttp.setKeepalive(true);
            poolProperties.setHttp(poolHttp);

            Pool pool = new Pool();
            pool.setProperties(poolProperties);
            return pool;
        }


        private URI getPoolPath() throws URISyntaxException {
            return new URI("https://localhost:9070/api/tm/3.4/config/active/" + "pool");
        }

        @Test
        public void getItemShouldReturnAPoolWhenResponseIsValid() throws URISyntaxException, VTMRestClientException, VTMRestClientObjectNotFoundException {

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockBuilder.put((Entity) any())).thenReturn(this.mockResponse);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(pool);

            Response response = requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);

            Assert.assertEquals(200, response.getStatus());
            Assert.assertNotNull(response.readEntity(Pool.class));
            Assert.assertTrue(response.readEntity(Pool.class).getProperties().getHttp().getKeepalive());

        }

        @Test(expected = VTMRestClientException.class)
        public void getItemShouldThrowVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException, URISyntaxException {
            Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(null);

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockBuilder.put((Entity) any())).thenReturn(this.mockResponse);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(pool);
            Response response = requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);

        }

        @Test(expected = VTMRestClientObjectNotFoundException.class)
        public void getItemShouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, VTMRestClientException, VTMRestClientObjectNotFoundException {

            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");

            requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenUpdatingAnItem {
        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);

        private VTMRequestManager requestManager;
        private String vsName;
        private Pool pool;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new VTMRequestManagerImpl();
            vsName = "12345_1234";
            pool = createPool();

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
//
            Mockito.when(mockBuilder.get(Response.class)).thenReturn(this.mockResponse);
            Mockito.when(mockBuilder.put(Entity.entity(Pool.class, MediaType.APPLICATION_JSON_TYPE))).thenReturn(this.mockResponse);
//
            WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
            Mockito.when(mockWebTarget.request((MediaType) any())).thenReturn(this.mockBuilder);
//
            Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(mockWebTarget);
        }

        private Pool createPool() {
            PoolProperties poolProperties = new PoolProperties();
            PoolHttp poolHttp = new PoolHttp();
            poolHttp.setKeepalive(true);
            poolProperties.setHttp(poolHttp);

            Pool pool = new Pool();
            pool.setProperties(poolProperties);
            return pool;
        }


        private URI getPoolPath() throws URISyntaxException {
            return new URI("https://localhost:9070/api/tm/3.4/config/active/" + "pool");
        }

        @Test
        public void updateItemShouldReturnAPool() throws URISyntaxException, VTMRestClientException, VTMRestClientObjectNotFoundException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockBuilder.put((Entity) any())).thenReturn(this.mockResponse);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(pool);

            Response response = requestManager.updateItem(getPoolPath(), this.mockClient, vsName, pool, MediaType.APPLICATION_JSON_TYPE);
            Assert.assertNotNull(response.readEntity(Pool.class));
            Assert.assertEquals(200, response.getStatus());
            Assert.assertTrue(response.readEntity(Pool.class).getProperties().getHttp().getKeepalive());
        }

        @Test(expected = VTMRestClientException.class)
        public void updateItemShouldThrowVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException, URISyntaxException {
            Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(null);

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockBuilder.put((Entity) any())).thenReturn(this.mockResponse);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(pool);
            Response response = requestManager.updateItem(getPoolPath(), this.mockClient, vsName, pool, MediaType.APPLICATION_JSON_TYPE);

        }


        @Test(expected = VTMRestClientObjectNotFoundException.class)
        public void updateItemShouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, VTMRestClientException, VTMRestClientObjectNotFoundException {

            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");
            requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenDeletingAnItem {
        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);

        private VTMRequestManager requestManager;
        private String vsName;
        private Pool pool;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new VTMRequestManagerImpl();
            vsName = "12345_1234";
            pool = createPool();

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
//
            Mockito.when(this.mockBuilder.delete(Response.class)).thenReturn(this.mockResponse);
//
            WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
            Mockito.when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(this.mockBuilder);
//
            Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(mockWebTarget);
        }

        private Pool createPool() {
            PoolProperties poolProperties = new PoolProperties();
            PoolHttp poolHttp = new PoolHttp();
            poolHttp.setKeepalive(true);
            poolProperties.setHttp(poolHttp);

            Pool pool = new Pool();
            pool.setProperties(poolProperties);
            return pool;
        }


        private URI getPoolPath() throws URISyntaxException {
            return new URI("https://localhost:9070/api/tm/3.4/config/active/" + "pool");
        }


        @Test
        public void deleteItemShouldReturnNoContentAfterSuccessfulDeletion() throws URISyntaxException, VTMRestClientException, VTMRestClientObjectNotFoundException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(204);
            Response response = requestManager.deleteItem(getPoolPath(), this.mockClient, vsName);
            Assert.assertNotNull(response);
            Assert.assertEquals(204, response.getStatus());
        }

        @Test(expected = VTMRestClientException.class)
        public void updateItemShouldThrowVTMRestClientException() throws VTMRestClientException, VTMRestClientObjectNotFoundException, URISyntaxException {
            Mockito.when(this.mockClient.target(Matchers.anyString())).thenReturn(null);
            Mockito.when(this.mockResponse.getStatus()).thenReturn(204);
            Response response = requestManager.deleteItem(getPoolPath(), this.mockClient, vsName);

        }



        @Test(expected = VTMRestClientObjectNotFoundException.class)
        public void deleteItemShouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, VTMRestClientException, VTMRestClientObjectNotFoundException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");
            requestManager.deleteItem(getPoolPath(), this.mockClient, vsName);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class VerifyVirtualServerStatProperties {
        VirtualServerStatsProperties vs;
        VirtualServerStatsStatistics stats;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            vs = new VirtualServerStatsProperties();
            stats = new VirtualServerStatsStatistics();
            stats.setBytesIn(10L);
            stats.setBytesOut(10L);
            stats.setConnectionFailures(3L);
            vs.setStatistics(stats);
        }

        @Test
        public void verifyStatFieldsAreLong() {
            // Verify virtual server stats byte* fields are of type long
            // See: CLB-1021
            Assert.assertThat(stats.getConnectionFailures(), instanceOf(Long.class));
            Assert.assertThat(stats.getBytesIn(), instanceOf(Long.class));
            Assert.assertThat(stats.getBytesOut(), instanceOf(Long.class));
        }

    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class VerifyGlobalCounterStatistics {
        GlobalCountersStatistics gs;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            gs = new GlobalCountersStatistics();
            gs.setTotalBytesIn(10L);
            gs.setTotalBytesOut(10L);
            gs.setTotalCurrentConn(10);
        }

        @Test
        public void verifyCountersFields() {
            Assert.assertThat(gs.getTotalBytesIn(), instanceOf(Long.class));
            Assert.assertThat(gs.getTotalBytesOut(), instanceOf(Long.class));
            Assert.assertThat(gs.getTotalCurrentConn(), instanceOf(Integer.class));
        }

    }
}
