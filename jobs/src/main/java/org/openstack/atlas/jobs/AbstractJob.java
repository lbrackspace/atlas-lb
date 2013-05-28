package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.openstack.atlas.util.common.CalendarUtils;
import org.openstack.atlas.util.common.Duration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

public abstract class AbstractJob implements JobInterface {

    @Autowired
    protected JobStateService jobStateService;
    @Autowired
    protected AlertRepository alertRepository;

    @Override
    public void init(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        getLogger().debug(String.format("Initializing %s...", getJobName()));
        setup(jobExecutionContext);
    }

    @Override
    public void execute() throws JobExecutionException {
        JobState masterJob = jobStateService.getByName(JobName.THE_ONE_TO_RULE_THEM_ALL);
        if (masterJob.getState().equals(JobStateVal.GO)) {
            getLogger().debug(String.format("Executing %s...", getJobName()));

            Calendar startTime = Calendar.getInstance();
            getLogger().info(String.format("%s started at %s (Timezone: %s)", getJobName().name(), startTime.getTime(), startTime.getTimeZone().getDisplayName()));
            jobStateService.updateJobState(getJobName(), JobStateVal.IN_PROGRESS);

            try {
                run();
            } catch (Exception e) {
                getLogger().error(String.format("%s failed!", getJobName().name()));
                getLogger().error(e.getCause(), e);
                jobStateService.updateJobState(getJobName(), JobStateVal.FAILED);
                return;
            }

            Calendar endTime = Calendar.getInstance();
            Duration duration = CalendarUtils.calcDuration(startTime, endTime);
            jobStateService.updateJobState(getJobName(), JobStateVal.FINISHED);
            getLogger().info(String.format("%s completed at '%s' (Total Time: %s)", getJobName().name(), endTime.getTime(), duration.toString()));
        }
    }

    @Override
    public void destroy() {
        getLogger().debug(String.format("Destroying %s...", getJobName()));
        cleanup();
    }

    public abstract Log getLogger();

    public abstract JobName getJobName();

    public abstract void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException;

    public abstract void run() throws Exception;

    public abstract void cleanup();
}
