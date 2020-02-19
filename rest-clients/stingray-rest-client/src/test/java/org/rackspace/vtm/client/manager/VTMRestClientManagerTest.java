package org.rackspace.vtm.client.manager;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.vtm.client.config.ClientConfigKeys;
import org.rackspace.vtm.client.config.VTMRestClientConfiguration;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.manager.util.VTMRequestManagerUtil;
import org.rackspace.vtm.client.pool.Pool;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RunWith(Enclosed.class)
public class VTMRestClientManagerTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"javax.crypto.*", "javax.management.*", "javax.net.ssl.*"})
    public static class WhenInvokingClientManager {

        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final VTMRequestManagerUtil requestManagerUtil = Mockito.mock(VTMRequestManagerUtil.class);
        final VTMRestClientConfiguration vtmRestClientConfiguration = Mockito.mock(VTMRestClientConfiguration.class);


        private VTMRestClientManager vtmRestClientManager;

        @Before
        public void standUp() throws URISyntaxException, IOException {

            Mockito.when(vtmRestClientConfiguration.getString(ClientConfigKeys.stingray_admin_user)).thenReturn("admin");
            Mockito.when(vtmRestClientConfiguration.getString(ClientConfigKeys.stingray_admin_key)).thenReturn("59c7e332d882b25ffde95fa0a12d51c9");
            Mockito.when(vtmRestClientConfiguration.getString(ClientConfigKeys.stingray_base_uri)).thenReturn("api/tm/7.0/config/active/");
            Mockito.when(vtmRestClientConfiguration.getString(ClientConfigKeys.stingray_rest_endpoint)).thenReturn("https://localhost:9070/");
            Mockito.when(vtmRestClientConfiguration.getString(ClientConfigKeys.stingray_read_timeout)).thenReturn("501");
            Mockito.when(vtmRestClientConfiguration.getString(ClientConfigKeys.stingray_connect_timeout)).thenReturn("444");
            vtmRestClientManager = new VTMRestClientManager(vtmRestClientConfiguration,
                    new URI("endpoint"), mockClient, true, null, null);

        }

        @Test
        public void shouldSetClientFields() throws VTMRestClientException {
            Assert.assertTrue(vtmRestClientManager.isDebugging);
            Assert.assertEquals(new Integer(444), vtmRestClientManager.connect_timeout);
            Assert.assertEquals(new Integer(501), vtmRestClientManager.read_timeout);
            Assert.assertEquals("adminkey", vtmRestClientManager.adminKey);
            Assert.assertEquals("admin", vtmRestClientManager.adminUser);
            Assert.assertEquals(mockClient, vtmRestClientManager.client);

        }

        @Test
        public void shouldIntrepretValidResponse() throws VTMRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(new Pool());
            Assert.assertNotNull(vtmRestClientManager.interpretResponse(mockResponse, Pool.class));
        }

        @Test(expected = VTMRestClientException.class)
        public void shouldThrowExceptionWhenInvalidResponse() throws VTMRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(500);
            Mockito.when(requestManagerUtil.isResponseValid(mockResponse)).thenReturn(false);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenThrow(Exception.class);
            Assert.assertNotNull(vtmRestClientManager.interpretResponse(mockResponse, Pool.class));
        }

        @Test
        public void shouldConfigureClient() throws VTMRestClientException {
            Client client = vtmRestClientManager.createClient(true);
            Assert.assertNotNull(client);
            Assert.assertEquals("TLS", client.getSslContext().getProtocol());
            Assert.assertEquals(new Integer("444"), client.getConfiguration().getProperty(ClientProperties.CONNECT_TIMEOUT));
            Assert.assertEquals(new Integer("501"), client.getConfiguration().getProperty(ClientProperties.READ_TIMEOUT));
        }
    }
}
