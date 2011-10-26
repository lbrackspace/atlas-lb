package org.openstack.atlas.api.auth.integration;

import org.junit.*;
import org.openstack.atlas.api.auth.*;
import org.openstack.atlas.api.auth.integration.helpers.FileUtil;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.auth.AuthData;
import org.openstack.keystone.auth.client.AuthClient;
import org.openstack.user.User;

import static org.mockito.Mockito.*;

@Ignore
//Run with proper credentials in 'test.properties' requires auth url, basic auth username and password.
@RunWith(Enclosed.class)
public class AuthServiceIntegrator {
    public static class WhenAuthenticatingAgainstTheAuthServiceWithATestAccount {

        private final static String NOT_SET = "";
        private static String authToken = NOT_SET;

        private final String TEST_USER_NAME = FileUtil.getProperty("username");
        private final String TEST_API_KEY = FileUtil.getProperty("key");
        private Integer accountId = FileUtil.getIntProperty("mossoId");

        private String auth_callback_uri = FileUtil.getProperty("auth_url");
        private String auth_username = FileUtil.getProperty("basic_user");
        private String auth_password = FileUtil.getProperty("basic_pass");

        private Configuration configuration;

        @Before
        public void Setup() throws IOException {

            configuration = mock(Configuration.class);
            doReturn(true).when(configuration).hasKeys(PublicApiServiceConfigurationKeys.auth_callback_uri, PublicApiServiceConfigurationKeys.auth_username, PublicApiServiceConfigurationKeys.auth_password);

            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_callback_uri)).thenReturn(auth_callback_uri);
            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_username)).thenReturn(auth_username);
            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_password)).thenReturn(auth_password);


            if(!authToken.equals(NOT_SET)) return;
            AuthClient authClient = new AuthClient();

            try {
                AuthData authData = authClient.authenticateUser(auth_callback_uri, TEST_USER_NAME, TEST_API_KEY);
                if (authData != null) authToken = authData.getToken().getId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Test
        public void shouldAuthenticateSauccessfully() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            Assert.assertNotNull(authService.authenticate(accountId, authToken));
        }

        @Test
        public void shouldReturnValidUserWhenAuthenticatedSuccessfully() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            User user = authService.authenticate(accountId, authToken);
            Assert.assertNotNull(user);
        }

        @Test
        public void shouldReturnValidAccountIdWhenAuthenticatedSuccessfully() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            User user = authService.authenticate(accountId, authToken);
            Assert.assertNotNull(user);
            Assert.assertEquals(accountId, user.getMossoId());
        }

        @Test
        public void shouldReturnValidKeyWhenAuthenticatedSuccessfully() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            User user = authService.authenticate(accountId, authToken);
            Assert.assertNotNull(user);
            Assert.assertEquals(TEST_API_KEY, user.getKey());
        }

        @Test
        public void shouldReturnValidEnabledUserWhenAuthenticatedSuccessfully() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            User user = authService.authenticate(accountId, authToken);
            Assert.assertNotNull(user);
            Assert.assertTrue(user.isEnabled());
        }

        @Test(expected = Exception.class)
        public void shouldFailWhenInvalidToken() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            authService.authenticate(accountId, "fake");
        }

        @Test(expected = Exception.class)
        public void shouldFailWhenInvalidAccountId() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            authService.authenticate(12345, authToken);
        }

        @Test
        public void shouldGrabTheUsernameByProvidingAToken() throws Exception {
            AuthServiceImpl authService = new AuthServiceImpl(configuration);
            Assert.assertEquals(TEST_USER_NAME, authService.authenticate(accountId, authToken).getId());
        }

        @AfterClass
        public static void Teardown() {
            authToken = NOT_SET;
        }
    }
}
