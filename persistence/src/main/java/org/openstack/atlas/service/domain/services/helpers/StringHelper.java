package org.openstack.atlas.service.domain.services.helpers;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.VirtualIpType;

public final class StringHelper {

    public static String immutableLoadBalancer(LoadBalancer lb) {
        //Are we or are we not using this format? **see Constants.LoadBalancerNotFound ..should we put that here?
        return String.format("Load Balancer '%d' has a status of '%s' and is considered immutable.", lb.getId(), lb.getStatus());
    }

    public static String immutableLoadBalancer(int id, LoadBalancerStatus loadBalancerStatus) {
        //Are we or are we not using this format? **see Constants.LoadBalancerNotFound ..should we put that here?
        return String.format("Load Balancer '%d' has a status of '%s' and is considered immutable.", id, loadBalancerStatus);
    }

    public static String mismatchingVipType(VirtualIpType vipType) {
        return String.format("The '%s' virtual ip type does not match the existing type for the loadbalancer.", vipType.name());
    }

    public static String imutableSslTermination(SslTermination sslTermination) {
        return String.format("The ssl termination with id: '%s' currently exists on loadbalancer '%s'. Please DELETE/POST or UPDATE to change the ssl credentials", sslTermination.getId(), sslTermination.getLoadbalancer().getId());
    }


}
