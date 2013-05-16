package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class AlertDeletionJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(AlertDeletionJob.class);

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.ALERT_DELETION_JOB;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        try {
            LOG.info(String.format("Attempting to remove alerts from the database..."));
            alertRepository.removeAlertEntries();
            LOG.info(String.format("Successfully removed alerts from the database."));
        } catch (Exception e) {
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            throw e;
        }
    }

    @Override
    public void cleanup() {
    }

}
