package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject;
import org.openstack.atlas.service.domain.services.AlgorithmsService;

import java.util.List;

public class AlgorithmsServiceImpl extends BaseService implements AlgorithmsService {

    @Override
    public List<LoadBalancerAlgorithmObject> get() {
        return loadBalancerRepository.getAllAlgorithms();
    }
}
