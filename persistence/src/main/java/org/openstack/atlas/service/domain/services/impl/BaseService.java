package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;

import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.*;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageEventRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.ip.IPv4Cidr;
import org.openstack.atlas.util.ip.IPv4Cidrs;
import org.openstack.atlas.util.ip.IPv6Cidr;
import org.openstack.atlas.util.ip.IPv6Cidrs;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.DELETED;

public class BaseService {
    protected final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    protected LoadBalancerRepository loadBalancerRepository;
    protected AccountLimitRepository accountLimitRepository;
    protected VirtualIpRepository virtualIpRepository;
    protected VirtualIpv6Repository virtualIpv6Repository;
    protected BlacklistRepository blacklistRepository;
    protected HostRepository hostRepository;
    protected LoadBalancerEventRepository loadBalancerEventRepository;
    protected AlertRepository alertRepository;
    protected GroupRepository groupRepository;
    protected ClusterRepository clusterRepository;
    protected MetadataRepository metadataRepository;
    protected NodeRepository nodeRepository;
    protected RateLimitRepository rateLimitRepository;
    protected JobStateRepository jobStateRepository;
    protected SslTerminationRepository sslTerminationRepository;
    protected UsageRepository usageRepository;
    protected LoadBalancerUsageRepository loadBalancerUsageRepository;
    protected LoadBalancerUsageEventRepository loadBalancerUsageEventRepository;
    protected AllowedDomainsRepository allowedDomainsRepository;

    static {
        org.openstack.atlas.util.ca.primitives.RsaConst.init();
    }

    public void setRateLimitRepository(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
    }

    public void setMetadataRepository(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public void setAlertRepository(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public void setLoadBalancerEventRepository(LoadBalancerEventRepository loadBalancerEventRepository) {
        this.loadBalancerEventRepository = loadBalancerEventRepository;
    }

    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    public void setAccountLimitRepository(AccountLimitRepository accountLimitRepository) {
        this.accountLimitRepository = accountLimitRepository;
    }

    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    public void setBlacklistRepository(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void setClusterRepository(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    public void setJobStateRepository(JobStateRepository jobStateRepository) {
        this.jobStateRepository = jobStateRepository;
    }

    public void setSslTerminationRepository(SslTerminationRepository sslTerminationRepository) {
        this.sslTerminationRepository = sslTerminationRepository;
    }

    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    public void setLoadBalancerUsageRepository(LoadBalancerUsageRepository loadBalancerUsageRepository) {
        this.loadBalancerUsageRepository = loadBalancerUsageRepository;
    }

    public void setLoadBalancerUsageEventRepository(LoadBalancerUsageEventRepository loadBalancerUsageEventRepository) {
        this.loadBalancerUsageEventRepository = loadBalancerUsageEventRepository;
    }

    public void setAllowedDomainsRepository(AllowedDomainsRepository allowedDomainsRepository) {
        this.allowedDomainsRepository = allowedDomainsRepository;
    }

    public void isLbActive(LoadBalancer dbLoadBalancer) throws UnprocessableEntityException, ImmutableEntityException {
        if (dbLoadBalancer.getStatus().equals(DELETED)) {
            throw new UnprocessableEntityException(Constants.LoadBalancerDeleted);
        }

        if (!dbLoadBalancer.getStatus().equals(ACTIVE)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

    }

    protected boolean isActiveLoadBalancer(LoadBalancer rLb, boolean refetch) throws EntityNotFoundException {
        boolean out;
        LoadBalancer testLb;
        if (refetch) {
            testLb = loadBalancerRepository.getByIdAndAccountId(rLb.getId(), rLb.getAccountId());
        } else {
            testLb = rLb;
        }
        out = testLb.getStatus().equals(LoadBalancerStatus.ACTIVE);
        return out;
    }

   protected Node blackListedItemNode(Set<Node> nodes) throws IPStringConversionException, IpTypeMissMatchException {
        IPv4Cidrs ip4Cidr = new IPv4Cidrs();
        IPv6Cidrs ip6Cidr = new IPv6Cidrs();
        Node badNode = new Node();
        List<BlacklistItem> blackItems = blacklistRepository.getAllBlacklistItems();

        for (Node testMe : nodes) {
            for (BlacklistItem bli : blackItems) {
                if (bli.getBlacklistType() == null || bli.getBlacklistType().equals(BlacklistType.NODE)) {
                    if (bli.getIpVersion().equals(IpVersion.IPV4)) {
                        ip4Cidr.getCidrs().add(new IPv4Cidr(bli.getCidrBlock()));
                        badNode.setIpAddress(testMe.getIpAddress());
                        if (ip4Cidr.contains(testMe.getIpAddress())) {
                            return badNode;
                        }
                    } else if (bli.getIpVersion().equals(IpVersion.IPV6)) {
                        ip6Cidr.getCidrs().add(new IPv6Cidr(bli.getCidrBlock()));
                        badNode.setIpAddress(testMe.getIpAddress());
                        if (ip6Cidr.contains(testMe.getIpAddress())) {
                            return badNode;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected AccessList blackListedItemAccessList(Set<AccessList> accessLists) throws IPStringConversionException, IpTypeMissMatchException {
        IPv4Cidrs ip4Cidr = new IPv4Cidrs();
        IPv6Cidrs ip6Cidr = new IPv6Cidrs();
        AccessList badAccessListItem = new AccessList();
        List<BlacklistItem> blackItems = blacklistRepository.getAllBlacklistItems();

        for (AccessList testMe : accessLists) {
            for (BlacklistItem bli : blackItems) {
                if (bli.getBlacklistType() == null || bli.getBlacklistType().equals(BlacklistType.ACCESSLIST)) {
                    if (bli.getIpVersion().equals(IpVersion.IPV4)) {
                        ip4Cidr.getCidrs().add(new IPv4Cidr(bli.getCidrBlock()));
                        badAccessListItem.setIpAddress(testMe.getIpAddress());
                        if (ip4Cidr.contains(testMe.getIpAddress())) {
                            return badAccessListItem;
                        }
                    } else if (bli.getIpVersion().equals(IpVersion.IPV6)) {
                        ip6Cidr.getCidrs().add(new IPv6Cidr(bli.getCidrBlock()));
                        badAccessListItem.setIpAddress(testMe.getIpAddress());
                        if (ip6Cidr.contains(testMe.getIpAddress())) {
                            return badAccessListItem;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setVirtualIpv6Repository(VirtualIpv6Repository virtualIpv6Repository) {
        this.virtualIpv6Repository = virtualIpv6Repository;
    }
}
