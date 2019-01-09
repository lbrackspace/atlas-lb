package org.openstack.atlas.restclients.auth.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.restclients.auth.IdentityClientImpl;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.roles.RoleList;
import org.openstack.identity.client.user.User;
import org.openstack.identity.client.user.UserList;

import javax.xml.bind.JAXBException;
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
            Assert.assertNotNull(authToken);
        }

        @Ignore
        @Test
        public void shouldRetrieveImpersonationToken() throws URISyntaxException, IdentityFault, JAXBException {
            final String authToken = identityClient.getAuthToken();
            Assert.assertNotNull(authToken);
            String impToken = identityClient.getImpersonationToken(authToken, "crc32");
            Assert.assertNotNull(impToken);
        }

        @Ignore
        @Test
        public void shouldListUsersByTenantId() throws URISyntaxException, IdentityFault, JAXBException {
            final String authToken = identityClient.getAuthToken();
            Assert.assertNotNull(authToken);
            UserList users = identityClient.getUsersByTenantId(authToken, "354934");
            Assert.assertTrue(users.getUser().size() > 0);
        }

        @Ignore
        @Test
        public void shouldGetRolesForUser() throws URISyntaxException, IdentityFault, JAXBException {
            final String authToken = identityClient.getAuthToken();
            Assert.assertNotNull(authToken);
            UserList users = identityClient.getUsersByTenantId(authToken, "354934");
            RoleList roles = identityClient.listUserGlobalRoles(authToken, users.getUser().get(0).getId());
            Assert.assertNotNull(roles.getRole().size() > 0);
        }

        @Ignore
        @Test
        public void shouldReturnPrimaryUserForTenantSingleUser() throws URISyntaxException, IdentityFault, JAXBException {
            final String authToken = identityClient.getAuthToken();
            Assert.assertNotNull(authToken);
            User user = identityClient.getPrimaryUserForTenantId(authToken, "354934");
            Assert.assertEquals("crc32", user.getUsername());
        }

        @Ignore
        @Test
        public void shouldReturnPrimaryUserForTenantWithMultipleUsers() throws URISyntaxException, IdentityFault, JAXBException {
            final String authToken = identityClient.getAuthToken();
            Assert.assertNotNull(authToken);
            User user = identityClient.getPrimaryUserForTenantId(authToken, "5806065");
            Assert.assertEquals("cloudqabrandon", user.getUsername());
        }
    }
}
