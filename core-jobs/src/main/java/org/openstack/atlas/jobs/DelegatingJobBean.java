package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

public class DelegatingJobBean extends AtlasJob implements StatefulJob {
    private static final String APPLICATION_CONTEXT_KEY = "applicationContext";
    private static final String JOB_BEAN_NAME_KEY = "job.bean.name";
    protected final Log LOG = LogFactory.getLog(DelegatingJobBean.class);

    @Override
    protected final void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SchedulerContext schedulerContext = null;

        try {
            schedulerContext = jobExecutionContext.getScheduler().getContext();
        } catch (SchedulerException e) {
            throw new JobExecutionException("Failure accessing scheduler context", e);
        }

        ApplicationContext appContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
        String jobBeanName = (String) jobExecutionContext.getJobDetail().getJobDataMap().get(JOB_BEAN_NAME_KEY);
        LOG.info("Starting job: " + jobBeanName);
        JobInterface jobBean = (JobInterface) appContext.getBean(jobBeanName);
        try {
            jobBean.init(jobExecutionContext);
            jobBean.execute();
        } finally {
            jobBean.destroy();
        }
    }
}
