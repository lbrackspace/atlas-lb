package org.openstack.atlas.util.ca.rootca;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class RootCaConfig {

    private static final String confExample = ""
            + "{\n"
            + "  \"key_size\": 1024, \n"
            + "  \"key_file\": \"~/root.key\", \n"
            + "  \"crt_file\": \"~/root.crt\", \n"
            + "  \"subjname\": \"CN=RootCa Lbaas Example,OU=Cloud LoadBalancing,O=Rackspace Hosting,L=San Antonio,ST=TX,C=US\", \n"
            + "  \"notbefore\": -1.0, \n"
            + "  \"notafter\": 1.0\n"
            + "}\n"
            + "\n"
            + "";
    private int keySize;
    private String keyFile;
    private String crtFile;
    private String subjName;
    private double notBefore;
    private double notAfter;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{keySize=").append(keySize).
                append(", keyFile=").append(keyFile).
                append(", crtFile=").append(crtFile).
                append(", notBefore=").append(notBefore).
                append(", notAfter=").append(notAfter).
                append(", subjName=").append(subjName);
        return sb.toString();
    }

    public static RootCaConfig loadRootCaConfig(String fileName) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        RootCaConfig conf = new RootCaConfig();
        String jsonStr = new String(StaticFileUtils.readFile(fileName), "utf-8");
        JSONParser jp = new JSONParser();
        JSONObject jsonRoot = (JSONObject) jp.parse(jsonStr);
        conf.setKeySize(((Long) jsonRoot.get("key_size")).intValue());
        conf.setKeyFile((String) jsonRoot.get("key_file"));
        conf.setCrtFile((String) jsonRoot.get("crt_file"));
        conf.setNotBefore((Double) jsonRoot.get("notbefore"));
        conf.setNotAfter((Double) jsonRoot.get("notafter"));
        conf.setSubjName((String) jsonRoot.get("subjname"));
        return conf;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getCrtFile() {
        return crtFile;
    }

    public void setCrtFile(String crtFile) {
        this.crtFile = crtFile;
    }

    public String getSubjName() {
        return subjName;
    }

    public void setSubjName(String subjName) {
        this.subjName = subjName;
    }

    public double getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(double notBefore) {
        this.notBefore = notBefore;
    }

    public double getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(double notAfter) {
        this.notAfter = notAfter;
    }

    public static String getConfExample() {
        return confExample;
    }
}
