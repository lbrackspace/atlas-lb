package org.openstack.atlas.auth;

import org.openstack.atlas.data.AuthUser;

import java.net.MalformedURLException;

public interface AuthService {
    public AuthUser getUser(String accountId) throws MalformedURLException;
}
