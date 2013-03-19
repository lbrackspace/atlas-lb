package org.openstack.atlas.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Calendar;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class SystemCleanupJob  extends QuartzJobBean implements StatefulJob {
    private static Log log = LogFactory.getLog(SystemCleanupJob.class);

    private JobStateService jobStateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Starting SystemCleanupJob  at " + Calendar.getInstance().getTime());
        String cacheLocation = HadoopLogsConfigs.getCacheDir();
        StaticFileUtils.deleteFilesOlderThanNDays(cacheLocation, 30); //Delete any log files from cache that are older than 30 days.

        jobStateService.deleteOldLoggingStates();
    }

    @Required
    public void setJobStateService(JobStateService jobStateService) {
        this.jobStateService = jobStateService;
    }

}
