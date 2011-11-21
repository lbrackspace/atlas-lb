package org.openstack.atlas.rax.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.usage.UsageProcessor;
import org.openstack.atlas.jobs.usage.logic.UsageCalculator;
import org.openstack.atlas.rax.domain.entity.RaxUsageRecord;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class RaxUsageProcessor extends UsageProcessor {
    private final Log LOG = LogFactory.getLog(RaxUsageProcessor.class);
    protected Map<Integer, Integer> currentConnectionMap;

    public RaxUsageProcessor(UsageRepository usageRepository, Map<Integer, Long> bytesInMap, Map<Integer, Long> bytesOutMap, Map<Integer, Integer> currentConnectionMap) {
        super(usageRepository, bytesInMap, bytesOutMap);
        this.currentConnectionMap = currentConnectionMap;
    }

    @Override
    public void execute(List<LoadBalancer> loadBalancers) {
        pollTime = Calendar.getInstance();
        recordsToInsert = recordsToInsert == null ? new ArrayList<UsageRecord>() : recordsToInsert;
        recordsToUpdate = recordsToUpdate == null ? new ArrayList<UsageRecord>() : recordsToUpdate;
        Map<Integer, Integer> lbIdAccountIdMap = generateLbIdAccountIdMap(loadBalancers);
        List<UsageRecord> usages = usageRepository.getMostRecentUsageRecordsForLoadBalancers(lbIdAccountIdMap.keySet());
        Map<Integer, UsageRecord> usagesAsMap = convertUsagesToMap(usages);

        for (LoadBalancer loadBalancer : loadBalancers) {
            try {
                if (!usagesAsMap.containsKey(loadBalancer.getId()) && (bytesInMap.containsKey(loadBalancer.getId()) || bytesOutMap.containsKey(loadBalancer.getId()) || currentConnectionMap.containsKey(loadBalancer.getId()))) {
                    // Case 1: No record exists at all and connections, inbound or outbound bandwidth exists. Create one.
                    recordsToInsert.add(createNewUsageRecord(loadBalancer));
                } else if (bytesInMap.containsKey(loadBalancer.getId()) || bytesOutMap.containsKey(loadBalancer.getId()) || currentConnectionMap.containsKey(loadBalancer.getId())) {
                    // Case 2: A record exists and connections, inbound or outbound bandwidth exists.
                    UsageRecord mostRecentRecord = usagesAsMap.get(loadBalancer.getId());

                    if (mostRecentRecord.getEndTime().get(Calendar.DAY_OF_YEAR) != pollTime.get(Calendar.DAY_OF_YEAR) && pollTime.after(mostRecentRecord.getEndTime())) {
                        // Case 2a: A record exists but need to create new one because current day is later than endTime day.
                        // Also, Add bandwidth that occurred between mostRecentRecord endTime and newRecord startTime to
                        // newRecord so that we don't lose it.
                        RaxUsageRecord newRecord = (RaxUsageRecord) createNewUsageRecord(loadBalancer);
                        newRecord.setTransferBytesIn(calcCumBandwidthBytesInForSingleRecord(mostRecentRecord, bytesInMap.get(loadBalancer.getId())) - mostRecentRecord.getTransferBytesIn());
                        newRecord.setTransferBytesOut(calcCumBandwidthBytesOutForSingleRecord(mostRecentRecord, bytesOutMap.get(loadBalancer.getId())) - mostRecentRecord.getTransferBytesOut());
                        recordsToInsert.add(newRecord);
                    } else {
                        // Case 2b: A record exists and we need to update because current day is the same as endTime day.
                        updateMostRecentRecord(mostRecentRecord);
                        recordsToUpdate.add(mostRecentRecord);
                    }
                }
            } catch (NumberFormatException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Ignoring usage record...", loadBalancer));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.warn(String.format("Invalid load balancer name '%s'. Ignoring usage record...", loadBalancer));
            }
        }
    }

    @Override
    public void updateMostRecentRecord(UsageRecord mostRecentRecord) {
        super.updateMostRecentRecord(mostRecentRecord);

        Integer currentConnectionValue = currentConnectionMap.get(mostRecentRecord.getLoadBalancer().getId());
        Integer oldNumPolls = ((RaxUsageRecord) mostRecentRecord).getNumberOfPolls();
        Integer newNumPolls = oldNumPolls + 1;
        ((RaxUsageRecord) mostRecentRecord).setNumberOfPolls(newNumPolls);

        if (currentConnectionValue != null) {
            if (oldNumPolls == 0) {
                // polls will equal 0 only when an event occurs, thus we act as if usage data is brand new
                ((RaxUsageRecord) mostRecentRecord).setAverageConcurrentConnections(currentConnectionValue.doubleValue());
            } else {
                ((RaxUsageRecord) mostRecentRecord).setAverageConcurrentConnections(UsageCalculator.calculateNewAverage(((RaxUsageRecord) mostRecentRecord).getAverageConcurrentConnections(), oldNumPolls, currentConnectionValue));
            }
        }
        
        // TODO: What to do when currentConnectionValue is null
    }

    @Override
    protected UsageRecord createNewUsageRecord(LoadBalancer loadBalancer) {
        Long bytesInValue = bytesInMap.get(loadBalancer.getId());
        Long bytesOutValue = bytesOutMap.get(loadBalancer.getId());
        Integer currentConnectionValue = currentConnectionMap.get(loadBalancer.getId());

        if (bytesInValue == null) bytesInValue = 0l;
        if (bytesOutValue == null) bytesOutValue = 0l;
        if (currentConnectionValue == null) currentConnectionValue = 0;

        RaxUsageRecord newRecord = new RaxUsageRecord();
        newRecord.setLoadBalancer(loadBalancer);
        newRecord.setTransferBytesIn(0l);
        newRecord.setTransferBytesOut(0l);
        newRecord.setLastBytesInCount(bytesInValue);
        newRecord.setLastBytesOutCount(bytesOutValue);
        newRecord.setAverageConcurrentConnections(currentConnectionValue.doubleValue());
        newRecord.setNumberOfPolls(1);
        newRecord.setStartTime(pollTime);
        newRecord.setEndTime(pollTime);
        return newRecord;
    }
}
