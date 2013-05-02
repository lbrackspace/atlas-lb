package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.UsageMappingHelper;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class UsageProcessor {

    final static Log LOG = LogFactory.getLog(UsageProcessor.class);

    public static UsageProcessorResult mergeRecords(Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages,
                                                 Map<Integer, Map<Integer, SnmpUsage>> currentUsages,
                                                 Calendar pollTime, int numHosts)
    {
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
        LOG.info("Merging load balancer host usage records...");
        UsagePollerHelper usagePollerHelper = new UsagePollerHelper();

        //Process events that have come in between now and last poller run
        List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePollerHelper.processExistingEvents(existingUsages);

        //Now parent key should be loadbalancerId, and child key hostId
        currentUsages = UsageMappingHelper.swapKeyGrouping(currentUsages);

        //Process current usage now. The method processExistingEvents should have removed
        UsageProcessorResult processorResult = usagePollerHelper.processCurrentUsage(existingUsages, currentUsages, pollTime);
        mergedHostUsages.addAll(processorResult.getMergedUsages());

        return new UsageProcessorResult(mergedHostUsages, processorResult.getLbHostUsages());
    }
}
