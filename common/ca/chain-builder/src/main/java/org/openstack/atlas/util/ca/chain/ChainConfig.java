package org.openstack.atlas.util.ca.chain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class ChainConfig {

    private static final String confExample = ""
            + "{\n"
            + "  \"root_key\": \"~/root.key\",\n"
            + "  \"root_crt\": \"~/root.crt\",\n"
            + "  \"key_file\": \"~/test.key\", \n"
            + "  \"crt_file\": \"~/test.crt\", \n"
            + "  \"imd_file\": \"~/imds.crt\", \n"
            + "  \"key_size\": 1024, \n"
            + "  \"notbefore\": -1.0, \n"
            + "  \"notafter\": 1.0, \n"
            + "  \"subjname\": \"C=US,ST=TX,L=San Antonio,O=Rackspace Hosting,OU=Clout LoadBalancing,CN=example.rackexp.org\", \n"
            + "  \"issuers\": [\n"
            + "    \"C=US,ST=TX,L=San Antonio,O=Rackspace Hosting,OU=Cloud LoadBalancing,CN=Lbaas Example Imd1 crt\", \n"
            + "    \"C=US,ST=TX,L=San Antonio,O=Rackspace Hosting,OU=Cloud LoadBalancing,CN=Lbaas Example Imd2 crt\", \n"
            + "    \"C=US,ST=TX,L=San Antonio,O=Rackspace Hosting,OU=Cloud LoadBalancing,CN=Lbaas Example Imd3 crt\" \n"
            + "  ]\n"
            + "}\n"
            + "";
    private String rootKeyFile;
    private String rootCrtFile;
    private String keyFile;
    private String crtFile;
    private String imdFile;
    private int keySize = 2048;
    private String subjName;
    private List<String> issuers;
    private double notBefore;
    private double notAfter;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{rootKey=").append(rootKeyFile).
                append(",rootCrt=").append(rootCrtFile).
                append(",keyFile=").append(keyFile).
                append(",crtFile=").append(crtFile).
                append(", imdFile=").append(imdFile).
                append(", subjName=").append(subjName).
                append(", keySize=").append(keySize).
                append(", notBefore=").append(notBefore).
                append(", notAfter=").append(notAfter).
                append(", issuers=[").append(StaticStringUtils.collectionToString(issuers, ",")).
                append("]}");
        return sb.toString();
    }

    public static ChainConfig loadChainerConfig(String fileName) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException {
        ChainConfig conf = new ChainConfig();
        String jsonStr = new String(StaticFileUtils.readFile(fileName), "utf-8");
        JSONParser jp = new JSONParser();
        JSONObject jsonRoot = (JSONObject) jp.parse(jsonStr);
        conf.setRootKeyFile((String) jsonRoot.get("root_key"));
        conf.setRootCrtFile((String) jsonRoot.get("root_crt"));
        conf.setKeyFile((String) jsonRoot.get("key_file"));
        conf.setCrtFile((String) jsonRoot.get("crt_file"));
        conf.setImdFile((String) jsonRoot.get("imd_file"));
        conf.setSubjName((String) jsonRoot.get("subjname"));
        conf.setKeySize(((Long) jsonRoot.get("key_size")).intValue());
        conf.setNotBefore((Double) jsonRoot.get("notbefore"));
        conf.setNotAfter((Double) jsonRoot.get("notafter"));
        JSONArray tissuers = (JSONArray) jsonRoot.get("issuers");
        for (Object issuerObj : tissuers) {
            conf.getIssuers().add((String) issuerObj);
        }
        return conf;
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

    public String getImdFile() {
        return imdFile;
    }

    public void setImdFile(String imdFile) {
        this.imdFile = imdFile;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public String getSubjName() {
        return subjName;
    }

    public void setSubjName(String subjName) {
        this.subjName = subjName;
    }

    public List<String> getIssuers() {
        if (issuers == null) {
            issuers = new ArrayList<String>();
        }
        return issuers;
    }

    public void setIssuers(List<String> issuers) {
        this.issuers = issuers;
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

    public String getRootKeyFile() {
        return rootKeyFile;
    }

    public void setRootKeyFile(String rootKeyFile) {
        this.rootKeyFile = rootKeyFile;
    }

    public String getRootCrtFile() {
        return rootCrtFile;
    }

    public void setRootCrtFile(String rootCrtFile) {
        this.rootCrtFile = rootCrtFile;
    }

    public static String getConfExample() {
        return confExample;
    }
}
