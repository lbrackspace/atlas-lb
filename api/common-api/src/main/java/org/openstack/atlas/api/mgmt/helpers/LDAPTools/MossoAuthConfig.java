package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MossoAuthConfig {
    private String fileName;
    private LDAPConnectMethod connectMethod;
    private String host;
    private Set<String> allowedGroups;
    private Map<String, HashSet<String>> roles;
    private int port;
    private boolean allowforcedRole=false;
    private static final Pattern opRe = Pattern.compile("(\\S+)\\s*=\\s*\\\"(.*)\\\"");
    private static final Pattern rolesRe = Pattern.compile("grouprole\\[\\s*\"(\\S+)\"\\s*\\]");

    public MossoAuthConfig() {
    }

    public MossoAuthConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public MossoAuthConfig(String fileName) throws IOException, GeneralSecurityException {
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
                } else if (name.equals("host")) {
                    this.host = value;
                } else if (name.equals("port")) {
                    this.port = Integer.parseInt(value);
                } else if (name.equals("connect") && value.equals("ssl")) {
                    this.connectMethod = LDAPConnectMethod.SSL;
                } else if (name.equals("connect") && value.equals("tls")) {
                    this.connectMethod = LDAPConnectMethod.TLS;
                }else if(name.equals("allowforcedrole")&&value.equals("true")){
                    this.allowforcedRole = true;
                } else {
                    continue;
                }
            }

        }
        br.close();
        fr.close();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Set<String> getAllowedGroups() {
        return allowedGroups;
    }

    public void setAllowedGroups(Set<String> allowedGroups) {
        this.setAllowedGroups(allowedGroups);
    }

    public LDAPConnectMethod getConnectMethod() {
        return connectMethod;
    }

    public void setConnectMethod(LDAPConnectMethod connectMethod) {
        this.connectMethod = connectMethod;
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

    public boolean isAllowforcedRole() {
        return allowforcedRole;
    }

    public void setAllowforcedRole(boolean allowforcedRole) {
        this.allowforcedRole = allowforcedRole;
    }

    public static enum LDAPConnectMethod {

        TLS, SSL
    };
}
