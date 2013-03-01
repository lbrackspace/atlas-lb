package org.openstack.atlas.tools;

import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.util.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.openstack.atlas.util.HadoopLogsConfigs;
import org.openstack.atlas.util.StaticFileUtils;
import org.openstack.atlas.util.VerboseLogger;

public abstract class DirectoryTool implements HadoopTool {

    private static final Log LOG = LogFactory.getLog(DirectoryTool.class);
    private static final VerboseLogger vlog = new VerboseLogger(DirectoryTool.class);
    private org.openstack.atlas.cfg.Configuration compositeConfiguration;
    private HadoopConfiguration conf;
    private FileSystemUtils fileSystemUtils;
    private String inputDir;
    private List<String> localFiles = new ArrayList<String>();
    private QuartzSchedulerConfigs schedulerConfigs;

    @Override
    public RUN_STATES executeHadoopRun() throws IOException {
        setSpecialConfigurations(conf, schedulerConfigs);
        if (conf.getJobConf().getJobName() != null) {
            conf.getJobConf().setJobName(conf.getJobConf().getJobName() + ":" + schedulerConfigs.getInputString());
        }

        RunningJob job = JobClient.runJob(conf.getJobConf());

        if (job.getJobState() == JobStatus.SUCCEEDED) {
            return RUN_STATES.SUCCESS;
        } else {
            return RUN_STATES.FAILURE;
        }
    }

    @Override
    public HadoopConfiguration getConfiguration() {
        return conf;
    }

    @Override
    public String getInputDirectory() {
        Path[] inputPaths = FileInputFormat.getInputPaths(conf.getJobConf());
        if (inputPaths.length == 0) {
            throw new RuntimeException("No input path defined. this should not happen. please setup the hadoop run first.");
        }
        return inputPaths[0].toUri().getPath();
    }

    @Override
    public String getOutputDirectory() {
        Path outputPath = FileOutputFormat.getOutputPath(conf.getJobConf());
        if (outputPath == null) {
            throw new RuntimeException(
                    "No output path defined. this should not happen. please setup the hadoop run first.");
        }
        return outputPath.toUri().getPath();
    }

    public void setConf(org.openstack.atlas.cfg.Configuration conf) {
        compositeConfiguration = conf;
    }

    public void setFileSystemUtils(FileSystemUtils fileSystemUtils) {
        this.fileSystemUtils = fileSystemUtils;
    }

    @Override
    public void setupHadoopRun(String setupDir) {
        this.inputDir = setupDir;
        localFiles.clear();
        conf = new HadoopConfiguration();
        conf.setJobConf(createJobConf(conf.getConfiguration()));
        LOG.info("Composite Configuration: " + compositeConfiguration);

        String jarPath = HadoopLogsConfigs.getJobJarPath();
        if (jarPath != null) {
            if (new File(jarPath).exists()) {
                conf.getJobConf().setJar(jarPath);
            }
        }
        createInputDir();
    }

    @Override
    public void setupHadoopRun(QuartzSchedulerConfigs localSchedulerConfigs) {
        this.schedulerConfigs = localSchedulerConfigs;
        this.inputDir = schedulerConfigs.getInputString();
        localFiles.clear();
        conf = new HadoopConfiguration();

        conf.setJobConf(createJobConf(conf.getConfiguration()));

        if (schedulerConfigs.getJobJarPath() == null) {

            String jarPath = findPathJar(DirectoryTool.class);
            if (jarPath != null) {
                if (new File(jarPath).exists()) {
                    conf.getJobConf().setJar(jarPath);
                }
            }
        } else {
            conf.getJobConf().setJar(schedulerConfigs.getJobJarPath());
        }
        createInputDir();
    }

    @Override
    public void setupHadoopRun(String setupDir, String jobJarPath) {
        this.inputDir = setupDir;
        localFiles.clear();
        conf = new HadoopConfiguration();

        conf.setJobConf(createJobConf(conf.getConfiguration()));
        if (jobJarPath != null) {
            conf.getJobConf().setJar(jobJarPath);
        }
        createInputDir();
    }

    protected String createHistoryOutputDir() {
        return compositeConfiguration.getString(LbLogsConfigurationKeys.mapreduce_output_prefix) + getOutputFolderPrefix()
                + "/_logs/" + getSanitizedInputDir() + "/";
    }

    protected String createOutputDir() {
        return compositeConfiguration.getString(LbLogsConfigurationKeys.mapreduce_output_prefix) + getOutputFolderPrefix()
                + getSanitizedInputDir() + "/";
    }

    protected String getLocalInputDir() {
        return compositeConfiguration.getString(LbLogsConfigurationKeys.mapreduce_input_prefix) + inputDir + "/";
    }

    protected abstract Class<? extends Mapper> getMapperClass();

    protected String getOutputFolderPrefix() {
        return "";
    }

    protected abstract Class<? extends Reducer> getReducerClass();

    protected abstract void setSpecialConfigurations(HadoopConfiguration specialConfigurations,
            QuartzSchedulerConfigs localSchedulerConfigs) throws IOException;

    private void createInputDir() {
        FileInputFormat.setInputPaths(conf.getJobConf(), new Path(getLocalInputDir()));
        FileOutputFormat.setOutputPath(conf.getJobConf(), new Path(createOutputDir()));
        conf.getJobConf().set("hadoop.job.history.user.location", createHistoryOutputDir());
    }

    private synchronized JobConf createJobConf(Configuration jobConf) {
        JobConf j = new JobConf(jobConf);

        j.setOutputKeyClass(Text.class);
        j.setOutputValueClass(Text.class);
        j.setInputFormat(TextInputFormat.class);
        j.setOutputFormat(TextOutputFormat.class);

        j.setMapperClass(getMapperClass());
        j.setReducerClass(getReducerClass());

        return j;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DirectoryTool{").append("schedulerConfigs=");
        if (schedulerConfigs == null) {
            sb.append("null");
        } else {
            sb.append(schedulerConfigs.toString());
        }
        sb.append(", createHistoryOutputDir()=").append(createHistoryOutputDir()).
                append(", createOutputDir()=").append(createOutputDir()).
                append(", getLocalInputDir()=").append(getLocalInputDir()).
                append(", sanitizedInputDir()=").append(getSanitizedInputDir()).
                append("}");
        return sb.toString();
    }

    private String getSanitizedInputDir() {
        return StaticFileUtils.sanitizeDir(inputDir);
    }

    public static String findPathJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        return jarPath.substring("file:".length(), jarPath.lastIndexOf("!"));
    }
}
