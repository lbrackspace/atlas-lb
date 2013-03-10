package org.openstack.atlas.logs.hadoop.jobs;


import java.util.List;
import org.apache.hadoop.conf.Configuration;

public abstract class HadoopJob {

    protected Configuration conf;

    // To make it easier just call setConfiguration(HadoopLogsConfigs.getHadoopConfiguration());
    public void setConfiguration(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getConfiguration(){
        return conf;
    }

    //Convience method for wrapping List<String> args instead of String[] args
    public int run(List<String> argList) throws Exception{
        String[] args = argList.toArray(new String[argList.size()]);
        return run(args);
    }

    public abstract int run(String args[]) throws Exception;
}
