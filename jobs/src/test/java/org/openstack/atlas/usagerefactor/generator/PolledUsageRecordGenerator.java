package org.openstack.atlas.usagerefactor.generator;

import org.openstack.atlas.usagerefactor.PolledUsageRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PolledUsageRecordGenerator {

    public static class GeneratorPojo {
        private Integer accountId;
        private Integer loadbalancerId;
        private Integer numRecords;

        public GeneratorPojo(Integer accountId, Integer loadbalancerId, Integer numRecords) {
            this.accountId = accountId;
            this.loadbalancerId = loadbalancerId;
            this.numRecords = numRecords;
        }
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime){
        return generate(generatorPojoList, initialPollTime, 5);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime, int pollIntervalInMins) {
        List<PolledUsageRecord> polledUsageRecords = new ArrayList<PolledUsageRecord>();

        long bandwidthOut = 0;
        long bandwidthIn = 0;
        long bandwidthOutSsl = 0;
        long bandwidthInSsl = 0;
        int numConnections = 0;
        String eventType = null;
        Calendar pollTime;
        int idCnt = 1;

        for (GeneratorPojo generatorPojo : generatorPojoList) {
            pollTime = initialPollTime;

            for (int j = 0; j < generatorPojo.numRecords; j++) {
                PolledUsageRecord polledUsageRecord = new PolledUsageRecord(
                        idCnt++,
                        generatorPojo.accountId,
                        generatorPojo.loadbalancerId,
                        bandwidthOut,
                        bandwidthIn,
                        bandwidthOutSsl,
                        bandwidthInSsl,
                        pollTime,
                        numConnections,
                        eventType
                );

                polledUsageRecords.add(polledUsageRecord);
                pollTime.add(Calendar.MINUTE, pollIntervalInMins);
            }
        }

        return polledUsageRecords;
    }
}
