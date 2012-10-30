package org.openstack.atlas.service.domain.services.helpers.authmangler;

import org.openstack.client.keystone.auth.AuthData;
import org.openstack.client.keystone.user.User;

public class AuthUserAndToken {

    private User user;
    private AuthData token;

    public AuthUserAndToken(User user, AuthData token) {
        this.user = user;
        this.token = token;
    }

    public String getUserName() {
        return user.getId();
    }

    public String getTokenString() {
        return token.getToken().getId();
    }

    public User getUser() {
        return user;
    }

    public AuthData getToken() {
        return token;
    }

    @Override
    public String toString() {
        String userStr = null;
        String tokenStr = null;
        if(user != null){
            userStr = user.getId();
        }

        if(token != null && token.getToken() != null){
            tokenStr = token.getToken().getId();
        }
        return String.format("{user=\"%s\",token=\"%s\"}", userStr, tokenStr);
    }
}
