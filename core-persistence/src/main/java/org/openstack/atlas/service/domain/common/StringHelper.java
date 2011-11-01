package org.openstack.atlas.service.domain.common;


import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIpType;

public final class StringHelper {

    public static String immutableLoadBalancer(LoadBalancer lb) {
        //Are we or are we not using this format? **see Constants.LoadBalancerNotFound ..should we put that here?
        return String.format("Load Balancer '%d' has a status of '%s' and is considered immutable.", lb.getId(), lb.getStatus());
    }

    public static String mismatchingVipType(VirtualIpType vipType) {
        return String.format("The '%s' virtual ip type does not match the existing type for the loadbalancer.", vipType.name());
    }


}
