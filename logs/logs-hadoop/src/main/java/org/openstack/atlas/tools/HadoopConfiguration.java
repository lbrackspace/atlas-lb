package org.openstack.atlas.tools;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;
import org.openstack.atlas.config.HadoopLogsConfigs;

public class HadoopConfiguration {

    private Configuration conf;
    private JobConf jobConf;
    private String[] restOfArgs;

    public HadoopConfiguration() {
        conf = HadoopLogsConfigs.getHadoopConfiguration();
        restOfArgs = new String[]{};
    }

    public HadoopConfiguration(String[] args) throws IOException{
        conf = HadoopLogsConfigs.getHadoopConfiguration();
        restOfArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    }

    public String[] getArgs() {
        return restOfArgs;
    }

    public Configuration getConfiguration() {
        return conf;
    }

    public JobConf getJobConf() {
        return jobConf;
    }

    public void setJobConf(JobConf jobConf) {
        this.jobConf = jobConf;
    }
}
