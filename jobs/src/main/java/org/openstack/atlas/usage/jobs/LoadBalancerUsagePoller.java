package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.HostThread;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.UsageProcessor;
import org.openstack.atlas.usagerefactor.helpers.HostIdUsageMap;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class LoadBalancerUsagePoller extends AbstractJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePoller.class);

    @Autowired
    private UsageRefactorService usageRefactorService;
    @Autowired
    private HostService hostService;
    @Autowired
    private UsageProcessor usageProcessor;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.LB_USAGE_POLLER;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() {
        Calendar pollTime = Calendar.getInstance();
        LOG.info("Set poll time to " + pollTime.getTime().toString() + "...");
        Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages = usageRefactorService.getRecordsBeforeTimeInclusive(pollTime);
        Integer maxId = findMaxId(existingUsages);
        LOG.info("Retrieved records for " + existingUsages.size() + " load balancers from lb_host_usage table.");
        Map<Integer, Map<Integer, SnmpUsage>> currentUsages;
        try {
            currentUsages = getCurrentData();
        } catch (Exception e) {
            LOG.error("There was an error retrieving current usage from stingray using snmp. " + e);
            return;
        }
        LOG.info("Retrieved records for " + currentUsages.size() + " hosts from stingray by SNMP.");
        UsageProcessorResult result = usageProcessor.mergeRecords(existingUsages, currentUsages, pollTime);
        LOG.info("Completed processing of current usage");
        LOG.info("Checking if any events were inserted between the beginning of this job and now...");
        Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> newEvents = usageRefactorService.getRecordsAfterTimeInclusive(pollTime);
        Set<Integer> loadBalancersNotToDelete = removeLoadBalancerRecordsThatHadNewEvents(result, newEvents);
        usageRefactorService.batchCreateLoadBalancerMergedHostUsages(result.getMergedUsages());
        LOG.info("Completed insertion of " + result.getMergedUsages().size() + " new records into lb_merged_host_usage table.");
        usageRefactorService.batchCreateLoadBalancerHostUsages(result.getLbHostUsages());
        LOG.info("Completed insertion of " + result.getLbHostUsages().size() + " new records into lb_host_usage table.");
        usageRefactorService.deleteOldLoadBalancerHostUsages(pollTime, loadBalancersNotToDelete, maxId);
        LOG.info("Completed deletion of records from lb_host_usage table prior to poll time: " + pollTime.getTime().toString());
    }

    @Override
    public void cleanup() {
    }

    private Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        List<Host> hostList = getAccessibleHosts();
        List<Callable<HostIdUsageMap>> callables = new ArrayList<Callable<HostIdUsageMap>>();

        ExecutorService threadPool = Executors.newFixedThreadPool(hostList.size());
        for (Host host : hostList) {
            callables.add(new HostThread(host));
        }

        try {
            List<Future<HostIdUsageMap>> futures = threadPool.invokeAll(callables);
            for (Future<HostIdUsageMap> future : futures) {
                mergedHostsUsage.put(future.get().getHostId(), future.get().getMap());
            }

            return mergedHostsUsage;
        } finally {
            shutdownAndAwaitTermination(threadPool);
        }
    }

    private List<Host> getAccessibleHosts() {
        LOG.info("Discovering accessible hosts...");
        List<Host> hostList = hostService.getAll();
        List<Host> accessibleHosts = new ArrayList<Host>();
        for (Host host : hostList) {
            try {
                if (host.isSoapEndpointActive()) {
                    LOG.info("Host: " + host.getName() + " is accessible.");
                    accessibleHosts.add(host);
                } else {
                    LOG.info("Host: " + host.getName() + " is NOT accessible.");
                }
            } catch (Exception e) {
                LOG.info("Exception while checking host: " + host.getName() + " endpoint. " + e.getMessage());
            }
        }
        return accessibleHosts;
    }

    private Set<Integer> removeLoadBalancerRecordsThatHadNewEvents(UsageProcessorResult recordsToInsert,
                                                           Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> newEvents) {
        Set<Integer> loadBalancersToExcludeFromDelete = new HashSet<Integer>();
        for (Integer loadbalancerId : newEvents.keySet()) {
            loadBalancersToExcludeFromDelete.add(loadbalancerId);
            Iterator<LoadBalancerMergedHostUsage> lbmhuIter = recordsToInsert.getMergedUsages().iterator();
            while (lbmhuIter.hasNext()) {
                LoadBalancerMergedHostUsage mergedUsage = lbmhuIter.next();
                if(mergedUsage.getLoadbalancerId() == loadbalancerId) {
                    LOG.info(String.format("Load balancer %d had event come in during poller run.  Removing records that were to be inserted.", loadbalancerId));
                    lbmhuIter.remove();
                }
            }
            Iterator<LoadBalancerHostUsage> lbhuIter = recordsToInsert.getLbHostUsages().iterator();
            while (lbhuIter.hasNext()) {
                LoadBalancerHostUsage hostUsage = lbhuIter.next();
                if (hostUsage.getLoadbalancerId() == loadbalancerId) {
                    lbhuIter.remove();
                }
            }
        }
        return loadBalancersToExcludeFromDelete;
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        final int THREAD_POOL_TIMEOUT = 30;

        pool.shutdown(); // Disable new tasks from being submitted
        
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(THREAD_POOL_TIMEOUT, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(THREAD_POOL_TIMEOUT, TimeUnit.SECONDS))
                    LOG.error(String.format("Pool '%s' did not terminate!", pool.toString()));
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private Integer findMaxId(Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages) {
        LOG.info("Getting max record id...");
        Integer maxId = -1;
        for (Integer loadbalancerId : existingUsages.keySet()) {
            for (Integer hostId : existingUsages.get(loadbalancerId).keySet()) {
                for(LoadBalancerHostUsage usage : existingUsages.get(loadbalancerId).get(hostId)) {
                    if(maxId <= usage.getId()){
                        maxId = usage.getId();
                    }
                }
            }
        }
        LOG.info("Found max record id - " + maxId);
        return maxId;
    }

}
