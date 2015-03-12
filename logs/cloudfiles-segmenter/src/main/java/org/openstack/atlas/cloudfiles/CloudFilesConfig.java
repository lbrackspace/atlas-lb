package org.openstack.atlas.cloudfiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CloudFilesConfig {

    @Override
    public String toString() {
        return "CloudFilesConfig{" + "authEndpoint=" + authEndpoint + "user=" + user + "apiKey=" + apiKey + "filesEndpoint=" + filesEndpoint + "filesAccount=" + filesAccount + '}';
    }
    private String authEndpoint;
    private String user;
    private String apiKey;
    private String filesEndpoint;
    private String filesAccount;

    public CloudFilesConfig() {
    }

    CloudFilesConfig(CloudFilesConfig cfg) {
        this.authEndpoint = cfg.getAuthEndpoint();
        this.apiKey = cfg.getApiKey();
        this.user = cfg.getUser();
        this.filesEndpoint = cfg.getFilesEndpoint();
        this.filesAccount = cfg.getFilesAccount();
    }

    public static CloudFilesConfig readJsonConfig(String jsonConfigFile) throws FileNotFoundException, UnsupportedEncodingException, IOException, ParseException {
        String jsonStr = new String(StaticFileUtils.readFile(jsonConfigFile), "utf-8");
        CloudFilesConfig cfg = new CloudFilesConfig();
        JSONParser jp = new JSONParser();
        JSONObject json = (JSONObject) jp.parse(jsonStr);
        cfg.setAuthEndpoint((String) json.get("authEndpoint"));
        cfg.setUser((String) json.get("user"));
        cfg.setApiKey((String) json.get("apiKey"));
        cfg.setFilesEndpoint((String) json.get("filesEndpoint"));
        cfg.setFilesAccount((String) json.get("filesAccount"));
        return cfg;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        this.authEndpoint = authEndpoint;
    }

    public String getFilesEndpoint() {
        return filesEndpoint;
    }

    public void setFilesEndpoint(String filesEndpoint) {
        this.filesEndpoint = filesEndpoint;
    }

    public String getFilesAccount() {
        return filesAccount;
    }

    public void setFilesAccount(String filesAccount) {
        this.filesAccount = filesAccount;
    }
}
