package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.common.StringHelper;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.openstack.atlas.service.domain.entity.LoadBalancerStatus.ACTIVE;
import static org.openstack.atlas.service.domain.entity.LoadBalancerStatus.DELETED;

public class BaseService {
    protected final Log LOG = LogFactory.getLog(LoadBalancerServiceImpl.class);

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected VirtualIpRepository virtualIpRepository;
    @Autowired
    protected VirtualIpv6Repository virtualIpv6Repository;

    @Autowired
    protected HostRepository hostRepository;

    @Autowired
    protected ClusterRepository clusterRepository;
    @Autowired
    protected NodeRepository nodeRepository;

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

   /*protected Node blackListedItemNode(Set<Node> nodes) throws IPStringConversionException, IpTypeMissMatchException {
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
    }*/

}
