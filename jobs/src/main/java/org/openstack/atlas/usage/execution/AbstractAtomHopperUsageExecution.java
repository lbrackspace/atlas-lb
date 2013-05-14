package org.openstack.atlas.usage.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.JobInterface;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClientImpl;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.restclients.auth.IdentityClientImpl;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.services.JobStateService;
import org.openstack.atlas.usage.thread.service.RejectedExecutionHandler;
import org.openstack.atlas.usage.thread.service.ThreadPoolExecutorService;
import org.openstack.atlas.usage.thread.service.ThreadPoolMonitorService;
import org.openstack.atlas.usage.thread.util.ThreadServiceUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractAtomHopperUsageExecution implements JobInterface {
    private final Log LOG = LogFactory.getLog(UsageAtomHopperExecution.class);

    public abstract JobName getJobName();
    public abstract void pushUsageToAtomHopper() throws Exception;

    private final Configuration configuration = new AtomHopperConfiguration();
    protected ThreadPoolExecutor poolExecutor;
    protected AtomHopperClient ahuslClient;
    protected IdentityAuthClient identityClient;

    private final static int QUERY_SIZE = 1000;

    @Autowired
    private JobStateService jobStateService;
    @Autowired
    private ThreadPoolMonitorService threadPoolMonitorService;
    @Autowired
    private ThreadPoolExecutorService threadPoolExecutorService;

    protected final int NUM_ATTEMPTS = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_num_attempts));
    protected final int BATCH_SIZE = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_task_count));
    private long keepAliveTime = Long.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_conn_timeout));
    private int corePoolSize = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_core_size));
    private int maxPoolSize = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_max_size));
    private String allowAHUSL = configuration.getString(AtomHopperConfigurationKeys.allow_ahusl);

    @Override
    public void init(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.debug("INIT");
    }

    @Override
    public void execute() throws JobExecutionException {
        LOG.debug("EXECUTE");
        runPoller();
    }

    private void runPoller() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom Hopper " + getJobName() +" usage poller job started at %s (Timezone: %s)",
                startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(getJobName(), JobStateVal.IN_PROGRESS);

        if (Boolean.valueOf(allowAHUSL)) {
            poolExecutor = threadPoolExecutorService.createNewThreadPool(corePoolSize, maxPoolSize,
                    keepAliveTime, QUERY_SIZE, new RejectedExecutionHandler());
            ThreadServiceUtil.startThreadMonitor(poolExecutor, threadPoolMonitorService);

            try {
                LOG.debug("Setting up the ahuslClient...");
                ahuslClient = new AtomHopperClientImpl();
                identityClient = new IdentityClientImpl();

                pushUsageToAtomHopper();
            } catch (Throwable t) {
                jobStateService.updateJobState(getJobName(), JobStateVal.FAILED);
                logException(t);
                ThreadServiceUtil.shutDownAHUSLServices(poolExecutor, threadPoolMonitorService, ahuslClient);
            }

            ThreadServiceUtil.shutDownAHUSLServices(poolExecutor, threadPoolMonitorService, ahuslClient);
        }

        Double elapsedMins = ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(getJobName(), JobStateVal.FINISHED);
        LOG.info(String.format("Atom Hopper " + getJobName()
                + " usage poller job completed at '%s' (Total Time: %f mins)",
                AtomHopperUtil.getNow().getTime(), elapsedMins));
    }

    private void logException(Throwable t) {
        System.out.printf("Exception: %s\n", AtomHopperUtil.getExtendedStackTrace(t));
        LOG.error(String.format("Exception: %s\n", AtomHopperUtil.getExtendedStackTrace(t)));
    }

    @Override
    public void destroy() {
        LOG.debug("DESTROY");
    }
}