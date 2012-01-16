package org.openstack.atlas.rax.domain.service.impl;

import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.service.RaxConnectionLoggingService;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.impl.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RaxConnectionLoggingServiceImpl extends BaseService implements RaxConnectionLoggingService {
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Override
    public void update(LoadBalancer loadBalancer) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        RaxLoadBalancer dbLoadBalancer = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
        isLbActive(dbLoadBalancer);
        if (loadBalancer instanceof RaxLoadBalancer) {
            RaxLoadBalancer raxLoadBalancer = (RaxLoadBalancer) loadBalancer;
            dbLoadBalancer.setConnectionLogging(raxLoadBalancer.getConnectionLogging());
            dbLoadBalancer.setStatus(CoreLoadBalancerStatus.PENDING_UPDATE);
            loadBalancerRepository.update(dbLoadBalancer);
        }
    }
}
