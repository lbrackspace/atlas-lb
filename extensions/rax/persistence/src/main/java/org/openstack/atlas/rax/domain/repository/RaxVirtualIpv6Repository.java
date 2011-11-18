package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;

import java.util.Set;

public interface RaxVirtualIpv6Repository extends VirtualIpv6Repository {
    Long getNumIpv6VipsForLoadBalancer(LoadBalancer lb);

    Set<VirtualIpv6> getVipsByLoadBalancerId(Integer id);
}
