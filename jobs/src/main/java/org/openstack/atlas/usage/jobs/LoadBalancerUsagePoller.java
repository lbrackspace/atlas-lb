package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;
import org.openstack.atlas.usage.logic.UsageCalculator;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

public class LoadBalancerUsagePoller extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePoller.class);
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    private LoadBalancerRepository loadBalancerRepository;
    private HostRepository hostRepository;
    private LoadBalancerUsageRepository hourlyUsageRepository;
    private LoadBalancerUsageEventRepository usageEventRepository;
    private final int BATCH_SIZE = 100;

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setReverseProxyLoadBalancerAdapter(ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter) {
        this.reverseProxyLoadBalancerAdapter = reverseProxyLoadBalancerAdapter;
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Required
    public void setHourlyUsageRepository(LoadBalancerUsageRepository hourlyUsageRepository) {
        this.hourlyUsageRepository = hourlyUsageRepository;
    }

    @Required
    public void setUsageEventRepository(LoadBalancerUsageEventRepository usageEventRepository) {
        this.usageEventRepository = usageEventRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        processUsageEvents();
        startUsagePoller();
    }

    private void processUsageEvents() {
        LOG.info("Processing usage events...");

        List<LoadBalancerUsageEvent> usageEventEntries = usageEventRepository.getAllUsageEventEntriesInOrder();
        List<LoadBalancerUsage> usagesToCreate = new ArrayList<LoadBalancerUsage>();
        List<LoadBalancerUsage> usagesToUpdate = new ArrayList<LoadBalancerUsage>();
        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = new HashMap<Integer, List<LoadBalancerUsage>>();

        for (LoadBalancerUsageEvent usageEventEntry : usageEventEntries) {
            UsageEvent usageEvent = UsageEvent.valueOf(usageEventEntry.getEventType());
            LoadBalancerUsage previousUsageRecord = getPreviousUsageRecord(usageEventEntry, newEventUsageMap);

            int updatedTags = getTags(usageEventEntry.getAccountId(), usageEventEntry.getLoadbalancerId(), usageEvent, previousUsageRecord);

            Calendar eventTime;
            if (previousUsageRecord != null && previousUsageRecord.getEndTime().after(usageEventEntry.getStartTime())) {
                eventTime = Calendar.getInstance();
            } else {
                eventTime = usageEventEntry.getStartTime();
            }

            LoadBalancerUsage newUsage = new LoadBalancerUsage();
            newUsage.setAccountId(usageEventEntry.getAccountId());
            newUsage.setLoadbalancerId(usageEventEntry.getLoadbalancerId());
            newUsage.setNumVips(usageEventEntry.getNumVips());
            newUsage.setStartTime(eventTime);
            newUsage.setEndTime(eventTime);
            newUsage.setNumberOfPolls(0);
            newUsage.setTags(updatedTags);
            newUsage.setEventType(usageEventEntry.getEventType());

            if (previousUsageRecord != null) {
                Integer oldNumPolls = previousUsageRecord.getNumberOfPolls();
                Integer newNumPolls = (previousUsageRecord.getId() != null) ? oldNumPolls + 1 : 1; // If it hasn't been created then its only 1 poll

                if (usageEventEntry.getLastBandwidthBytesIn() != null) {
                    previousUsageRecord.setCumulativeBandwidthBytesIn(UsageCalculator.calculateCumBandwidthBytesIn(previousUsageRecord, usageEventEntry.getLastBandwidthBytesIn()));
                    previousUsageRecord.setLastBandwidthBytesIn(usageEventEntry.getLastBandwidthBytesIn());
                    previousUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastBandwidthBytesOut() != null) {
                    previousUsageRecord.setCumulativeBandwidthBytesOut(UsageCalculator.calculateCumBandwidthBytesOut(previousUsageRecord, usageEventEntry.getLastBandwidthBytesOut()));
                    previousUsageRecord.setLastBandwidthBytesOut(usageEventEntry.getLastBandwidthBytesOut());
                    previousUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastBandwidthBytesInSsl() != null) {
                    previousUsageRecord.setCumulativeBandwidthBytesInSsl(UsageCalculator.calculateCumBandwidthBytesInSsl(previousUsageRecord, usageEventEntry.getLastBandwidthBytesInSsl()));
                    previousUsageRecord.setLastBandwidthBytesInSsl(usageEventEntry.getLastBandwidthBytesInSsl());
                    previousUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastBandwidthBytesOutSsl() != null) {
                    previousUsageRecord.setCumulativeBandwidthBytesOutSsl(UsageCalculator.calculateCumBandwidthBytesOutSsl(previousUsageRecord, usageEventEntry.getLastBandwidthBytesOutSsl()));
                    previousUsageRecord.setLastBandwidthBytesOutSsl(usageEventEntry.getLastBandwidthBytesOutSsl());
                    previousUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastConcurrentConnections() != null) {
                    if(UsageEvent.SSL_ONLY_ON.name().equals(previousUsageRecord.getEventType())) {
                        previousUsageRecord.setAverageConcurrentConnections(0.0);
                    } else {
                        previousUsageRecord.setAverageConcurrentConnections(UsageCalculator.calculateNewAverage(previousUsageRecord.getAverageConcurrentConnections(), oldNumPolls, usageEventEntry.getLastConcurrentConnections()));
                    }
                    previousUsageRecord.setNumberOfPolls(newNumPolls);
                    previousUsageRecord.setEndTime(eventTime);
                }
                if (usageEventEntry.getLastConcurrentConnectionsSsl() != null) {
                    if(UsageEvent.SSL_OFF.name().equals(previousUsageRecord.getEventType())) {
                        previousUsageRecord.setAverageConcurrentConnectionsSsl(0.0);
                    } else {
                        previousUsageRecord.setAverageConcurrentConnectionsSsl(UsageCalculator.calculateNewAverage(previousUsageRecord.getAverageConcurrentConnectionsSsl(), oldNumPolls, usageEventEntry.getLastConcurrentConnectionsSsl()));
                    }
                    previousUsageRecord.setNumberOfPolls(newNumPolls);
                    previousUsageRecord.setEndTime(eventTime);
                }

                if (previousUsageRecord.getId() != null) {
                    usagesToUpdate.add(previousUsageRecord);
                }

                newUsage.setLastBandwidthBytesIn(previousUsageRecord.getLastBandwidthBytesIn());
                newUsage.setLastBandwidthBytesInSsl(previousUsageRecord.getLastBandwidthBytesInSsl());
                newUsage.setLastBandwidthBytesOut(previousUsageRecord.getLastBandwidthBytesOut());
                newUsage.setLastBandwidthBytesOutSsl(previousUsageRecord.getLastBandwidthBytesOutSsl());
            }

            if (newEventUsageMap.containsKey(newUsage.getLoadbalancerId())) {
                newEventUsageMap.get(newUsage.getLoadbalancerId()).add(newUsage);
            } else {
                List<LoadBalancerUsage> recentUsagesForLb = new ArrayList<LoadBalancerUsage>();
                recentUsagesForLb.add(newUsage);
                newEventUsageMap.put(newUsage.getLoadbalancerId(), recentUsagesForLb);
            }
        }

        // Move usages over to array
        for (Integer lbId : newEventUsageMap.keySet()) {
            for (LoadBalancerUsage loadBalancerUsage : newEventUsageMap.get(lbId)) {
                usagesToCreate.add(loadBalancerUsage);
            }
        }

        if (!usagesToCreate.isEmpty()) hourlyUsageRepository.batchCreate(usagesToCreate);
        if (!usagesToUpdate.isEmpty()) hourlyUsageRepository.batchUpdate(usagesToUpdate);

        try {
            BatchAction<LoadBalancerUsageEvent> deleteEventUsagesAction = new BatchAction<LoadBalancerUsageEvent>() {
                public void execute(Collection<LoadBalancerUsageEvent> usageEventEntries) throws Exception {
                    usageEventRepository.batchDelete(usageEventEntries);
                }
            };
            ExecutionUtilities.executeInBatches(usageEventEntries, BATCH_SIZE, deleteEventUsagesAction);
        } catch (Exception e) {
            LOG.error("Exception occurred while deleting usage event entries.", e);
        }

        LOG.info(String.format("%d usage events processed.", usagesToCreate.size()));
    }

    private LoadBalancerUsage getPreviousUsageRecord(LoadBalancerUsageEvent usageEventEntry, Map<Integer, List<LoadBalancerUsage>> newEventUsageMap) {
        // return previous event that has yet to be created
        if (newEventUsageMap.containsKey(usageEventEntry.getLoadbalancerId())) {
            List<LoadBalancerUsage> loadBalancerUsagesForLb = newEventUsageMap.get(usageEventEntry.getLoadbalancerId());
            return (LoadBalancerUsage) loadBalancerUsagesForLb.toArray()[loadBalancerUsagesForLb.size()-1];
        }

        return hourlyUsageRepository.getMostRecentUsageForLoadBalancer(usageEventEntry.getLoadbalancerId());
    }

    private int getTags(Integer accountId, Integer lbId, UsageEvent usageEvent, LoadBalancerUsage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTags());
        } else {
            tags = new BitTags();
        }

        switch (usageEvent) {
            case CREATE_LOADBALANCER:
                tags.flipAllTagsOff();
                break;
            case DELETE_LOADBALANCER:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_OFF:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_ONLY_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_MIXED_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOn(BitTag.SSL_MIXED_MODE);
                break;
            default:
        }

        if (isServiceNetLoadBalancer(accountId, lbId)) {
            tags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return tags.getBitTags();
    }

    private void startUsagePoller() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.LB_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        boolean failed = false;
        List<Host> hosts;
        List<LoadBalancerUsagePollerThread> threads = new ArrayList<LoadBalancerUsagePollerThread>();

        try {
            hosts = hostRepository.getAllActive();
        } catch (Exception ex) {
            LOG.error(ex.getCause(), ex);
            return;
        }

        for (final Host host : hosts) {
            LoadBalancerUsagePollerThread thread = new LoadBalancerUsagePollerThread(loadBalancerRepository, host.getName() + "-poller-thread", host, reverseProxyLoadBalancerAdapter, hostRepository, hourlyUsageRepository);
            threads.add(thread);
            thread.start();
        }

        for (LoadBalancerUsagePollerThread thread : threads) {
            try {
                thread.join();
                LOG.debug(String.format("Load balancer usage poller thread '%s' completed.", thread.getName()));
            } catch (InterruptedException e) {
                LOG.error(String.format("Load balancer usage poller thread interrupted for thread '%s'", thread.getName()), e);
                e.printStackTrace();
                failed = true;
            }
        }

        if (failed) {
            jobStateService.updateJobState(JobName.LB_USAGE_POLLER, JobStateVal.FAILED);
        } else {
            Calendar endTime = Calendar.getInstance();
            Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
            LOG.info(String.format("Usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
            jobStateService.updateJobState(JobName.LB_USAGE_POLLER, JobStateVal.FINISHED);
        }
    }

    private boolean isServiceNetLoadBalancer(Integer accountId, Integer lbId) {
        try {
            final Set<VirtualIp> vipsByAccountIdLoadBalancerId = loadBalancerRepository.getVipsByAccountIdLoadBalancerId(accountId, lbId);

            for (VirtualIp virtualIp : vipsByAccountIdLoadBalancerId) {
                if (virtualIp.getVipType().equals(VirtualIpType.SERVICENET)) return true;
            }

        } catch (EntityNotFoundException e) {
            return false;
        } catch (DeletedStatusException e) {
            return false;
        }

        return false;
    }

}
