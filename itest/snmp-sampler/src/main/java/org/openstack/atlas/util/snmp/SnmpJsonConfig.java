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
    public static final String exampleJson2 = ""
            + "{\n"
            + "  \"max_repetitions\": 350, \n"
            + "  \"non_repeaters\": 5, \n"
            + "  \"hosts\": {\n"
            + "    \"z1\": \"10.12.99.45/1161\", \n"
            + "    \"z2\": \"10.12.99.46/1161\"\n"
            + "  }, \n"
            + "  \"defaultHost\": \"z1\"\n"
            + "}\n"
            + "";
    private String defaultHostKey;
    private Integer nonRepeaters;
    private Integer maxRepetitions;

    public SnmpJsonConfig() {
    }

    @Override
    public String toString() {
        return "SnmpJsonConfig{hosts=" + StaticStringUtils.mapToString(zxtmHosts, ",")
                + ", defaultHostKey=" + defaultHostKey
                + ", max_repetitions=" + maxRepetitions
                + ", non_repeaters=" + nonRepeaters
                + "}";
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
            conf.setMaxRepetitions((Long) json.get("max_repetitions"));
            conf.setNonRepeaters((Long) json.get("non_repeaters"));
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

    public String getDefaultHostKey() {
        return defaultHostKey;
    }

    public void setDefaultHostKey(String defaultHostKey) {
        this.defaultHostKey = defaultHostKey;
    }

    public Integer getNonRepeaters() {
        return nonRepeaters;
    }

    public void setNonRepeaters(Long nonRepeaters) {
        if (nonRepeaters == null) {
            this.nonRepeaters = null;
            return;
        }
        this.nonRepeaters = (int) nonRepeaters.longValue();
    }

    public void setNonRepeaters(Integer nonRepeaters) {
        this.nonRepeaters = nonRepeaters;
    }

    public Integer getMaxRepetitions() {
        return maxRepetitions;
    }

    public void setMaxRepetitions(Integer maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    public void setMaxRepetitions(Long maxRepetitions) {
        if (maxRepetitions == null) {
            this.maxRepetitions = null;
            return;
        }
        this.maxRepetitions = (int) maxRepetitions.longValue();
    }
}
