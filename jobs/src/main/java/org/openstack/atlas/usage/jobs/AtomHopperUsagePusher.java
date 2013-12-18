package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClientImpl;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.restclients.auth.IdentityAuthClient;
import org.openstack.atlas.restclients.auth.IdentityClientImpl;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.usage.BatchAction;
import org.openstack.atlas.usage.ExecutionUtilities;
import org.openstack.atlas.usage.thread.UsageThread;
import org.openstack.atlas.usage.thread.service.RejectedExecutionHandler;
import org.openstack.atlas.usage.thread.service.ThreadPoolExecutorService;
import org.openstack.atlas.usage.thread.service.ThreadPoolMonitorService;
import org.openstack.atlas.usage.thread.util.ThreadServiceUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys.*;

@Component
public class AtomHopperUsagePusher extends AbstractJob {
    private final Log LOG = LogFactory.getLog(AtomHopperUsagePusher.class);

    private final static int QUERY_SIZE = 1000;
    private int NUM_ATTEMPTS;
    private int BATCH_SIZE;

    private boolean pushUsage;
    private boolean pushFailedUsage;
    private long keepAliveTime;
    private int corePoolSize;
    private int maxPoolSize;
    private ThreadPoolExecutor poolExecutor;
    private AtomHopperClient ahuslClient;
    private IdentityAuthClient identityClient;

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private LoadBalancerEventRepository loadBalancerEventRepository;
    @Autowired
    private UsageRepository usageRepository;
    @Autowired
    private ThreadPoolMonitorService threadPoolMonitorService;
    @Autowired
    private ThreadPoolExecutorService threadPoolExecutorService;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.ATOM_LOADBALANCER_USAGE_POLLER;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        AtomHopperConfiguration configuration = new AtomHopperConfiguration();

        NUM_ATTEMPTS = Integer.valueOf(configuration.getString(ahusl_num_attempts));
        BATCH_SIZE = Integer.valueOf(configuration.getString(ahusl_pool_task_count));

        pushUsage = Boolean.valueOf(configuration.getString(allow_ahusl));
        pushFailedUsage = Boolean.valueOf(configuration.getString(ahusl_run_failed_entries));
        maxPoolSize = Integer.valueOf(configuration.getString(ahusl_pool_max_size));
        corePoolSize = Integer.valueOf(configuration.getString(ahusl_pool_core_size));
        keepAliveTime = Long.valueOf(configuration.getString(ahusl_pool_conn_timeout));

        try {
            poolExecutor = threadPoolExecutorService.createNewThreadPool(corePoolSize, maxPoolSize, keepAliveTime, QUERY_SIZE, new RejectedExecutionHandler());
            ThreadServiceUtil.startThreadMonitor(poolExecutor, threadPoolMonitorService);
            LOG.debug("Setting up the ahuslClient...");
            ahuslClient = new AtomHopperClientImpl();
            identityClient = new IdentityClientImpl();
        } catch (Exception e) {
            System.out.printf("Exception: %s\n", AtomHopperUtil.getExtendedStackTrace(e));
            LOG.error(String.format("Exception: %s\n", AtomHopperUtil.getExtendedStackTrace(e)));
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void run() throws Exception {
        if (pushUsage) {
            pushUsage();
        }

        if (pushFailedUsage) {
            pushFailedUsage();
        }
    }

    @Override
    public void cleanup() {
        ThreadServiceUtil.shutDownAHUSLServices(poolExecutor, threadPoolMonitorService, ahuslClient);
    }

    private void pushUsage() throws Exception {
        final List<Usage> allUsages = loadBalancerRepository.getUsageNeedsPushed(AtomHopperUtil.getStartCal(), AtomHopperUtil.getNow(), NUM_ATTEMPTS);
        LOG.info(String.format("Pushing %d records marked for push", allUsages.size()));
        push(allUsages);
    }

    private void pushFailedUsage() throws Exception {
        final List<Usage> allUsages = loadBalancerRepository.getUsageRetryNeedsPushed(AtomHopperUtil.getStartCal(), AtomHopperUtil.getNow(), NUM_ATTEMPTS);
        LOG.info(String.format("Pushing %d records marked for retry", allUsages.size()));
        push(allUsages);
    }

    private void push(List<Usage> allUsages) throws Exception {
        if (!allUsages.isEmpty()) {
            sortUsages(allUsages);

            BatchAction<Usage> batchAction = new BatchAction<Usage>() {
                public void execute(Collection<Usage> allUsages) throws Exception {
                    poolExecutor.execute(new UsageThread(allUsages, ahuslClient, identityClient, usageRepository, loadBalancerEventRepository, alertRepository));
                }
            };

            ExecutionUtilities.ExecuteInBatches(allUsages, BATCH_SIZE, batchAction);
        } else {
            LOG.debug("No usages to push to ATOM Hopper at this time.");
        }
    }

    private void sortUsages(List<Usage> usages) {
        Collections.sort(usages, new Comparator<Usage>() {
            public int compare(Usage usage1, Usage usage2) {
                return usage1.getId().compareTo(usage2.getId());
            }
        });
    }
}