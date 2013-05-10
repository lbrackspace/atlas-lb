package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.Job;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.util.common.MapUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UsagePollerImpl extends Job implements UsagePoller, StatefulJob{

    private final Log LOG = LogFactory.getLog(UsagePollerImpl.class);
    UsageRefactorService usageRefactorService;
    SnmpUsageCollector snmpUsageCollector = new SnmpUsageCollectorImpl();

    @Required
    public void setUsageRefactorService(UsageRefactorService usageRefactorService) {
        this.usageRefactorService = usageRefactorService;
    }

    @Override
    public void run(){
        Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages = usageRefactorService.getAllLoadBalancerHostUsages();
        LOG.info("Retrieved records for " + existingUsages.size() + " load balancers from lb_host_usage table.");
        Calendar pollTime = Calendar.getInstance();
        LOG.info("Set poll time to " + pollTime.getTime().toString() + "...");
        Map<Integer, Map<Integer, SnmpUsage>> currentUsages = null;
        try {
            currentUsages = snmpUsageCollector.getCurrentData();
        }catch(Exception e) {
            LOG.error("There was an error retrieving current usage from stingray using snmp. " + e);
            return;
        }
        LOG.info("Retrieved records for " + currentUsages.size() + " hosts from stingray by SNMP.");
        UsageProcessorResult result = UsageProcessor.mergeRecords(existingUsages, currentUsages, pollTime);
        LOG.info("Completed processing of current usage");
        usageRefactorService.batchCreateLoadBalancerMergedHostUsages(result.getMergedUsages());
        LOG.info("Completed insertion of " + result.getMergedUsages().size() + " new records into lb_merged_host_usage table.");
        usageRefactorService.batchCreateLoadBalancerHostUsages(result.getLbHostUsages());
        LOG.info("Completed insertion of " + result.getLbHostUsages() + " new records into lb_host_usage table.");
        usageRefactorService.deleteOldLoadBalancerHostUsages(pollTime);
        LOG.info("Completed deletion of records from lb_host_usage table prior to poll time: " + pollTime.getTime().toString());
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        run();
    }
}
