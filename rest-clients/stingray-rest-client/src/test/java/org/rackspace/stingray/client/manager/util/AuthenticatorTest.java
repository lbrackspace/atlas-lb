package org.rackspace.stingray.client.manager.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.net.URISyntaxException;


@RunWith(Enclosed.class)
public class AuthenticatorTest {

    @RunWith(PowerMockRunner.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenHandlingRequest {

        final ClientRequestContext clientRequestContext = Mockito.mock(ClientRequestContext.class);


        private Authenticator authenticator;

        @Before
        public void standUp() throws URISyntaxException, IOException {
            authenticator = new Authenticator("user", "pass");
        }

        @Test
        public void shouldAddBasicAuthHeader() throws IOException {
            Mockito.when(clientRequestContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
            authenticator.filter(clientRequestContext);
            Assert.assertEquals("BASIC dXNlcjpwYXNz", clientRequestContext.getHeaders().get("Authorization").get(0));

        }
    }
}
