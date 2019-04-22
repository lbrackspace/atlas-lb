package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.LbIdAccountId;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;
import org.openstack.atlas.usagerefactor.UsageRollupProcessor;
import org.openstack.atlas.util.common.CalendarUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class LoadBalancerUsageRollupJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsageRollupJob.class);
    private final int BATCH_SIZE = 1000;

    @Autowired
    private UsageRepository usageRepository;
    @Autowired
    private LoadBalancerMergedHostUsageRepository lbMergedHostUsageRepository;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private UsageRollupProcessor usageRollupProcessor;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.LB_USAGE_ROLLUP;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        if (shouldRollup()) {//check if lbUsagePollerJob ran successfully at least once after the start of the current hour.
            rollupUsage();
        } else {
            throw new Exception("Warning! We are currently not rolling up usage! Something may be wrong with the usage poller!");
        }
    }

    @Override
    public void cleanup() {
    }

    protected boolean shouldRollup() {
        boolean rollup = false;
        final JobState lbUsagePollerJobState = jobStateService.getByName(JobName.LB_USAGE_POLLER);
        Calendar thisHour = Calendar.getInstance();
        thisHour = CalendarUtils.stripOutMinsAndSecs(thisHour);

        if (lbUsagePollerJobState.getEndTime().after(thisHour) && !lbUsagePollerJobState.getState().equals(JobStateVal.FAILED)) {
            rollup = true;
        }

        return rollup;
    }

    /**
     * Rollup the usage that came after the last hour processed.
     * In case if there was a failure of the rollup job, this will process all the records from the hour the poller failed.
     * If poller's lastHourProcessed is 1pm and the current time is 4:16 pm then the usage is rolled up for 2 to 3 and 3 to 4.
     *
     * @throws Exception
     */
    private void rollupUsage() throws Exception {
        Calendar hourToStop = getHourToStop();//current time with stripOutMinsAndSecs, if the current time is 4:16 pm then hourToStop=4pm
        Calendar hourToRollup = null;//hour to start the rollup
        Calendar rollupMarker = null;//marker till when the records should be rolledup: (hourToRollup+One Hour)

        while (hourToRollup == null || hourToRollup.before(hourToStop)) {//while loop to rollup for multiple hours if needed.
            try {
                hourToRollup = getHourToRollup(hourToStop);//lastHourProcessed + 1 hour, this is the increment to repeat the while loop
                if (hourToRollup == null) return;
                rollupMarker = CalendarUtils.copy(hourToRollup);
                rollupMarker.add(Calendar.HOUR, 1);
            } catch (ParseException pe) {
                LOG.error("Usage rollup job failed! Unable to parse inputPath which stores the last successful rollup hour.", pe);
                throw pe;
            }
            LOG.info(String.format("Finding loadbalancers that were active for hour '%s'...", hourToRollup.getTime().toString()));
            Set<LbIdAccountId> loadBalancersActiveDuringPeriod = loadBalancerRepository.getLoadBalancersActiveDuringPeriod(hourToRollup, rollupMarker);
            LOG.info(String.format("%d loadbalancers were active for hour '%s'.", loadBalancersActiveDuringPeriod.size(), hourToRollup.getTime().toString()));

            LOG.info(String.format("Retrieving usage entries to process from polling DB for hour '%s'...", hourToRollup.getTime().toString()));
            List<LoadBalancerMergedHostUsage> pollingUsages = lbMergedHostUsageRepository.getAllUsageRecordsInOrderBeforeOrEqualToTime(rollupMarker);

            LOG.info(String.format("Processing usage entries for hour '%s'...", hourToRollup.getTime().toString()));
            List<Usage> usagesToInsert = usageRollupProcessor.processRecords(pollingUsages, hourToRollup, loadBalancersActiveDuringPeriod);

            if (!usagesToInsert.isEmpty()) {
                BatchAction<Usage> usageInsertBatchAction = new BatchAction<Usage>() {
                    @Override
                    public void execute(Collection<Usage> usagesToInsert) throws Exception {
                        LOG.info(String.format("Inserting %d new records into lb_usage table...", usagesToInsert.size()));
                        usageRepository.batchCreate(usagesToInsert);
                        LOG.info(String.format("Inserted %d new records into lb_usage table.", usagesToInsert.size()));
                    }
                };
                ExecutionUtilities.ExecuteInBatches(usagesToInsert, BATCH_SIZE, usageInsertBatchAction);
            }

            //update lastSuccessfulHourProcessed for the LB_USAGE_ROLLUP job so that the next run will know from when to process the records
            String lastSuccessfulHourProcessed = CalendarUtils.calendarToString(hourToRollup);
            jobStateService.updateInputPath(JobName.LB_USAGE_ROLLUP, lastSuccessfulHourProcessed);

            LOG.info(String.format("Deleting polling usage entries before hour '%s'...", hourToRollup.getTime().toString()));
            lbMergedHostUsageRepository.deleteAllRecordsBefore(hourToRollup);//records before the hourToRollup are deleted not the records that were just processed.
        }
    }

    protected Calendar getHourToStop() {
        Calendar hourToStop = Calendar.getInstance();
        hourToStop = CalendarUtils.stripOutMinsAndSecs(hourToStop);
        return hourToStop;
    }

    /**
     * Calculates the next hour to be rolled up.
     *
     * @param hourToStop when to stop the entire rollup process, this will be the top of the current hour.
     * @return
     * @throws ParseException
     */
    protected Calendar getHourToRollup(Calendar hourToStop) throws ParseException {
        Calendar hourToRollup;

        try {
            JobState jobState = jobStateService.getByName(JobName.LB_USAGE_ROLLUP);
            String lastHourProcessedString = jobState.getInputPath();
            hourToRollup = CalendarUtils.stringToCalendar(lastHourProcessedString);
            hourToRollup.add(Calendar.HOUR, 1);
            hourToRollup = CalendarUtils.stripOutMinsAndSecs(hourToRollup);

            if (hourToRollup.compareTo(hourToStop) >= 0) {
                return null;
            }
        } catch (NullPointerException npe) {
            hourToRollup = Calendar.getInstance();
            hourToRollup.add(Calendar.HOUR, -1);
            hourToRollup = CalendarUtils.stripOutMinsAndSecs(hourToRollup);
        }

        return hourToRollup;
    }

}
