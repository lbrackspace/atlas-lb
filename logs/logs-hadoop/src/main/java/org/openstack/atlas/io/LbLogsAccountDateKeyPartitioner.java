package org.openstack.atlas.io;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

public class LbLogsAccountDateKeyPartitioner implements Partitioner<LbLogsAccountDateKey, LbLogsWritable> {


    @Override
    public int getPartition(LbLogsAccountDateKey key, LbLogsWritable lbLogs, int numPartitions) {
         return ((key.getAccountId() + ":"+ key.getLoadBalancerId()).hashCode() & Integer.MAX_VALUE) % numPartitions;
    }

    @Override
    public void configure(JobConf jobConf) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
