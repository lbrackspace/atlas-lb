package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.*;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.openstack.atlas.service.domain.usage.repository.*;
import org.openstack.atlas.service.domain.util.Constants;
import org.openstack.atlas.util.ip.IPv4Cidr;
import org.openstack.atlas.util.ip.IPv4Cidrs;
import org.openstack.atlas.util.ip.IPv6Cidr;
import org.openstack.atlas.util.ip.IPv6Cidrs;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.openstack.atlas.util.ip.exception.IpTypeMissMatchException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entities.LoadBalancerStatus.DELETED;

public class BaseService {
    protected final Log LOG = LogFactory.getLog(BaseService.class);

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected AccountLimitRepository accountLimitRepository;
    @Autowired
    protected VirtualIpRepository virtualIpRepository;
    @Autowired
    protected VirtualIpv6Repository virtualIpv6Repository;
    @Autowired
    protected BlacklistRepository blacklistRepository;
    @Autowired
    protected HostRepository hostRepository;
    @Autowired
    protected LoadBalancerEventRepository loadBalancerEventRepository;
    @Autowired
    protected AlertRepository alertRepository;
    @Autowired
    protected GroupRepository groupRepository;
    @Autowired
    protected ClusterRepository clusterRepository;
    @Autowired
    protected LoadbalancerMetadataRepository loadbalancerMetadataRepository;
    @Autowired
    protected NodeMetadataRepository nodeMetadataRepository;
    @Autowired
    protected NodeRepository nodeRepository;
    @Autowired
    protected RateLimitRepository rateLimitRepository;
    @Autowired
    protected JobStateRepository jobStateRepository;
    @Autowired
    protected SslTerminationRepository sslTerminationRepository;
    @Autowired
    protected UsageRepository usageRepository;
    @Autowired
    protected HostUsageRepository hostUsageRepository;
    @Autowired
    protected LoadBalancerUsageRepository loadBalancerUsageRepository;
    @Autowired
    protected AllowedDomainsRepository allowedDomainsRepository;
    @Autowired
    protected LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository;
    @Autowired
    protected LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository;
    @Autowired
    protected HostUsageRefactorRepository hostUsageRefactorRepository;

    static {
        org.openstack.atlas.util.ca.primitives.RsaConst.init();
    }

    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    public void setAccountLimitRepository(AccountLimitRepository accountLimitRepository) {
        this.accountLimitRepository = accountLimitRepository;
    }

    public void setVirtualIpRepository(VirtualIpRepository virtualIpRepository) {
        this.virtualIpRepository = virtualIpRepository;
    }

    public void setVirtualIpv6Repository(VirtualIpv6Repository virtualIpv6Repository) {
        this.virtualIpv6Repository = virtualIpv6Repository;
    }

    public void setBlacklistRepository(BlacklistRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public void setLoadBalancerEventRepository(LoadBalancerEventRepository loadBalancerEventRepository) {
        this.loadBalancerEventRepository = loadBalancerEventRepository;
    }

    public void setAlertRepository(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void setClusterRepository(ClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    public void setLoadbalancerMetadataRepository(LoadbalancerMetadataRepository loadbalancerMetadataRepository) {
        this.loadbalancerMetadataRepository = loadbalancerMetadataRepository;
    }

    public void setNodeMetadataRepository(NodeMetadataRepository nodeMetadataRepository) {
        this.nodeMetadataRepository = nodeMetadataRepository;
    }

    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public void setRateLimitRepository(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
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

    public void setHostUsageRepository(HostUsageRepository hostUsageRepository) {
        this.hostUsageRepository = hostUsageRepository;
    }

    public void setAllowedDomainsRepository(AllowedDomainsRepository allowedDomainsRepository) {
        this.allowedDomainsRepository = allowedDomainsRepository;
    }

    public void setLoadBalancerStatusHistoryRepository(LoadBalancerStatusHistoryRepository loadBalancerStatusHistoryRepository) {
        this.loadBalancerStatusHistoryRepository = loadBalancerStatusHistoryRepository;
    }

    public void setLoadBalancerMergedHostUsageRepository(LoadBalancerMergedHostUsageRepository loadBalancerMergedHostUsageRepository) {
        this.loadBalancerMergedHostUsageRepository = loadBalancerMergedHostUsageRepository;
    }

    public void setHostUsageRefactorRepository(HostUsageRefactorRepository hostUsageRefactorRepository) {
        this.hostUsageRefactorRepository = hostUsageRefactorRepository;
    }

    public void setLoadBalancerUsageRepository(LoadBalancerUsageRepository loadBalancerUsageRepository) {
        this.loadBalancerUsageRepository = loadBalancerUsageRepository;
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

    public boolean verifyForOp(LoadBalancer dbLoadBalancer) throws UnprocessableEntityException, ImmutableEntityException, EntityNotFoundException {
        return loadBalancerRepository.getByIdForOp(dbLoadBalancer.getId());
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
}
