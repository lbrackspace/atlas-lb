package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import javax.naming.directory.SearchControls;
import java.util.Map.Entry;
import java.util.TreeMap;

public class JsonConfig {

    public static String getExampleJson() {
        return exampleJson;
    }
    private MossoAuthConfig mossoAuthConfig;
    private ClassConfig classConfig;
    private GroupConfig groupConfig;
    private Map<String, String> roles;
    private String user;
    private String password;
    private int port;
    private static final String exampleJson = ""
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
            + "  \"bind\": {\n"
            + "    \"password\": \"CENSORED\", \n"
            + "    \"user\": \"CENSORD\"\n"
            + "  }, \n"
            + "  \"isactivedirectory\": true, \n"
            + "  \"userConfig\": {\n"
            + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
            + "    \"sdn\": \"uid\"\n"
            + "  }, \n"
            + "  \"host\": \"ad.auth.rackspace.com\", \n"
            + "  \"connect\": \"ssl\", \n"
            + "  \"scope\": \"subtree\", \n"
            + "  \"port\": 636\n"
            + "}";

    public JsonConfig() {
    }

    public static JsonConfig readConfig(File file) throws FileNotFoundException, IOException {
        JSONParser jp = new JSONParser();
        byte[] jsonBytes = StaticFileUtils.readFile(file);
        String jsonStr = new String(jsonBytes, "utf-8");
        JsonConfig config = readJsonConfig(jsonStr);
        return config;
    }

    public static JsonConfig readJsonConfig(String jsonStr) throws IOException {
        JsonConfig conf = new JsonConfig();
        JSONParser jp = new JSONParser();
        MossoAuthConfig mossoAuthConfig = new MossoAuthConfig();
        conf.setMossoAuthConfig(mossoAuthConfig);
        ClassConfig userConfig = new ClassConfig();
        GroupConfig groupConfig = new GroupConfig();
        Map<String, String> roles = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

        try {
            String tmpStr;
            Long tmpLong;
            Boolean tmpBool;

            JSONObject json = (JSONObject) jp.parse(jsonStr);
            tmpStr = (String) json.get("host");
            mossoAuthConfig.setHost(tmpStr);

            JSONObject bind = (JSONObject) json.get("bind");
            tmpStr = (String) bind.get("user");
            conf.setUser(tmpStr);

            tmpStr = (String) bind.get("password");
            conf.setPassword(tmpStr);

            tmpLong = (Long) json.get("port");
            mossoAuthConfig.setPort(tmpLong.intValue());

            tmpStr = (String) json.get("connect");
            if (tmpStr.equalsIgnoreCase("SSL")) {
                mossoAuthConfig.setConnectMethod(MossoAuthConfig.LDAPConnectMethod.SSL);
            } else if (tmpStr.equalsIgnoreCase("TLS")) {
                mossoAuthConfig.setConnectMethod(MossoAuthConfig.LDAPConnectMethod.TLS);
            }

            // Get the group config
            JSONObject jsonGroupConfig = (JSONObject) json.get("groupConfig");

            tmpStr = (String) jsonGroupConfig.get("dn");
            groupConfig.setDn(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("memberField");
            tmpStr = (String) jsonGroupConfig.get("memberField");
            groupConfig.setMemberField(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("sdn");
            groupConfig.setSdn(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("userQuery");
            groupConfig.setUserQuery(tmpStr);

            tmpStr = (String) jsonGroupConfig.get("objectClass");
            groupConfig.setObjectClass(tmpStr);

            // Get the user config
            JSONObject jsonUserConfig = (JSONObject) json.get("userConfig");

            tmpStr = (String) jsonUserConfig.get("dn");
            userConfig.setDn(tmpStr);

            tmpStr = (String) jsonUserConfig.get("sdn");
            userConfig.setSdn(tmpStr);

            conf.setGroupConfig(groupConfig);
            conf.setClassConfig(userConfig);

            // set all the roles
            JSONObject jsonRoles = (JSONObject) json.get("roles");
            for (Object obj : jsonRoles.entrySet()) {
                Entry<String, String> ent = (Entry<String, String>) obj;
                String roleName = ent.getKey();
                String ldapGroup = ent.getValue();
                roles.put(ldapGroup, roleName);
            }
            tmpBool = (Boolean) json.get("isactivedirectory");
            mossoAuthConfig.setIsActiveDirectory(tmpBool);

            tmpStr = (String) json.get("appendtoname");
            mossoAuthConfig.setAppendName(tmpStr);

            tmpStr = (String) json.get("scope");
            if (tmpStr.equalsIgnoreCase("onelevel")) {
                mossoAuthConfig.setScope(SearchControls.ONELEVEL_SCOPE);
            } else if (tmpStr.equalsIgnoreCase("subtree")) {
                mossoAuthConfig.setScope(SearchControls.SUBTREE_SCOPE);
            } else if (tmpStr.equalsIgnoreCase("object")) {
                mossoAuthConfig.setScope(SearchControls.OBJECT_SCOPE);
            }

            conf.setRoles(roles);
            Debug.nop();
        } catch (ParseException ex) {
            throw new IOException("Error parsing json", ex);
        }
        return conf;
    }

    public MossoAuthConfig getMossoAuthConfig() {
        return mossoAuthConfig;
    }

    public void setMossoAuthConfig(MossoAuthConfig mossoAuthConfig) {
        this.mossoAuthConfig = mossoAuthConfig;
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

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
