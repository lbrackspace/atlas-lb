package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
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
    public LoadBalancer create(LoadBalancer loadBalancer) throws Exception {
        return loadBalancerRepository.create(loadBalancer);
    }
}
