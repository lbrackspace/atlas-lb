package org.openstack.atlas.api.auth;

import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;

public interface AuthService {
    boolean authenticate(String authToken) throws MalformedURLException, XmlRpcException;
}
