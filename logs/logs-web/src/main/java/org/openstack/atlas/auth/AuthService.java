package org.openstack.atlas.auth;

import org.openstack.atlas.exception.AuthException;
import org.openstack.client.keystone.KeyStoneException;

import java.net.URISyntaxException;

public interface AuthService {
    public AuthUser getUser(String accountId) throws KeyStoneException, URISyntaxException, AuthException;
}
