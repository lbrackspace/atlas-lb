package com.rackspace.cloud.sum.exp.hibernate;

import org.openstack.atlas.util.b64aes.Aes;
import org.openstack.atlas.util.b64aes.PaddingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class HibernateDbConf {

    private String dbKey;
    private String url;
    private String user;
    private String passwd;
    private String driver;
    private String dialect;
    private String hbm2ddl;
    private String packageName;
    private List<String> classNames;

    public HibernateDbConf() {
    }

    public static HibernateDbConf newCacheBuilder(String url, String user, String passwd) {
        HibernateDbConf conf = new HibernateDbConf();
        return conf;
    }

    public static HibernateDbConf newHibernateConf(String fileName, String keyFile) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException, PaddingException, java.text.ParseException {
        HibernateDbConf conf = new HibernateDbConf();
        String jsonStr = new String(StaticFileUtils.readFile(fileName), "utf-8");
        JSONParser jp = new JSONParser();
        JSONObject jsonConf = (JSONObject) jp.parse(jsonStr);
        List<String> classList = new ArrayList<String>();
        String key = HibernateStaticUtils.readKeyFromJsonFile(keyFile);
        conf.setUrl((String) jsonConf.get("url"));
        conf.setUser((String) jsonConf.get("user"));
        conf.setPasswd(Aes.b64decrypt_str((String) jsonConf.get("passwd"), key));
        conf.setClassNames(classList);
        conf.setDbKey((String)jsonConf.get("dbkey"));
        conf.setDialect((String)jsonConf.get("dialect"));
        conf.setDriver((String)jsonConf.get("driver"));
        conf.setPackageName((String)jsonConf.get("package"));
        conf.setHbm2ddl((String)jsonConf.get("hbm2ddl"));
        JSONArray classes = (JSONArray)jsonConf.get("classes");
        int cl = classes.size();
        for(int i=0;i<cl;i++){
            classList.add((String)classes.get(i));
        }
        return conf;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String fmt = "dbKey: %s\nurl: %s\nuser: %s\npasswd: %s\n"
                + "driver: %s\ndialect: %s\nhdm2ddl: %s\npackage: %s\n";
        sb.append(String.format(fmt, dbKey, url, user, passwd, driver, dialect, hbm2ddl, packageName));
        sb.append("Classes:\n");
        for (String className : classNames) {
            sb.append(String.format("    %s\n", className));
        }
        return sb.toString();

    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getHbm2ddl() {
        return hbm2ddl;
    }

    public void setHbm2ddl(String hbm2ddl) {
        this.hbm2ddl = hbm2ddl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }
}
