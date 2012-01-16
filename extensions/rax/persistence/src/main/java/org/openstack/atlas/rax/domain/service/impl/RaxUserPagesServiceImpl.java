package org.openstack.atlas.rax.domain.service.impl;

import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.domain.entity.RaxDefaults;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.entity.RaxUserPages;
import org.openstack.atlas.rax.domain.repository.RaxUserPagesRepository;
import org.openstack.atlas.rax.domain.service.RaxUserPagesService;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.service.impl.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class RaxUserPagesServiceImpl extends BaseService implements RaxUserPagesService {

    @Autowired
    protected RaxUserPagesRepository raxUserPagesRepository;

    public void setErrorPage(Integer accountId, Integer loadBalancerId, String errorpage) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        RaxLoadBalancer dbLoadBalancer = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        isLbActive(dbLoadBalancer);
        dbLoadBalancer.setStatus(CoreLoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.update(dbLoadBalancer);
        raxUserPagesRepository.setErrorPage(accountId, loadBalancerId, errorpage);
    }

    public void setDefaultErrorPage(String errorpage) throws EntityNotFoundException {
    }

    public void deleteErrorPage(Integer accountId, Integer loadBalancerId) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        RaxLoadBalancer dbLoadBalancer = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
        isLbActive(dbLoadBalancer);
        dbLoadBalancer.setStatus(CoreLoadBalancerStatus.PENDING_UPDATE);
        loadBalancerRepository.update(dbLoadBalancer);
        raxUserPagesRepository.deleteErrorPage(accountId, loadBalancerId);
    }
}
