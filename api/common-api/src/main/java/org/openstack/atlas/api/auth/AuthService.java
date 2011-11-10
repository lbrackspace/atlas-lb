package org.openstack.atlas.api.auth;

import org.openstack.client.keystone.KeyStoneException;

import java.net.URISyntaxException;

public interface AuthService {
    String authenticate(Integer passedAccountId, String authToken, String type) throws KeyStoneException, URISyntaxException;
}
