package org.openstack.atlas.api.auth;

public interface AccountService {
    String getUsernameByToken(String authToken) throws Exception;
}
