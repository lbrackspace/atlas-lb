package org.openstack.atlas.logs.hadoop.reducers;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;
import org.openstack.atlas.logs.hadoop.counters.LogCounters;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;

public class LogReducer extends Reducer<LogMapperOutputKey, LogMapperOutputValue, LogReducerOutputKey, LogReducerOutputValue> {
    @Override
    public void setup(Context ctx){
        ctx.getCounter(LogCounters.REDUCER_SETUP_CALLS).increment(1);
    }

    @Override
    public void reduce(LogMapperOutputKey rKey,Iterable<LogMapperOutputValue>rVals,Context ctx) throws IOException, InterruptedException{
        int accountId = rKey.getAccountId();
        int loadbalancerId = rKey.getLoadbalancerId();
        long dateOrd = rKey.getDate();

        LogReducerOutputKey oKey = new LogReducerOutputKey();
        LogReducerOutputValue oVal = new LogReducerOutputValue();

        oKey.setLoadbalancerId(loadbalancerId);
        oKey.setAccountId(accountId);

        oVal.setAccountId(accountId);
        oVal.setLoadbalancerId(loadbalancerId);
        oVal.setCrc(-1);
        int nLines = 0;
        oVal.setLogFile(getLogFileName(accountId,loadbalancerId));
        for(LogMapperOutputValue rVal : rVals){
            nLines++;
        }
        oVal.setnLines(nLines);
        ctx.write(oKey, oVal);
    }

    private String getLogFileName(int accountId,int loadbalancerId){
        StringBuilder sb = new StringBuilder();
        sb.append(accountId).append("_").append(loadbalancerId).append(".zip");
        return sb.toString();

    }
}
