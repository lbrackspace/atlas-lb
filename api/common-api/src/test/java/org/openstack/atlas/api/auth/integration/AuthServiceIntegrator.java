package org.openstack.atlas.api.auth.integration;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.auth.AccountService;
import org.openstack.atlas.api.auth.AccountServiceImpl;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.auth.AuthServiceImpl;
import org.openstack.atlas.api.auth.XmlRpcClientConfigFactory;
import org.openstack.atlas.api.auth.XmlRpcClientFactory;
import org.openstack.atlas.api.auth.integration.helpers.AuthHelper;
import org.openstack.atlas.api.config.ServiceConfigDefaults;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class AuthServiceIntegrator {
    public static class WhenAuthenticatingAgainstTheAuthServiceWithATestAccount {

        private final static String NOT_SET = "";
        private static String authToken = NOT_SET;
        private XmlRpcClientConfigImpl xmlRpcClientConfig;

        private final String TEST_USER_NAME = "pftestacct2";
        private final String TEST_API_KEY = "0f97f489c848438090250d50c7e1ea88";

        private Configuration configuration;

        @Before
        public void Setup() throws IOException {
            configuration = mock(Configuration.class);

            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_callback_uri)).thenReturn(ServiceConfigDefaults.AUTH_LOCATION_URI);
            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_username)).thenReturn(ServiceConfigDefaults.AUTH_USERNAME);
            when(configuration.getString(PublicApiServiceConfigurationKeys.auth_username)).thenReturn(ServiceConfigDefaults.AUTH_PASSWORD);


            xmlRpcClientConfig = new XmlRpcClientConfigImpl();
            if(!authToken.equals(NOT_SET)) return;

            authToken = AuthHelper.getAuthToken(TEST_USER_NAME, TEST_API_KEY);
        }

        @Test
        public void ShouldAuthenticateSuccessfully() throws MalformedURLException, XmlRpcException {
            XmlRpcClientConfigFactory clientConfigFactory = new XmlRpcClientConfigFactory(xmlRpcClientConfig);
            XmlRpcClientFactory clientFactory = new XmlRpcClientFactory(clientConfigFactory);

            AuthServiceImpl authService = new AuthServiceImpl(clientFactory, configuration);

            Assert.assertTrue(authService.authenticate(authToken));
        }

        @Test
        public void ShouldGrabTheAccountIdByProvidingAToken() throws MalformedURLException, XmlRpcException {
            XmlRpcClientConfigFactory clientConfigFactory = new XmlRpcClientConfigFactory(xmlRpcClientConfig);
            XmlRpcClientFactory clientFactory = new XmlRpcClientFactory(clientConfigFactory);

            AccountService authService = new AccountServiceImpl(clientFactory, configuration);

            Assert.assertEquals(new Integer(440369), authService.getAccountIdByToken(authToken));
        }

        @AfterClass
        public static void Teardown() {
            authToken = NOT_SET;
        }
    }
}
