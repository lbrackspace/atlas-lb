package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.usage.execution.UsageAtomHopperExecution;
import org.openstack.atlas.usage.execution.UsageAtomHopperRetryExecution;
import org.openstack.atlas.usage.logic.UsageRollupProcessor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoadBalancerUsageRollupJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsageRollupJob.class);

    // Shared Dependencies
    private UsageRepository usageRepository;

    // Rollup Dependencies
    private LoadBalancerUsageRepository pollingUsageRepository;

    // Suspended Load Balancer Dependencies
    private LoadBalancerUsageEventRepository usageEventRepository;

    // Atom Hopper Pusher Dependencies
    private UsageAtomHopperExecution atomHopperUsageJobExecution;
    private UsageAtomHopperRetryExecution atomHopperUsageJobRetryExecution;
    private Configuration configuration = new AtomHopperConfiguration();
    private LoadBalancerRepository loadBalancerRepository;


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
    public void setAtomHopperUsageJobExecution(UsageAtomHopperExecution atomHopperUsageJobExecution) {
        this.atomHopperUsageJobExecution = atomHopperUsageJobExecution;
    }

    @Required
    public void setAtomHopperUsageJobRetryExecution(UsageAtomHopperRetryExecution atomHopperUsageJobRetryExecution) {
        this.atomHopperUsageJobRetryExecution = atomHopperUsageJobRetryExecution;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        rollupUsage();
        addSuspendedUsageEvents();
//        pushUsageToAtomHopper();
        try {
            atomHopperUsageJobExecution.execute();
            if (Boolean.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_run_failed_entries))) {
                atomHopperUsageJobRetryExecution.execute();
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }

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
            pollingUsageRepository.deleteAllRecordsBeforeOrEqualTo(rollupTimeMarker);
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

    @Deprecated
    private void pushUsageToAtomHopper() {
//        Calendar startTime = Calendar.getInstance();
//        LOG.info(String.format("Atom hopper load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
//        jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.IN_PROGRESS);
//
//        if (configuration.getString(AtomHopperConfigurationKeys.allow_ahusl).equals("true")) {
//            try {
//                authToken = retrieveAndProcessAuthToken();
//                if (authToken != null) {
////                    threadPoolMonitorService = AHUSLServiceUtil.startThreadMonitor(taskExecutor, threadPoolMonitorService);
////                    taskExecutor = AHUSLServiceUtil.startThreadExecutor(taskExecutor, threadPoolExecutorService, corePoolSize, maxPoolSize, keepAliveTime);
//
//                    //Process usage rows, send batched rows to tasks@loadBalancerAHUSLTask
//                    LOG.debug("Setting up the client...");
//                    ahclient = new AtomHopperClientImpl();
//                    int totalUsageRowsToSend = 0;
//                    List<Usage> lbusages = loadBalancerRepository.getAllUsageNeedsPushed(AtomHopperUtil.getStartCal(), AtomHopperUtil.getNow());
//
//                    List<Usage> processUsage;
//                    if (!lbusages.isEmpty()) {
//                        int taskCounter = 0;
//                        while (totalUsageRowsToSend < lbusages.size()) {
//                            processUsage = new ArrayList<Usage>();
//                            LOG.debug("Processing usage into tasks.. task: " + taskCounter);
//                            for (int i = 0; i < nTasks; i++) {
//                                if (totalUsageRowsToSend <= lbusages.size() - 1) {
//                                    processUsage.add(lbusages.get(totalUsageRowsToSend));
//                                    totalUsageRowsToSend++;
//
//                                } else {
//                                    break;
//                                }
//                            }
//                            taskCounter++;
////                            taskExecutor.execute(new AtomHopperLBTask(processUsage, ahclient, authToken, usageRepository));
//                        }
//                    } else {
//                        LOG.debug("No usage found for processing at this time...");
//                    }
//
////                    AHUSLServiceUtil.shutDownAHUSLServices(taskExecutor, threadPoolMonitorService, ahclient);
//                } else {
//                    LOG.error("Could not retrieve authentication token, no requests are being processed, please notify operations... ::AUTH FAILED ALERT::");
//                }
//            } catch (Throwable t) {
//                LOG.error(String.format("Exception: %s\n", AtomHopperUtil.getExtendedStackTrace(t)));
//                Calendar endTime = Calendar.getInstance();
//                Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
//                jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FAILED);
//                LOG.info(String.format("Atom hopper load balancer usage poller job failed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
////                AHUSLServiceUtil.shutDownAHUSLServices(taskExecutor, threadPoolMonitorService, ahclient);
//                return;
//
//            }
//
//            Calendar endTime = Calendar.getInstance();
//            Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
//            jobStateService.updateJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FINISHED);
//            LOG.info(String.format("Atom hopper load balancer usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
//        }
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

//    private String retrieveAndProcessAuthToken() {
//        try {
//            IdentityAuthClient ahuslAuthentication = new IdentityClientImpl();
//            authToken = ahuslAuthentication.getAuthResponse().getToken().getId();
//            if (authToken != null) {
//                LOG.info("Successfully retrieved token for Admin user: " + configuration.getString(AuthenticationCredentialConfigurationKeys.identity_user));
//                return authToken;
//            }
//        } catch (Exception e) {
//            LOG.error("Could not retrieve authentication token, no requests are being processed, please notify operations... ::AUTH FAILED ALERT::");
//            throw new AtomHopperUSLJobExecutionException("Error retrieving auth token: " + e);
//        }
//        return null;
//    }
}
