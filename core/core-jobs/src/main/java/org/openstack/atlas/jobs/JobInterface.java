package org.openstack.atlas.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public interface JobInterface {
    void init(JobExecutionContext jobExecutionContext) throws JobExecutionException;

    void execute() throws JobExecutionException;

    void destroy();
}
