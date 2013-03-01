package org.openstack.atlas.util;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.logs.hadoop.jobs.HadoopJob;

public class HadoopLogsConfigs {

    private static final Log LOG = LogFactory.getLog(HadoopLogsConfigs.class);
    protected static final String cacheDir;
    protected static final String backupDir;
    protected static final String fileSystemRootDir;
    protected static final String jobJarPath;
    protected static final String hadoopXmlFile;
    protected static final String mapreduceInputPrefix;
    protected static final String mapreduceOutputPrefix;
    protected static final String fileRegion;
    protected static final String hdfsUserName;
    protected static Configuration hadoopConfiguration = null;
    protected static HdfsUtils hdfsUtils = null;

    static {
        LbLogsConfiguration lbLogsConf = new LbLogsConfiguration();
        cacheDir = lbLogsConf.getString(LbLogsConfigurationKeys.rawlogs_cache_dir);
        backupDir = lbLogsConf.getString(LbLogsConfigurationKeys.rawlogs_backup_dir);
        fileSystemRootDir = lbLogsConf.getString(LbLogsConfigurationKeys.filesystem_root_dir);
        jobJarPath = lbLogsConf.getString(LbLogsConfigurationKeys.job_jar_path);
        hadoopXmlFile = lbLogsConf.getString(LbLogsConfigurationKeys.hadoop_xml_file);
        mapreduceInputPrefix = lbLogsConf.getString(LbLogsConfigurationKeys.mapreduce_input_prefix);
        mapreduceOutputPrefix = lbLogsConf.getString(LbLogsConfigurationKeys.mapreduce_output_prefix);
        fileRegion = lbLogsConf.getString(LbLogsConfigurationKeys.files_region);
        hdfsUserName = lbLogsConf.getString(LbLogsConfigurationKeys.hdfs_user_name);
    }

    public static HadoopJob getHadoopJob(Class<? extends HadoopJob> jobClass) {
        HadoopJob implementation = null;
        try {
            implementation = jobClass.newInstance();
        } catch (InstantiationException ex) {
            String msg = String.format("Could not instantiate class %s", jobClass.getName());
            LOG.error(msg, ex);
            throw new IllegalArgumentException(msg, ex);
        } catch (IllegalAccessException ex) {
            String msg = String.format("Could not instantiate class %s", jobClass.getName());
            LOG.error(msg, ex);
            throw new IllegalArgumentException(msg, ex);
        }
        implementation.setConfiguration(getHadoopConfiguration());
        return implementation;
    }

    public static String staticToString() {
        StringBuilder sb = new StringBuilder();
        sb = sb.append("{cacheDir=").append(cacheDir).
                append(", backupdir=").append(backupDir).
                append(", fileSystemRootDir=").append(fileSystemRootDir).
                append(", hadoopXmlFile=").append(hadoopXmlFile).
                append(", mapreduceInputPrefix=").append(mapreduceInputPrefix).
                append(", mapreduceOutputPrefix=").append(mapreduceOutputPrefix).
                append(", fileRegion=").append(fileRegion).
                append(", hdfsUserName=").append(hdfsUserName).
                append("}");
        return sb.toString();
    }

    public static Configuration getHadoopConfiguration() {
        if (hadoopConfiguration == null) {
            hadoopConfiguration = new Configuration();
            hadoopConfiguration.addResource(new Path(StaticFileUtils.expandUser(hadoopXmlFile)));
        }
        return hadoopConfiguration;
    }

    public static HdfsUtils getHdfsUtils() {
        if (hdfsUtils == null) {
            hdfsUtils = new HdfsUtils();
            try {
                hdfsUtils.setConf(getHadoopConfiguration());
                hdfsUtils.setUser(hdfsUserName);
                hdfsUtils.init();
            } catch (IOException ex) {
                hdfsUtils = null;
                throw new IllegalStateException("Could not initialize HadoopLogsConfigs class", ex);
            } catch (InterruptedException ex) {
                hdfsUtils = null;
                throw new IllegalStateException("Could not initialize HadoopLogsConfigs class", ex);
            }
        }
        return hdfsUtils;
    }

    public static String getCacheDir() {
        return cacheDir;
    }

    public static String getBackupDir() {
        return backupDir;
    }

    public static String getFileSystemRootDir() {
        return fileSystemRootDir;
    }

    public static String getJobJarPath() {
        return jobJarPath;
    }

    public static String getHadoopXmlFile() {
        return hadoopXmlFile;
    }

    public static String getMapreduceInputPrefix() {
        return mapreduceInputPrefix;
    }

    public static String getMapreduceOutputPrefix() {
        return mapreduceOutputPrefix;
    }

    public static String getFileRegion() {
        return fileRegion;
    }

    public static String getHdfsUserName() {
        return hdfsUserName;
    }
}
