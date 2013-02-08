package org.openstack.atlas.api.auth;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.filters.AuthenticationFilter;
import org.openstack.atlas.api.helpers.UrlAccountIdExtractor;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.client.keystone.KeyStoneAdminClient;
import org.openstack.client.keystone.token.FullToken;
import org.openstack.client.keystone.user.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.mockito.Mockito.*;

@Ignore
@RunWith(Enclosed.class)
public class AuthServiceTest {
    public static class WhenVerifyingReposeApiKey {
        private Configuration configuration;
        private ServletRequest servletRequest;
        private ServletResponse servletResponse;
        private FilterChain filterChain;

        @Before
        public void Setup() throws Exception {
            configuration = mock(Configuration.class);
            servletRequest = mock(ServletRequest.class);
            servletResponse = mock(ServletResponse.class);
            filterChain = mock(FilterChain.class);
        }

        @Test
        public void should_filter_when_headers_set() throws Exception {
            AuthenticationFilter filter = new AuthenticationFilter(new AuthTokenValidator(configuration), new UrlAccountIdExtractor());
            filter.doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    public static class WhenAuthenticatingAgainstAuthService {
        private String authToken = "Some Auth Token";
        private Integer accountId = 111111;
        private String auth_callback_uri = "https://fake.com/v1.1/";
        private String auth_username = "someUser";
        private String auth_password = "somePass";

        private KeyStoneAdminClient keyStoneAdminClient;
        private AuthTokenValidator authTokenValidator;
        private Configuration configuration;
        private FullToken fullToken;
        private User user;

        private WebResource webResource;
        private ClientResponse clientResponse;

        @Before
        public void Setup() throws Exception {
            configuration = mock(Configuration.class);
            doReturn(true).when(configuration).hasKeys(PublicApiServiceConfigurationKeys.auth_callback_uri, PublicApiServiceConfigurationKeys.basic_auth_user, PublicApiServiceConfigurationKeys.basic_auth_key);

            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_callback_uri)).thenReturn(auth_callback_uri);
            when(configuration.getString(PublicApiServiceConfigurationKeys.basic_auth_user)).thenReturn(auth_username);
            when(configuration.getString(PublicApiServiceConfigurationKeys.basic_auth_key)).thenReturn(auth_password);

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
//            authTokenValidator = mock(AuthServiceImpl.class);
//            doReturn(user).when(authTokenValidator).authenticate(Matchers.<Integer>any(), Matchers.<String>any());

            keyStoneAdminClient = mock(KeyStoneAdminClient.class);
            doReturn(fullToken).when(keyStoneAdminClient).validateToken(Matchers.<String>any(), Matchers.<String>any(), Matchers.<String>any());
        }

        @Test
        public void should_authenticate_token_successfully() throws Exception {
//            user = new AuthServiceImpl(configuration).authenticate(accountId, authToken, "cloud");
            Assert.assertNotNull(user);
        }
    }
}
