package org.openstack.atlas.util.snmp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class SnmpJsonConfig {

    public static final String exampleJson = ""
            + "{\n"
            + "  \"hosts\": {\n"
            + "    \"z1\": \"127.0.0.1/1161\", \n"
            + "    \"z2\": \"127.0.0.2/1161\", \n"
            + "    \"z3\": \"127.0.0.3/1161\"\n"
            + "  }, \n"
            + "  \"defaultHost\": \"z1\"\n"
            + "}";
    private Map<String, String> zxtmHosts;
    private String defaultHostKey;

    public SnmpJsonConfig() {
    }

    @Override
    public String toString() {
        return "SnmpJsonConfig{hosts=" + StaticStringUtils.mapToString(zxtmHosts, ",")
                + ", defaultHostKey=" + defaultHostKey + "}";
    }

    public static SnmpJsonConfig readJsonConfig(File file) throws IOException {
        byte[] jsonBytes = StaticFileUtils.readFile(file);
        String jsonStr = new String(jsonBytes, "utf-8");
        SnmpJsonConfig snmpConfig = readJsonConfig(jsonStr);
        return snmpConfig;
    }

    public static SnmpJsonConfig readJsonConfig(String jsonStr) throws IOException {
        SnmpJsonConfig conf = new SnmpJsonConfig();
        JSONParser jp = new JSONParser();
        try {
            JSONObject json = (JSONObject) jp.parse(jsonStr);
            JSONObject hosts = (JSONObject) json.get("hosts");
            conf.setDefaultHostKey((String) json.get("defaultHost"));
            for (Object obj : hosts.entrySet()) {
                Entry<String, String> ent = (Entry<String, String>) obj;
                String key = ent.getKey();
                String val = ent.getValue();
                conf.getZxtmHosts().put(key, val);
            }

        } catch (ParseException ex) {
            throw new IOException("Error parsing Json", ex);
        }
        return conf;
    }

    public Map<String, String> getZxtmHosts() {
        if (zxtmHosts == null) {
            zxtmHosts = new HashMap<String, String>();
        }
        return zxtmHosts;
    }

    public void setZxtmHosts(Map<String, String> zxtmHosts) {
        this.zxtmHosts = zxtmHosts;
    }

    public static void nop() {
    }

    public String getDefaultHostKey() {
        return defaultHostKey;
    }

    public void setDefaultHostKey(String defaultHostKey) {
        this.defaultHostKey = defaultHostKey;
    }
}
