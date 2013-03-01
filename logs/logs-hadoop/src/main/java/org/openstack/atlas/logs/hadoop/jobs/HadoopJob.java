package org.openstack.atlas.logs.hadoop.jobs;

import org.apache.hadoop.conf.Configuration;

public abstract class HadoopJob {

    protected Configuration conf;

    public void setConfiguration(Configuration conf) {
        this.conf = conf;
    }

    public abstract int run(String args[]) throws Exception;
}
