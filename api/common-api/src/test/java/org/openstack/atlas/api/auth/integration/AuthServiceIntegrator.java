package org.openstack.atlas.api.auth.integration;

import org.junit.*;
import org.openstack.atlas.api.auth.*;
import org.openstack.atlas.api.auth.integration.helpers.FileUtil;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.client.keystone.KeyStoneClient;
import org.openstack.client.keystone.KeyStoneException;
import org.openstack.client.keystone.auth.AuthData;

import static org.mockito.Mockito.*;

@Ignore
//Run with proper credentials in 'test.properties' requires auth url, basic auth username and password.
@RunWith(Enclosed.class)
public class AuthServiceIntegrator {
    public static class WhenAuthenticatingAgainstTheAuthServiceWithATestAccount {

        private final static String NOT_SET = "";
        private static String authToken = NOT_SET;
        private final static String CLOUD = "cloud";
        private final static String MOSSO = "mosso";
        private final static String NAST = "nast";

        private final String TEST_USER_NAME = FileUtil.getProperty("username");
        private final String TEST_API_KEY = FileUtil.getProperty("key");
        private Integer accountId = FileUtil.getIntProperty("mossoId");

        private String auth_callback_uri = FileUtil.getProperty("auth_management_uri");
        private String auth_username = FileUtil.getProperty("basic_auth_user");
        private String auth_password = FileUtil.getProperty("basic_auth_key");

        AuthTokenValidator authService;

        private Configuration configuration;

        @Before
        public void Setup() throws IOException, URISyntaxException, KeyStoneException {

            configuration = mock(Configuration.class);
            doReturn(true).when(configuration).hasKeys(PublicApiServiceConfigurationKeys.auth_management_uri, PublicApiServiceConfigurationKeys.basic_auth_user, PublicApiServiceConfigurationKeys.basic_auth_key);

            doReturn(auth_username).when(configuration).getString(PublicApiServiceConfigurationKeys.basic_auth_user);
            doReturn(auth_password).when(configuration).getString(PublicApiServiceConfigurationKeys.basic_auth_key);
            doReturn(auth_callback_uri).when(configuration).getString(PublicApiServiceConfigurationKeys.auth_management_uri);


            if (!authToken.equals(NOT_SET)) return;
            KeyStoneClient authClient = new KeyStoneClient(auth_callback_uri);

            try {
                AuthData authData = authClient.authenticateUser(TEST_USER_NAME, TEST_API_KEY);
                if (authData != null) authToken = authData.getToken().getId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Test
        public void shouldAuthenticateSauccessfully() throws URISyntaxException, MalformedURLException, KeyStoneException {
             authService = new AuthTokenValidator(configuration);
            Assert.assertNotNull(authService.validate(accountId, authToken));
        }

//        @Test
//        public void shouldReturnValidUserWhenAuthenticatedSuccessfully() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertNotNull(authService.authenticate(accountId, authToken, CLOUD));
//        }
//
//        @Test
//        public void shouldReturnValidAccountIdWhenAuthenticatedSuccessfully() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            String user = authService.authenticate(accountId, authToken, CLOUD);
//            Assert.assertNotNull(user);
//            Assert.assertEquals(accountId, authService.getUser(user).getMossoId());
//        }
//
//        @Test
//        public void shouldReturnValidKeyWhenAuthenticatedSuccessfully() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            String user = authService.authenticate(accountId, authToken, CLOUD);
//            Assert.assertNotNull(user);
//            Assert.assertEquals(TEST_API_KEY, authService.getUser(user).getKey());
//        }
//
//        @Test
//        public void shouldReturnValidEnabledUserWhenAuthenticatedSuccessfully() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            String user = authService.authenticate(accountId, authToken, CLOUD);
//            Assert.assertNotNull(user);
//            Assert.assertTrue(authService.getUser(user).isEnabled());
//        }
//
//        @Test(expected = KeyStoreException.class)
//        public void shouldFailWhenInvalidToken() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            authService.authenticate(accountId, "fake", CLOUD);
//        }
//
//        @Test(expected = KeyStoreException.class)
//        public void shouldFailWhenInvalidAccountId() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            authService.authenticate(12345, authToken, CLOUD);
//        }
//
//        @Test
//        public void shouldGrabTheUsernameByProvidingAToken() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(TEST_USER_NAME, authService.authenticate(accountId, authToken, CLOUD));
//        }
//
//        @Test
//        public void shouldGrabTheUserByUserName() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(TEST_USER_NAME, authService.getUser(TEST_USER_NAME).getId());
//        }
//
//        @Test
//        public void shouldGrabKeyForUserByUserName() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(TEST_API_KEY, authService.getUser(TEST_USER_NAME).getKey());
//        }
//
//        @Test
//        public void shouldGrabAccountIdForUserByUserName() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(accountId, authService.getUser(TEST_USER_NAME).getMossoId());
//        }
//
//        @Test(expected = KeyStoreException.class)
//        public void shouldThrowExceptionIfUserNotFound() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            authService.getUser("aTest");
//        }
//
//        @Test
//        public void shouldGrabTheUserByAlternateId() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(TEST_USER_NAME, authService.getUserByAlternateId(String.valueOf(accountId), MOSSO).getId());
//        }
//
//        @Test
//        public void shouldGrabAccountIdForUserByAlternateId() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(TEST_USER_NAME, authService.getUserByAlternateId(String.valueOf(accountId), MOSSO).getId());
//            Assert.assertEquals(accountId, authService.getUserByAlternateId(String.valueOf(accountId), MOSSO).getMossoId());
//        }
//
//        @Test
//        public void shouldGrabKeyForUserByAlternateId() throws KeyStoreException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            Assert.assertEquals(TEST_USER_NAME, authService.getUserByAlternateId(String.valueOf(accountId), MOSSO).getId());
//            Assert.assertEquals(TEST_API_KEY, authService.getUserByAlternateId(String.valueOf(accountId), MOSSO).getKey());
//        }
//
//        @Test(expected = KeyStoreException.class)
//        public void shouldThrowExceptionIfUserByAlternateIdNotFound() throws AuthException, URISyntaxException, MalformedURLException {
//             authService = new AuthServiceImpl(configuration);
//            authService.getUserByAlternateId("aTest", MOSSO);
//        }

        @AfterClass
        public static void Teardown() {
            authToken = NOT_SET;
        }
    }
}
