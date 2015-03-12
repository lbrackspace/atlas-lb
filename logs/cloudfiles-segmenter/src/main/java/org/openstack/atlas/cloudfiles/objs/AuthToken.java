package org.openstack.atlas.cloudfiles.objs;

import org.joda.time.DateTime;

public class AuthToken {

    @Override
    public String toString() {
        return "AuthToken{" + "token=" + token + ", expires=" + expires + '}';
    }
    private String token;
    private DateTime expires;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DateTime getExpires() {
        return expires;
    }

    public void setExpires(DateTime expires) {
        this.expires = expires;
    }
}
