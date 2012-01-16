package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;

import java.util.List;

public interface RaxVirtualIpRepository extends VirtualIpRepository {
    Long getNumIpv4VipsForLoadBalancer(LoadBalancer lb);

    List<VirtualIp> getVipsByLoadBalancerId(Integer id);

    List<Integer> getAccountIds(VirtualIp virtualIp);
}
