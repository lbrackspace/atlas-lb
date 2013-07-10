package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VirtualIpService {

    public VirtualIp get(Integer id) throws EntityNotFoundException;

    public Set<VirtualIp> get(Integer accountId, Integer loadBalancerId, Integer offset, Integer limit, Integer marker) throws EntityNotFoundException, DeletedStatusException;

    public Set<VirtualIpv6> getVirtualIpv6ByAccountIdLoadBalancerId(Integer aid, Integer lid) throws EntityNotFoundException;

    public Set<VirtualIpv6> getVirtualIpv6ByLoadBalancerId(Integer lid) throws EntityNotFoundException;

    public List<LoadBalancer> getLoadBalancerByVipId(Integer vipId);

    public List<LoadBalancer> getLoadBalancerByVip6Id(Integer vip6Id);

    public List<LoadBalancer> getLoadBalancerByVipAddress(String address);
    
    public List<LoadBalancer> getLoadBalancerByVip6Address(String address) throws IPStringConversionException, EntityNotFoundException;

    public VirtualIp allocateIpv4VirtualIp(VirtualIp vipConfig, Cluster cluster) throws OutOfVipsException;

    public VirtualIpv6 allocateIpv6VirtualIp(LoadBalancer loadBalancer) throws EntityNotFoundException;

    public VirtualIp addVirtualIpToLoadBalancer(VirtualIp vipConfig, LoadBalancer lb, Ticket ticket) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException, OutOfVipsException, UniqueLbPortViolationException, AccountMismatchException;

    public VirtualIpv6 addIpv6VirtualIpToLoadBalancer(VirtualIpv6 vipConfig, LoadBalancer lb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException, BadRequestException, OutOfVipsException, UniqueLbPortViolationException, AccountMismatchException, LimitReachedException;

    public boolean isVipAllocatedToMultipleLoadBalancers(VirtualIp virtualIp);

    public boolean isVipAllocatedToAnyLoadBalancer(VirtualIp virtualIp);

    public boolean isVipAllocatedToAnotherLoadBalancer(LoadBalancer lb, VirtualIp virtualIp);

    public boolean isIpv6VipAllocatedToAnotherLoadBalancer(LoadBalancer lb, VirtualIpv6 virtualIp);

    public void removeAllVipsFromLoadBalancer(LoadBalancer lb);

    public void removeVipFromLoadBalancer(LoadBalancer lb, Integer vipId);

    public void removeVipsFromLoadBalancer(LoadBalancer lb, List<Integer> vipIds);

    public void prepareForVirtualIpDeletion(LoadBalancer lb, Integer vipId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;

    public void prepareForVirtualIpsDeletion(Integer accountId, Integer loadbalancerId, List<Integer> virtualIpIds) throws EntityNotFoundException, BadRequestException, UnprocessableEntityException, ImmutableEntityException;

    public boolean hasAtLeastMinRequiredVips(LoadBalancer lb, List<Integer> virtualIpIds);

    public boolean hasExactlyMinRequiredVips(LoadBalancer lb);

    public boolean doesVipBelongToLoadBalancer(LoadBalancer lb, Integer vipId);

    public boolean doesVipBelongToAccount(VirtualIp virtualIp, Integer accountId);

    public void removeVipFromCluster(VirtualIp virtualIp) throws ImmutableEntityException, EntityNotFoundException;

    public boolean isIpv4VipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort);

    public boolean isIpv6VipPortCombinationInUse(VirtualIpv6 virtualIp, Integer loadBalancerPort);

    public void persist(Object obj);

    public List<VirtualIp> getVipsByClusterId(Integer clusterId);

    public Integer getNextVipOctet(Integer accountId);

    public VirtualIpv6 newVirtualIpv6(Integer clusterId, Integer accountId, Integer vipOctets) throws EntityNotFoundException;

    public List<Integer> genSha1SumsForAccountTable() throws NoSuchAlgorithmException;

    public void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException;

    public String getVirtualIpv6String(Integer vip6Id) throws EntityNotFoundException, IPStringConversionException;

    public String getVirtualIpv6String(VirtualIpv6 vip6) throws IPStringConversionException;

    public Map<Integer, List<VirtualIp>> getAllocatedVipsMappedByLbId();

}
