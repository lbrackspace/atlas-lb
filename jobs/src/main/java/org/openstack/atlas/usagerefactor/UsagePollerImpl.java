package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.service.domain.services.UsageRefactorService;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.HostIdUsageMap;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UsagePollerImpl implements UsagePoller {
    final Log LOG = LogFactory.getLog(UsagePollerImpl.class);

    HostService hostService;
    UsageRefactorService usageRefactorService;
    SnmpUsageCollector snmpUsageCollector = new SnmpUsageCollectorImpl();

    @Required
    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    @Required
    public void setUsageRefactorService(UsageRefactorService usageRefactorService) {
        this.usageRefactorService = usageRefactorService;
    }

    @Override
    public List<LoadBalancerMergedHostUsage> processRecords() {
        /*
         * 1. Query SNMP
         * 2. Query host usage table for previous records. store time as deleteTimeMarker
         * 3. Process Records For Each LB
         *      a. If no record in host usage table for LB but it is in SNMP results, then write snmp results to hosts table
         *      but do NOT write any data to the Merged LB Usage table.
         *      b. If CREATE_LOADBALANCER encountered in host usage table disregard any earlier records.
         *      c. If UNSUSPEND event encountered in host usage table disregard any earlier records unless earlier record is SUSPEND event.
         *      d. If earlier record's value is greater than current record then a reset happened.
         *          i. Record 0 Usage in Merged LB Usage table
         *          ii. Do not modify the counter in the host usage table.
         *      e. Write SNMP data to LB Host Usage table.
         *      d. Delete records from LB Host Usage table that have an ID less than the markerID
         */
        LOG.info("Usage Poller Starting...");
        UsagePollerHelper usagePollerHelper = new UsagePollerHelper(this.hostService.getAllHosts().size());
        //Once currentdata is retrieved, the parent key will be hostId and the child key loadbalancerId
        Map<Integer, Map<Integer, SnmpUsage>> currentLBHostUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        try {
            currentLBHostUsage = snmpUsageCollector.getCurrentData();
        } catch (Exception e) {
            LOG.error("Could not get current data...\n" + e.getMessage());
        }
        Calendar pollTime = Calendar.getInstance();
        //Key is loadbalancerId
        LOG.info("Querying database for existing load balancer host usage records...");
        Map<Integer, List<LoadBalancerHostUsage>> existingLBHostUsages = usageRefactorService.getAllLoadBalancerHostUsages();
        List<LoadBalancerHostUsage> newHostUsage = new ArrayList<LoadBalancerHostUsage>();

        //Process events that have come in between now and last poller run
        List<LoadBalancerMergedHostUsage> mergedHostUsage = usagePollerHelper.processExistingEvents(existingLBHostUsages);

        //Now parent key should be loadbalancerId, and child key hostId
        currentLBHostUsage = UsageMappingHelper.swapKeyGrouping(currentLBHostUsage);
        mergedHostUsage.addAll(usagePollerHelper.processCurrentUsage(existingLBHostUsages, currentLBHostUsage,
                pollTime));
        //TODO: Insert mergedHostUsage
        //TODO: Delete records in lb_host_usage table prior to deleteTimeMarker

        return mergedHostUsage;
    }
}
