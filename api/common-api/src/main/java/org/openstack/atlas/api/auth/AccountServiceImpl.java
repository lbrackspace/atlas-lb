package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.openstack.keystone.auth.client.AdminAuthClient;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

public class AccountServiceImpl implements AccountService {

    private static final Log LOG = LogFactory.getLog(AccountServiceImpl.class);
    private final AdminAuthClient adminAuthClient;
    private final String basicAuthUsername;
    private final String basicAuthPassword;
    private final String authUrl;

    public AccountServiceImpl(Configuration cfg) throws MalformedURLException {
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

        if(HttpsCertIgnore.getInitException() != null){
            Exception ex = HttpsCertIgnore.getInitException();
            throw new MalformedURLException(StringUtilities.getHttpsInitExceptionString(ex));
        }

    }

    @Override
    public String getUsernameByToken(String authToken) throws Exception {
        return adminAuthClient.getToken(authToken).getUserId();
    }
}
