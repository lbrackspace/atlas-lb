package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import org.openstack.atlas.cfg.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public class IdentityAuthConfig {
    private Configuration configuration;

    private Set<String> allowedRoles;

    public IdentityAuthConfig() {
    }

    public IdentityAuthConfig(Configuration cfg) throws IOException, GeneralSecurityException {
        this.configuration = cfg;
//        if (configuration.hasKeys(ManagementApiServiceConfigurationKeys.group_role)) {
//            String[] roles = configuration.getString(ManagementApiServiceConfigurationKeys.group_role).split(",");
//            this.allowedRoles = new HashSet<String>();
//            Collections.addAll(allowedRoles, roles);
//        }
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }
}
