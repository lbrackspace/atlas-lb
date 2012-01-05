package org.openstack.atlas.service.domain.common;

public final class Constants {
    public static final int NUM_DAYS_OF_USAGE = 90;
    public static final int DEFAULT_NODE_WEIGHT = 1;
    public static final int NUM_DAYS_BEFORE_VIP_REUSE = 1;
    public static final int MIN_REQUIRED_VIPS = 1;
    public static final int MIN_ACCOUNTS_PER_VIP = 1;
    public static final String LoadBalancerDeleted = "The load balancer is deleted and considered immutable.";

    public static final int MAX_ERRORPAGE_CONTENT_LENGTH = 1024*64;


 /*   public static final String LoadBalancerNotFound = "Load balancer not found";
    public static final String VirtualIpNotFound = "Virtual ip not found";
    public static final String LoadBalancerDeleted = "The load balancer is deleted and considered immutable.";
    public static final String OutOfVips = "No available virtual ips. Please contact support.";*/
}
