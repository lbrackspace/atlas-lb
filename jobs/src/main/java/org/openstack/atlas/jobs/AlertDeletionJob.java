package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;

public class AlertDeletionJob extends Job {
    private final Log LOG = LogFactory.getLog(AlertDeletionJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom alert deletion job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.ALERT_DELETION_JOB, JobStateVal.IN_PROGRESS);

        try {
            LOG.info(String.format("Attempting to remove alerts from the database... "));
            alertRepository.removeAlertEntries();
        } catch (Exception e) {
            jobStateService.updateJobState(JobName.ALERT_DELETION_JOB, JobStateVal.FAILED);
            LOG.error(String.format("Alert deletion job failed while removing one of the alert entries: %s", e.getMessage()));
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.ALERT_DELETION_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Atom alert deletion job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
