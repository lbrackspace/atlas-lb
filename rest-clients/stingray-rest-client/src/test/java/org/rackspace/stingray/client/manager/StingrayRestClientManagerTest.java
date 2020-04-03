package org.rackspace.stingray.client.manager;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.EncryptException;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.stingray.client.config.ClientConfigKeys;
import org.rackspace.stingray.client.config.StingrayRestClientConfiguration;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.manager.util.StingrayRequestManagerUtil;
import org.rackspace.stingray.client.pool.Pool;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RunWith(Enclosed.class)
public class StingrayRestClientManagerTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore({"javax.crypto.*", "javax.management.*", "javax.net.ssl.*"})
    public static class WhenInvokingClientManager {

        final Client mockClient = Mockito.mock(Client.class);
        final Response mockResponse = Mockito.mock(Response.class);
        final StingrayRequestManagerUtil requestManagerUtil = Mockito.mock(StingrayRequestManagerUtil.class);
        final StingrayRestClientConfiguration stingrayRestClientConfiguration = Mockito.mock(StingrayRestClientConfiguration.class);


        private StingrayRestClientManager stingrayRestClientManager;
        private StingrayRestClientManager stingrayRestClientManagerTwo;

        @Before
        public void standUp() throws URISyntaxException, IOException {

            Mockito.when(stingrayRestClientConfiguration.getString(ClientConfigKeys.stingray_admin_user)).thenReturn("admin");
            Mockito.when(stingrayRestClientConfiguration.getString(ClientConfigKeys.stingray_admin_key)).thenReturn("59c7e332d882b25ffde95fa0a12d51c9");
            Mockito.when(stingrayRestClientConfiguration.getString(ClientConfigKeys.stingray_base_uri)).thenReturn("api/tm/7.0/config/active/");
            Mockito.when(stingrayRestClientConfiguration.getString(ClientConfigKeys.stingray_rest_endpoint)).thenReturn("https://localhost:9070/");
            Mockito.when(stingrayRestClientConfiguration.getString(ClientConfigKeys.stingray_read_timeout)).thenReturn("501");
            Mockito.when(stingrayRestClientConfiguration.getString(ClientConfigKeys.stingray_connect_timeout)).thenReturn("444");
            stingrayRestClientManager = new StingrayRestClientManager(stingrayRestClientConfiguration,
                    new URI("endpoint"), mockClient, true, null, null);

        }

        @Test
        public void stingrayRestClientManagerShouldSetFieldsWhenNullParametersEntered() throws StingrayRestClientException {
            Assert.assertTrue(stingrayRestClientManager.isDebugging);
            Assert.assertEquals(new Integer(444), stingrayRestClientManager.connect_timeout);
            Assert.assertEquals(new Integer(501), stingrayRestClientManager.read_timeout);
            Assert.assertEquals("adminkey", stingrayRestClientManager.adminKey);
            Assert.assertEquals("admin", stingrayRestClientManager.adminUser);
            Assert.assertEquals(mockClient, stingrayRestClientManager.client);

        }

        @Test
        public void stingrayRestClientManagerShouldSetFieldsWithPassedParameters() throws StingrayRestClientException, URISyntaxException, EncryptException {
            System.out.println(CryptoUtil.encrypt("testKey"));
            stingrayRestClientManagerTwo = new StingrayRestClientManager(stingrayRestClientConfiguration, new URI("endpoint"), mockClient, false, "testAdmin", "testKey");
            Assert.assertEquals("testAdmin", stingrayRestClientManagerTwo.adminUser);
            Assert.assertEquals("testKey", stingrayRestClientManagerTwo.adminKey);
            Assert.assertFalse(stingrayRestClientManagerTwo.isDebugging);
        }

        @Test
        public void shouldIntrepretValidResponse() throws StingrayRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(200);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenReturn(new Pool());
            Assert.assertNotNull(stingrayRestClientManager.interpretResponse(mockResponse, Pool.class));
        }

        @Test(expected = StingrayRestClientException.class)
        public void shouldThrowExceptionWhenInvalidResponse() throws StingrayRestClientException {
            Mockito.when(this.mockResponse.getStatus()).thenReturn(500);
            Mockito.when(requestManagerUtil.isResponseValid(mockResponse)).thenReturn(false);
            Mockito.when(this.mockResponse.readEntity(Pool.class)).thenThrow(Exception.class);
            Assert.assertNotNull(stingrayRestClientManager.interpretResponse(mockResponse, Pool.class));
        }

        @Test
        public void shouldConfigureClient() throws StingrayRestClientException {
            Client client = stingrayRestClientManager.createClient(true);
            Assert.assertNotNull(client);
            Assert.assertEquals("TLS", client.getSslContext().getProtocol());
            Assert.assertEquals(new Integer("444"), client.getConfiguration().getProperty(ClientProperties.CONNECT_TIMEOUT));
            Assert.assertEquals(new Integer("501"), client.getConfiguration().getProperty(ClientProperties.READ_TIMEOUT));
        }
    }
}
