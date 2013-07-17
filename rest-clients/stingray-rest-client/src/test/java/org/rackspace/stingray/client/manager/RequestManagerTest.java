package org.rackspace.stingray.client.manager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.manager.impl.RequestManagerImpl;
import org.rackspace.stingray.client.mock.MockClientHandler;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolHttp;
import org.rackspace.stingray.client.pool.PoolProperties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class RequestManagerTest {


    @RunWith(MockitoJUnitRunner.class)
    public static class WhenRetrievingAPool {
        private RequestManager requestManager;
        private Client client;
        private WebResource webResource;
        private WebResource.Builder builder;
        private String vsName;
        private ClientResponse mockedResponse;
        private MockClientHandler mockClientHandler;
        private Pool pool;
        private MediaType mockedType;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new RequestManagerImpl();
            vsName = "12345_1234";

            pool = createPool();

            mockClientHandler = new MockClientHandler();

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

        private void setupMocks() throws URISyntaxException, StingrayRestClientException {
            ClientRequest clientRequest = new ClientRequest.Builder().accept(MediaType.APPLICATION_JSON).build(getPoolPath(), "GET");
            mockedResponse = mockClientHandler.handle(clientRequest);

            client = mock(Client.class);
            webResource = mock(WebResource.class);
            builder = mock(WebResource.Builder.class);
            //requestManager = mock(RequestManagerImpl.class);

            when(client.resource(anyString())).thenReturn(webResource);
            when(webResource.accept(Matchers.<MediaType>any())).thenReturn(builder);
            when(builder.get(ClientResponse.class)).thenReturn(mockedResponse);

        }

        private URI getPoolPath() throws URISyntaxException {
            return new URI(MockClientHandler.ROOT + "pool");
        }



        @Test
        public void shouldReturnAPoolWhenResponseIsValid() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

            mockClientHandler.when("pool", "GET").thenReturn(Response.Status.ACCEPTED, pool);

            setupMocks();

            ClientResponse response = requestManager.getItem(getPoolPath(), client, vsName, MediaType.APPLICATION_JSON_TYPE);

            Assert.assertNotNull(response.getEntity(Pool.class));
            Assert.assertTrue(true);
        }

        @Ignore
        @Test(expected = StingrayRestClientException.class)
        public void shouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            mockClientHandler.when("pool", "GET").thenReturn(Response.Status.BAD_REQUEST, pool);

            setupMocks();

            requestManager.getItem(getPoolPath(), client, vsName, MediaType.APPLICATION_JSON_TYPE);
        }


    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenUpdatingAPool {
        private RequestManager requestManager;
        private Client client;
        private WebResource webResource;
        private String vsName;
        private ClientResponse mockedResponse;
        private MockClientHandler mockClientHandler;
        private Pool pool;
        private WebResource.Builder builder;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new RequestManagerImpl();
            vsName = "12345_1234";

            pool = createPool();

            mockClientHandler = new MockClientHandler();
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
            return new URI(MockClientHandler.ROOT + "pool");
        }


        private void setupMocks() throws URISyntaxException {
            ClientRequest clientRequest = new ClientRequest.Builder().accept(MediaType.APPLICATION_JSON).build(getPoolPath(), "PUT");
            mockedResponse = mockClientHandler.handle(clientRequest);

            client = mock(Client.class);
            webResource = mock(WebResource.class);
            builder = mock(WebResource.Builder.class);

            when(client.resource(anyString())).thenReturn(webResource);
            when(webResource.accept(Matchers.<MediaType>any())).thenReturn(builder);
            when(builder.type(Matchers.<MediaType>any())).thenReturn(builder);
            when(builder.entity(pool)).thenReturn(builder);
            when(builder.put(ClientResponse.class)).thenReturn(mockedResponse);
        }

        @Ignore
        @Test
        public void shouldReturnAPoolAfterUpdate() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            mockClientHandler.when("pool", "PUT").thenReturn(Response.Status.ACCEPTED, pool);
            pool = mock(Pool.class);

            setupMocks();

            ClientResponse response = requestManager.updateItem(getPoolPath(), client, vsName, pool, MediaType.APPLICATION_JSON_TYPE);
            Assert.assertNotNull(response.getEntity(Pool.class));
            Pool poolEntity = response.getEntity(Pool.class);
            Assert.assertTrue(true);
        }


        //TODO: need to update this in the mockedHandler...
        @Ignore
        @Test(expected = StingrayRestClientException.class)
        public void shouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

            mockClientHandler.when("pool", "PUT").thenReturn(Response.Status.BAD_REQUEST, pool);

            setupMocks();

            requestManager.getItem(getPoolPath(), client, vsName, MediaType.APPLICATION_JSON_TYPE);

        }
    }

    public static class WhenDeletingAPool {
        private RequestManager requestManager;
        private Client client;
        private WebResource webResource;
        private String vsName;
        private ClientResponse mockedResponse;
        private MockClientHandler mockClientHandler;
        private Pool pool;
        private WebResource.Builder builder;


        @Before
        public void standUp() throws URISyntaxException, IOException {
            requestManager = new RequestManagerImpl();
            vsName = "12345_1234";

            pool = createPool();

            mockClientHandler = new MockClientHandler();
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
            return new URI(MockClientHandler.ROOT + "pool");
        }


        private void setupMocks() throws URISyntaxException {
            ClientRequest clientRequest = new ClientRequest.Builder().accept(MediaType.APPLICATION_JSON).build(getPoolPath(), "DELETE");
            mockedResponse = mockClientHandler.handle(clientRequest);

            client = mock(Client.class);
            webResource = mock(WebResource.class);
            builder = mock(WebResource.Builder.class);

            when(client.resource(anyString())).thenReturn(webResource);
            when(webResource.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);
            when(builder.type(MediaType.APPLICATION_JSON)).thenReturn(builder);
            when(builder.delete(ClientResponse.class)).thenReturn(mockedResponse);
        }


         @Test
        public void shouldReturnTrueAfterSuccessfulDelete() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            mockClientHandler.when("pool", "DELETE").thenReturn(Response.Status.ACCEPTED, pool);
            pool = mock(Pool.class);

            setupMocks();

            Assert.assertTrue(requestManager.deleteItem(getPoolPath(), client, vsName));
        }

        @Test(expected = StingrayRestClientException.class)
        public void shouldThrowExceptionWhenBadResponseStatus() throws URISyntaxException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
            mockClientHandler.when("pool", "DELETE").thenReturn(Response.Status.BAD_REQUEST, pool);

            setupMocks();

            requestManager.getItem(getPoolPath(), client, vsName, MediaType.APPLICATION_JSON_TYPE);
        }
    }
}
