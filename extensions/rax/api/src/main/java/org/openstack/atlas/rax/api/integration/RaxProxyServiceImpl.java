package org.openstack.atlas.rax.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.rax.adapter.zxtm.RaxZxtmAdapter;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Primary
@Service
public class RaxProxyServiceImpl extends ReverseProxyLoadBalancerServiceImpl implements RaxProxyService {

    @Override
    public void addVirtualIps(Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).addVirtualIps(config, accountId, lbId, ipv4Vips, ipv6Vips);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer dbLoadBalancer, List<Integer> vipIdsToDelete) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(dbLoadBalancer.getId());
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).deleteVirtualIps(config, dbLoadBalancer, vipIdsToDelete);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateAccessList(Integer accountId, Integer lbId, Collection<RaxAccessList> accessListItems) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).updateAccessList(config, accountId, lbId, accessListItems);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteAccessList(Integer accountId, Integer lbId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).deleteAccessList(config, accountId, lbId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void updateConnectionLogging(Integer accountId, Integer lbId, boolean isConnectionLogging, String protocol) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(lbId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).updateConnectionLogging(config, accountId, lbId, isConnectionLogging, protocol);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void uploadDefaultErrorPage(Integer clusterId, String content) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyClusterId(clusterId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).uploadDefaultErrorPage(config, content);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void setDefaultErrorPage(Integer loadBalancerId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancerId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).setDefaultErrorPage(config, accountId, loadBalancerId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void setErrorPage(Integer loadBalancerId, Integer accountId, String content) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancerId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).setErrorPage(config, accountId, loadBalancerId, content);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public void deleteErrorPage(Integer loadBalancerId, Integer accountId) throws Exception {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancerId);
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).deleteErrorPage(config, accountId, loadBalancerId);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }
}
