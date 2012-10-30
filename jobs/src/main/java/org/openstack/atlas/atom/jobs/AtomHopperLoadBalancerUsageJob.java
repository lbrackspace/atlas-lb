package org.openstack.atlas.atom.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.handler.RejectedExecutionHandler;
import org.openstack.atlas.atom.service.ThreadPoolExecutorService;
import org.openstack.atlas.atom.service.ThreadPoolMonitorService;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Deprecated // Moved code to LoadBalancerUsageRollupJob
public class AtomHopperLoadBalancerUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperLoadBalancerUsageJob.class);

    ThreadPoolMonitorService threadPoolMonitorService;
    ThreadPoolExecutorService threadPoolExecutorService;

    //Configuration
    private Configuration configuration = new AtomHopperConfiguration();
    private LoadBalancerRepository loadBalancerRepository;
    private UsageRepository usageRepository;

    private long nTasks = Long.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_task_count));
    private int maxPoolSize = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_max_size));
    private int corePoolSize = Integer.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_core_size));
    private long keepAliveTime = Long.valueOf(configuration.getString(AtomHopperConfigurationKeys.ahusl_pool_conn_timeout));

    private AHUSLClient client;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startPoller();
    }

    private void startPoller() {
        /**
         * LOG START job-state
         *
         * **/
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom hopper load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        processJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.IN_PROGRESS);

        if (configuration.getString(AtomHopperConfigurationKeys.allow_ahusl).equals("true")) {

            LOG.debug("Setting up the threadPoolExecutor with " + maxPoolSize + " pools");
            ThreadPoolExecutor taskExecutor = threadPoolExecutorService.createNewThreadPool(corePoolSize, maxPoolSize, keepAliveTime, 1000, new RejectedExecutionHandler());

            // ThreadPoolMonitorService is started...
            threadPoolMonitorService.setExecutor(taskExecutor);
            Thread monitor = new Thread(threadPoolMonitorService);
            monitor.start();

            try {
                LOG.debug("Setting up the client...");
                client = new AHUSLClient();

                int totalUsageRowsToSend = 0;
                    List<Usage> lbusages = loadBalancerRepository.getAllUsageNeedsPushed(AHUSLUtil.getStartCal(), AHUSLUtil.getNow());

                    List<Usage> processUsage;
                    if (!lbusages.isEmpty()) {
                        int taskCounter = 0;
                        while (totalUsageRowsToSend < lbusages.size()) {
                            processUsage = new ArrayList<Usage>();

                            LOG.debug("Processing usage into tasks.. task: " + taskCounter);
                            for (int i = 0; i < nTasks; i++) {
                                if (totalUsageRowsToSend <= lbusages.size() - 1) {
                                    processUsage.add(lbusages.get(totalUsageRowsToSend));
                                    totalUsageRowsToSend++;

                                } else {
                                    break;
                                }
                            }

                            taskCounter++;

//                            taskExecutor.execute(new LoadBalancerAHUSLTask(processUsage, client, usageRepository)); //TODO: Need to move repository deps...
                        }
                    } else {
                        LOG.debug("No usage found for processing at this time...");
                    }

                } catch (Throwable t) {
                    System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
                    LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
                }

            try {
                LOG.debug("Shutting down the thread pool and monitors..");
                taskExecutor.shutdown();
                taskExecutor.awaitTermination(300, TimeUnit.SECONDS);
                threadPoolMonitorService.shutDown();
            } catch (InterruptedException e) {
                LOG.error("There was an error shutting down threadPool: " + AHUSLUtil.getStackTrace(e));
            }

            LOG.debug("Destroying the client");
            client.destroy();
        }

        /**
         * LOG END job-state
         */
        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        processJobState(JobName.ATOM_LOADBALANCER_USAGE_POLLER, JobStateVal.FINISHED);
        LOG.info(String.format("Atom hopper load balancer usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

    private void processJobState(JobName jobName, JobStateVal jobStateVal) {
        jobStateService.updateJobState(jobName, jobStateVal);
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Required
    public void setThreadPoolMonitorService(ThreadPoolMonitorService threadPoolMonitorService) {
        this.threadPoolMonitorService = threadPoolMonitorService;
    }

    @Required
    public void setThreadPoolExecutorService(ThreadPoolExecutorService threadPoolExecutorService) {
        this.threadPoolExecutorService = threadPoolExecutorService;
    }
}
