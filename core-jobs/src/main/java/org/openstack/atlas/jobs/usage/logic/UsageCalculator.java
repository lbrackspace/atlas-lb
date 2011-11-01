package org.openstack.atlas.jobs.usage.logic;

public class UsageCalculator {
    public static Double calculateNewAverage(Double oldAverage, Integer oldDivisor, Integer newDataPoint) {
        Integer newDivisor = oldDivisor + 1;
        return (oldAverage * oldDivisor + newDataPoint) / newDivisor;
    }

    public static Long calculateCumulativeBytes(Long currentAccumulation, Long lastCounterValue, Long newCounterValue) {
        if (newCounterValue >= lastCounterValue) {
            return currentAccumulation + newCounterValue - lastCounterValue;
        } else {
            return currentAccumulation + newCounterValue;
        }
    }
}
