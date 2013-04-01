package org.openstack.atlas.service.domain.util;

public final class Constants {

    public static final int NUM_DAYS_OF_USAGE = 90;
    public static final int DEFAULT_NODE_WEIGHT = 1;
    public static final int NUM_DAYS_BEFORE_VIP_REUSE = 1;
    public static final int MIN_REQUIRED_VIPS = 1;
    public static final int MIN_ACCOUNTS_PER_VIP = 1;
    public static final int MAX_ERRORPAGE_CONTENT_LENGTH = 1024 * 64;
    public static final String LoadBalancerNotFound = "Load balancer not found";
    public static final String MetaNotFound = "Meta data item not found";
    public static final String ErrorPageNotFound = "Error Page not found";
    public static final String SslTerminationNotFound = "SSL termination not found for requested load balancer.";
    public static final String NoMonitorLogs = "Monitor logs not found for load balancer.";
    public static final String JobNotFound = "Job not found";
    public static final String VirtualIpNotFound = "Virtual ip not found";
    public static final String LoadBalancerDeleted = "The load balancer is deleted and considered immutable.";
    public static final String OutOfVips = "No available virtual ips. Please contact support.";
    public static final String DEFAULT_ERRORFILE = "global_error.html";
    public static final String DEFAULT_ERROR_MESSAGE = "<html>No suitable nodes.</html>";
    public static final String NoPrimaryNodeError = "you must have at least one primary node enabled";
    public static final String NoMonitorForSecNodes = "you must enable health monitoring to use secondary nodes";
    public static final String WontDeleteMonitorCauseSecNodes = "Can not delete HealthMonitor since this lb contains Secondary nodes that depend on it. Please remove secondary nodes first.";
    public static final String AH_USAGE_EVENT_FAILURE = "FailedAtomHopperUsageEvent";
    public static final String AH_USAGE_EVENT_AUTHOR = "AtomHopperUsageAuthor";
}
