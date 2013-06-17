package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

public class UsageCalculator {
    public static Double calculateNewAverage(Double oldAverage, Integer oldDivisor, Integer newDataPoint) {
        Integer newDivisor = oldDivisor + 1;
        return (oldAverage * oldDivisor + newDataPoint) / newDivisor;
    }

    public static Long calculateCumBandwidthBytesIn(LoadBalancerUsage mostRecentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null || mostRecentRecord.getLastBandwidthBytesIn() == null) return mostRecentRecord.getCumulativeBandwidthBytesIn();
        if (currentSnapshotValue >= mostRecentRecord.getLastBandwidthBytesIn()) {
            return mostRecentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue - mostRecentRecord.getLastBandwidthBytesIn();
        } else {
            return mostRecentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue;
        }
    }

    public static Long calculateCumBandwidthBytesInSsl(LoadBalancerUsage mostRecentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null || mostRecentRecord.getLastBandwidthBytesInSsl() == null) return mostRecentRecord.getCumulativeBandwidthBytesInSsl();
        if (currentSnapshotValue >= mostRecentRecord.getLastBandwidthBytesInSsl()) {
            return mostRecentRecord.getCumulativeBandwidthBytesInSsl() + currentSnapshotValue - mostRecentRecord.getLastBandwidthBytesInSsl();
        } else {
            return mostRecentRecord.getCumulativeBandwidthBytesInSsl() + currentSnapshotValue;
        }
    }

    public static Long calculateCumBandwidthBytesOut(LoadBalancerUsage mostRecentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null || mostRecentRecord.getLastBandwidthBytesOut() == null) return mostRecentRecord.getCumulativeBandwidthBytesOut();
        if (currentSnapshotValue >= mostRecentRecord.getLastBandwidthBytesOut()) {
            return mostRecentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue - mostRecentRecord.getLastBandwidthBytesOut();
        } else {
            return mostRecentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue;
        }
    }

    public static Long calculateCumBandwidthBytesOutSsl(LoadBalancerUsage mostRecentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null || mostRecentRecord.getLastBandwidthBytesOutSsl() == null) return mostRecentRecord.getCumulativeBandwidthBytesOutSsl();
        if (currentSnapshotValue >= mostRecentRecord.getLastBandwidthBytesOutSsl()) {
            return mostRecentRecord.getCumulativeBandwidthBytesOutSsl() + currentSnapshotValue - mostRecentRecord.getLastBandwidthBytesOutSsl();
        } else {
            return mostRecentRecord.getCumulativeBandwidthBytesOutSsl() + currentSnapshotValue;
        }
    }
}