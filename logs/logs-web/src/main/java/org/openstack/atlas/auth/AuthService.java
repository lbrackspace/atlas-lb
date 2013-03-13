package org.openstack.atlas.auth;

import org.openstack.atlas.exception.AuthException;

public interface AuthService {
    public AuthUser getUser(String accountId) throws AuthException;
}
