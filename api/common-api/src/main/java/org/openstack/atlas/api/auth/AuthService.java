package org.openstack.atlas.api.auth;

import org.openstack.keystone.auth.pojo.exceptions.AuthException;
import org.openstack.user.User;

import java.net.URISyntaxException;

public interface AuthService {
    String authenticate(Integer passedAccountId, String authToken, String type) throws AuthException, URISyntaxException;
    User getUser(String userId) throws AuthException, URISyntaxException;
    User getUserByAlternateId(String userId, String type) throws AuthException, URISyntaxException;
}
