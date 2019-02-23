package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.usagerefactor.helpers.UsagePollerHelper;
import org.openstack.atlas.usagerefactor.helpers.UsageProcessorResult;
import org.openstack.atlas.util.common.MapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Component
public class UsageProcessor {
    private final Log LOG = LogFactory.getLog(UsageProcessor.class);

    @Autowired
    private UsagePollerHelper usagePollerHelper;

    public UsageProcessor() {
    }

    public UsageProcessorResult mergeRecords(Map<Integer, Map<Integer, List<LoadBalancerHostUsage>>> existingUsages,
                                                 Map<Integer, Map<Integer, SnmpUsage>> currentUsages,
                                                 Calendar pollTime)
    {   //Ignore the flowing steps, they do not seem to be correct for the latest logic of Usage Poller,
        // this might be from the legacy usage processing
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
        //Ignore till here
        LOG.info("Merging load balancer host usage records...");

        //Process events that have come in between now and last poller run and create LoadBalancerMergedHostUsage entries.
        //one LoadBalancerMergedHostUsage per loadbalancer per event is created by adding up the usage from each host.
        //Ex: if there are two events in the existingUsages for a lb then two entries of LoadBalancerMergedHostUsage are created for that lb.
        List<LoadBalancerMergedHostUsage> mergedHostUsages = usagePollerHelper.processExistingEvents(existingUsages);

        //Now parent key should be loadbalancerId, and child key should be hostId in currentUsages
        currentUsages = MapUtil.swapKeys(currentUsages);

        /**
         * Process current usage and create LoadBalancerMergedHostUsage and LoadBalancerHostUsage entries.
         * One LoadBalancerMergedHostUsage per lb is created by adding up the current SNMP usage from each host.
         * And one LoadBalancerHostUsage is created from the each of the current SNMP usages. Ex: if there are 5 hosts then there will be
         * 5 SNMP records for a lb hence 5 LoadBalancerHostUsage records are created for that lb.
         */
        UsageProcessorResult processorResult = usagePollerHelper.processCurrentUsage(existingUsages, currentUsages,
                pollTime);

        //Combine the LoadBalancerMergedHostUsage records from the existingUsage and the currentUsage.
        mergedHostUsages.addAll(processorResult.getMergedUsages());

        //Return UsageProcessorResult with mergedHostUsages and the LbHostUsages
        return new UsageProcessorResult(mergedHostUsages, processorResult.getLbHostUsages());
    }
}
