package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.filters.helpers.StringUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

public class AccountServiceImpl implements AccountService {

    private static final Log LOG = LogFactory.getLog(AccountServiceImpl.class);
    private XmlRpcClient xmlRpcClient;

    public AccountServiceImpl(XmlRpcClientFactory xmlRpcClientFactory, Configuration cfg) throws MalformedURLException {
        if (cfg.hasKeys(PublicApiServiceConfigurationKeys.auth_management_uri)) {
            final String authUrl = cfg.getString(PublicApiServiceConfigurationKeys.auth_management_uri);

            LOG.info("Management URI from LOCAL CONF: " + authUrl);

            this.xmlRpcClient = xmlRpcClientFactory.get(authUrl);
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
    public Integer getAccountIdByToken(String authToken) throws XmlRpcException {
        List<String> xmlRpcParameters = Arrays.asList(authToken);

        Object[] xmlRpcReturn = (Object[]) xmlRpcClient.execute("getUserByToken", xmlRpcParameters);

        return Integer.parseInt((String) ((Object[]) xmlRpcReturn[1])[0]);
    }

    @Override
    public String getUserNameByToken(String authToken) throws XmlRpcException {
        List<String> xmlRpcParameters = Arrays.asList(authToken);

        Object[] xmlRpcReturn = (Object[]) xmlRpcClient.execute("getUserByToken", xmlRpcParameters);

        return (String) ((Object[]) xmlRpcReturn[1])[3];
    }
}
