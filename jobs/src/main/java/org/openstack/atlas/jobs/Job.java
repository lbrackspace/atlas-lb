package org.openstack.atlas.jobs;

import org.openstack.atlas.service.domain.services.JobStateService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class Job extends QuartzJobBean {
    protected JobStateService jobStateService;

    @Required
    public void setJobStateService(JobStateService jobStateService) {
        this.jobStateService = jobStateService;
    }
}
