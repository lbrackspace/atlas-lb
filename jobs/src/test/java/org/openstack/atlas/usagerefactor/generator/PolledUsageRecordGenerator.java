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
                                                   String eventType){
        return generate(generatorPojoList, initialPollTime, 5, 0, 0, 0, 0, 0, eventType);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long bandwidthOut, long bandwidthIn,
                                                   long bandwidthOutSsl, long bandwidthInSsl){
        return generate(generatorPojoList, initialPollTime, 5, bandwidthOut, bandwidthIn,
                        bandwidthOutSsl, bandwidthInSsl, 0, null);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long bandwidthOut, long bandwidthIn,
                                                   long bandwidthOutSsl, long bandwidthInSsl, String eventType){
        return generate(generatorPojoList, initialPollTime, 5, bandwidthOut, bandwidthIn,
                        bandwidthOutSsl, bandwidthInSsl, 0, eventType);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   int pollIntervalInMins, long bandwidthOut, long bandwidthIn,
                                                   long bandwidthOutSsl, long bandwidthInSsl,
                                                   long concurrentConnections, String eventType) {
        List<PolledUsageRecord> polledUsageRecords = new ArrayList<PolledUsageRecord>();

        Calendar pollTime;
        int idCnt = 1;

        for (GeneratorPojo generatorPojo : generatorPojoList) {
            pollTime = initialPollTime;

            for (int j = 0; j < generatorPojo.getNumRecords(); j++) {
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
