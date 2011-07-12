package org.openstack.atlas.api.auth;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;

public class XmlRpcClientConfigFactory {
    private XmlRpcClientConfigImpl xmlRpcClientConfig;

    public XmlRpcClientConfigFactory(XmlRpcClientConfigImpl xmlRpcClientConfig) {
        this.xmlRpcClientConfig = xmlRpcClientConfig;
    }

    public XmlRpcClientConfigImpl get(String authUrl) throws MalformedURLException {
        xmlRpcClientConfig = new XmlRpcClientConfigImpl();
        xmlRpcClientConfig.setServerURL(new URL(authUrl));
        return xmlRpcClientConfig;
    }
}
