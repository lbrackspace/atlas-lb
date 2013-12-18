package org.openstack.atlas.logs.hadoop.mappers;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openstack.atlas.exception.DateParseException;
import org.openstack.atlas.exception.StringParseException;
import org.openstack.atlas.logs.hadoop.counters.LogCounters;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;

import org.openstack.atlas.logs.hadoop.util.LogChopper;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class LogMapper extends Mapper<LongWritable, Text, LogMapperOutputKey, LogMapperOutputValue> {

    private LogMapperOutputKey oKey = new LogMapperOutputKey();
    private LogMapperOutputValue oVal = new LogMapperOutputValue();

    @Override
    public void setup(Context ctx) throws IOException {
        ctx.getCounter(LogCounters.MAPPER_SETUP_CALLS).increment(1);
        oKey.setDate(-1);
        oKey.setLoadbalancerId(-1);
        oKey.setAccountId(-1);
        oVal.setAccountId(-1);
        oVal.setLoadbalancerId(-1);
        oVal.setLoadbalancerName("null");
        oVal.setLogLine("null");
        oVal.setSourceIp("127.0.0.1");
    }

    private String getDebugInfo(Context ctx) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("host: ").append(Debug.hostName()).append("\n").
                append("Directory: ").append(StaticFileUtils.getWorkingDirectory()).append("\n").
                append("CacheFiles: \n");
        URI[] cacheFiles = DistributedCache.getCacheFiles(ctx.getConfiguration());
        if (cacheFiles == null) {
            sb.append("No cache files found\n");
            throw new IOException(sb.toString());
        }
        for (URI cacheFile : cacheFiles) {
            sb.append("   ").append(cacheFile.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void map(LongWritable mKey, Text mVal, Context ctx) throws IOException, InterruptedException {
        ctx.getCounter(LogCounters.MAPPER_CALLS).increment(1);
        String line = mVal.toString();
        try {
            LogChopper.getLogLineValues(line, oVal);
        } catch (DateParseException ex) {
            ctx.getCounter(LogCounters.BAD_LOG_DATE).increment(1);
            return;
        } catch (StringParseException ex) {
            ctx.getCounter(LogCounters.BAD_LOG_STRING).increment(1);
            return;
        } catch (Exception ex) {
            ctx.getCounter(LogCounters.BAD_LOG_STRING).increment(1);
            return;
        }
        oKey.setAccountId(oVal.getAccountId());
        oKey.setLoadbalancerId(oVal.getLoadbalancerId());
        oKey.setDate(oVal.getDate());

        ctx.getCounter(LogCounters.MAPPER_WRITES).increment(1);
        ctx.write(oKey, oVal);
    }
}
