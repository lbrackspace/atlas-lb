package org.openstack.atlas.util.constants;

public class ConnectionThrottleDefaultConstants {

    private static final Integer minConnections;
    private static final Integer maxConnectionRate;
    private static final Integer rateInterval;

    static {
        minConnections = new Integer(0);
        maxConnectionRate = new Integer(0);
        rateInterval = new Integer(1);
    }

    public static Integer getMaxConnectionRate() {
        return new Integer(maxConnectionRate);
    }

    public static Integer getMinConnections() {
        return new Integer(minConnections);
    }

    public static Integer getRateInterval() {
        return new Integer(rateInterval);
    }
}
