package org.openstack.atlas.restclients.auth.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.restclients.auth.IdentityClientImpl;
import org.openstack.identity.client.fault.IdentityFault;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@RunWith(Enclosed.class)
public class IdentityAuthClientTest {
    public static class WhenAuthenticatingUsers {
        IdentityAuthClient identityClient;


        @Before
        public void standUp() throws MalformedURLException, URISyntaxException, IdentityFault {
            identityClient = new IdentityClientImpl();
        }

        @Ignore
        @Test
        public void shouldRetrieveTokenForAdmin() throws URISyntaxException, IdentityFault {
            final String authToken = identityClient.getAuthToken();
            System.out.print(authToken);
            Assert.assertNotNull(authToken);
        }
    }
}
