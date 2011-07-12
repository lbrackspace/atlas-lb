package org.openstack.atlas.api.auth;

import org.apache.xmlrpc.XmlRpcException;

public interface AccountService {
    Integer getAccountIdByToken(String authToken) throws XmlRpcException;
    String getUserNameByToken(String authToken) throws XmlRpcException;
}
