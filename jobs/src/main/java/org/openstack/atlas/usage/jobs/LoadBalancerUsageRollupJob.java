package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.handler.RejectedExecutionHandler;
import org.openstack.atlas.atom.service.ThreadPoolExecutorService;
import org.openstack.atlas.atom.service.ThreadPoolMonitorService;
import org.openstack.atlas.atom.tasks.LoadBalancerAHUSLTask;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.logic.UsageRollupProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.PersistenceException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadBalancerUsageRollupJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsageRollupJob.class);

    // Shared Dependencies
    private UsageRepository usageRepository;

    // Rollup Dependencies
    private LoadBalancerUsageRepository pollingUsageRepository;

    // Atom Hopper Pusher Dependencies
    private Configuration configuration = new AtomHopperConfiguration();
    private ThreadPoolMonitorService threadPoolMonitorService;
    private ThreadPoolExecutorService threadPoolExecutorService;
    private LoadBalancerRepository loadBalancerRepository;
    private long nTasks = Long.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_task_count));
    private int maxPoolSize = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_max_size));
    private int corePoolSize = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_core_size));
    private long keepAliveTime = Long.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_conn_timeout));

    private AHUSLClient client;

    @Required
    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Required
    public void setPollingUsageRepository(LoadBalancerUsageRepository pollingUsageRepository) {
        this.pollingUsageRepository = pollingUsageRepository;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setThreadPoolMonitorService(ThreadPoolMonitorService threadPoolMonitorService) {
        this.threadPoolMonitorService = threadPoolMonitorService;
    }

    @Required
    public void setThreadPoolExecutorService(ThreadPoolExecutorService threadPoolExecutorService) {
        this.threadPoolExecutorService = threadPoolExecutorService;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        rollupUsage();
        pushUsageToAtomHopper();
    }

    private void rollupUsage() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Usage rollup job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.IN_PROGRESS);

        try {
            // Leaves at least one hour of data in the polling database. Ensures bitmask/numVips gets copied over and all events are accounted for.
            Calendar rollupTimeMarker = Calendar.getInstance();
            rollupTimeMarker.add(Calendar.HOUR_OF_DAY, -1);
            rollupTimeMarker.set(Calendar.MINUTE, 0);
            rollupTimeMarker.set(Calendar.SECOND, 0);
            rollupTimeMarker.set(Calendar.MILLISECOND, 0);

            LOG.info("Retrieving usage entries to process from polling DB...");
            List<LoadBalancerUsage> pollingUsages = pollingUsageRepository.getAllRecordsBeforeTimeInOrder(rollupTimeMarker);

            LOG.info("Processing usage entries...");
            UsageRollupProcessor usagesForDatabase = new UsageRollupProcessor(pollingUsages, this.usageRepository).process();

            int retries = 3;
            while (retries > 0) {
                if (!usagesForDatabase.getUsagesToUpdate().isEmpty()) {
                    try {
                        this.usageRepository.batchUpdate(usagesForDatabase.getUsagesToUpdate());
                        retries = 0;
                    } catch (PersistenceException e) {
                        LOG.warn("Deleted load balancer(s) detected! Finding and removing from batch...", e);
                        deleteBadEntries(usagesForDatabase.getUsagesToUpdate());
                        retries--;
                        LOG.warn(String.format("%d retries left.", retries));
                    }
                } else {
                    break;
                }
            }

            retries = 3;
            while (retries > 0) {
                if (!usagesForDatabase.getUsagesToCreate().isEmpty()) {
                    try {
                        this.usageRepository.batchCreate(usagesForDatabase.getUsagesToCreate());
                        retries = 0;
                    } catch (PersistenceException e) {
                        LOG.warn("Deleted load balancer(s) detected! Finding and removing from batch...", e);
                        deleteBadEntries(usagesForDatabase.getUsagesToCreate());
                        retries--;
                        LOG.warn(String.format("%d retries left.", retries));
                    }
                } else {
                    break;
                }
            }
            LOG.info("Deleting processed usage entries...");
            pollingUsageRepository.deleteAllRecordsBefore(rollupTimeMarker);
        } catch (Exception e) {
            LOG.error("Usage rollup job failed!", e);
            jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.FAILED);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.FINISHED);
        LOG.info(String.format("Usage rollup job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void pushUsageToAtomHopper() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        if (configuration.getString(AtomHopperConfigurationKeys.allow_ahusl).equals("true")) {

            LOG.debug("Setting up the threadPoolExecutor with " + maxPoolSize + " pools");
            ThreadPoolExecutor taskExecutor = threadPoolExecutorService.createNewThreadPool(corePoolSize, maxPoolSize, keepAliveTime, 1000, new RejectedExecutionHandler());

            // ThreadPoolMonitorService is started...
            threadPoolMonitorService.setExecutor(taskExecutor);
            Thread monitor = new Thread(threadPoolMonitorService);
            monitor.start();

            try {
                LOG.debug("Setting up the client...");
                client = new AHUSLClient();

                int totalUsageRowsToSend = 0;
                    List<Usage> lbusages = loadBalancerRepository.getAllUsageNeedsPushed(AHUSLUtil.getStartCal(), AHUSLUtil.getNow());

                    List<Usage> processUsage;
                    if (!lbusages.isEmpty()) {
                        int taskCounter = 0;
                        while (totalUsageRowsToSend < lbusages.size()) {
                            processUsage = new ArrayList<Usage>();

                            LOG.debug("Processing usage into tasks.. task: " + taskCounter);
                            for (int i = 0; i < nTasks; i++) {
                                if (totalUsageRowsToSend <= lbusages.size() - 1) {
                                    processUsage.add(lbusages.get(totalUsageRowsToSend));
                                    totalUsageRowsToSend++;

                                } else {
                                    break;
                                }
                            }

                            taskCounter++;

                            taskExecutor.execute(new LoadBalancerAHUSLTask(processUsage, client, usageRepository)); //TODO: Need to move repository deps...
                        }
                    } else {
                        LOG.debug("No usage found for processing at this time...");
                    }

                } catch (Throwable t) {
                    System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
                    LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
                }

            try {
                LOG.debug("Shutting down the thread pool and monitors..");
                taskExecutor.shutdown();
                taskExecutor.awaitTermination(300, TimeUnit.SECONDS);
                threadPoolMonitorService.shutDown();
            } catch (InterruptedException e) {
                LOG.error("There was an error shutting down threadPool: " + AHUSLUtil.getStackTrace(e));
            }

            LOG.debug("Destroying the client");
            client.destroy();
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper load balancer usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void deleteBadEntries(List<Usage> usagesWithBadEntries) {
        List<Integer> loadBalancerIdsWithBadId = new ArrayList<Integer>();
        for (Usage usage : usagesWithBadEntries) {
            loadBalancerIdsWithBadId.add(usage.getLoadbalancer().getId());
        }

        List<Integer> loadBalancersFromDatabase = this.usageRepository.getLoadBalancerIdsIn(loadBalancerIdsWithBadId);
        loadBalancerIdsWithBadId.removeAll(loadBalancersFromDatabase); // Remove valid ids from list

        for (Integer loadBalancerId : loadBalancerIdsWithBadId) {
            pollingUsageRepository.deleteAllRecordsForLoadBalancer(loadBalancerId);
        }

        List<Usage> usageItemsToDelete = new ArrayList<Usage>();

        for (Usage usageItem : usagesWithBadEntries) {
            if (loadBalancerIdsWithBadId.contains(usageItem.getLoadbalancer().getId())) {
                usageItemsToDelete.add(usageItem);
            }
        }

        usagesWithBadEntries.removeAll(usageItemsToDelete);
    }
}
