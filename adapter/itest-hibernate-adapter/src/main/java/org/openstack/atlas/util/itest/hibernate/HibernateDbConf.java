package org.openstack.atlas.util.itest.hibernate;

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
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HibernateDbConf{dbKey=").append(dbKey).
                append(", url=").append(url).
                append(", user=").append(user).
                append(", passwd=").append(passwd).
                append(", driver=").append(driver).
                append(", dialect=").append(dialect).
                append(", hbm2ddl=").append(hbm2ddl).
                append(", packageName=").append(packageName).
                append(", classNames=");
        if (classNames == null) {
            sb.append("null");
        } else {
            sb.append(StaticStringUtils.collectionToString(classNames, ", "));
        }
        sb.append("}");
        return sb.toString();
    }

    public HibernateDbConf() {
    }

    public static HibernateDbConf newCacheBuilder(String url, String user, String passwd) {
        HibernateDbConf conf = new HibernateDbConf();
        return conf;
    }

    public static HibernateDbConf newHibernateConf(String fileName) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        HibernateDbConf conf = new HibernateDbConf();
        String jsonStr = new String(StaticFileUtils.readFile(fileName), "utf-8");
        JSONParser jp = new JSONParser();
        JSONObject rootConf = (JSONObject) jp.parse(jsonStr);
        JSONObject dbConf = (JSONObject) rootConf.get("db");
        List<String> classList = new ArrayList<String>();
        conf.setUrl((String) dbConf.get("url"));
        conf.setUser((String) dbConf.get("user"));
        conf.setPasswd((String) dbConf.get("passwd"));
        conf.setClassNames(classList);
        conf.setDbKey((String) dbConf.get("dbkey"));
        conf.setDialect((String) dbConf.get("dialect"));
        conf.setDriver((String) dbConf.get("driver"));
        conf.setPackageName((String) dbConf.get("package"));
        conf.setHbm2ddl((String) dbConf.get("hbm2ddl"));
        JSONArray classes = (JSONArray) dbConf.get("classes");

        if (classes != null) {
            int cl = classes.size();
            for (int i = 0; i < cl; i++) {
                classList.add((String) classes.get(i));
            }
        }
        return conf;
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
