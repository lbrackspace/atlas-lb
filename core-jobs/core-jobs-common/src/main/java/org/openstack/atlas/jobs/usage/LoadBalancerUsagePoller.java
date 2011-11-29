package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.jobs.JobInterface;
import org.openstack.atlas.jobs.batch.BatchAction;
import org.openstack.atlas.jobs.batch.BatchExecutor;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.UsageEventRecord;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.UsageEventRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class LoadBalancerUsagePoller implements JobInterface {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePoller.class);
    private final int BATCH_SIZE = 100;

    @Autowired
    LoadBalancerUsagePollerThread hostThread;
    @Autowired
    UsageAdapter usageAdapter;
    @Autowired
    HostRepository hostRepository;
    @Autowired
    UsageRepository usageRepository;
    @Autowired
    UsageEventRepository usageEventRepository;

    @Override
    public void init(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.debug("INIT");
    }

    @Override
    public void execute() throws JobExecutionException {
        LOG.debug("EXECUTE");
        processUsageEvents();
        startUsagePoller();
    }

    @Override
    public void destroy() {
        LOG.debug("DESTROY");
    }

    private void processUsageEvents() {
        LOG.info("Processing usage events...");

        List<UsageEventRecord> usageEventRecords = usageEventRepository.getAllUsageEventEntries();
        List<UsageRecord> newUsageRecords = new ArrayList<UsageRecord>();

        for (UsageEventRecord usageEventRecord : usageEventRecords) {
            UsageRecord recentUsage = usageRepository.getMostRecentUsageForLoadBalancer(usageEventRecord.getLoadBalancer().getId());

            Calendar eventTime;
            if (recentUsage != null && recentUsage.getEndTime().after(usageEventRecord.getStartTime())) {
                // TODO: Find a better way to process events when this case happens as this is incorrect.
                eventTime = Calendar.getInstance();
            } else {
                eventTime = usageEventRecord.getStartTime();
            }

            UsageRecord newUsageRecord = new UsageRecord();
            newUsageRecord.setLoadBalancer(usageEventRecord.getLoadBalancer());
            newUsageRecord.setEvent(usageEventRecord.getEvent());
            newUsageRecord.setTransferBytesIn(0l);
            newUsageRecord.setTransferBytesOut(0l);
            newUsageRecord.setLastBytesInCount(0l);
            newUsageRecord.setLastBytesOutCount(0l);
            newUsageRecord.setStartTime(eventTime);
            newUsageRecord.setEndTime(eventTime);

            newUsageRecords.add(newUsageRecord);
        }

        if (!newUsageRecords.isEmpty()) usageRepository.batchCreate(newUsageRecords);

        try {
            BatchAction<UsageEventRecord> deleteEventUsagesAction = new BatchAction<UsageEventRecord>() {
                public void execute(List<UsageEventRecord> usageEventRecords) throws Exception {
                    usageEventRepository.batchDelete(usageEventRecords);
                }
            };
            BatchExecutor.executeInBatches(usageEventRecords, BATCH_SIZE, deleteEventUsagesAction);
        } catch (Exception e) {
            LOG.error("Exception occurred while deleting usage event entries.", e);
        }

        LOG.info(String.format("%d usage events processed.", newUsageRecords.size()));
    }

    private void startUsagePoller() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));

        List<Host> hosts;
        List<LoadBalancerUsagePollerThread> threads = new ArrayList<LoadBalancerUsagePollerThread>();

        try {
            hosts = hostRepository.getActiveHosts();
        } catch (Exception ex) {
            LOG.error(ex.getCause(), ex);
            return;
        }

        for (final Host host : hosts) {
            final LoadBalancerUsagePollerThread thread = createHostThread(host);
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
            }
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    protected LoadBalancerUsagePollerThread createHostThread(Host host) {
        try {
            final LoadBalancerUsagePollerThread thread = hostThread.getClass().newInstance();
            thread.setName(host.getName() + "-poller-thread");
            thread.setHost(host);
            thread.setHostRepository(hostRepository);
            thread.setUsageRepository(usageRepository);
            thread.setUsageAdapter(usageAdapter);
            return thread;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}