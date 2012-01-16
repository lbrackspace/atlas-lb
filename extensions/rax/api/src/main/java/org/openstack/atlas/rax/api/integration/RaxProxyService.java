package org.openstack.atlas.rax.api.integration;

import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RaxProxyService extends ReverseProxyLoadBalancerService {
    void addVirtualIps(Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws Exception;

    void deleteVirtualIps(LoadBalancer dbLoadBalancer, List<Integer> vipIdsToDelete) throws Exception;

    void updateAccessList(Integer accountId, Integer lbId, Collection<RaxAccessList> accessListItems) throws Exception;

    void deleteAccessList(Integer accountId, Integer lbId) throws Exception;

    void updateConnectionLogging(Integer accountId, Integer lbId, boolean connectionLogging, String protocol) throws Exception;

    void uploadDefaultErrorPage(Integer clusterId, String content) throws Exception;

    void setDefaultErrorPage(Integer loadBalancerId, Integer accountId) throws Exception;

    void setErrorPage(Integer loadBalancerId, Integer accountId, String content) throws Exception;

    void deleteErrorPage(Integer loadBalancerId, Integer accountId) throws Exception;
}
