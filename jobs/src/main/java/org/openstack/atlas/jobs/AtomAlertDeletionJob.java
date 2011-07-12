package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class AtomAlertDeletionJob extends QuartzJobBean {
    private final Log LOG = LogFactory.getLog(AtomAlertDeletionJob.class);
    private AlertRepository alertRepository;

    @Required
    public void setAlertRepository(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("Loadbalancer expired alerts deletion job started...");
        try {
            LOG.info(String.format("Attempting to remove alerts from the database... "));
            alertRepository.removeAlertEntries();
        } catch (Exception e) {
            LOG.error(String.format("Failed while removing one of the alert entries: %s", e.getMessage()));
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
        }
        LOG.info("Alert Deletion Job Completed.");
    }
}
