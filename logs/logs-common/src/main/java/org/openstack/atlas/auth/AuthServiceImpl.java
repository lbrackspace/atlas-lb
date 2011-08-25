package org.openstack.atlas.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.data.AuthUser;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.openstack.atlas.exception.AuthException;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthServiceImpl implements AuthService{

    private static final Logger LOG = Logger.getLogger(AuthServiceImpl.class);

    private final XmlRpcClient xmlRpcClient;
    private XmlRpcClientConfigImpl xmlRpcConfig = new XmlRpcClientConfigImpl();
    private Configuration configuration;

    public AuthServiceImpl(Configuration cfg) throws MalformedURLException {
        this.configuration = cfg;
        xmlRpcClient = new XmlRpcClient();
        xmlRpcConfig.setServerURL(new URL(cfg.getString(LbLogsConfigurationKeys.auth_management_uri)));
        xmlRpcConfig.setBasicUserName(cfg.getString(LbLogsConfigurationKeys.auth_username));
        xmlRpcConfig.setBasicPassword(cfg.getString(LbLogsConfigurationKeys.auth_password));
        xmlRpcClient.setConfig(xmlRpcConfig);
    }

    public AuthUser getUser(String accountId) throws AuthException {
        AuthUser user = new AuthUser();
        try {
            Object response = xmlRpcClient.execute("getUser", new Object[]{accountId});
            if(response instanceof Object[] && ((Object[])response).length == 6) {
                LOG.info("Successfully Fetched auth info for account: " + accountId);
                user.setMossoAccountId((String)((Object[])response)[0]);
                user.setNastAccountId((String) ((Object[]) response)[1]);
                user.setSliceAccountId((String) ((Object[]) response)[2]);
                user.setUsername((String) ((Object[]) response)[3]);
                user.setAuthKey((String) ((Object[]) response)[4]);
                user.setEnabled(((Object[]) response)[5] != null ? true : false);
            } else {
                throw new Exception("Exception getting auth info for account " + accountId + " from " + xmlRpcConfig.getServerURL() + " Response: " + response);
            }
        } catch (Exception e) {
            throw new AuthException("Exception getting auth info for account " + accountId + " from " + xmlRpcConfig.getServerURL() , e);
        }
        user.setCloudFilesAuthUrl(getCloudFilesAuthUrl());
        return user;
    }

    public String getCloudFilesAuthUrl() {
        return configuration.getString(LbLogsConfigurationKeys.cloud_files_auth_url);
    }


}
