package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.tools.HadoopRunner;

public interface QuartzExecutable {

    void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException;
}
