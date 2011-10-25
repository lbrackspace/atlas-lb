package org.openstack.atlas.api.auth;

import java.net.MalformedURLException;

public interface AuthService {
    boolean authenticate(String passedAccountId, String authToken) throws Exception;
}
