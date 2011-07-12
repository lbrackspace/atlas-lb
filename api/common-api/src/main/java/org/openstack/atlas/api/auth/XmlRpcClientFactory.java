package org.openstack.atlas.api.auth;

import org.apache.xmlrpc.client.XmlRpcClient;

import java.net.MalformedURLException;

public class XmlRpcClientFactory {
    private XmlRpcClientConfigFactory xmlRpcClientConfigFactory;

    public XmlRpcClientFactory(XmlRpcClientConfigFactory xmlRpcClientConfigFactory) {
        this.xmlRpcClientConfigFactory = xmlRpcClientConfigFactory;
    }

    public XmlRpcClient get(String authUrl) throws MalformedURLException {
        XmlRpcClient xmlRpcClient = new XmlRpcClient();
        xmlRpcClient.setConfig(xmlRpcClientConfigFactory.get(authUrl));
        return xmlRpcClient;
    }
}
