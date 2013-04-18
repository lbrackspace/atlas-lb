package org.openstack.atlas.usagerefactor.generator;

import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PolledUsageRecordGenerator {

    private static final long DEFAULT_OUTGOING_TRANSFER = 0;
    private static final long DEFAULT_INCOMING_TRANSFER = 0;
    private static final long DEFAULT_OUTGOING_TRANSFER_SSL = 0;
    private static final long DEFAULT_INCOMING_TRANSFER_SSL = 0;
    private static final int DEFAULT_CONCURRENT_CONNECTIONS = 0;
    private static final int DEFAULT_CONCURRENT_CONNECTIONS_SSL = 0;
    private static final int DEFAULT_NUM_VIPS = 1;
    private static final int DEFAULT_TAGS_BITMASK = 0;
    private static final int DEFAULT_POLL_INTERVAL = 5;

    public static List<LoadBalancerMergedHostUsage> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime){
        return generate(generatorPojoList, initialPollTime, DEFAULT_POLL_INTERVAL, DEFAULT_OUTGOING_TRANSFER, DEFAULT_INCOMING_TRANSFER,
                        DEFAULT_OUTGOING_TRANSFER_SSL, DEFAULT_INCOMING_TRANSFER_SSL, DEFAULT_CONCURRENT_CONNECTIONS,
                DEFAULT_CONCURRENT_CONNECTIONS_SSL, DEFAULT_NUM_VIPS, DEFAULT_TAGS_BITMASK, null);
    }

    public static List<LoadBalancerMergedHostUsage> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   int tagsBitMask){
        return generate(generatorPojoList, initialPollTime, DEFAULT_POLL_INTERVAL, DEFAULT_OUTGOING_TRANSFER, DEFAULT_INCOMING_TRANSFER,
                        DEFAULT_OUTGOING_TRANSFER_SSL, DEFAULT_INCOMING_TRANSFER_SSL, DEFAULT_CONCURRENT_CONNECTIONS,
                DEFAULT_CONCURRENT_CONNECTIONS_SSL, DEFAULT_NUM_VIPS, tagsBitMask, null);
    }

    public static List<LoadBalancerMergedHostUsage> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   List<UsageEvent> eventTypes){
        return generate(generatorPojoList, initialPollTime, DEFAULT_POLL_INTERVAL, DEFAULT_OUTGOING_TRANSFER, DEFAULT_INCOMING_TRANSFER,
                        DEFAULT_OUTGOING_TRANSFER_SSL, DEFAULT_INCOMING_TRANSFER_SSL, DEFAULT_CONCURRENT_CONNECTIONS,
                DEFAULT_CONCURRENT_CONNECTIONS_SSL, DEFAULT_NUM_VIPS, DEFAULT_TAGS_BITMASK, eventTypes);
    }

    public static List<LoadBalancerMergedHostUsage> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long outgoingTransfer, long incomingTransfer){
        return generate(generatorPojoList, initialPollTime, DEFAULT_POLL_INTERVAL, outgoingTransfer, incomingTransfer,
                        DEFAULT_OUTGOING_TRANSFER_SSL, DEFAULT_INCOMING_TRANSFER_SSL, DEFAULT_CONCURRENT_CONNECTIONS,
                DEFAULT_CONCURRENT_CONNECTIONS_SSL, DEFAULT_NUM_VIPS, DEFAULT_TAGS_BITMASK, null);
    }

    public static List<LoadBalancerMergedHostUsage> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   long outgoingTransfer, long incomingTransfer, long outgoingTransferSsl,
                                                   long incomingTransferSsl, List<UsageEvent> eventTypes){
        return generate(generatorPojoList, initialPollTime, DEFAULT_POLL_INTERVAL, outgoingTransfer, incomingTransfer, outgoingTransferSsl,
                        incomingTransferSsl, DEFAULT_CONCURRENT_CONNECTIONS, DEFAULT_CONCURRENT_CONNECTIONS_SSL,
                        DEFAULT_NUM_VIPS, DEFAULT_TAGS_BITMASK, eventTypes);
    }

    public static List<LoadBalancerMergedHostUsage> generate(List<GeneratorPojo> generatorPojoList, Calendar initialPollTime,
                                                   int pollIntervalInMins, long outgoingTransfer, long incomingTransfer,
                                                   long outgoingTransferSsl, long incomingTransferSsl,
                                                   int concurrentConnections, int concurrentConnectionsSsl,
                                                   int numVips, int tagsBitmask, List<UsageEvent> eventTypes) {
        List<LoadBalancerMergedHostUsage> LoadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        Calendar pollTime;
        int idCnt = 1;

        for (GeneratorPojo generatorPojo : generatorPojoList) {
            pollTime = initialPollTime;
            UsageEvent eventType = null;
            for (int j = 0; j < generatorPojo.getNumRecords(); j++) {
                if(eventTypes != null && j < eventTypes.size()){
                    eventType = eventTypes.get(j);
                }
                LoadBalancerMergedHostUsage LoadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage(
                        generatorPojo.getAccountId(),
                        generatorPojo.getLoadbalancerId(),
                        outgoingTransfer,
                        incomingTransfer,
                        incomingTransferSsl,
                        outgoingTransferSsl,
                        concurrentConnections,
                        concurrentConnectionsSsl,
                        numVips,
                        tagsBitmask,
                        pollTime,
                        eventType
                );

                LoadBalancerMergedHostUsages.add(LoadBalancerMergedHostUsage);
                Calendar newPollTime = new GregorianCalendar(pollTime.get(Calendar.YEAR), pollTime.get(Calendar.MONTH),
                        pollTime.get(Calendar.DAY_OF_MONTH), pollTime.get(Calendar.HOUR), pollTime.get(Calendar.MINUTE),
                        pollTime.get(Calendar.SECOND));
                newPollTime.add(Calendar.MINUTE, pollIntervalInMins);
                pollTime = newPollTime;
            }
        }

        return LoadBalancerMergedHostUsages;
    }
}
