package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.rax.domain.entity.AccessList;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;
import org.openstack.atlas.service.domain.service.Validator;
import org.openstack.atlas.service.domain.service.impl.LoadBalancerServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class RaxLoadBalancerServiceImpl extends LoadBalancerServiceImpl {

    @Override
    protected void validate(LoadBalancer loadBalancer) throws BadRequestException, EntityNotFoundException, LimitReachedException {
        super.validate(loadBalancer);
        //Add validation for any service level extension here.
    }
}
