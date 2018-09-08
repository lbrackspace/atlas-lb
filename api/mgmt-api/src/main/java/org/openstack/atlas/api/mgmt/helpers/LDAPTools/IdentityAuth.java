package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IdentityAuth {
    private IdentityAuthConfig identityAuthConfig;

    public IdentityAuth() {
    }

    public IdentityAuth(IdentityAuthConfig config) {
        this.identityAuthConfig = config;
    }

    public Set<String> getRoles(String userRoles) {
        return parseRoles(userRoles);
    }

    public boolean isUserRoleValidated(String user, String userRoles) {
        Set<String> roles = parseRoles(userRoles);
        return roles.contains(user);
    }

    private Set<String> parseRoles(String userRoles) {
        String[] roles = userRoles.split(",");
        Set<String> rolesSet = new HashSet<String>();
        Collections.addAll(rolesSet, roles);
        return rolesSet;
    }

    public void setIdentityAuthConfig(IdentityAuthConfig identityAuthConfig) {
        this.identityAuthConfig = identityAuthConfig;
    }

    public IdentityAuthConfig getIdentityAuthConfig() {
        return identityAuthConfig;
    }
}
