package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;

import java.util.HashSet;
import java.util.Set;

public final class ZxtmNameBuilder {

    public static String genVSName(Integer lbId, Integer accountId) throws InsufficientRequestException {
        if (lbId == null) {
            throw new InsufficientRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new InsufficientRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId;
    }

    public static String genSslVSName(Integer lbId, Integer accountId) throws InsufficientRequestException {
        if (lbId == null) {
            throw new InsufficientRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new InsufficientRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId + "_S";
    }

    public static String genVSName(LoadBalancer lb) throws InsufficientRequestException {
        if (lb.getAccountId() == null)
            throw new InsufficientRequestException(
                    "Missing account id for load balancer.");
        if (lb.getId() == null)
            throw new InsufficientRequestException(
                    "Missing id for load balancer.");
        return genVSName(lb.getId(), lb.getAccountId());
    }

    public static Set<String> generateNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws InsufficientRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(genVSName(loadBalancer));
            //TODO: figure out usage for ssl
//            if (loadBalancer.hasSsl()) {
//                generatedNames.add(genSslVSName(loadBalancer.getId(), loadBalancer.getAccountId()));
//            }
        }
        return generatedNames;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, Integer vipId) throws InsufficientRequestException {
        if (vipId == null)
            throw new InsufficientRequestException("Missing id for virtual ip.");
        return lb.getAccountId() + "_" + vipId;
    }

    public static String generateTrafficIpGroupName(Integer accountId, String vipId) throws InsufficientRequestException {
        if (vipId == null)
            throw new InsufficientRequestException("Missing id for virtual ip.");
        return accountId + "_" + vipId;
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

    public static String generateErrorPageNameWithAccountIdAndLoadBalancerId(Integer lbId, Integer accountId) throws InsufficientRequestException {
        if (lbId == null) {
            throw new InsufficientRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new InsufficientRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId + "_error.html";
    }
}
