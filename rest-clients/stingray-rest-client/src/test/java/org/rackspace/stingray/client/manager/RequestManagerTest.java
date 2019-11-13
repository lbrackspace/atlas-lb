package org.rackspace.stingray.client.manager;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.counters.VirtualServerStatsProperties;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.manager.impl.RequestManagerImpl;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolHttp;
import org.rackspace.stingray.client.pool.PoolProperties;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;

@RunWith(Enclosed.class)
public class RequestManagerTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenRetrievingAPool {

        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);


        private RequestManager requestManager;
        private String vsName;
        private Pool pool;

        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new RequestManagerImpl();
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
            return new URI("https://localhost:9070/api/tm/1.0/config/active/" + "pool");
        }

        @Test
        public void shouldReturnAPoolWhenResponseIsValid() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockBuilder.put((Entity) any())).thenReturn(this.mockResponse);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(pool);

            Response response = requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);

            Assert.assertEquals(200, response.getStatus());
            Assert.assertNotNull(response.readEntity(Pool.class));
            Assert.assertTrue(response.readEntity(Pool.class).getProperties().getHttp().getKeepalive());

        }

        @Test(expected = StingrayRestClientObjectNotFoundException.class)
        public void shouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");

            requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenUpdatingAPool {
        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);

        private RequestManager requestManager;
        private String vsName;
        private Pool pool;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new RequestManagerImpl();
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
            return new URI("https://localhost:9070/api/tm/1.0/config/active/" + "pool");
        }

        @Test
        public void shouldReturnAPoolAfterUpdate() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockBuilder.put((Entity) any())).thenReturn(this.mockResponse);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(pool);

            Response response = requestManager.updateItem(getPoolPath(), this.mockClient, vsName, pool, MediaType.APPLICATION_JSON_TYPE);
            Assert.assertNotNull(response.readEntity(Pool.class));
            Assert.assertEquals(200, response.getStatus());
            Assert.assertTrue(response.readEntity(Pool.class).getProperties().getHttp().getKeepalive());
        }


        @Test(expected = StingrayRestClientObjectNotFoundException.class)
        public void shouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");
            requestManager.getItem(getPoolPath(), this.mockClient, vsName, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenDeletingAPool {
        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);

        private RequestManager requestManager;
        private String vsName;
        private Pool pool;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new RequestManagerImpl();
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
            return new URI("https://localhost:9070/api/tm/1.0/config/active/" + "pool");
        }


        @Test
        public void shouldReturnTrueAfterSuccessfulDelete() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(204);
            Response response = requestManager.deleteItem(getPoolPath(), this.mockClient, vsName);
            Assert.assertNotNull(response);
            Assert.assertEquals(204, response.getStatus());
        }

        @Test(expected = StingrayRestClientObjectNotFoundException.class)
        public void shouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(400);
            Mockito.when(this.mockResponse.readEntity(String.class)).thenReturn("Invalid resource URI");
            requestManager.deleteItem(getPoolPath(), this.mockClient, vsName);
        }
    }

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class VerifyVirtualServerStatProperties {
        VirtualServerStatsProperties vs;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            vs = new VirtualServerStatsProperties();
            vs.setBytesIn(10L);
            vs.setBytesInHi(10L);
            vs.setBytesInLo(10L);
            vs.setBytesOut(10L);
            vs.setBytesOutHi(10L);
            vs.setBytesOutLo(10L);
            vs.setConnectionFailures(3L);
            vs.setTotalConn(2218728488L);
        }

        @Test
        public void verifyStatFieldsAreLong() {
            // Verify virtual server stats byte* fields are of type long
            // See: CLB-1021
            Assert.assertThat(vs.getConnectionFailures(), instanceOf(Long.class));
            Assert.assertThat(vs.getBytesIn(), instanceOf(Long.class));
            Assert.assertThat(vs.getBytesInHi(), instanceOf(Long.class));
            Assert.assertThat(vs.getBytesInLo(), instanceOf(Long.class));
            Assert.assertThat(vs.getBytesOut(), instanceOf(Long.class));
            Assert.assertThat(vs.getBytesOutHi(), instanceOf(Long.class));
            Assert.assertThat(vs.getBytesOutLo(), instanceOf(Long.class));
            Assert.assertThat(vs.getTotalConn(), instanceOf(Long.class));
        }

    }
}
