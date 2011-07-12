package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

public class GroupConfig extends ClassConfig {

    private String objectClass;
    private String memberField;
    private String userQuery;

    public GroupConfig() {
    }

    public GroupConfig(String objectClass, String memberField, String userQuery, String dn, String sdn) {
        super(dn, sdn);
        this.objectClass = objectClass;
        this.memberField = memberField;
        this.userQuery = userQuery;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getMemberField() {
        return memberField;
    }

    public void setMemberField(String memberField) {
        this.memberField = memberField;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }
}
