package org.openstack.atlas.tools;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;

public class HadoopConfiguration {

    private Configuration conf;

    private JobConf jobConf;

    private String[] restOfArgs;

    public HadoopConfiguration() {
        conf = new Configuration();
        restOfArgs = new String[]{};
    }

    public HadoopConfiguration(String[] args) {
        conf = new Configuration();
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
