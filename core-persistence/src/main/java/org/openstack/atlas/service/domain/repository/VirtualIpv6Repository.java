package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.Account;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VirtualIpv6Repository {

    List<VirtualIpv6> getVipsByAccountId(Integer accountId);

    Set<VirtualIpv6> getVipsByLoadBalancerId(Integer loadBalancerId);

    List<Integer> getAccountIdsAlreadyShaHashed();

    void deleteVirtualIp(VirtualIpv6 virtualIpv6);

    void removeJoinRecord(LoadBalancerJoinVip6 loadBalancerJoinVip6);

    List<LoadBalancerJoinVip6> getJoinRecordsForVip(VirtualIpv6 virtualIp);

    Account getLockedAccountRecord(Integer accountId);

    Integer getNextVipOctet(Integer accountId);

    Map<Integer, List<LoadBalancer>> getPorts(Integer vid);
}
