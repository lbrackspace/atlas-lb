package org.openstack.atlas.logs.hadoop.counters;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.openstack.atlas.util.staticutils.StaticStringUtils;


public class CounterUtils {
 public static String showCounters(Job job, Enum[] counters) throws IOException {
        StringBuilder sb = new StringBuilder();
        int maxNameLen = getMaxCounterNameLength(counters);
        Counters jobCounters = job.getCounters();
        for (int i = 0; i < counters.length; i++) {
            Enum counterKey = counters[i];
            String counterName = StaticStringUtils.lpad(counterKey.name(), " ", maxNameLen);
            long val = jobCounters.findCounter(counterKey).getValue();
            String valStr = StaticStringUtils.lpadLong(val, " ", 20);
            sb.append(counterName).append(":").append(valStr).append("\n");
        }
        return sb.toString();
    }

    private static int getMaxCounterNameLength(Enum[] counters) {
        int maxVal = 0;
        for (int i = 0; i < counters.length; i++) {
            int curVal = counters[i].name().length();
            if (maxVal < curVal) {
                maxVal = curVal;
            }
        }
        return maxVal;
    }
}
