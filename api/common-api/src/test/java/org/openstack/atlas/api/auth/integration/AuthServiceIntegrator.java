package org.openstack.atlas.api.auth.integration;

import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@Ignore
//Run with proper credentials in 'test.properties' requires auth url, basic auth username and password.
@RunWith(Enclosed.class)
public class AuthServiceIntegrator {
//    public static class WhenAuthenticatingAgainstTheAuthServiceWithATestAccount {
//
//        private final static String NOT_SET = "";
//        private static String authToken = NOT_SET;
//        private final static String CLOUD = "cloud";
//
//        private static final String TEST_USER_NAME = "bobTester";
//        private static final String TEST_API_KEY = "1234567891011121313";
//        private static String TEST_NAST_ID;
//        private static Integer accountId = 123456;
//
//        private static String auth_callback_uri = FileUtil.getProperty("auth_stag_url");
//        private String auth_management_uri = FileUtil.getProperty("auth_management_uri");
//        private static String auth_username = FileUtil.getProperty("basic_auth_user");
//        private static String auth_password = FileUtil.getProperty("basic_auth_key");
//
//        AuthTokenValidator authTokenValidator;
//
//        private Configuration configuration;
//        private static KeyStoneAdminClient keyStoneAdminClient;
//
//        @BeforeClass
//        public static void SetupTestUser() throws IOException, URISyntaxException, KeyStoneException {
//            try {
//                keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password, auth_username);
//                User user = keyStoneAdminClient.createUser(TEST_USER_NAME, TEST_API_KEY, accountId, "14bb72c1-237c-42aa-9307-893045b596e0", true);
//                TEST_NAST_ID = user.getNastId();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        @Before
//        public void Setup() throws IOException, URISyntaxException, KeyStoneException {
//
//            configuration = mock(Configuration.class);
//            doReturn(true).when(configuration).hasKeys(PublicApiServiceConfigurationKeys.auth_management_uri, PublicApiServiceConfigurationKeys.basic_auth_user, PublicApiServiceConfigurationKeys.basic_auth_key);
//
//            doReturn(auth_username).when(configuration).getString(PublicApiServiceConfigurationKeys.basic_auth_user);
//            doReturn(auth_password).when(configuration).getString(PublicApiServiceConfigurationKeys.basic_auth_key);
//            doReturn(auth_callback_uri).when(configuration).getString(PublicApiServiceConfigurationKeys.auth_management_uri);
//
//
//            if (!authToken.equals(NOT_SET)) return;
//            KeyStoneClient authClient = new KeyStoneClient(auth_callback_uri);
//            try {
//                AuthData authData = authClient.authenticateUser(TEST_USER_NAME, TEST_API_KEY);
//                if (authData != null) authToken = authData.getToken().getId();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Test
//        public void shouldAuthenticateSuccessfully() throws URISyntaxException, MalformedURLException, KeyStoneException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            Assert.assertNotNull(authTokenValidator.validate(accountId, authToken));
//        }
//
//        @Test
//        public void shouldReturnValidUserWhenAuthenticatedSuccessfully() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            Assert.assertNotNull(authTokenValidator.validate(accountId, authToken));
//        }
//
//        @Test
//        public void shouldReturnValidAccountIdWhenAuthenticatedSuccessfully() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            String user = authTokenValidator.validate(accountId, authToken).getUserId();
//            Assert.assertNotNull(user);
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password, auth_username);
//            Assert.assertEquals(accountId, keyStoneAdminClient.listUser(user).getMossoId());
//        }
//
//        @Test
//        public void shouldReturnValidKeyWhenAuthenticatedSuccessfully() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            String user = authTokenValidator.validate(accountId, authToken).getUserId();
//            Assert.assertNotNull(user);
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password, auth_username);
//            Assert.assertEquals(TEST_API_KEY, keyStoneAdminClient.listUser(user).getKey());
//        }
//
//        @Test
//        public void shouldReturnValidEnabledUserWhenAuthenticatedSuccessfully() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            String user = authTokenValidator.validate(accountId, authToken).getUserId();
//            Assert.assertNotNull(user);
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password,auth_username);
//            Assert.assertTrue(keyStoneAdminClient.listUser(user).isEnabled());
//        }
//
//        @Test(expected = KeyStoneException.class)
//        public void shouldFailWhenInvalidToken() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            authTokenValidator.validate(accountId, "fake");
//        }
//
//        @Test(expected = KeyStoneException.class)
//        public void shouldFailWhenInvalidAccountId() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            authTokenValidator.validate(12345, authToken);
//        }
//
//        @Test
//        public void shouldGrabTheUsernameByProvidingAToken() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            authTokenValidator = new AuthTokenValidator(configuration);
//            Assert.assertEquals(TEST_USER_NAME, authTokenValidator.validate(accountId, authToken).getUserId());
//        }
//
//        @Test
//        public void shouldGrabTheUserByUserName() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password,auth_username);
//            Assert.assertEquals(TEST_USER_NAME, keyStoneAdminClient.listUser(TEST_USER_NAME).getId());
//        }
//
//        @Test
//        public void shouldGrabKeyForUserByUserName() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password,auth_username);
//            Assert.assertEquals(TEST_API_KEY, keyStoneAdminClient.listUser(TEST_USER_NAME).getKey());
//        }
//
//        @Test
//        public void shouldGrabAccountIdForUserByUserName() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password,auth_username);
//            Assert.assertEquals(accountId, keyStoneAdminClient.listUser(TEST_USER_NAME).getMossoId());
//        }
//
//        @Test(expected = KeyStoneException.class)
//        public void shouldThrowExceptionIfUserNotFound() throws KeyStoneException, URISyntaxException, MalformedURLException {
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password,auth_username);
//            keyStoneAdminClient.listUser("aTest");
//        }
//
//
//        @AfterClass
//        public static void Teardown() throws KeyStoneException, URISyntaxException {
//            keyStoneAdminClient = new KeyStoneAdminClient(auth_callback_uri, auth_password,auth_username);
//            keyStoneAdminClient.deleteUser(TEST_USER_NAME);
//            authToken = NOT_SET;
//        }
//    }
}
