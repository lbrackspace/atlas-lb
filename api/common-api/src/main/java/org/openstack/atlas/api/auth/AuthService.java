package org.openstack.atlas.api.auth;

import org.openstack.user.User;

import java.net.MalformedURLException;

public interface AuthService {
    User authenticate(Integer passedAccountId, String authToken) throws Exception;
}
