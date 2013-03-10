package org.openstack.atlas.logs.hadoop.reducers;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;
import org.openstack.atlas.logs.hadoop.counters.LogCounters;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.CRC32;

public class LogReducer extends Reducer<LogMapperOutputKey, LogMapperOutputValue, LogReducerOutputKey, LogReducerOutputValue> {

    private static final int BUFFER_SIZE = 1024 * 128;
    private int fileHour = -1;
    private String hdfsUserName;
    private String hdfsZipDir;

    @Override
    public void setup(Context ctx) throws IOException {
        ctx.getCounter(LogCounters.REDUCER_SETUP_CALLS).increment(1);
        String fileHourString = ctx.getConfiguration().get("fileHour");
        hdfsUserName = ctx.getConfiguration().get("hdfs_user_name");
        hdfsZipDir = ctx.getConfiguration().get("hdfs_zip_dir");
    }

    @Override
    public void reduce(LogMapperOutputKey rKey, Iterable<LogMapperOutputValue> rVals, Context ctx) throws IOException, InterruptedException {
        int accountId = rKey.getAccountId();
        int loadbalancerId = rKey.getLoadbalancerId();

        LogReducerOutputKey oKey = new LogReducerOutputKey();
        LogReducerOutputValue oVal = new LogReducerOutputValue();

        oKey.setAccountId(accountId);
        oKey.setLoadbalancerId(loadbalancerId);

        oVal.setAccountId(accountId);
        oVal.setLoadbalancerId(loadbalancerId);
        oVal.setCrc(-1);
        String zipName = getZipFileName(loadbalancerId, fileHour);
        String zipContentsName = getZipContentsName(loadbalancerId, fileHour);


        int nLines = 0;
        for (LogMapperOutputValue rVal : rVals) {
            nLines++;
        }
        oVal.setnLines(nLines);

        oVal.setLogFile(zipName);
        ctx.write(oKey, oVal);
    }

    private static String getZipFileName(int loadbalancerId, int fileHour) {
        return "access_log_" + loadbalancerId + "_" + fileHour + ".zip";
    }

    private static String getZipContentsName(int loadbalancerId, int fileHour) {
        return "access_log_" + loadbalancerId + "_" + fileHour;
    }
}
