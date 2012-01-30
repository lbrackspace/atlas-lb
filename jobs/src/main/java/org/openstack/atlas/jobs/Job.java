package org.openstack.atlas.jobs;

import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class Job extends QuartzJobBean {
    protected JobStateService jobStateService;
    protected AlertRepository alertRepository;

    @Required
    public void setJobStateService(JobStateService jobStateService) {
        this.jobStateService = jobStateService;
    }

    @Required
    public void setAlertRepository(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }
}
