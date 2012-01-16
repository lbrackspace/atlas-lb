package org.openstack.atlas.service.domain.common;

import java.text.MessageFormat;

public enum ErrorMessages {

    ACCOUNT_LIMIT_NOT_FOUND("No limit type found for {0}"),
    ACCOUNT_LIMIT_REACHED("{0} limit reached. Total must not exceed {1}."),

    DUPLICATE_ITEMS_FOUND("Must supply a unique {0} item to update the current list."),
    ACCESSLIST_NOT_FOUND("Access List not found."),
    NETWORK_ITEM_NOT_FOUND("Network Item {0} not found."),

    LB_NOT_FOUND("Load balancer not found."),
    LB_DELETED("The load balancer is deleted and considered immutable."),
    LB_IMMUTABLE("Load Balancer {0} has a status of {1} and is considered immutable."),
    LBS_NOT_FOUND("Must provide valid load balancers, {0} , could not be found."),
    LBS_IMMUTABLE("Must provide valid load balancers, {0} , are immutable and could not be processed."),

    VIP_NOT_FOUND("Virtual ip not found"),
    OUT_OF_VIPS("No available virtual ips. Please contact support."),
    VIP_TYPE_MISMATCH("The virtual ip type {0} does not match the existing type for the loadbalancer."),

    PORT_IN_USE("Port currently assigned to one of the virtual ips. Please try another port."),
    TCP_PORT_REQUIRED("Must Provide port for TCP Protocol."),
    PORT_HEALTH_MONITOR_INCOMPATIBLE("Cannot update port as the loadbalancer has a incompatible Health Monitor type"),

    HTTPS_HEALTH_MONITOR_PROTOCOL_INCOMPATIBLE("Protocol must be HTTPS for an HTTPS health monitor."),
    HTTP_HEALTH_MONITOR_PROTOCOL_INCOMPATIBLE("Protocol must be HTTPs for an HTTP health monitor.");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public String getMessage(Object... args) {
        return MessageFormat.format(message, args);
    }


/*      public static String immutableLoadBalancer(LoadBalancer lb) {
        //Are we or are we not using this format? **see Constants.LoadBalancerNotFound ..should we put that here?
        return String.format("Load Balancer '%d' has a status of '%s' and is considered immutable.", lb.getId(), lb.getStatus());
    }

    public static String mismatchingVipType(VirtualIpType vipType) {
        return String.format("The '%s' virtual ip type does not match the existing type for the loadbalancer.", vipType.name());
    }*/
}
