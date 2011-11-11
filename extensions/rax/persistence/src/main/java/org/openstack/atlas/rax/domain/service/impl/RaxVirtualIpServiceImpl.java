package org.openstack.atlas.rax.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.domain.service.RaxVirtualIpService;
import org.openstack.atlas.service.domain.common.StringHelper;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.AccountLimitService;
import org.openstack.atlas.service.domain.service.impl.VirtualIpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
public class RaxVirtualIpServiceImpl extends VirtualIpServiceImpl implements RaxVirtualIpService {
    private final Log LOG = LogFactory.getLog(RaxVirtualIpServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, UnprocessableEntityException.class, ImmutableEntityException.class, BadRequestException.class, OutOfVipsException.class, UniqueLbPortViolationException.class, AccountMismatchException.class})
    public VirtualIpv6 addIpv6VirtualIpToLoadBalancer(VirtualIpv6 vipConfig, LoadBalancer lb) throws PersistenceServiceException {
        VirtualIpv6 vipToAdd;
        LoadBalancer dlb = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());

        Integer ipv6Limit = accountLimitService.getLimit(dlb.getAccountId(), AccountLimitType.IPV6_LIMIT);
        if (dlb.getLoadBalancerJoinVip6Set().size() >= ipv6Limit) {
            throw new BadRequestException(String.format("Your load balancer cannot have more than %d IPv6 virtual ips.", ipv6Limit));
        }

        if (!vipTypeMatchesTypeForLoadBalancer(VirtualIpType.PUBLIC, dlb)) {
            String message = StringHelper.mismatchingVipType(VirtualIpType.PUBLIC);
            LOG.debug(message);
            throw new BadRequestException(message);
        }

        vipToAdd = allocateIpv6VirtualIp(dlb);

        if (!loadBalancerRepository.testAndSetStatus(dlb.getAccountId(), dlb.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dlb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        LoadBalancerJoinVip6 jv = new LoadBalancerJoinVip6(dlb.getPort(), dlb, vipToAdd);
        virtualIpRepository.persist(jv);
        return vipToAdd;
    }

    private boolean vipTypeMatchesTypeForLoadBalancer(VirtualIpType vipType, LoadBalancer dbLoadBalancer) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
            if (!loadBalancerJoinVip.getVirtualIp().getVipType().equals(vipType)) {
                return false;
            }
        }
        return true;
    }
}
