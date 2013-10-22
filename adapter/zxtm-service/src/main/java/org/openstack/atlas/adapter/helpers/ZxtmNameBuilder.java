package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;

import java.util.HashSet;
import java.util.Set;

public final class ZxtmNameBuilder {
    private static final String ssl_suffix = "_S";
    private static final String redirect_suffix = "_R";

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

        return accountId + "_" + lbId + ssl_suffix;
    }

    public static String genRedirectVSName(Integer lbId, Integer accountId) throws InsufficientRequestException {
        if (lbId == null) {
            throw new InsufficientRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new InsufficientRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId + redirect_suffix;
    }

    public static String genSslVSName(String vsName) {
        return vsName + ssl_suffix;
    }

    public static String genRedirectVSName(String vsName) {
        return vsName + redirect_suffix;
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

    public static String genSslVSName(LoadBalancer lb) throws InsufficientRequestException {
        if (lb.getAccountId() == null)
            throw new InsufficientRequestException(
                    "Missing account id for load balancer.");
        if (lb.getId() == null)
            throw new InsufficientRequestException(
                    "Missing id for load balancer.");
        return genSslVSName(lb.getId(), lb.getAccountId());
    }

    public static String genRedirectVSName(LoadBalancer lb) throws InsufficientRequestException {
        if (lb.getAccountId() == null)
            throw new InsufficientRequestException(
                    "Missing account id for load balancer.");
        if (lb.getId() == null)
            throw new InsufficientRequestException(
                    "Missing id for load balancer.");
        return genRedirectVSName(lb.getId(), lb.getAccountId());
    }

    public static Set<String> generateNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws InsufficientRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(genVSName(loadBalancer));
        }
        return generatedNames;
    }

    public static Set<String> generateSslNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws InsufficientRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(genSslVSName(loadBalancer));
        }
        return generatedNames;
    }

    public static Set<String> generateRedirectNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws InsufficientRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(genRedirectVSName(loadBalancer));
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
