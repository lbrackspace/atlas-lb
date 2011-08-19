package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.ip.IPv4Cidr;
import org.openstack.atlas.common.ip.IPv4Cidrs;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.IPv6Cidrs;
import org.openstack.atlas.service.domain.entity.BlacklistItem;
import org.openstack.atlas.service.domain.entity.BlacklistType;
import org.openstack.atlas.service.domain.entity.IpVersion;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.repository.BlacklistRepository;
import org.openstack.atlas.service.domain.service.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BlacklistServiceImpl implements BlacklistService {
    private final Log LOG = LogFactory.getLog(BlacklistServiceImpl.class);

    @Autowired
    private BlacklistRepository blacklistRepository;

    public void verifyNoBlacklistNodes(Set<Node> nodes) throws BadRequestException {
        IPv4Cidrs ip4Cidr = new IPv4Cidrs();
        IPv6Cidrs ip6Cidr = new IPv6Cidrs();
        Node badNode = new Node();
        List<BlacklistItem> blackItems = blacklistRepository.getAllBlacklistItems();

        try {
            loop: for (Node node : nodes) {
                for (BlacklistItem blacklistItem : blackItems) {
                    if (blacklistItem.getBlacklistType() == null || blacklistItem.getBlacklistType().equals(BlacklistType.NODE)) {
                        if (blacklistItem.getIpVersion().equals(IpVersion.IPV4)) {
                            ip4Cidr.getCidrs().add(new IPv4Cidr(blacklistItem.getCidrBlock()));
                            if (ip4Cidr.contains(node.getAddress())) {
                                badNode.setAddress(node.getAddress());
                                break loop;
                            }
                        } else if (blacklistItem.getIpVersion().equals(IpVersion.IPV6)) {
                            ip6Cidr.getCidrs().add(new IPv6Cidr(blacklistItem.getCidrBlock()));
                            if (ip6Cidr.contains(node.getAddress())) {
                                badNode.setAddress(node.getAddress());
                                break loop;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BadRequestException(String.format("Error while trying to validate the nodes."));
        }
        if (badNode != null && badNode.getAddress() != null) {
            LOG.info("Found a blacklisted node: " + badNode);
            throw new BadRequestException(String.format("Invalid node address. The address '%s' is currently not accepted for this request.", badNode.getAddress()));
        }
    }
}
