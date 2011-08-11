package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.common.ip.exception.IPBlocksOverLapException;
import org.openstack.atlas.common.ip.exception.IPOctetOutOfRangeException;
import org.openstack.atlas.common.ip.exception.IPRangeTooBigException;
import org.openstack.atlas.common.ip.exception.IPStringConversionException;
import org.openstack.atlas.service.domain.entity.Cluster;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojo.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.pojo.VirtualIpBlocks;

import java.util.List;

public interface ClusterService {
    public Cluster get(Integer clusterId) throws EntityNotFoundException;

    public List<Cluster> getAll();

    public List<LoadBalancerCountByAccountIdClusterId> getAccountsInCluster(Integer clusterId);

    public List<Host> getHosts(Integer clusterId);

    public List<VirtualIp> getVirtualIps(Integer id, Integer offset, Integer limit);

    //public List<LBDeviceRateLimitedLoadBalancer> getRateLimitedLoadBalancersInCluster(Integer clusterId) throws EntityNotFoundException;

    //public List<AccountGroup> getAPIRateLimitedAccounts(Integer clusterId) throws EntityNotFoundException;

    public Integer getNumberOfUniqueAccountsForCluster(Integer id);

    public Integer getNumberOfActiveLoadBalancersForCluster(Integer id);

    //public List<VirtualIpAvailabilityReport> getVirtualIpAvailabilityReport(Integer clusterId);

    public VirtualIpBlocks addVirtualIpBlocks(VirtualIpBlocks vipBlocks, Integer clusterId) throws BadRequestException, IPStringConversionException, IPBlocksOverLapException, IPRangeTooBigException, IPOctetOutOfRangeException, EntityNotFoundException;
}
