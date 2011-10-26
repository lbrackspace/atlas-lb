package org.openstack.atlas.api.auth;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.stubbing.Answer;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.Configuration;
import org.junit.Assert;
import org.junit.experimental.runners.Enclosed;
import org.openstack.keystone.auth.client.AdminAuthClient;
import org.openstack.token.FullToken;
import org.openstack.user.User;

import static org.mockito.Mockito.*;

@Ignore
@RunWith(Enclosed.class)
public class AuthServiceTest {
    public static class WhenAuthenticatingAgainstAuthService {
        private String authToken = "Some Auth Token";
        private Integer accountId = 111111;
        private String auth_callback_uri = "https://fake.com/v1.1/";
        private String auth_username = "someUser";
        private String auth_password = "somePass";

        private AdminAuthClient adminAuthClient;
        private AuthServiceImpl authService;
        private Configuration configuration;
        private FullToken fullToken;
        private User user;

        private WebResource webResource;
        private ClientResponse clientResponse;

        @Before
        public void Setup() throws Exception {
            configuration = mock(Configuration.class);
            doReturn(true).when(configuration).hasKeys(PublicApiServiceConfigurationKeys.auth_callback_uri, PublicApiServiceConfigurationKeys.auth_username, PublicApiServiceConfigurationKeys.auth_password);

            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_callback_uri)).thenReturn(auth_callback_uri);
            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_username)).thenReturn(auth_username);
            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_password)).thenReturn(auth_password);

            fullToken = new FullToken();
            fullToken.setUserId("aName");
            user = new User();
            user.setId("aUser");
            user.setMossoId(accountId);
            user.setKey("1234567890abcdefghij");
//
//            webResource = mock(WebResource.class);
//            clientResponse = mock(ClientResponse.class);
//            when(webResource.get(ClientResponse.class)).thenReturn(clientResponse);
//
//            authService = mock(AuthServiceImpl.class);
//            doReturn(user).when(authService).authenticate(Matchers.<Integer>any(), Matchers.<String>any());

            adminAuthClient = mock(AdminAuthClient.class);
            doReturn(user).when(adminAuthClient).listUserByMossoId(Matchers.<String>any());
            doReturn(fullToken).when(adminAuthClient).validateToken(Matchers.<String>any(), Matchers.<String>any());
        }

        @Test
        public void should_authenticate_token_successfully() throws Exception {
            user = new AuthServiceImpl(configuration).authenticate(accountId, authToken);
            Assert.assertNotNull(user);
        }
    }
}
