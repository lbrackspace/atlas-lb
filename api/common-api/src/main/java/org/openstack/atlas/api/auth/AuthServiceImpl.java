package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;

import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.openstack.keystone.auth.client.AdminAuthClient;
import org.openstack.user.User;

public class AuthServiceImpl implements AuthService {
    private static final Log LOG = LogFactory.getLog(AuthServiceImpl.class);
    public AdminAuthClient adminAuthClient;
    private String basicAuthUsername;
    private String basicAuthPassword;
    private String authUrl;

    public AuthServiceImpl(Configuration cfg) throws MalformedURLException {
        if (cfg.hasKeys(PublicApiServiceConfigurationKeys.auth_callback_uri, PublicApiServiceConfigurationKeys.auth_username, PublicApiServiceConfigurationKeys.auth_password)) {
            basicAuthUsername = cfg.getString(PublicApiServiceConfigurationKeys.auth_username);
            basicAuthPassword = cfg.getString(PublicApiServiceConfigurationKeys.auth_password);

            authUrl = cfg.getString(PublicApiServiceConfigurationKeys.auth_callback_uri);
            LOG.info("AUTH URI from LOCAL CONF: " + authUrl);

            adminAuthClient = new AdminAuthClient(authUrl, basicAuthUsername, basicAuthPassword);
        } else {
            LOG.error(StringUtilities.AUTH_INIT_FAIL);
            throw new MalformedURLException(StringUtilities.AUTH_INIT_FAIL);
        }

        if (HttpsCertIgnore.getInitException() != null) {
            Exception ex = HttpsCertIgnore.getInitException();
            throw new MalformedURLException(StringUtilities.getHttpsInitExceptionString(ex));
        }
    }

    @Override
    public User authenticate(Integer passedAccountId, String authToken) throws Exception {
        try {
            User mossoUser = adminAuthClient.listUserByMossoId(String.valueOf(passedAccountId));
            return adminAuthClient.validateToken(mossoUser.getId(), authToken) != null ? mossoUser : null;
        } catch (Exception e) {
            throw new Exception("There was an error communicating with the auth service: " + e.getMessage());
        }
    }
}
