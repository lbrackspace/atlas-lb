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
import java.util.LinkedList;
import java.util.List;
import org.openstack.atlas.util.StaticFileUtils;

public abstract class DirectoryTool implements HadoopTool {

    private static final Log LOG = LogFactory.getLog(DirectoryTool.class);

    private org.openstack.atlas.cfg.Configuration compositeConfiguration;

    private HadoopConfiguration conf;

    private FileSystemUtils fileSystemUtils;

    private String inputDir;

    private List<String> localFiles = new LinkedList<String>();

    private HadoopRunner runner;

    public RUN_STATES executeHadoopRun() throws IOException {
        setSpecialConfigurations(conf, runner);
        if (conf.getJobConf().getJobName() != null) {
            conf.getJobConf().setJobName(conf.getJobConf().getJobName() + ":" + runner.getInputString());
        }

        beforeJobRun();
        RunningJob job = JobClient.runJob(conf.getJobConf());
        afterJobRun();

        if (job.getJobState() == JobStatus.SUCCEEDED) {
            return RUN_STATES.SUCCESS;
        } else {
            return RUN_STATES.FAILURE;
        }
    }

    public HadoopConfiguration getConfiguration() {
        return conf;
    }

    public String getInputDirectory() {
        Path[] inputPaths = FileInputFormat.getInputPaths(conf.getJobConf());
        if (inputPaths.length == 0) {
            throw new RuntimeException("No input path defined. this should not happen. please setup the hadoop run first.");
        }
        return inputPaths[0].toUri().getPath();
    }

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

    public void setupHadoopRun(String setupDir) {
        this.inputDir = setupDir;
        localFiles.clear();
        conf = new HadoopConfiguration();
        conf.setJobConf(createJobConf(conf.getConfiguration()));
        LOG.info("Composite Configuration: " + compositeConfiguration);

        String jarPath = findPathJar(DirectoryTool.class);
        if (jarPath != null) {
            if (new File(jarPath).exists()) {
                conf.getJobConf().setJar(jarPath);
            }
        }
        createInputDir();
    }

    public void setupHadoopRun(HadoopRunner localrunner) {
        this.runner = localrunner;
        this.inputDir = runner.getInputString();
        localFiles.clear();
        conf = new HadoopConfiguration();

        conf.setJobConf(createJobConf(conf.getConfiguration()));

        if (runner.getJobJarPath() == null) {

            String jarPath = findPathJar(DirectoryTool.class);
            if (jarPath != null) {
                if(new File(jarPath).exists()) {
                    conf.getJobConf().setJar(jarPath);
                }
            }
        } else {
            conf.getJobConf().setJar(runner.getJobJarPath());
        }
        createInputDir();
    }

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

    protected void afterJobRun() {
    }

    protected void beforeJobRun() {
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
                                                     HadoopRunner localRunner) throws IOException;

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

    private String getSanitizedInputDir() {
        return StaticFileUtils.sanitizeDir(inputDir);
    }

    public static String findPathJar(Class<?> context) throws IllegalStateException {
        URL location = context.getResource('/' + context.getName().replace(".", "/") + ".class");
        String jarPath = location.getPath();
        return jarPath.substring("file:".length(), jarPath.lastIndexOf("!"));
    }
}
