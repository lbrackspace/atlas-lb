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
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class LoadBalancerUsagePoller extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePoller.class);
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    LoadBalancerRepository loadBalancerRepository;
    private HostRepository hostRepository;
    private LoadBalancerUsageRepository usageRepository;
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
    public void setUsageRepository(LoadBalancerUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
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

        List<LoadBalancerUsageEvent> usageEventEntries = usageEventRepository.getAllUsageEventEntries();
        List<LoadBalancerUsage> newUsages = new ArrayList<LoadBalancerUsage>();

        for (LoadBalancerUsageEvent usageEventEntry : usageEventEntries) {
            UsageEvent usageEvent = UsageEvent.valueOf(usageEventEntry.getEventType());
            LoadBalancerUsage recentUsage = usageRepository.getMostRecentUsageForLoadBalancer(usageEventEntry.getLoadbalancerId());
            int updatedTags = getTags(usageEventEntry.getAccountId(), usageEventEntry.getLoadbalancerId(), usageEvent, recentUsage);

            Calendar eventTime;
            if (recentUsage != null && recentUsage.getEndTime().after(usageEventEntry.getStartTime())) {
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
            newUsage.setEventType(usageEventEntry.getEventType()); // TODO: Use cached values from database???

            newUsages.add(newUsage);
        }

        if (!newUsages.isEmpty()) usageRepository.batchCreate(newUsages);

        try {
            BatchAction<LoadBalancerUsageEvent> deleteEventUsagesAction = new BatchAction<LoadBalancerUsageEvent>() {
                public void execute(List<LoadBalancerUsageEvent> usageEventEntries) throws Exception {
                    usageEventRepository.batchDelete(usageEventEntries);
                }
            };
            ExecutionUtilities.executeInBatches(usageEventEntries, BATCH_SIZE, deleteEventUsagesAction);
        } catch (Exception e) {
            LOG.error("Exception occurred while deleting usage event entries.", e);
        }

        LOG.info(String.format("%d usage events processed.", newUsages.size()));
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
                // Leave tags as they were
                break;
            case SSL_OFF:
                tags.flipTagOff(BitTag.SSL);
                break;
            case SSL_ON:
                tags.flipTagOn(BitTag.SSL);
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
            LoadBalancerUsagePollerThread thread = new LoadBalancerUsagePollerThread(loadBalancerRepository, host.getName() + "-poller-thread", host, reverseProxyLoadBalancerAdapter, hostRepository, usageRepository);
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
