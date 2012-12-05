package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.security.Security;
import java.util.Map;

public class IdentityAuth {
    private IdentityAuthConfig config;
    private Map<String, GroupConfig> groupMap;
    private Map<String, ClassConfig> classMap;

    static {
        Security.addProvider(new OverTrustingTrustProvider());
        Security.setProperty("ssl.TrustManagerFactory.algorithm", "TrustAllCertificates");
    }

    public IdentityAuth() {
    }

    public IdentityAuth(IdentityAuthConfig config, Map<String, GroupConfig> groupMap, Map<String, ClassConfig> classMap) {
        this.config = config;
        this.groupMap = groupMap;
        this.classMap = classMap;
    }

}
