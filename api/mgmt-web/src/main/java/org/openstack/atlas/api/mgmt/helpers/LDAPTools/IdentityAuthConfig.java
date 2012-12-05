package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class IdentityAuthConfig {
    private String fileName;

    private Set<String> groups;
    private Set<String> allowedGroups;
    private Map<String, HashSet<String>> roles;
    private Map<String, HashSet<String>> allowedRoles;


    public IdentityAuthConfig() {
    }

    public IdentityAuthConfig(String fileName) throws IOException, GeneralSecurityException {
        int i;
        this.fileName = fileName;
        String line;
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        allowedGroups = new HashSet<String>();
        String[] cols;
        roles = new HashMap<String, HashSet<String>>();
        while ((line = br.readLine()) != null) {
            line.replace("\n", "");
            Matcher m = opRe.matcher(line);
            if (m.find()) {
                String name = m.group(1).trim();
                String value = m.group(2).trim();
                Matcher r = rolesRe.matcher(name);
                if (r.find()) {
                    String roleName = r.group(1).trim();
                    String[] groupNames = value.trim().split(",");
                    if (!roles.containsKey(roleName)) {
                        roles.put(roleName, new HashSet<String>());
                    }
                    for (i = 0; i < groupNames.length; i++) {
                        String groupName = groupNames[i];
                        allowedGroups.add(groupName);
                        roles.get(roleName).add(groupName);
                    }
                }
            }

        }
        br.close();
        fr.close();
    }


    public Set<String> getAllowedGroups() {
        return allowedGroups;
    }

    public void setAllowedGroups(Set<String> allowedGroups) {
        this.setAllowedGroups(allowedGroups);
    }

    public Map<String, HashSet<String>> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, HashSet<String>> roles) {
        this.roles = roles;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }
}
