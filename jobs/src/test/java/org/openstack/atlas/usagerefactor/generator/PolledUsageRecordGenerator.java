package org.openstack.atlas.usagerefactor.generator;

import org.openstack.atlas.usagerefactor.PolledUsageRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PolledUsageRecordGenerator {

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime){
        return generate(generatorPojoList, initialPollTime, 5, 0, 0, 0, 0, 0, null);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   List<String> eventTypes){
        return generate(generatorPojoList, initialPollTime, 5, 0, 0, 0, 0, 0, eventTypes);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long bandwidthOut, long bandwidthIn,
                                                   long bandwidthOutSsl, long bandwidthInSsl){
        return generate(generatorPojoList, initialPollTime, 5, bandwidthOut, bandwidthIn,
                        bandwidthOutSsl, bandwidthInSsl, 0, null);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long bandwidthOut, long bandwidthIn,
                                                   long bandwidthOutSsl, long bandwidthInSsl, List<String> eventTypes){
        return generate(generatorPojoList, initialPollTime, 5, bandwidthOut, bandwidthIn,
                        bandwidthOutSsl, bandwidthInSsl, 0, eventTypes);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   int pollIntervalInMins, long bandwidthOut, long bandwidthIn,
                                                   long bandwidthOutSsl, long bandwidthInSsl,
                                                   long concurrentConnections, List<String> eventTypes) {
        List<PolledUsageRecord> polledUsageRecords = new ArrayList<PolledUsageRecord>();

        Calendar pollTime;
        int idCnt = 1;

        for (GeneratorPojo generatorPojo : generatorPojoList) {
            pollTime = initialPollTime;
            String eventType = null;
            for (int j = 0; j < generatorPojo.getNumRecords(); j++) {
                if(eventTypes != null && j < eventTypes.size()){
                    eventType = eventTypes.get(j);
                }
                PolledUsageRecord polledUsageRecord = new PolledUsageRecord(
                        idCnt++,
                        generatorPojo.getAccountId(),
                        generatorPojo.getLoadbalancerId(),
                        bandwidthOut,
                        bandwidthIn,
                        bandwidthOutSsl,
                        bandwidthInSsl,
                        pollTime,
                        concurrentConnections,
                        eventType
                );

                polledUsageRecords.add(polledUsageRecord);
                pollTime.add(Calendar.MINUTE, pollIntervalInMins);
            }
        }

        return polledUsageRecords;
    }
}
