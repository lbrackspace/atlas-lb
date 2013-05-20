package org.openstack.atlas.restclients.auth;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.restclients.auth.config.AuthenticationCredentialConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.identity.client.client.IdentityClient;
import org.openstack.identity.client.fault.IdentityFault;
import org.openstack.identity.client.token.AuthenticateResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class IdentityClientImpl implements IdentityAuthClient {
    private static final Log LOG = LogFactory.getLog(IdentityClientImpl.class);

    public IdentityClient identityClient;
    private static Configuration configuration = new AuthenticationCredentialConfiguration();

    public IdentityClientImpl() throws MalformedURLException, URISyntaxException, IdentityFault {
        if (configuration.hasKeys(PublicApiServiceConfigurationKeys.identity_auth_url,
                PublicApiServiceConfigurationKeys.identity_user,
                PublicApiServiceConfigurationKeys.identity_pass)) {

            LOG.info("Auth URI from local conf: " + configuration.getString(PublicApiServiceConfigurationKeys.identity_auth_url));
            identityClient = new IdentityClient(configuration.getString(PublicApiServiceConfigurationKeys.identity_auth_url));
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new RuntimeException(StringUtilities.AUTH_INIT_FAIL);
        }
    }

    @Override
    public AuthenticateResponse getAuthResponse() throws URISyntaxException, IdentityFault {
        String username = configuration.getString(PublicApiServiceConfigurationKeys.identity_user);
        String password = configuration.getString(PublicApiServiceConfigurationKeys.identity_pass);
        return this.getAuthResponse(username, password);
    }

    @Override
    public AuthenticateResponse getAuthResponse(String username, String password) throws URISyntaxException, IdentityFault {
        return identityClient.authenticateUsernamePassword(username, password);
    }

    @Override
    public String getAuthToken() throws URISyntaxException, IdentityFault {
        String username = configuration.getString(PublicApiServiceConfigurationKeys.identity_user);
        String password = configuration.getString(PublicApiServiceConfigurationKeys.identity_pass);
        return this.getAuthResponse(username, password).getToken().getId();
    }

    @Override
    public String getAuthToken(String username, String password) throws URISyntaxException, IdentityFault {
        return getAuthResponse(username, password).getToken().getId();
    }
}
