package org.openstack.atlas.rax.api.integration;

import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;

import java.util.List;
import java.util.Set;

public interface RaxProxyService extends ReverseProxyLoadBalancerService {
    void addVirtualIps(Integer accountId, Integer lbId, Set<VirtualIp> ipv4Vips, Set<VirtualIpv6> ipv6Vips) throws Exception;

    void deleteVirtualIps(LoadBalancer dbLoadBalancer, List<Integer> vipIdsToDelete) throws Exception;
}
