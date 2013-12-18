package org.openstack.atlas.scheduler;

import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.openstack.atlas.logs.hadoop.util.LogDateFormat;
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

    protected QuartzSchedulerConfigs getSchedulerConfigs(JobExecutionContext context) {
        QuartzSchedulerConfigs schedulerConfigs = QuartzSchedulerConfigs.createSchedulerConfigsFromMap(context.getJobDetail().getJobDataMap());
        if (schedulerConfigs.getRunTime() == null) {
            schedulerConfigs.setRunTime(getRuntime(context));
        }
        return schedulerConfigs;
    }

    protected JobScheduler createSchedulerInstance(JobExecutionContext context) {
        JobScheduler scheduler = new JobScheduler();
        scheduler.setSchedulerFactoryBean(context.getScheduler());
        return scheduler;
    }
}
