package org.openstack.atlas.jobs.usage.helper;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;

import java.util.Calendar;

public class UsageRecordHelper {

    public static UsageRecord createUsageRecord(Integer lbId, Long transferBytesIn, Long transferBytesOut, Long lastBytesInCount, Long lastBytesOutCount, Calendar startTime, Calendar endTime) {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setId(lbId);

        UsageRecord usageRecord = new UsageRecord();
        usageRecord.setLoadBalancer(loadBalancer);
        usageRecord.setTransferBytesIn(transferBytesIn);
        usageRecord.setTransferBytesOut(transferBytesOut);
        usageRecord.setLastBytesInCount(lastBytesInCount);
        usageRecord.setLastBytesOutCount(lastBytesOutCount);
        usageRecord.setStartTime(startTime);
        usageRecord.setEndTime(endTime);

        return usageRecord;
    }
}
