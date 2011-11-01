package org.openstack.atlas.jobs;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class AtlasJob extends QuartzJobBean {
//    protected JobStateService jobStateService;
//
//    @Required
//    public void setJobStateService(JobStateService jobStateService) {
//        this.jobStateService = jobStateService;
//    }
}