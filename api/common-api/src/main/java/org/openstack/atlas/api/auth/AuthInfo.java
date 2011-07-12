package org.openstack.atlas.api.auth;

import java.io.Serializable;

public class AuthInfo implements Serializable {
    private String userName;
    private String authToken;

    public AuthInfo(String userName, String authToken) {
        this.userName = userName;
        this.authToken = authToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
