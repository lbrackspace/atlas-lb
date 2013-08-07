package org.openstack.atlas.auth;

import org.apache.log4j.Logger;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.exception.AuthException;
import org.openstack.client.keystone.KeyStoneAdminClient;
import org.openstack.client.keystone.KeyStoneException;
import org.openstack.client.keystone.user.User;

import org.openstack.atlas.config.LbLogsConfiguration;

public class AuthServiceImpl implements AuthService {

    private static final Logger LOG = Logger.getLogger(AuthServiceImpl.class);

    private KeyStoneAdminClient keyStoneAdminAuthClient;
    private Configuration configuration;

    public AuthServiceImpl(Configuration cfg) throws AuthException {
        this.configuration = cfg;
        keyStoneAdminAuthClient = null;
        try {
            keyStoneAdminAuthClient = new KeyStoneAdminClient(configuration.getString(LbLogsConfigurationKeys.auth_management_uri),
                        configuration.getString(LbLogsConfigurationKeys.basic_auth_key),
                        configuration.getString(LbLogsConfigurationKeys.basic_auth_user));
        } catch (KeyStoneException e) {
            throw new AuthException("Error initializing the client.", e);
        }
    }

    public AuthUser getUser(String accountId) throws AuthException {
        AuthUser user = new AuthUser();
        try {
            User validatedUser = keyStoneAdminAuthClient.listUser(accountId, "mosso");
            LOG.debug("Successfully Fetched auth info for account: " + accountId);
            user.setMossoAccountId(String.valueOf(validatedUser.getMossoId()));
            user.setNastAccountId(validatedUser.getNastId());
            user.setUsername(validatedUser.getId());
            user.setAuthKey(validatedUser.getKey());
            user.setEnabled(validatedUser.isEnabled());
            user.setRegion(configuration.getString(LbLogsConfigurationKeys.files_region));
            user.setCloudFilesAuthUrl(getCloudFilesAuthUrl() + "auth");
            return user;
        } catch (Exception e) {
            throw new AuthException("Exception getting auth info for account " + accountId + " from " + configuration.getString(LbLogsConfigurationKeys.auth_management_uri), e);
        }
    }

    public String getCloudFilesAuthUrl() {
        return configuration.getString(LbLogsConfigurationKeys.auth_management_uri);
    }

}
