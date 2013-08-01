package org.openstack.atlas.logs.itest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.logs.hadoop.util.LbLidAidNameContainer;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class ReuploadTestStatic {

    public static final String DEFAULT_HADOOP_CONF_FILE = "/etc/openstack/atlas/hadoop-logs.conf";
    private static final int BUFFSIZE = 1024 * 32;

    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        if (args.length < 1) {
            System.out.printf("usage is <conf.json> [hadoop-logs.conf]\n");
            System.out.printf("Externally test the reuploader code for CloudFiles\n");
            System.out.printf("the json conf file will be of the form:\n%s\n", HibernateDbConf.exampleJson);
            System.out.printf("if the hadoopConfiguration.xml file param is blank the value\n");
            System.out.printf("will be deduced from the %s file\n", DEFAULT_HADOOP_CONF_FILE);
            return;
        }

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, BUFFSIZE);
        System.out.printf("Press enter to continue\n");
        stdin.readLine();

        if (args.length >= 2) {
            System.out.printf("Useing confFile %s\n", args[1]);
            HadoopLogsConfigs.resetConfigs(args[1]);
        } else {
            System.out.printf("useing confFile %s\n", LbLogsConfiguration.defaultConfigurationLocation);
        }

        HdfsUtils hdfsUtils = HadoopLogsConfigs.getHdfsUtils();
        String user = HadoopLogsConfigs.getHdfsUserName();
        Configuration conf = HadoopLogsConfigs.getHadoopConfiguration();
        HadoopLogsConfigs.markJobsJarAsAlreadyCopied();
        URI defaultHdfsUri = FileSystem.getDefaultUri(conf);
        FileSystem fs = hdfsUtils.getFileSystem();
        System.setProperty(CommonItestStatic.HDUNAME, user);
        FileSystem lfs = hdfsUtils.getLocalFileSystem();

        System.out.printf("ReuploadTestStatic.main Spinning up\n");
        System.out.printf("JAVA_LIBRARY_PATH=%s\n", System.getProperty("java.library.path"));
        String jsonDbConfFileName = StaticFileUtils.expandUser(args[0]);
        HuApp huApp = new HuApp();
        HibernateDbConf hConf = HibernateDbConf.newHibernateConf(jsonDbConfFileName);
        System.out.printf("Useing db config %s\n", hConf.toString());
        huApp.setDbMap(hConf);
        System.out.printf("Reading LoadBalancers from databases\n");
        Map<Integer, LbLidAidNameContainer> lbMap = new HashMap();
        System.out.printf("HadoopLogsConfig=%s\n", HadoopLogsConfigs.staticToString());

    }
}
