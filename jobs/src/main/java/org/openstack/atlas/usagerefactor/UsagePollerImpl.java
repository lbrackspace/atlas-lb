package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.snmp4j.Snmp;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UsagePollerImpl implements UsagePoller{

    HostService hostService;
    UsageRefactorService usageRefactorService;
    SnmpUsageCollector snmpUsageCollector = new SnmpUsageCollectorImpl();

    @Override
    public void poll() throws Exception {
        int numHosts = hostService.getAllHosts().size();
        Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages = usageRefactorService.getAllLoadBalancerHostUsages();
        Calendar pollTime = Calendar.getInstance();
        Map<Integer, Map<Integer, SnmpUsage>> currentUsages = snmpUsageCollector.getCurrentData();
        currentUsages = UsageMappingHelper.swapKeyGrouping(currentUsages);
        UsageProcessorResult result = UsageProcessor.mergeRecords(existingUsages, currentUsages, pollTime, numHosts);
        usageRefactorService.batchCreateLoadBalancerMergedHostUsages(result.getMergedUsages());
        usageRefactorService.batchCreateLoadBalancerHostUsages(result.getLbHostUsages());
        usageRefactorService.deleteOldLoadBalancerHostUsages(pollTime);
    }
}
