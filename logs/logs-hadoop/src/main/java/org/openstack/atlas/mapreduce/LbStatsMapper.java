package org.openstack.atlas.mapreduce;

import org.openstack.atlas.io.LbLogsAccountDateKey;
import org.openstack.atlas.io.LbLogsWritable;
import org.openstack.atlas.util.DateTime;
import org.openstack.atlas.util.LogChopper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

public class LbStatsMapper implements Mapper<LongWritable, Text, LbLogsAccountDateKey, LbLogsWritable> {
    private static Log LOG = LogFactory.getLog(LbStatsMapper.class);

    public void map(LongWritable key, Text value, OutputCollector<LbLogsAccountDateKey, LbLogsWritable> output, Reporter reporter) throws IOException {
        LbLogsWritable lbLogs = null;
        try {
            lbLogs= LogChopper.getLbLogStats(value.toString());

            LbLogsAccountDateKey accountDateKey = new LbLogsAccountDateKey();
            accountDateKey.setAccountId(String.valueOf(lbLogs.getAccountId()));
            accountDateKey.setLoadBalancerId(String.valueOf(lbLogs.getLoadBalancerId()));
            accountDateKey.setDate(new DateTime(lbLogs.getDate()).toString());
            //keep the same key passed in regardless of the logline,
            //assume its a logline of a fqdn/domain/alias of the key
            //key.setAccountId(String.valueOf(lbLogs.getAccountId()));
            //key.setDate(logContents.getDate());
            //if they have rawlogs enabled collect them
            //if (values.get(key.getFqdn()) != null) {
            //    textTextOutputCollector.collect(key, value);
            //}
            output.collect(accountDateKey , lbLogs);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void close() throws IOException {
    }

    public void configure(JobConf job) {
    }
}