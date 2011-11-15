package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.client.keystone.KeyStoneAdminClient;
import org.openstack.client.keystone.KeyStoneException;

public class AuthServiceImpl implements AuthService {
    private static final Log LOG = LogFactory.getLog(AuthServiceImpl.class);
    public KeyStoneAdminClient keyStoneAdminClient;
    private Configuration configuration;

    public AuthServiceImpl(Configuration cfg) throws MalformedURLException, URISyntaxException, KeyStoneException {
        this.configuration = cfg;
        if (cfg.hasKeys(PublicApiServiceConfigurationKeys.auth_management_uri, PublicApiServiceConfigurationKeys.basic_auth_user, PublicApiServiceConfigurationKeys.basic_auth_key)) {
            LOG.info("AUTH URI from LOCAL CONF: " + configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri));
            keyStoneAdminClient = new KeyStoneAdminClient(configuration.getString(PublicApiServiceConfigurationKeys.auth_management_uri), cfg.getString(PublicApiServiceConfigurationKeys.basic_auth_key),cfg.getString(PublicApiServiceConfigurationKeys.basic_auth_user));
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new MalformedURLException(StringUtilities.AUTH_INIT_FAIL);
        }
    }

    @Override
    public String authenticate(Integer passedAccountId, String authToken, String type) throws KeyStoneException, URISyntaxException {
        return keyStoneAdminClient.validateToken(String.valueOf(passedAccountId), authToken, type).getUserId();
    }
}
