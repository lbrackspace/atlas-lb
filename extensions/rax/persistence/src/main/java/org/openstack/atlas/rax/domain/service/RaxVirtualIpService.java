package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.service.VirtualIpService;

import java.util.List;

public interface RaxVirtualIpService extends VirtualIpService {

    VirtualIpv6 addIpv6VirtualIpToLoadBalancer(VirtualIpv6 virtualIpv6, LoadBalancer loadBalancer) throws PersistenceServiceException;

    void prepareForVirtualIpDeletion(LoadBalancer lb, Integer id) throws PersistenceServiceException;

    void prepareForVirtualIpsDeletion(Integer accountId, Integer loadbalancerId, List<Integer> virtualIpIds) throws PersistenceServiceException;

    boolean hasAtLeastMinRequiredVips(LoadBalancer lb, List<Integer> virtualIpIds);

    boolean hasExactlyMinRequiredVips(LoadBalancer lb);

    boolean doesVipBelongToLoadBalancer(LoadBalancer lb, Integer vipId);

    boolean doesVipBelongToAccount(VirtualIp virtualIp, Integer accountId);

    void removeVipsFromLoadBalancer(LoadBalancer lb, List<Integer> vipIdsToDelete);

    void removeVipFromLoadBalancer(LoadBalancer lb, Integer vipId);
}
