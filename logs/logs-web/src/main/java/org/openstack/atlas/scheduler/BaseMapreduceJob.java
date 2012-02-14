package org.openstack.atlas.scheduler;

import org.openstack.atlas.tools.HadoopRunner;
import org.openstack.atlas.util.LogDateFormat;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class BaseMapreduceJob extends QuartzJobBean {

    protected LogDateFormat format;

    @Required
    public void setLogDateFormat(LogDateFormat logDateFormat) {
        this.format = logDateFormat;
    }

    protected String getRuntime(JobExecutionContext context) {
        return format.format(context.getScheduledFireTime());
    }

    protected HadoopRunner getRunner(JobExecutionContext context) {
        HadoopRunner runner = HadoopRunner.createRunnerFromValues(context.getJobDetail().getJobDataMap());
        if (runner.getRunTime() == null) {
            runner.setRunTime(getRuntime(context));
        }
        return runner;
    }

    protected JobScheduler createSchedulerInstance(JobExecutionContext context) {
        JobScheduler scheduler = new JobScheduler();
        scheduler.setSchedulerFactoryBean(context.getScheduler());
        return scheduler;
    }
}
