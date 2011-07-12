package org.openstack.atlas.api.auth;

import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.openstack.atlas.api.filters.helpers.StringUtilities;

public class AuthServiceImpl implements AuthService {

    private static final Log LOG = LogFactory.getLog(AuthServiceImpl.class);
    private final XmlRpcClient xmlRpcClient;
    private final String xmlRpcUserName;
    private final String xmlRpcPassword;
    private final String xmlRpcAuthUrl;

    public AuthServiceImpl(XmlRpcClientFactory xmlRpcClientFactory, Configuration cfg) throws MalformedURLException {
        if (cfg.hasKeys(PublicApiServiceConfigurationKeys.auth_callback_uri, PublicApiServiceConfigurationKeys.auth_username, PublicApiServiceConfigurationKeys.auth_password)) {
            xmlRpcUserName = cfg.getString(PublicApiServiceConfigurationKeys.auth_username);
            xmlRpcPassword = cfg.getString(PublicApiServiceConfigurationKeys.auth_password);

            xmlRpcAuthUrl = cfg.getString(PublicApiServiceConfigurationKeys.auth_callback_uri);
            LOG.info("AUTH URI from LOCAL CONF: " + xmlRpcAuthUrl);

            xmlRpcClient = xmlRpcClientFactory.get(xmlRpcAuthUrl);
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
    public boolean authenticate(String authToken) throws MalformedURLException, XmlRpcException {
        List<String> xmlRpcParameters = Arrays.asList(authToken, xmlRpcUserName, xmlRpcPassword);

        boolean validToken = false;

        Object xmlRpcReturn = xmlRpcClient.execute("validateToken", xmlRpcParameters);

        if (xmlRpcReturn instanceof Integer) {
            switch ((Integer) xmlRpcReturn) {
                case 1:
                    throw new RuntimeException(String.format("XLM RPC Authentication Failed While Validating Token.  Token: %s", authToken));

                case 2:
                    return validToken;
//                    throw new RuntimeException(String.format("Token Validation Failed.  Token: %s", authToken));

                default:
                    throw new RuntimeException(String.format("Unknown XML RPC Error Code While Validating Token.  Token: %s, Error Code: %d", authToken, xmlRpcReturn));
            }
        } else {
            Object[] objArray = (Object[]) xmlRpcReturn;
            if (objArray.length == 2) {
                //TODO: Change Method Signature to return an array containing TTL, ExpirationTime and If Token is valid, as a part of the Token Caching story.
                validToken = true;
            }

            return validToken;
        }
    }
}
