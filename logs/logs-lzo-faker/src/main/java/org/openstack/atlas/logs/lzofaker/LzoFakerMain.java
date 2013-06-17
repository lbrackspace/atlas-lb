package org.openstack.atlas.logs.lzofaker;

import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import com.hadoop.compression.lzo.LzopCodec;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.openstack.atlas.config.HadoopLogsConfigs;

import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.logs.hibernatetoy.HibernateDbConf;
import org.openstack.atlas.logs.hibernatetoy.HuApp;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class LzoFakerMain {

    private static final int SB_MAX_SIZE = 256 * 1024 + 4096;
    private static final int BUFFSIZE = 512 * 1024;
    private static final long ORD_MILLIS_PER_HOUR = 10000000;
    private static final int REAL_MILLIS_PER_HOUR = 60 * 60 * 1000;
    private static final String exampleJson = ""
            + "{\n"
            + "  \"dialect\": \"org.hibernate.dialect.MySQL5InnoDBDialect\", \n"
            + "  \"url\": \"jdbc:mysql://mysql-master-n01.ord1.lbaas.rackspace.net:3306/loadbalancing\", \n"
            + "  \"driver\": \"com.mysql.jdbc.Driver\", \n"
            + "  \"passwd\": \"Eirohr0m\", \n"
            + "  \"classes\": [\n"
            + "    \"org.openstack.atlas.service.domain.entities.AccessList\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.AccessListType\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Cluster\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.GroupRateLimit\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.AccountGroup\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.ConnectionLimit\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Entity\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.HealthMonitor\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.HealthMonitorType\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Host\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.HostStatus\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Backup\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.IpVersion\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancer\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerProtocol\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.RateLimit\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerStatus\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Node\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.NodeCondition\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.NodeStatus\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.SessionPersistence\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.SessionPersistenceObject\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Suspension\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Usage\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.AccountUsage\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.VirtualIp\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.VirtualIpv6\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.TrafficScripts\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Ticket\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.BlacklistItem\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.BlacklistType\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Account\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.AccountLimit\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LimitType\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.JobState\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.UserPages\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.SslTermination\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.Defaults\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.AllowedDomain\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadbalancerMeta\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.NodeMeta\", \n"
            + "    \"org.openstack.atlas.service.domain.entities.LoadBalancerStatusHistory\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.Event\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.Alert\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.AlertStatus\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.AccessListEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.ConnectionLimitEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.HealthMonitorEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.LoadBalancerEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.NodeEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.NodeServiceEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.VirtualIpEvent\", \n"
            + "    \"org.openstack.atlas.service.domain.events.entities.SessionPersistenceEvent\"\n"
            + "  ], \n"
            + "  \"db_key\": \"lb\", \n"
            + "  \"user\": \"lbaas\", \n"
            + "  \"hbm2ddl\": \"none\"\n"
            + "}\n"
            + "\n"
            + "";

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.printf("Usage is <configFile> <hourkey> <nLines> <outputDir> [lbId...]\n");
            System.out.printf("\n");
            System.out.printf("Test if the configuration file can be loaded.");
            System.out.printf("ExampleUsage of a config file is:\n");
            System.out.printf("%s\n", exampleJson);
            return;
        }

        String jsonConfFileName = StaticFileUtils.expandUser(args[0]);
        int hourKey = Integer.parseInt(args[1]);
        int nLines = Integer.parseInt(args[2]);
        String outPath = args[3];
        String lzoFileName = hourKey + "-access_log.aggregated.lzo";
        String lzoPath = StaticFileUtils.joinPath(outPath, lzoFileName);
        boolean useCustomLbs = false;
        Set<Integer> customLbIds = new HashSet<Integer>();
        if (args.length > 4) {
            useCustomLbs = true;
            for (int i = 3; i < args.length; i++) {
                customLbIds.add(Integer.parseInt(args[i]));
            }
        }
        HuApp huApp = new HuApp();
        HibernateDbConf hConf = HibernateDbConf.newHibernateConf(jsonConfFileName);

        System.out.printf("Useing db config %s\n", hConf.toString());

        huApp.setDbMap(hConf);

        huApp.begin();
        List<LoadBalancerIdAndName> lbsIds = getActiveLoadbalancerIdsAndNames(huApp);
        for (LoadBalancerIdAndName lbId : lbsIds) {
            System.out.printf("FOUND %d_%d %s\n", lbId.getAccountId(), lbId.getLoadbalancerId(), lbId.getName());
        }
        huApp.commit();

        // If the user tried to customize the lbs then replace the orginal lbsIds list
        if (useCustomLbs) {
            Map<Integer, LoadBalancerIdAndName> foundIds = new HashMap<Integer, LoadBalancerIdAndName>();
            for (LoadBalancerIdAndName lbId : lbsIds) {
                foundIds.put(lbId.getLoadbalancerId(), lbId);
            }
            List<LoadBalancerIdAndName> replaceIds = new ArrayList<LoadBalancerIdAndName>();
            for (Integer lbId : customLbIds) {
                if (foundIds.containsKey(lbId)) {
                    replaceIds.add(foundIds.get(lbId));
                } else {
                    LoadBalancerIdAndName bogusId = new LoadBalancerIdAndName();
                    bogusId.setAccountId(911);
                    bogusId.setLoadbalancerId(lbId);
                    bogusId.setName("911_" + lbId);
                    replaceIds.add(bogusId);
                }

            }
            lbsIds = replaceIds;
        }

        int lbArrayLength = lbsIds.size();
        LoadBalancerIdAndName[] lbArray = new LoadBalancerIdAndName[lbArrayLength];
        Collections.sort(lbsIds);
        for (int i = 0; i < lbArrayLength; i++) {
            lbArray[i] = lbsIds.get(i);
        }
        lbsIds = null;
        System.out.printf("Found a total of %d active loadbalancers\n", lbArrayLength);
        DateTime dt = StaticDateTimeUtils.OrdinalMillisToDateTime(hourKey * ORD_MILLIS_PER_HOUR, false);
        double stepMillis = (double) REAL_MILLIS_PER_HOUR / (double) nLines;

        Configuration conf = new Configuration();
        conf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        conf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        codec.setConf(conf);

        OutputStream os = StaticFileUtils.openOutputFile(lzoPath, BUFFSIZE);
        CompressionOutputStream cos = codec.createOutputStream(os);


        StringBuilder logBuilder = new StringBuilder(SB_MAX_SIZE);
        for (int i = 0; i < nLines; i++) {
            if (i % 100000 == 0) {
                System.out.printf("wrote %d lines %d lines left to go\n", i, nLines - i);
            }
            DateTime offsetDt = dt.plusMillis((int) (stepMillis * i));
            String apacheTime = StaticDateTimeUtils.toApacheDateTime(offsetDt);
            LoadBalancerIdAndName lb = lbArray[i % lbArrayLength];
            String vsName = lb.getAccountId() + "_" + lb.getLoadbalancerId();
            buildFakeLogLine(vsName, apacheTime, logBuilder);

            if (logBuilder.length() >= SB_MAX_SIZE) {
                cos.write(logBuilder.toString().getBytes());
                logBuilder.setLength(0);
            }
        }
        cos.write(logBuilder.toString().getBytes());
        cos.flush();
        cos.finish();

        StaticFileUtils.close(cos);
    }

    public static void buildFakeLogLine(String lbName, String apacheTime, StringBuilder sb) {
        sb.append(lbName).
                append(" www.refferer.com 127.0.0.1 - - [").append(apacheTime).
                append("] \"GET /blah/blah?f=u HTTP/1.1\" 200 2769 \"http://www.blah.org/pfft\" \"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.57 Safari/537.17\" 127.0.0.1:80\n");
    }

    public static List<LoadBalancerIdAndName> getActiveLoadbalancerIdsAndNames(HuApp huApp) {
        List<LoadBalancerIdAndName> lbs = new ArrayList<LoadBalancerIdAndName>();
        String queryString = "select l.id,l.accountId,l.name from LoadBalancer l where l.status = 'ACTIVE'";

        // In the real repository just use q = entityManager.createQuery(queryString)
        Query q = huApp.getSession().createQuery(queryString);

        // In the real repository use List<Object> objList = q.getResultList()
        List<Object> rows = q.list();
        for (Object uncastedRowArrayObj : rows) {
            // Each element of rows is actually an Object[] whith each element representing a column
            Object[] row = (Object[]) uncastedRowArrayObj;
            LoadBalancerIdAndName lb = new LoadBalancerIdAndName();
            lb.setLoadbalancerId((Integer) row[0]);
            lb.setAccountId((Integer) row[1]);
            lb.setName((String) row[2]);
            lbs.add(lb);
        }

        return lbs;
    }
}
