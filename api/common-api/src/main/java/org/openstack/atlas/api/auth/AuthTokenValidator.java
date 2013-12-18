package org.openstack.atlas.api.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.exceptions.MissingFieldException;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.identity.client.client.IdentityClient;
import org.openstack.identity.client.fault.IdentityFault;
import org.openstack.identity.client.token.AuthenticateResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class AuthTokenValidator {
    private static final Log LOG = LogFactory.getLog(AuthTokenValidator.class);

    public IdentityClient identityClient;
    private Configuration configuration;

    public AuthTokenValidator(Configuration cfg) throws MalformedURLException, URISyntaxException, IdentityFault {
        this.configuration = cfg;
        if (cfg.hasKeys(PublicApiServiceConfigurationKeys.identity_auth_url,
                PublicApiServiceConfigurationKeys.identity_user,
                PublicApiServiceConfigurationKeys.identity_pass)) {

            LOG.info("Auth URI from local conf: " + configuration.getString(PublicApiServiceConfigurationKeys.identity_auth_url));
            identityClient = new IdentityClient(configuration.getString(PublicApiServiceConfigurationKeys.identity_auth_url));
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new MissingFieldException(StringUtilities.AUTH_INIT_FAIL);
        }
    }

    public AuthenticateResponse validate(String userToken, String tenantId) throws URISyntaxException, IdentityFault {
        LOG.info("Within validate ... about to call client authenticate...");
        AuthenticateResponse admin = identityClient.authenticateUsernamePassword(configuration.getString(PublicApiServiceConfigurationKeys.identity_user), configuration.getString(PublicApiServiceConfigurationKeys.identity_pass));
        return identityClient.validateToken(admin.getToken().getId(), userToken, tenantId);
    }
}
