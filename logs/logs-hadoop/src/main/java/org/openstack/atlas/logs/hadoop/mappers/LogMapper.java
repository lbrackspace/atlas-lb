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
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class LogMapper extends Mapper<LongWritable, Text, LogMapperOutputKey, LogMapperOutputValue> {

    private LogMapperOutputKey oKey = new LogMapperOutputKey();
    private LogMapperOutputValue oVal = new LogMapperOutputValue();

    @Override
    public void setup(Context ctx) throws IOException {
        ctx.getCounter(LogCounters.MAPPER_SETUP_CALLS).increment(1);
        clearValues();
    }

    private void mapValuesToKey() {
        oKey.setAccountId(oVal.getAccountId());
        oKey.setLoadbalancerId(oVal.getLoadbalancerId());
        oKey.setDate(oVal.getDate());
    }

    private void clearValues() {
        oVal.setAccountId(-1);
        oVal.setLoadbalancerId(-1);
        oVal.setLoadbalancerName("null");
        oVal.setLogLine("null");
        oVal.setSourceIp("0.0.0.0");
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
        String line = StaticStringUtils.justOneCR(mVal.toString());
        try {
            LogChopper.getLogLineValues(line, oVal);
        } catch (DateParseException ex) {
            ctx.getCounter(LogCounters.BAD_LOG_DATE).increment(1);
            clearValues();
            oVal.setLogLine(line);
        } catch (StringParseException ex) {
            ctx.getCounter(LogCounters.BAD_LOG_STRING).increment(1);
            clearValues();
            oVal.setLogLine(line);
        } catch (Exception ex) {
            ctx.getCounter(LogCounters.MAPPER_UNKNOWN_EXCEPTION).increment(1);
            clearValues();
            oVal.setLogLine(line);
        }
        mapValuesToKey();
        ctx.getCounter(LogCounters.MAPPER_WRITES).increment(1);
        ctx.write(oKey, oVal);
    }
}
