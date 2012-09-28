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
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;
import org.openstack.atlas.usage.logic.UsageCalculator;
import org.openstack.atlas.usage.logic.UsageEventProcessor;
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
    private UsageRepository rollupUsageRepository;
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
    public void setRollupUsageRepository(UsageRepository rollupUsageRepository) {
        this.rollupUsageRepository = rollupUsageRepository;
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
        List<LoadBalancerUsageEvent> usageEventEntries = usageEventRepository.getAllUsageEventEntriesInOrder();
        UsageEventProcessor usageEventProcessor = new UsageEventProcessor(usageEventEntries, hourlyUsageRepository, rollupUsageRepository, loadBalancerRepository);

        usageEventProcessor.process();

        List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();
        List<LoadBalancerUsage> usagesToUpdate = usageEventProcessor.getUsagesToUpdate();

        if (!usagesToUpdate.isEmpty()) hourlyUsageRepository.batchUpdate(usagesToUpdate);
        LOG.info(String.format("%d records updated.", usagesToUpdate.size()));
        
        if (!usagesToCreate.isEmpty()) hourlyUsageRepository.batchCreate(usagesToCreate);
        LOG.info(String.format("%d records created.", usagesToCreate.size()));

        try {
            BatchAction<LoadBalancerUsageEvent> deleteEventUsagesAction = new BatchAction<LoadBalancerUsageEvent>() {
                public void execute(Collection<LoadBalancerUsageEvent> usageEventEntries) throws Exception {
                    usageEventRepository.batchDelete(usageEventEntries);
                }
            };
            ExecutionUtilities.executeInBatches(usageEventEntries, BATCH_SIZE, deleteEventUsagesAction);
            LOG.info(String.format("%d records deleted.", usageEventEntries.size()));
        } catch (Exception e) {
            LOG.error("Exception occurred while deleting usage event entries.", e);
        }
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
            LoadBalancerUsagePollerThread thread = new LoadBalancerUsagePollerThread(loadBalancerRepository, host.getName() + "-poller-thread", host, reverseProxyLoadBalancerAdapter, hostRepository, hourlyUsageRepository, rollupUsageRepository);
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

}
