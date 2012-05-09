package org.openstack.atlas.usage.logic;

import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;

public class UsageCalculator {
    public static Double calculateNewAverage(Double oldAverage, Integer oldDivisor, Integer newDataPoint) {
        Integer newDivisor = oldDivisor + 1;
        return (oldAverage * oldDivisor + newDataPoint) / newDivisor;
    }

    public static Long calculateCumBandwidthBytesIn(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesIn();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesIn()) {
            return currentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue - currentRecord.getLastBandwidthBytesIn();
        } else {
            return currentRecord.getCumulativeBandwidthBytesIn() + currentSnapshotValue;
        }
    }

    public static Long calculateCumBandwidthBytesInSsl(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesInSsl();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesInSsl()) {
            return currentRecord.getCumulativeBandwidthBytesInSsl() + currentSnapshotValue - currentRecord.getLastBandwidthBytesInSsl();
        } else {
            return currentRecord.getCumulativeBandwidthBytesInSsl() + currentSnapshotValue;
        }
    }

    public static Long calculateCumBandwidthBytesOut(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesOut();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesOut()) {
            return currentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue - currentRecord.getLastBandwidthBytesOut();
        } else {
            return currentRecord.getCumulativeBandwidthBytesOut() + currentSnapshotValue;
        }
    }

    public static Long calculateCumBandwidthBytesOutSsl(LoadBalancerUsage currentRecord, Long currentSnapshotValue) {
        if (currentSnapshotValue == null) return currentRecord.getCumulativeBandwidthBytesOutSsl();
        if (currentSnapshotValue >= currentRecord.getLastBandwidthBytesOutSsl()) {
            return currentRecord.getCumulativeBandwidthBytesOutSsl() + currentSnapshotValue - currentRecord.getLastBandwidthBytesOutSsl();
        } else {
            return currentRecord.getCumulativeBandwidthBytesOutSsl() + currentSnapshotValue;
        }
    }
}
