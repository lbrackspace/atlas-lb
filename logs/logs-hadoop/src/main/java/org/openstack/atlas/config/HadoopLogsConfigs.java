package org.openstack.atlas.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.openstack.atlas.logs.hadoop.jobs.HadoopJob;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class HadoopLogsConfigs {

    private static final Log LOG = LogFactory.getLog(HadoopLogsConfigs.class);
    protected static int resetCount = 0;
    protected static String cacheDir;
    protected static String backupDir;
    protected static String fileSystemRootDir;
    protected static String localJobsJarPath;
    protected static String hadoopXmlFile;
    protected static String mapreduceInputPrefix;
    protected static String mapreduceOutputPrefix;
    protected static String fileRegion;
    protected static String hdfsUserName;
    protected static int daysOfZipsToKeep;
    protected static int daysOfLZOsToKeep;
    protected static int replicationCount;
    protected static int hdfsBlockSize;
    private static String cloudFilesUser;
    private static String cloudFilesKey;
    private static int cloudFilesDaysLzos;
    private static int cloudFilesLzoSegmentSize;
    protected static String numReducers;
    protected static String hdfsJobsJarPath;
    private static String cloudFilesAccount;
    private static String cloudFilesAuthEndpoint;
    private static String cloudFilesStorageEndpoint;
    protected static Configuration hadoopConfiguration = null;
    protected static HdfsUtils hdfsUtils = null;
    protected static boolean jarCopyed = false;

    static {
        resetConfigs(null);
    }

    public HadoopLogsConfigs() {
    }

    public static void resetConfigs(String filePath) {
        resetCount++;
        hadoopConfiguration = null;
        hdfsUtils = null;
        jarCopyed = false;
        LbLogsConfiguration lbLogsConf;
        if (filePath == null) {
            lbLogsConf = new LbLogsConfiguration();
        } else {
            lbLogsConf = new LbLogsConfiguration(StaticFileUtils.expandUser(filePath));
        }
        jarCopyed = false;
        cacheDir = lbLogsConf.getString(LbLogsConfigurationKeys.rawlogs_cache_dir);
        backupDir = lbLogsConf.getString(LbLogsConfigurationKeys.rawlogs_backup_dir);
        fileSystemRootDir = lbLogsConf.getString(LbLogsConfigurationKeys.filesystem_root_dir);
        localJobsJarPath = lbLogsConf.getString(LbLogsConfigurationKeys.job_jar_path);
        hadoopXmlFile = lbLogsConf.getString(LbLogsConfigurationKeys.hadoop_xml_file);
        mapreduceInputPrefix = lbLogsConf.getString(LbLogsConfigurationKeys.mapreduce_input_prefix);
        mapreduceOutputPrefix = lbLogsConf.getString(LbLogsConfigurationKeys.mapreduce_output_prefix);
        fileRegion = lbLogsConf.getString(LbLogsConfigurationKeys.files_region);
        hdfsUserName = lbLogsConf.getString(LbLogsConfigurationKeys.hdfs_user_name);
        hdfsJobsJarPath = lbLogsConf.getString(LbLogsConfigurationKeys.hdfs_job_jar_path);
        numReducers = lbLogsConf.getString(LbLogsConfigurationKeys.num_reducers);
        daysOfLZOsToKeep = getInt(lbLogsConf, LbLogsConfigurationKeys.days_of_lzos_to_keep);
        daysOfZipsToKeep = getInt(lbLogsConf, LbLogsConfigurationKeys.days_of_zips_to_keep);
        hdfsBlockSize = getInt(lbLogsConf, LbLogsConfigurationKeys.hdfs_block_size_megs);
        replicationCount = getInt(lbLogsConf, LbLogsConfigurationKeys.replication_count);
        cloudFilesDaysLzos = getInt(lbLogsConf, LbLogsConfigurationKeys.cloud_files_days_of_lzos_to_keep);
        cloudFilesLzoSegmentSize = getInt(lbLogsConf, LbLogsConfigurationKeys.cloud_files_lzo_segment_size);
        cloudFilesUser = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_user);
        cloudFilesKey = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_key);
        cloudFilesAccount = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_account);
        cloudFilesAuthEndpoint = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_auth_api_endpoint);
        cloudFilesStorageEndpoint = lbLogsConf.getString(LbLogsConfigurationKeys.cloud_files_lzo_storage_api_endpoint);
    }

    private static int getInt(LbLogsConfiguration lbConf, LbLogsConfigurationKeys key) {
        return Integer.parseInt(lbConf.getString(key));
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
        sb = sb.append("{\n").
                append("    cacheDir = ").append(cacheDir).append("\n").
                append("    backupdir = ").append(backupDir).append("\n").
                append("    fileSystemRootDir = ").append(fileSystemRootDir).append("\n").
                append("    localHobsJarPath= ").append(localJobsJarPath).append("\n").
                append("    hadoopXmlFile =").append(hadoopXmlFile).append("\n").
                append("    mapreduceInputPrefix = ").append(mapreduceInputPrefix).append("\n").
                append("    mapreduceOutputPrefix = ").append(mapreduceOutputPrefix).append("\n").
                append("    fileRegion = ").append(fileRegion).append("\n").
                append("    hdfsUserName = ").append(hdfsUserName).append("\n").
                append("    numReducers = ").append(numReducers).append("\n").
                append("    hdfsJobsJarPath = ").append(hdfsJobsJarPath).append("\n").
                append("    jarCopyed = ").append(jarCopyed).append("\n").
                append("    resetCount = ").append(resetCount).append("\n").
                append("    replicationCount = ").append(replicationCount).append("\n").
                append("    hdfs_block_size_megs = ").append(hdfsBlockSize).append("\n").
                append("    days_of_lzos_to_keep = ").append(daysOfLZOsToKeep).append("\n").
                append("    days_of_zips_to_keep = ").append(daysOfZipsToKeep).append("\n").
                append("    cloud_files_lzo_segment_size = ").append(cloudFilesLzoSegmentSize).append("\n").
                append("    cloud_files_days_of_lzos_to_keep = ").append(cloudFilesDaysLzos).append("\n").
                append("    cloud_files_lzo_user = ").append(cloudFilesUser).append("\n").
                append("    cloud_files_lzo_key = ").append(cloudFilesKey).append("\n").
                append("    cloud_files_lzo_account = ").append(cloudFilesAccount).append("\n").
                append("    cloud_files_lzo_auth_api_endpoint = ").append(cloudFilesAuthEndpoint).append("\n").
                append("    cloud_files_lzo_storage_api_endpoint = ").append(cloudFilesStorageEndpoint).append("\n").
                append("}\n");
        return sb.toString();
    }

    public static void setHadoopConfiguration(Configuration conf) {
        hadoopConfiguration = conf;
    }

    public static Configuration getHadoopConfiguration() {
        if (hadoopConfiguration == null) {
            hadoopConfiguration = new Configuration();
            hadoopConfiguration.addResource(new Path(StaticFileUtils.expandUser(hadoopXmlFile)));

            // Disable Speculative Execution
            hadoopConfiguration.setBoolean("mapred.reduce.tasks.speculative.execution", true);
            hadoopConfiguration.setBoolean("mapred.map.tasks.speculative.execution", true);
            // Cause its wastful.
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

    public static String getHdfsJobsJarPath() {
        return hdfsJobsJarPath;
    }

    public static String getLocalJobsJarPath() {
        return localJobsJarPath;
    }

    public static boolean isJarCopyed() {
        return jarCopyed;
    }

    public static void markJobsJarAsAlreadyCopied() {
        jarCopyed = true;
    }

    public static void markJobsJarAsUnCopied() {
        jarCopyed = false;
    }

    public static void copyJobsJar() throws FileNotFoundException, IOException {
        if (!jarCopyed) { // If this is the first run since the app was deployed then copy the jobs jar
            LOG.info(String.format("First hadoop run: Copying jobsJar %s -> %s", localJobsJarPath, hdfsJobsJarPath));
            InputStream is = StaticFileUtils.openInputFile(localJobsJarPath);
            FSDataOutputStream os = hdfsUtils.openHdfsOutputFile(hdfsJobsJarPath, false, true);
            StaticFileUtils.copyStreams(is, os, null, hdfsUtils.getBufferSize());
            StaticFileUtils.close(is);
            StaticFileUtils.close(os);
            jarCopyed = true;
        }
    }

    public static String getNumReducers() {
        return numReducers;
    }

    public static int getResetCount() {
        return resetCount;
    }

    public static int getDaysOfZipsToKeep() {
        return daysOfZipsToKeep;
    }

    public static int getDaysOfLZOsToKeep() {
        return daysOfLZOsToKeep;
    }

    public static int getReplicationCount() {
        return replicationCount;
    }

    public static int getHdfsBlockSize() {
        return hdfsBlockSize;
    }

    public static String getCloudFilesUser() {
        return cloudFilesUser;
    }

    public static String getCloudFilesKey() {
        return cloudFilesKey;
    }

    public static int getCloudFilesDaysLzos() {
        return cloudFilesDaysLzos;
    }

    public static int getCloudFilesLzoSegmentSize() {
        return cloudFilesLzoSegmentSize;
    }

    public static String getCloudFilesAccount() {
        return cloudFilesAccount;
    }

    public static String getCloudFilesAuthEndpoint() {
        return cloudFilesAuthEndpoint;
    }

    public static String getCloudFilesStorageEndpoint() {
        return cloudFilesStorageEndpoint;
    }

    public static HadoopLogsConfigs getInstance() {
        return new HadoopLogsConfigs();
    }
}
