package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

import java.util.regex.Pattern;
import javax.naming.directory.SearchControls;

public class MossoAuthConfig {

    public static final String exampleJson;
    private ClassConfig classConfig;
    private GroupConfig groupConfig;
    private String fileName;
    private LDAPConnectMethod connectMethod;
    private String host;
    private Set<String> allowedGroups;
    private Map<String, String> roles;
    private String appendName;
    private int port;
    private int scope = SearchControls.ONELEVEL_SCOPE; // default for eDir
    private boolean isActiveDirectory = false;
    private boolean allowforcedRole = false;
    private boolean allowBypassAuth = false;
    private int ttl = 300; // Cache timeout
    private static final Pattern opRe = Pattern.compile("(\\S+)\\s*=\\s*\\\"(.*)\\\"");
    private static final Pattern rolesRe = Pattern.compile("grouprole\\[\\s*\"(\\S+)\"\\s*\\]");

    static {
        exampleJson = ""
                + "{\n"
                + "  \"groupConfig\": {\n"
                + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
                + "    \"memberField\": \"memberOf\", \n"
                + "    \"sdn\": \"cn\", \n"
                + "    \"userQuery\": \"(uid=%s)\", \n"
                + "    \"objectClass\": \"(objectClass=*)\"\n"
                + "  }, \n"
                + "  \"appendtoname\": \"@rackspace.corp\", \n"
                + "  \"roles\": {\n"
                + "    \"support\": \"lbaas_support\", \n"
                + "    \"cp\": \"lbaas_cloud_control\", \n"
                + "    \"billing\": \"legacy_billing\", \n"
                + "    \"ops\": \"lbaas_ops\"\n"
                + "  }, \n"
                + "  \"isactivedirectory\": true, \n"
                + "  \"userConfig\": {\n"
                + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
                + "    \"sdn\": \"uid\"\n"
                + "  }, \n"
                + "  \"host\": \"10.12.99.71\", \n"
                + "  \"connect\": \"ssl\", \n"
                + "  \"allowbypassauth\": false, \n"
                + "  \"allowforcedrole\": false\n"
                + "  \"scope\": \"subtree\", \n"
                + "  \"port\": 636\n"
                + "}";
    }

    public MossoAuthConfig() {
    }

    public MossoAuthConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public MossoAuthConfig(String fileName) throws IOException, GeneralSecurityException {
        int i;
        this.fileName = fileName;

        String[] cols;

        allowedGroups = new HashSet<String>();
        roles = new HashMap<String, String>();

        ClassConfig userConfig = new ClassConfig();
        GroupConfig groupConfig = new GroupConfig();
        Map<String, String> roles = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

        JSONParser jp = new JSONParser();
        byte[] jsonBytes = StaticFileUtils.readFile(new File(fileName));
        String jsonStr = new String(jsonBytes, "utf-8");


        try {
            String tmpStr;
            Long tmpLong;
            Boolean tmpBool;

            JSONObject json = (JSONObject) jp.parse(jsonStr);
            this.host = (String) json.get("host");

            tmpLong = (Long)json.get("port");
            this.port = tmpLong.intValue();
            
            tmpStr = (String) json.get("connect");
            if (tmpStr.equalsIgnoreCase("SSL")) {
                this.connectMethod = LDAPConnectMethod.SSL;
            } else if (tmpStr.equalsIgnoreCase("TLS")) {
                this.connectMethod = LDAPConnectMethod.TLS;
            }

            // Get the group config
            JSONObject jsonGroupConfig = (JSONObject) json.get("groupConfig");

            tmpStr = (String) jsonGroupConfig.get("dn");
            groupConfig.setDn(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("memberField");
            groupConfig.setMemberField(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("sdn");
            groupConfig.setSdn(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("userQuery");
            groupConfig.setUserQuery(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("objectClass");
            groupConfig.setObjectClass(tmpStr);
            this.groupConfig = groupConfig;

            // Get the user config
            JSONObject jsonUserConfig = (JSONObject) json.get("userConfig");

            tmpStr = (String) jsonUserConfig.get("dn");
            userConfig.setDn(tmpStr);

            tmpStr = (String) jsonUserConfig.get("sdn");
            userConfig.setSdn(tmpStr);
            this.classConfig = userConfig;

            // set all the roles
            JSONObject jsonRoles = (JSONObject) json.get("roles");
            for (Object obj : jsonRoles.entrySet()) {
                Map.Entry<String, String> ent = (Map.Entry<String, String>) obj;
                String roleName = ent.getKey();
                String ldapGroup = ent.getValue();
                roles.put(ldapGroup, roleName);
            }
            this.roles = roles;

            this.isActiveDirectory = (Boolean) json.get("isactivedirectory");

            tmpBool = (Boolean) json.get("allowbypassauth");
            if (tmpBool != null) {
                this.allowBypassAuth = tmpBool;
            }

            tmpBool = (Boolean) json.get("allowforcedroles");
            if (tmpBool != null) {
                this.allowforcedRole = tmpBool;
            }

            this.appendName = (String) json.get("appendtoname");

            tmpStr = (String) json.get("scope");
            if (tmpStr.equalsIgnoreCase("onelevel")) {
                this.scope = SearchControls.ONELEVEL_SCOPE;
            } else if (tmpStr.equalsIgnoreCase("subtree")) {
                this.scope = SearchControls.SUBTREE_SCOPE;
            } else if (tmpStr.equalsIgnoreCase("object")) {
                this.scope = SearchControls.OBJECT_SCOPE;
            }

            Debug.nop();
        } catch (ParseException ex) {
            throw new IOException("Error parsing json", ex);
        }
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

    public ClassConfig getClassConfig() {
        return classConfig;
    }

    public void setClassConfig(ClassConfig classConfig) {
        this.classConfig = classConfig;
    }

    public GroupConfig getGroupConfig() {
        return groupConfig;
    }

    public void setGroupConfig(GroupConfig groupConfig) {
        this.groupConfig = groupConfig;
    }

    public LDAPConnectMethod getConnectMethod() {
        return connectMethod;
    }

    public void setConnectMethod(LDAPConnectMethod connectMethod) {
        this.connectMethod = connectMethod;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
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

    public boolean isAllowBypassAuth() {
        return allowBypassAuth;
    }

    public void setAllowBypassAuth(boolean allowBypassAuth) {
        this.allowBypassAuth = allowBypassAuth;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public static enum LDAPConnectMethod {

        TLS, SSL
    };

    public boolean isIsActiveDirectory() {
        return isActiveDirectory;
    }

    public void setIsActiveDirectory(boolean isActiveDirectory) {
        this.isActiveDirectory = isActiveDirectory;
    }

    public String getAppendName() {
        return appendName;
    }

    public void setAppendName(String appendName) {
        this.appendName = appendName;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }
}
