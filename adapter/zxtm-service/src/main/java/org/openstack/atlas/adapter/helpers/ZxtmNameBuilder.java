package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;

import java.util.HashSet;
import java.util.Set;

public final class ZxtmNameBuilder {

    public static String generateNameWithAccountIdAndLoadBalancerId(Integer lbId, Integer accountId) throws InsufficientRequestException {
        if (lbId == null) {
            throw new InsufficientRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new InsufficientRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId;
    }

    public static String generateNameWithAccountIdAndLoadBalancerId(LoadBalancer lb) throws InsufficientRequestException {
        if (lb.getAccountId() == null)
            throw new InsufficientRequestException(
                    "Missing account id for load balancer.");
        if (lb.getId() == null)
            throw new InsufficientRequestException(
                    "Missing id for load balancer.");
        return generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
    }

    public static Set<String> generateNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws InsufficientRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(generateNameWithAccountIdAndLoadBalancerId(loadBalancer));
        }
        return generatedNames;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, Integer vipId) throws InsufficientRequestException {
        if (vipId == null)
            throw new InsufficientRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vipId;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, VirtualIp vip) throws InsufficientRequestException {
        if (vip.getId() == null)
            throw new InsufficientRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vip.getId();
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, VirtualIpv6 vip) throws InsufficientRequestException {
        if (vip.getId() == null)
            throw new InsufficientRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vip.getId();
    }
}
