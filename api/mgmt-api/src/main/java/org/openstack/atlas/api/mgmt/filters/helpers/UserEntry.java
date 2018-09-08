package org.openstack.atlas.api.mgmt.filters.helpers;

import java.util.HashSet;
import java.util.Set;


public class UserEntry {
    private String name;
    private String passwd;
    private Set<String> groups;

    public UserEntry(){
        groups = new HashSet<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

}
