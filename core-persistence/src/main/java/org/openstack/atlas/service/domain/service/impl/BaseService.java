package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.ip.IPv4Cidr;
import org.openstack.atlas.common.ip.IPv4Cidrs;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.IPv6Cidrs;
import org.openstack.atlas.common.ip.exception.IPStringConversionException;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.common.ip.exception.IpTypeMissMatchException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.common.StringHelper;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.BlacklistRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BaseService {
    protected final Log LOG = LogFactory.getLog(BaseService.class);

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected BlacklistRepository blacklistRepository;

    public void isLbActive(LoadBalancer dbLoadBalancer) throws UnprocessableEntityException, ImmutableEntityException {
        if (dbLoadBalancer.getStatus().equals(CoreLoadBalancerStatus.DELETED)) {
            throw new UnprocessableEntityException(Constants.LoadBalancerDeleted);
        }

        if (!dbLoadBalancer.getStatus().equals(CoreLoadBalancerStatus.ACTIVE)) {
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
        out = testLb.getStatus().equals(CoreLoadBalancerStatus.ACTIVE);
        return out;
    }

    protected Node blackListedItemNode(Set<Node> nodes) throws IPStringConversionException1, IpTypeMissMatchException {
        IPv4Cidrs ip4Cidr = new IPv4Cidrs();
        IPv6Cidrs ip6Cidr = new IPv6Cidrs();
        Node badNode = new Node();
        List<BlacklistItem> blackItems = blacklistRepository.getAllBlacklistItems();

        for (Node testMe : nodes) {
            for (BlacklistItem bli : blackItems) {
                if (bli.getBlacklistType() == null || bli.getBlacklistType().equals(BlacklistType.NODE)) {
                    if (bli.getIpVersion().equals(IpVersion.IPV4)) {
                        ip4Cidr.getCidrs().add(new IPv4Cidr(bli.getCidrBlock()));
                        badNode.setAddress(testMe.getAddress());
                        if (ip4Cidr.contains(testMe.getAddress())) {
                            return badNode;
                        }
                    } else if (bli.getIpVersion().equals(IpVersion.IPV6)) {
                        ip6Cidr.getCidrs().add(new IPv6Cidr(bli.getCidrBlock()));
                        badNode.setAddress(testMe.getAddress());
                        if (ip6Cidr.contains(testMe.getAddress())) {
                            return badNode;
                        }
                    }
                }
            }
        }
        return null;
    }
}
