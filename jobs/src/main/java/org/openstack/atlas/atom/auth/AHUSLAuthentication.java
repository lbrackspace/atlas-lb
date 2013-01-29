package org.openstack.atlas.atom.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.exceptions.MissingFieldException;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.identity.client.client.IdentityClient;
import org.openstack.identity.client.fault.IdentityFault;
import org.openstack.identity.client.token.AuthenticateResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class AHUSLAuthentication {
    private static final Log LOG = LogFactory.getLog(AHUSLAuthentication.class);

    public IdentityClient identityClient;
    private static Configuration configuration = new AtomHopperConfiguration();

    public AHUSLAuthentication() throws MalformedURLException, URISyntaxException, IdentityFault {
        if (configuration.hasKeys(PublicApiServiceConfigurationKeys.auth_management_uri,
                PublicApiServiceConfigurationKeys.basic_auth_user,
                PublicApiServiceConfigurationKeys.basic_auth_key)) {

            LOG.info("Auth URI from local conf: " + configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri));
            identityClient = new IdentityClient(configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri));
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new MissingFieldException(StringUtilities.AUTH_INIT_FAIL);
        }
    }

    public AuthenticateResponse getToken(String username, String password) throws URISyntaxException, IdentityFault {
       return identityClient.authenticateUsernamePassword(username, password);
    }
}
