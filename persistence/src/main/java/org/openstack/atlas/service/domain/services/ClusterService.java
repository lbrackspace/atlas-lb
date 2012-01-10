package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusRateLimitedLoadBalancer;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPRangeTooBigException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.service.domain.entities.AccountGroup;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ClusterStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport;
import org.openstack.atlas.service.domain.pojos.VirtualIpBlocks;

import java.util.List;

public interface ClusterService {
    public Cluster get(Integer clusterId) throws EntityNotFoundException;

    public Cluster getActiveCluster() throws EntityNotFoundException, ClusterStatusException;

    public List<Cluster> getAll();

    public List<LoadBalancerCountByAccountIdClusterId> getAccountsInCluster(Integer clusterId);

    public List<Host> getHosts(Integer clusterId);

    public List<VirtualIp> getVirtualIps(Integer id, Integer offset, Integer limit);

    public List<ZeusRateLimitedLoadBalancer> getRateLimitedLoadBalancersInCluster(Integer clusterId) throws EntityNotFoundException;

    public List<AccountGroup> getAPIRateLimitedAccounts(Integer clusterId) throws EntityNotFoundException;

    public Integer getNumberOfUniqueAccountsForCluster(Integer id);

    public Integer getNumberOfActiveLoadBalancersForCluster(Integer id);

    public List<VirtualIpAvailabilityReport> getVirtualIpAvailabilityReport(Integer clusterId);

    public VirtualIpBlocks addVirtualIpBlocks(VirtualIpBlocks vipBlocks, Integer clusterId) throws BadRequestException, IPStringConversionException, IPBlocksOverLapException, IPRangeTooBigException, IPOctetOutOfRangeException, EntityNotFoundException;
}
