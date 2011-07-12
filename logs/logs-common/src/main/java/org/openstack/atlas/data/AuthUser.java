package org.openstack.atlas.data;

public class AuthUser {
    private String username;
    private String mossoAccountId;
    private String nastAccountId;
    private String sliceAccountId;
    private String authKey;
    private boolean enabled;

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getNastAccountId() {
        return nastAccountId;
    }

    public void setNastAccountId(String nastAccountId) {
        this.nastAccountId = nastAccountId;
    }

    public String getMossoAccountId() {
        return mossoAccountId;
    }

    public void setMossoAccountId(String mossoAccountId) {
        this.mossoAccountId = mossoAccountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSliceAccountId() {
        return sliceAccountId;
    }

    public void setSliceAccountId(String sliceAccountId) {
        this.sliceAccountId = sliceAccountId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
