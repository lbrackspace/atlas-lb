package org.openstack.atlas.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.data.AuthUser;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.openstack.keystone.auth.client.AdminAuthClient;
import org.openstack.keystone.auth.pojo.exceptions.AuthException;
import org.openstack.user.User;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class AuthServiceImpl implements AuthService {

    private static final Logger LOG = Logger.getLogger(AuthServiceImpl.class);

    private final AdminAuthClient adminAuthClient;
    private XmlRpcClientConfigImpl xmlRpcConfig = new XmlRpcClientConfigImpl();
    private Configuration configuration;

    public AuthServiceImpl(Configuration cfg) throws MalformedURLException, AuthException {
        this.configuration = cfg;
        adminAuthClient = new AdminAuthClient(cfg.getString(LbLogsConfigurationKeys.auth_management_uri), cfg.getString(LbLogsConfigurationKeys.auth_username), cfg.getString(LbLogsConfigurationKeys.auth_password));
    }

    public AuthUser getUser(String accountId) throws AuthException, URISyntaxException, org.openstack.atlas.exception.AuthException {
        AuthUser user = new AuthUser();
        try {
            User validatedUser = adminAuthClient.listUserByAlternateId(accountId, "mosso");
            LOG.info("Successfully Fetched auth info for account: " + accountId);
            user.setMossoAccountId(String.valueOf(validatedUser.getMossoId()));
            user.setNastAccountId(validatedUser.getNastId());
            user.setUsername(validatedUser.getId());
            user.setAuthKey(validatedUser.getKey());
            user.setEnabled(validatedUser.isEnabled());
            user.setCloudFilesAuthUrl(getCloudFilesAuthUrl());
            return user;
        } catch (Exception e) {
            throw new org.openstack.atlas.exception.AuthException("Exception getting auth info for account " + accountId + " from " + xmlRpcConfig.getServerURL(), e);
        }
    }

    public String getCloudFilesAuthUrl() {
        return configuration.getString(LbLogsConfigurationKeys.cloud_files_auth_url);
    }


}
