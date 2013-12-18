package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MigrationProcessor {

    public List<LoadBalancerMergedHostUsage> process(List<LoadBalancerUsage> loadBalancerUsages, List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerHostUsage> loadBalancerHostUsages) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        loadBalancerUsageEvents = removeDuplicateEvents(loadBalancerUsageEvents, loadBalancerHostUsages);


        UsageEventProcessor usageEventProcessor = new UsageEventProcessor(loadBalancerUsageEvents);
        usageEventProcessor.process();

        List<LoadBalancerUsage> usagesToUpdate = usageEventProcessor.getUsagesToUpdate();

        for (LoadBalancerUsage updatedRecord : usagesToUpdate) {
            for (LoadBalancerUsage recordToUpdate : loadBalancerUsages) {
                if (recordToUpdate.getId().equals(updatedRecord.getId())) {
                    System.out.println(String.format("Updating lb_usage record '%d for lb '%d'...", recordToUpdate.getId(), recordToUpdate.getLoadbalancerId()));
                    updateRecord(recordToUpdate, updatedRecord);
                    System.out.println(String.format("Updated lb_usage record '%d for lb '%d'.", recordToUpdate.getId(), recordToUpdate.getLoadbalancerId()));
                    break;
                }
            }
        }

        List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();
        loadBalancerUsages.addAll(usagesToCreate);

        List<LoadBalancerMergedHostUsage> lbUsageRecords = convertLoadBalancerUsageRecords(loadBalancerUsages);
        loadBalancerMergedHostUsages.addAll(lbUsageRecords);

        return loadBalancerMergedHostUsages;
    }

    // Contains side effects FYI
    private void updateRecord(LoadBalancerUsage recordToUpdate, LoadBalancerUsage updatedRecord) {
        recordToUpdate.setCumulativeBandwidthBytesIn(updatedRecord.getCumulativeBandwidthBytesIn());
        recordToUpdate.setCumulativeBandwidthBytesInSsl(updatedRecord.getCumulativeBandwidthBytesInSsl());
        recordToUpdate.setCumulativeBandwidthBytesOut(updatedRecord.getCumulativeBandwidthBytesOut());
        recordToUpdate.setCumulativeBandwidthBytesOutSsl(updatedRecord.getCumulativeBandwidthBytesOutSsl());
        recordToUpdate.setAverageConcurrentConnections(updatedRecord.getAverageConcurrentConnections());
        recordToUpdate.setAverageConcurrentConnectionsSsl(updatedRecord.getAverageConcurrentConnectionsSsl());
        recordToUpdate.setLastBandwidthBytesIn(updatedRecord.getLastBandwidthBytesIn());
        recordToUpdate.setLastBandwidthBytesInSsl(updatedRecord.getLastBandwidthBytesInSsl());
        recordToUpdate.setLastBandwidthBytesOut(updatedRecord.getLastBandwidthBytesOut());
        recordToUpdate.setLastBandwidthBytesOutSsl(updatedRecord.getLastBandwidthBytesOutSsl());
        recordToUpdate.setStartTime(updatedRecord.getStartTime());
        recordToUpdate.setEndTime(updatedRecord.getEndTime());
        recordToUpdate.setEventType(updatedRecord.getEventType());
        recordToUpdate.setNumberOfPolls(updatedRecord.getNumberOfPolls());
        recordToUpdate.setTags(updatedRecord.getTags());
        recordToUpdate.setNumVips(updatedRecord.getNumVips());
    }

    private List<LoadBalancerMergedHostUsage> convertLoadBalancerUsageRecords(List<LoadBalancerUsage> loadBalancerUsages) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        for (LoadBalancerUsage loadBalancerUsage : loadBalancerUsages) {
            loadBalancerMergedHostUsages.addAll(convertLoadBalancerUsage(loadBalancerUsage));
        }

        return loadBalancerMergedHostUsages;
    }

    // TODO: Test
    protected List<LoadBalancerMergedHostUsage> convertLoadBalancerUsage(LoadBalancerUsage loadBalancerUsage) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        String eventType = loadBalancerUsage.getEventType();

        if (eventType != null) {
            LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage();

            loadBalancerMergedHostUsage.setAccountId(loadBalancerUsage.getAccountId());
            loadBalancerMergedHostUsage.setLoadbalancerId(loadBalancerUsage.getLoadbalancerId());
            loadBalancerMergedHostUsage.setNumVips(loadBalancerUsage.getNumVips());
            loadBalancerMergedHostUsage.setPollTime(loadBalancerUsage.getStartTime());
            loadBalancerMergedHostUsage.setTagsBitmask(loadBalancerUsage.getTags());
            loadBalancerMergedHostUsage.setEventType(UsageEvent.valueOf(eventType));
            loadBalancerMergedHostUsage.setIncomingTransfer(0);
            loadBalancerMergedHostUsage.setIncomingTransferSsl(0);
            loadBalancerMergedHostUsage.setOutgoingTransfer(0);
            loadBalancerMergedHostUsage.setOutgoingTransferSsl(0);
            loadBalancerMergedHostUsage.setConcurrentConnections(0);
            loadBalancerMergedHostUsage.setConcurrentConnectionsSsl(0);

            loadBalancerMergedHostUsages.add(loadBalancerMergedHostUsage);
        }

        if (loadBalancerUsage.getEndTime().after(loadBalancerUsage.getStartTime())) {
            Calendar endTime = loadBalancerUsage.getEndTime();
            endTime.add(Calendar.SECOND, -1);

            LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage();
            loadBalancerMergedHostUsage.setAccountId(loadBalancerUsage.getAccountId());
            loadBalancerMergedHostUsage.setLoadbalancerId(loadBalancerUsage.getLoadbalancerId());
            loadBalancerMergedHostUsage.setNumVips(loadBalancerUsage.getNumVips());
            loadBalancerMergedHostUsage.setPollTime(endTime);
            loadBalancerMergedHostUsage.setTagsBitmask(loadBalancerUsage.getTags());
            loadBalancerMergedHostUsage.setEventType(null);
            loadBalancerMergedHostUsage.setIncomingTransfer(loadBalancerUsage.getCumulativeBandwidthBytesIn());
            loadBalancerMergedHostUsage.setIncomingTransferSsl(loadBalancerUsage.getCumulativeBandwidthBytesInSsl());
            loadBalancerMergedHostUsage.setOutgoingTransfer(loadBalancerUsage.getCumulativeBandwidthBytesOut());
            loadBalancerMergedHostUsage.setOutgoingTransferSsl(loadBalancerUsage.getCumulativeBandwidthBytesOutSsl());
            loadBalancerMergedHostUsage.setConcurrentConnections(loadBalancerUsage.getAverageConcurrentConnections().longValue());
            loadBalancerMergedHostUsage.setConcurrentConnectionsSsl(loadBalancerUsage.getAverageConcurrentConnectionsSsl().longValue());

            loadBalancerMergedHostUsages.add(loadBalancerMergedHostUsage);
        }

        return loadBalancerMergedHostUsages;
    }

    protected List<LoadBalancerUsageEvent> removeDuplicateEvents(List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerHostUsage> loadBalancerHostUsages) {
        int numRemovedEvents =0;
        for (LoadBalancerHostUsage loadBalancerHostUsage : loadBalancerHostUsages) {
            for (LoadBalancerUsageEvent loadBalancerUsageEvent : loadBalancerUsageEvents) {
                if (loadBalancerHostUsage.getLoadbalancerId() == loadBalancerUsageEvent.getLoadbalancerId()
                        && loadBalancerHostUsage.getPollTime().equals(loadBalancerUsageEvent.getStartTime())
                        && loadBalancerHostUsage.getEventType().name().equals(loadBalancerUsageEvent.getEventType())) {
                    System.out.println(String.format("Removing duplicate usage event for loadbalancer %d with time '%s'...", loadBalancerUsageEvent.getLoadbalancerId(), loadBalancerUsageEvent.getStartTime().getTime().toString()));
                    loadBalancerUsageEvents.remove(loadBalancerUsageEvent);
                    numRemovedEvents++;
                    break;
                }
            }
        }

        System.out.println(String.format("Removed '%d' duplicate usage events.", numRemovedEvents));
        return loadBalancerUsageEvents;
    }

}