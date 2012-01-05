package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SslTerminationServiceImpl extends BaseService implements SslTerminationService {

    @Transactional
    @Override
    public SslTermination updateSslTermination(Integer lbId, Integer accountId, SslTermination sslTermination) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lbId, accountId);
        isLbActive(dbLoadBalancer);
        isProtocolSecure(dbLoadBalancer);
        //TODO: validate here...

        if (sslTermination != null) {
            return sslTerminationRepository.setSslTermination(lbId, sslTermination);
        }
        return new SslTermination();
    }

    @Transactional
    @Override
    public boolean deleteSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(lid, accountId);
        isLbActive(dbLoadBalancer);

        return sslTerminationRepository.removeSslTermination(lid, accountId);
    }

    @Transactional
    @Override
    public SslTermination getSslTermination(Integer lid, Integer accountId) throws EntityNotFoundException {
        return sslTerminationRepository.getSslTerminationByLbId(lid, accountId);
    }

    private boolean isProtocolSecure(LoadBalancer loadBalancer) throws BadRequestException {
        LoadBalancerProtocol protocol = loadBalancer.getProtocol();
        if (protocol == LoadBalancerProtocol.HTTPS || protocol == LoadBalancerProtocol.IMAPS
                || protocol == LoadBalancerProtocol.LDAPS || protocol == LoadBalancerProtocol.POP3S) {
            throw new BadRequestException("Can not create ssl termination on a load balancer using a secure protocol.");
        }
        return true;
    }
}

