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
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.usage.execution.UsageAtomHopperExecution;
import org.openstack.atlas.usage.execution.UsageAtomHopperRetryExecution;
import org.openstack.atlas.usagerefactor.UsageRollupProcessor;
import org.openstack.atlas.util.common.CalendarUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

public class LoadBalancerUsageRollupJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsageRollupJob.class);

    // Shared Dependencies
    private UsageRepository usageRepository;
    private LoadBalancerMergedHostUsageRepository lbMergedHostUsageRepository;

    // Rollup Dependencies
    private UsageRollupProcessor usageRollupProcessor;
    private LoadBalancerService loadBalancerService;

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
    public void setUsageRollupProcessor(UsageRollupProcessor usageRollupProcessor) {
        this.usageRollupProcessor = usageRollupProcessor;
    }

    @Required
    public void setLoadBalancerMergedHostUsageRepository(LoadBalancerMergedHostUsageRepository lbMergedHostUsageRepository) {
        this.lbMergedHostUsageRepository = lbMergedHostUsageRepository;
    }

    @Required
    public void setLoadBalancerService(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
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

        try {
            atomHopperUsageJobExecution.execute();
            if (Boolean.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_run_failed_entries))) {
                atomHopperUsageJobRetryExecution.execute();
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    private void rollupUsage() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Usage rollup job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.IN_PROGRESS);

        try {
            Calendar hourToStop = getHourToStop();
            Calendar hourToRollup = null;
            Calendar rollupMarker = null;

            while (hourToRollup == null || hourToRollup.before(hourToStop)) {
                try {
                    hourToRollup = getHourToRollup();
                    rollupMarker = CalendarUtils.copy(hourToRollup);
                    rollupMarker.add(Calendar.HOUR, 1);
                } catch (ParseException pe) {
                    LOG.error("Usage rollup job failed! Unable to parse inputPath which stores the last successful rollup hour.", pe);
                    jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.FAILED);
                    return;
                }

                LOG.info(String.format("Retrieving usage entries to process from polling DB for hour '%s'...", hourToRollup.getTime().toString()));
                List<LoadBalancerMergedHostUsage> pollingUsages = lbMergedHostUsageRepository.getAllUsageRecordsInOrderBeforeTime(rollupMarker);

                LOG.info(String.format("Processing usage entries for hour '%s'...", hourToRollup.getTime().toString()));
                List<Usage> usagesToInsert = usageRollupProcessor.processRecords(pollingUsages, hourToRollup);

                if (!usagesToInsert.isEmpty()) {
                    usageRepository.batchCreate(usagesToInsert);
                }

                String lastSuccessfulHourProcessed = CalendarUtils.calendarToString(hourToRollup);
                jobStateService.updateJobState(JobName.LB_USAGE_ROLLUP, JobStateVal.IN_PROGRESS, lastSuccessfulHourProcessed);

                LOG.info(String.format("Deleting polling usage entries before hour '%s'...", hourToRollup.getTime().toString()));
                lbMergedHostUsageRepository.deleteAllRecordsBefore(hourToRollup);
            }
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

    private Calendar getHourToRollup() throws ParseException {
        Calendar hourToProcess;

        try {
            JobState jobState = jobStateService.getByName(JobName.LB_USAGE_ROLLUP);
            String lastSuccessfulHourProcessed = jobState.getInputPath();
            hourToProcess = CalendarUtils.stringToCalendar(lastSuccessfulHourProcessed);
            hourToProcess.add(Calendar.HOUR, 1);
        } catch (NullPointerException npe) {
            hourToProcess = Calendar.getInstance();
            hourToProcess.add(Calendar.HOUR, -1);
        }

        hourToProcess = CalendarUtils.stripOutMinsAndSecs(hourToProcess);
        return hourToProcess;
    }

    private Calendar getHourToStop() {
        Calendar hourToStop = Calendar.getInstance();
        hourToStop = CalendarUtils.stripOutMinsAndSecs(hourToStop);
        return hourToStop;
    }

    private void addSuspendedUsageEvents() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Usage rollup job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.SUSPENDED_LB_JOB, JobStateVal.IN_PROGRESS);

        try {
            final Calendar now = Calendar.getInstance();
            List<LoadBalancer> suspendedLoadBalancers = loadBalancerRepository.getLoadBalancersWithStatus(LoadBalancerStatus.SUSPENDED);

            for (LoadBalancer suspendedLoadBalancer : suspendedLoadBalancers) {
                BitTags tags = loadBalancerService.getCurrentBitTags(suspendedLoadBalancer.getId(), suspendedLoadBalancer.getAccountId());
                LoadBalancerMergedHostUsage newSuspendedEvent = new LoadBalancerMergedHostUsage(suspendedLoadBalancer.getAccountId(), suspendedLoadBalancer.getId(), 0l, 0l, 0l, 0l, 0, 0, suspendedLoadBalancer.getLoadBalancerJoinVipSet().size(), tags.getBitTags(), now, UsageEvent.SUSPENDED_LOADBALANCER);
                LOG.debug(String.format("Adding suspended usage event for load balancer '%d'...", suspendedLoadBalancer.getId()));
                lbMergedHostUsageRepository.create(newSuspendedEvent);
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
}
