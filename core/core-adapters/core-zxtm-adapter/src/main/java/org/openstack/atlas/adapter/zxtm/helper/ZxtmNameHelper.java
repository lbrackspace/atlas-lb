package org.openstack.atlas.adapter.zxtm.helper;

import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;

import java.util.HashSet;
import java.util.Set;

public final class ZxtmNameHelper {

    public static String generateNameWithAccountIdAndLoadBalancerId(Integer lbId, Integer accountId) throws BadRequestException {
        if (lbId == null) {
            throw new BadRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new BadRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId;
    }

    public static String generateNameWithAccountIdAndLoadBalancerId(LoadBalancer lb) throws BadRequestException {
        if (lb.getAccountId() == null)
            throw new BadRequestException(
                    "Missing account id for load balancer.");
        if (lb.getId() == null)
            throw new BadRequestException(
                    "Missing id for load balancer.");
        return generateNameWithAccountIdAndLoadBalancerId(lb.getId(), lb.getAccountId());
    }

    public static Set<String> generateNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws BadRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(generateNameWithAccountIdAndLoadBalancerId(loadBalancer));
        }
        return generatedNames;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, Integer vipId) throws BadRequestException {
        if (vipId == null)
            throw new BadRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vipId;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, VirtualIp vip) throws BadRequestException {
        if (vip.getId() == null)
            throw new BadRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vip.getId();
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, VirtualIpv6 vip) throws BadRequestException {
        if (vip.getId() == null)
            throw new BadRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vip.getId();
    }

    public static Integer stripAccountIdFromName(String name) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(name.split("_")[0]);
    }

    public static Integer stripLbIdFromName(String name) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(name.split("_")[1]);
    }
}
