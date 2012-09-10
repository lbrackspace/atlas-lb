package org.openstack.atlas.atom.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.exceptions.MissingFieldException;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.client.keystone.KeyStoneAdminClient;
import org.openstack.client.keystone.KeyStoneClient;
import org.openstack.client.keystone.KeyStoneException;
import org.openstack.client.keystone.auth.AuthData;
import org.openstack.client.keystone.user.User;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class AHUSLAuthentication {
    private static final Log LOG = LogFactory.getLog(AHUSLAuthentication.class);

    public KeyStoneAdminClient keyStoneAdminClient;
    public KeyStoneClient keyStoneClient;
    private static Configuration configuration = new AtomHopperConfiguration();

    public AHUSLAuthentication() throws MalformedURLException, URISyntaxException, KeyStoneException {
        if (configuration.hasKeys(PublicApiServiceConfigurationKeys.auth_management_uri,
                PublicApiServiceConfigurationKeys.basic_auth_user,
                PublicApiServiceConfigurationKeys.basic_auth_key)) {

            LOG.info("Auth URI from local conf: " + configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri));
            keyStoneAdminClient = new KeyStoneAdminClient(configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri),
                    configuration.getString(PublicApiServiceConfigurationKeys.basic_auth_key),
                    configuration.getString(PublicApiServiceConfigurationKeys.basic_auth_user));
            keyStoneClient = new KeyStoneClient(configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri));
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new MissingFieldException(StringUtilities.AUTH_INIT_FAIL);
        }
    }

    public User retrieveKey(String userName) throws KeyStoneException, URISyntaxException {
        LOG.info("Retrieving LBaaS Usage Auth Key...");
        return keyStoneAdminClient.getUserKey(userName);
    }

    public AuthData retrieveToken(String userName, String key) throws KeyStoneException, URISyntaxException {
        LOG.info("Retrieving LBaaS Usage Auth Token...");
        return keyStoneClient.authenticateUser(userName, key);
    }

    public AuthData getToken(String userName) throws KeyStoneException, URISyntaxException {
       return retrieveToken(userName, retrieveKey(userName).getKey());
    }
}
