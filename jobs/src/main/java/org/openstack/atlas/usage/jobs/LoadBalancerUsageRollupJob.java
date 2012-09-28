package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.auth.AHUSLAuthentication;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.exception.AtomHopperUSLJobExecutionException;
import org.openstack.atlas.atom.handler.RejectedExecutionHandler;
import org.openstack.atlas.atom.service.ThreadPoolExecutorService;
import org.openstack.atlas.atom.service.ThreadPoolMonitorService;
import org.openstack.atlas.atom.tasks.LoadBalancerAHUSLTask;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.logic.UsageRollupProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LoadBalancerUsageRollupJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsageRollupJob.class);

    // Shared Dependencies
    private UsageRepository usageRepository;

    // Rollup Dependencies
    private LoadBalancerUsageRepository pollingUsageRepository;

    // Suspended Load Balancer Dependencies
    private LoadBalancerUsageEventRepository usageEventRepository;

    // Atom Hopper Pusher Dependencies
    private Configuration configuration = new AtomHopperConfiguration();
    private ThreadPoolMonitorService threadPoolMonitorService;
    private ThreadPoolExecutorService threadPoolExecutorService;
    private LoadBalancerRepository loadBalancerRepository;
    private String authToken;
    private ThreadPoolExecutor taskExecutor;
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
    public void setUsageEventRepository(LoadBalancerUsageEventRepository usageEventRepository) {
        this.usageEventRepository = usageEventRepository;
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
        addSuspendedUsageEvents();
        rollupUsage();
        pushUsageToAtomHopper();
    }

    private void addSuspendedUsageEvents() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Usage rollup job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.IN_PROGRESS);

        try {
            final Calendar now = Calendar.getInstance();
            List<LoadBalancer> suspendedLoadBalancers = loadBalancerRepository.getLoadBalancersWithStatus(LoadBalancerStatus.SUSPENDED);

            for (LoadBalancer suspendedLoadBalancer : suspendedLoadBalancers) {
                LoadBalancerUsageEvent newSuspendedEvent = new LoadBalancerUsageEvent(suspendedLoadBalancer.getAccountId(), suspendedLoadBalancer.getId(), now, suspendedLoadBalancer.getLoadBalancerJoinVipSet().size(), UsageEvent.SUSPENDED_LOADBALANCER.name(), null, null, null, null, null, null);
                LOG.debug(String.format("Adding suspended usage event for load balancer '%d'...", suspendedLoadBalancer.getId()));
                usageEventRepository.create(newSuspendedEvent);
            }
        } catch (Exception e) {
            LOG.error("Suspended load balancer job failed!", e);
            jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.FAILED);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Suspended load balancer job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
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
            try {
                authToken = retrieveAndProcessAuthToken();
                if (authToken != null) {
                    initiateThreadMonitorAndTaskExecutors();

                    //Process usage rows, send batched rows to tasks@loadBalancerAHUSLTask

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

                            taskExecutor.execute(new LoadBalancerAHUSLTask(processUsage, client, authToken, usageRepository)); //TODO: Need to move repository deps...
                        }
                    } else {
                        LOG.debug("No usage found for processing at this time...");
                    }

                    destroyThreadMonitorAndTaskExecutors();

                    LOG.debug("Destroying the rest client");
                    client.destroy();
                    LOG.debug("Successfully destroyed the rest client");

                } else {
                    LOG.error("Could not retrieve authentication token, no requests are being processed, please notify operations... ::AUTH FAILED ALERT::");
                }
            } catch (Throwable t) {
                LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
                Calendar endTime = Calendar.getInstance();
                Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
                jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FAILED);
                LOG.info(String.format("Atom hopper load balancer usage poller job failed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
                return;

            }

            Calendar endTime = Calendar.getInstance();
            Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
            jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FINISHED);
            LOG.info(String.format("Atom hopper load balancer usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
        }
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

    private String retrieveAndProcessAuthToken() {
        try {
            String userName = configuration.getString(AtomHopperConfigurationKeys.ahusl_auth_username);
            AHUSLAuthentication ahuslAuthentication = new AHUSLAuthentication();
            authToken = ahuslAuthentication.getToken(userName).getToken().getId();
            if (authToken != null) {
                LOG.info("Token successfully retrieved: " + authToken + " For User: " + userName);
                return authToken;
            }
        } catch (Exception e) {
            LOG.error("Could not retrieve authentication token, no requests are being processed, please notify operations... ::AUTH FAILED ALERT::");
            throw new AtomHopperUSLJobExecutionException("Error retrieving auth token: " + e);
        }
        return null;
    }

    private void initiateThreadMonitorAndTaskExecutors() {
        try {
            LOG.debug("Setting up the threadPoolExecutor with " + maxPoolSize + " pools");
            taskExecutor = threadPoolExecutorService.createNewThreadPool(corePoolSize, maxPoolSize, keepAliveTime, 1000, new RejectedExecutionHandler());
            threadPoolMonitorService.setExecutor(taskExecutor);
            Thread monitor = new Thread(threadPoolMonitorService);
            monitor.start();
        } catch (Exception e) {
            LOG.error("There was an error initiating thread monitors and task executors: " + e);
            throw new AtomHopperUSLJobExecutionException("There was an error initiating thread monitors and task executors: " + e);

        }
    }

    private void destroyThreadMonitorAndTaskExecutors() {
        try {
            LOG.debug("Shutting down the thread pool and monitors..");
            taskExecutor.shutdown();
            taskExecutor.awaitTermination(300, TimeUnit.SECONDS);
            threadPoolMonitorService.shutDown();
        } catch (InterruptedException e) {
            LOG.error("There was an error shutting down threadPool: " + AHUSLUtil.getStackTrace(e));
            throw new AtomHopperUSLJobExecutionException("There was an error destroying thread monitors and task executors: " + e);
        }
    }
}
