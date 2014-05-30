package org.openstack.atlas.logs.hadoop.partitioners;

import org.apache.hadoop.mapreduce.Partitioner;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;

public class LogPartitioner extends Partitioner<LogMapperOutputKey, LogMapperOutputValue> {

    @Override
    public int getPartition(LogMapperOutputKey key, LogMapperOutputValue value, int nReducers) {
        if (key.getAccountId() < 0 || key.getLoadbalancerId() < 0) {
            return nReducers - 1; // All unidentified lbs and accounts get their own reducer which is the last one.
        }
        int hash = (31 * new Integer(key.getAccountId()).hashCode()) + new Integer(key.getLoadbalancerId()).hashCode();
        return Math.abs(hash) % (nReducers - 1);
    }
}
