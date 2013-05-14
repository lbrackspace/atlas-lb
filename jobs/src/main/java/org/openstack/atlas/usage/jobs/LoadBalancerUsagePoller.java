package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages = usageRefactorService.getAllLoadBalancerHostUsages();
        LOG.info("Retrieved records for " + existingUsages.size() + " load balancers from lb_host_usage table.");
        Calendar pollTime = Calendar.getInstance();
        LOG.info("Set poll time to " + pollTime.getTime().toString() + "...");
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
        usageRefactorService.batchCreateLoadBalancerMergedHostUsages(result.getMergedUsages());
        LOG.info("Completed insertion of " + result.getMergedUsages().size() + " new records into lb_merged_host_usage table.");
        usageRefactorService.batchCreateLoadBalancerHostUsages(result.getLbHostUsages());
        LOG.info("Completed insertion of " + result.getLbHostUsages().size() + " new records into lb_host_usage table.");
        usageRefactorService.deleteOldLoadBalancerHostUsages(pollTime);
        LOG.info("Completed deletion of records from lb_host_usage table prior to poll time: " + pollTime.getTime().toString());
    }

    @Override
    public void cleanup() {
    }

    private Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        List<Host> hostList = hostService.getAllHosts();
        List<Callable<HostIdUsageMap>> callables = new ArrayList<Callable<HostIdUsageMap>>();

        ExecutorService executor = Executors.newFixedThreadPool(hostList.size());
        for (Host host : hostList) {
            callables.add(new HostThread(host));
        }

        List<Future<HostIdUsageMap>> futures = executor.invokeAll(callables);
        for (Future<HostIdUsageMap> future : futures) {
            mergedHostsUsage.put(future.get().getHostId(), future.get().getMap());
        }

        return mergedHostsUsage;
    }

    private void removeInaccessibleHostsFromList(List<Host> hostList) {
        for(Host host : hostList) {

        }
    }

}
