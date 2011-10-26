package org.openstack.atlas.api.auth;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.Configuration;
import org.junit.Assert;
import org.junit.experimental.runners.Enclosed;
import org.openstack.keystone.auth.client.AdminAuthClient;
import org.openstack.token.FullToken;
import org.openstack.user.User;

import java.net.MalformedURLException;

import static org.mockito.Mockito.*;


@Ignore
@RunWith(Enclosed.class)
public class AuthServiceTest {
    public static class WhenAuthenticatingAgainstAuthService {
        private String authToken = "Some Auth Token";
        private Integer accountId = 111111;
        private String auth_callback_uri = "https://auth.staging.us.ccp.rackspace.net/v1.1/";
        private String auth_username = "someUser";
        private String auth_password = "somePass";

        private AdminAuthClient adminAuthClient;
        private Configuration configuration;
        FullToken fullToken;
        User user;

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

            adminAuthClient = mock(AdminAuthClient.class);
            doReturn(fullToken).when(adminAuthClient).validateToken(Matchers.<String>any(), Matchers.<String>any());
            doReturn(user).when(adminAuthClient).listUserByMossoId(Matchers.<String>any());
        }

        @Test
        public void should_authenticate_token_successfully() throws Exception, MalformedURLException {
            Assert.assertNotNull(new AuthServiceImpl(configuration).authenticate(accountId, authToken));
        }
    }
}
