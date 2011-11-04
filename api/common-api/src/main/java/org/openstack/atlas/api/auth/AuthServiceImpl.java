package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.keystone.auth.client.AdminAuthClient;
import org.openstack.keystone.auth.pojo.exceptions.AuthException;
import org.openstack.user.User;

public class AuthServiceImpl implements AuthService {
    private static final Log LOG = LogFactory.getLog(AuthServiceImpl.class);
    public AdminAuthClient adminAuthClient;

    public AuthServiceImpl(Configuration cfg) throws MalformedURLException {
        if (cfg.hasKeys(PublicApiServiceConfigurationKeys.auth_callback_uri, PublicApiServiceConfigurationKeys.auth_username, PublicApiServiceConfigurationKeys.auth_password)) {
            LOG.info("AUTH URI from LOCAL CONF: " + cfg.getString(PublicApiServiceConfigurationKeys.auth_callback_uri));
            adminAuthClient = new AdminAuthClient(cfg.getString(PublicApiServiceConfigurationKeys.auth_callback_uri), cfg.getString(PublicApiServiceConfigurationKeys.auth_username), cfg.getString(PublicApiServiceConfigurationKeys.auth_password));
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new MalformedURLException(StringUtilities.AUTH_INIT_FAIL);
        }
    }

    @Override
    public String authenticate(Integer passedAccountId, String authToken, String type) throws AuthException, URISyntaxException {
        return adminAuthClient.validateToken(passedAccountId, authToken, type).getUserId();
    }

    @Override
    public User getUser(String userId) throws AuthException, URISyntaxException {
        return adminAuthClient.listUser(userId);
    }

    @Override
    public User getUserByAlternateId(String userId, String type) throws AuthException, URISyntaxException {
        return adminAuthClient.listUserByAlternateId(userId, type);
    }
}
