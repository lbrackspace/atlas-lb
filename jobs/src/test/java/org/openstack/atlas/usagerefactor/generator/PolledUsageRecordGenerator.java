package org.openstack.atlas.usagerefactor.generator;

import org.openstack.atlas.usagerefactor.PolledUsageRecord;
import sun.util.calendar.Gregorian;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PolledUsageRecordGenerator {

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime){
        return generate(generatorPojoList, initialPollTime, 5, 0, 0, 0, 0, 0, 0, null);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   List<String> eventTypes){
        return generate(generatorPojoList, initialPollTime, 5, 0, 0, 0, 0, 0, 0, eventTypes);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long outgoingTransfer, long incomingTransfer){
        return generate(generatorPojoList, initialPollTime, 5, outgoingTransfer, incomingTransfer, 0, 0, 0, 0, null);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long outgoingTransfer, long incomingTransfer, long outgoingTransferSsl,
                                                   long incomingTransferSsl, List<String> eventTypes){
        return generate(generatorPojoList, initialPollTime, 5, outgoingTransfer, incomingTransfer, outgoingTransferSsl,
                        incomingTransferSsl, 0, 0, eventTypes);
    }

    public static List<PolledUsageRecord> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   int pollIntervalInMins, long outgoingTransfer, long incomingTransfer,
                                                   long outgoingTransferSsl, long incomingTransferSsl,
                                                   long averageConcurrentConnections, long averageConcurrentConnectionsSsl,
                                                   List<String> eventTypes) {
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
                        outgoingTransfer,
                        incomingTransfer,
                        incomingTransferSsl,
                        outgoingTransferSsl,
                        averageConcurrentConnections,
                        averageConcurrentConnectionsSsl,
                        pollTime,
                        eventType
                );

                polledUsageRecords.add(polledUsageRecord);
                Calendar newPollTime = new GregorianCalendar(pollTime.get(Calendar.YEAR), pollTime.get(Calendar.MONTH),
                        pollTime.get(Calendar.DAY_OF_MONTH), pollTime.get(Calendar.HOUR), pollTime.get(Calendar.MINUTE),
                        pollTime.get(Calendar.SECOND));
                newPollTime.add(Calendar.MINUTE, pollIntervalInMins);
                pollTime = newPollTime;
            }
        }

        return polledUsageRecords;
    }
}
