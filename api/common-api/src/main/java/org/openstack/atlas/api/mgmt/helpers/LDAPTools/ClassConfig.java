package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

public class ClassConfig {

    private String dn;
    private String sdn;

    public ClassConfig() {
    }

    public ClassConfig(String dn, String sdn) {
        this.dn = dn;
        this.sdn = sdn;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getSdn() {
        return sdn;
    }

    public void setSdn(String sdn) {
        this.sdn = sdn;
    }
}
