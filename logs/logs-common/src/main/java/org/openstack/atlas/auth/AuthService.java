package org.openstack.atlas.auth;

import org.openstack.atlas.data.AuthUser;
import org.openstack.atlas.exception.AuthException;

import java.net.MalformedURLException;

public interface AuthService {
    public AuthUser getUser(String accountId) throws AuthException;
}
