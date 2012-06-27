package org.openstack.atlas.atom.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.atom.client.AHUSLClient;
import org.openstack.atlas.atom.config.AtomHopperConfiguration;
import org.openstack.atlas.atom.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.atom.util.AHUSLUtil;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.pojos.AccountLoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;

public class AtomHopperLoadBalancerUsageJob extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(AtomHopperLoadBalancerUsageJob.class);

    //Configuration
    private Configuration configuration = new AtomHopperConfiguration();
    private LoadBalancerRepository loadBalancerRepository;
    private UsageRepository usageRepository;

    //ThreadPool
    private int nTasks = 50;
    private long n = 1000L;
    private int tpSize = 15;
    private int connectionLimit = 20;

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


            LOG.info("Setting up the threadPoolExecutor with " + tpSize + " pools");
            ThreadPoolExecutor tpe = new ThreadPoolExecutor(tpSize, tpSize, 50000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());


            //Create the threaded client to handle requests...
            try {
                LOG.info("Setting up the client...");
                client = new AHUSLClient();
            } catch (Exception e) {
                LOG.info("The client failed to initialize: " + Arrays.toString(e.getStackTrace()));
            }

            //Grab all accounts a begin processing usage...
            List<Integer> accounts = loadBalancerRepository.getAllAccountIds();
            for (int accountID : accounts) {
                for (AccountLoadBalancer lb : loadBalancerRepository.getAccountLoadBalancers(accountID)) {
                    LOG.info("Process lb: " + lb.getLoadBalancerId() + " for account: " + accountID);
                    int totalUsageRowsToSend = 1;
                    try {
                        //Retrieve usage for account by lbId
                        List<Usage> lbusages = loadBalancerRepository.getUsageByAccountIdandLbId(accountID, lb.getLoadBalancerId(), AHUSLUtil.getStartCal(), AHUSLUtil.getNow());
                        List<Usage> processUsage = null;

                        if (!lbusages.isEmpty()) {
                            LOG.info("There is " + lbusages.size() + " usage records to process for lb: " + lb.getLoadBalancerId());
//                            List<Callable<Object>> todo = new ArrayList<Callable<Object>>(lbusages.size());
                            int taskCounter = 0;
                            while (totalUsageRowsToSend < lbusages.size()) {

                                LOG.info("Processing usage into tasks.. task: " + taskCounter);
                                //Chunk the data into seprate tasks
                                for (int i = 0; i < nTasks; i++) {
                                    if (totalUsageRowsToSend != lbusages.size()) {
                                        processUsage =  new ArrayList<Usage>();
                                        processUsage.add(lbusages.get(totalUsageRowsToSend));
                                        totalUsageRowsToSend++;

                                    } else {
                                        break;
                                    }
                                }
                                taskCounter++;

                                LOG.info("Sending task for lb: " + lb.getLoadBalancerId() + " with " + processUsage.size() + " usage rows for this task to the que...");
                                tpe.execute(new LoadBalancerAHUSLTask(processUsage, client, usageRepository)); //TODO: Need to move repository deps...
                                LOG.info("lb: " + lb.getLoadBalancerId() + " sent to the que succesfully... with a total of " + tpe.getTaskCount() + " tasks in the task pool");

//                                LOG.info("Adding to the que pool");
//                                todo.add(Executors.callable(new LoadBalancerAHUSLTask(processUsage, client, usageRepository)));

                            }

//                            List<Future<Object>> answers = tpe.invokeAll(todo);
//                            LOG.info("This should be true when all has finished processing :: isLastDone: " + answers.get(0));
                            LOG.info("Done processing usage for lb: " + lb.getLoadBalancerId());
                            LOG.info("Tasks completed: " + tpe.getCompletedTaskCount() + "Active tasks count: " + tpe.getActiveCount() + "Current tasks in QUE: " + tpe.getTaskCount());

                        } else {
                            LOG.info("No usage found for account: " + lb.getLoadBalancerId() + " Continue...");
                        }

                    } catch (Throwable t) {
                        System.out.printf("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t));
                        LOG.error(String.format("Exception: %s\n", AHUSLUtil.getExtendedStackTrace(t)));
                    }
                }
            }
            LOG.info("Shutting down the thread pool..");
            try {
                tpe.shutdown();
                tpe.awaitTermination(300, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.error("There was an error shutting down threadPool: " + AHUSLUtil.getStackTrace(e));
            }
            LOG.info("Destroying the client");
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
}
