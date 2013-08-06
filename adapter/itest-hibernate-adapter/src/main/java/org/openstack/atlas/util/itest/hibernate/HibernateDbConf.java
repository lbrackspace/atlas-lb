package org.openstack.atlas.util.itest.hibernate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class HibernateDbConf {

    public static final String exampleJson = ""
            + "{\n"
            + "  \"db\": {\n"
            + "    \"dialect\": \"org.hibernate.dialect.MySQL5InnoDBDialect\", \n"
            + "    \"url\": \"jdbc:mysql://mysql-master-n01.ord1.lbaas.rackspace.net:3306/loadbalancing\", \n"
            + "    \"driver\": \"com.mysql.jdbc.Driver\", \n"
            + "    \"passwd\": \"YourPassword\", \n"
            + "    \"classes\": [\n"
            + "      \"org.openstack.atlas.service.domain.entities.AccessList\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.AccessListType\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Cluster\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.GroupRateLimit\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.AccountGroup\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.ConnectionLimit\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Entity\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.HealthMonitor\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.HealthMonitorType\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Host\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.HostStatus\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Backup\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.IpVersion\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancer\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerProtocol\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.RateLimit\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerStatus\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Node\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.NodeCondition\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.NodeStatus\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.SessionPersistence\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.SessionPersistenceObject\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Suspension\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Usage\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.AccountUsage\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.VirtualIp\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.VirtualIpv6\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.TrafficScripts\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Ticket\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.BlacklistItem\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.BlacklistType\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Account\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.AccountLimit\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LimitType\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.JobState\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.UserPages\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.SslTermination\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.Defaults\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.AllowedDomain\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadbalancerMeta\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.NodeMeta\", \n"
            + "      \"org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.Event\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.Alert\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.AlertStatus\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.AccessListEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.ConnectionLimitEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.HealthMonitorEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.LoadBalancerEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.NodeEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.NodeServiceEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.VirtualIpEvent\", \n"
            + "      \"org.openstack.atlas.service.domain.events.entities.SessionPersistenceEvent\"\n"
            + "    ], \n"
            + "    \"db_key\": \"lb\", \n"
            + "    \"user\": \"lbaas\", \n"
            + "    \"hbm2ddl\": \"none\"\n"
            + "  }\n"
            + "}";
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
