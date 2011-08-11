package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoadBalancerServiceImpl implements LoadBalancerService {

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    public LoadBalancer get(Integer id) throws EntityNotFoundException {
        return loadBalancerRepository.getById(id);
    }

    @Override
    @Transactional
    public LoadBalancer get(Integer id, Integer accountId) throws EntityNotFoundException {
        return loadBalancerRepository.getByIdAndAccountId(id, accountId);
    }
    @Override
    public LoadBalancer create(LoadBalancer loadBalancer) throws Exception {
        return loadBalancerRepository.create(loadBalancer);
    }

    @Override
    @Transactional
    public LoadBalancer update(LoadBalancer lb) throws Exception {
        return loadBalancerRepository.update(lb);
    }
}
