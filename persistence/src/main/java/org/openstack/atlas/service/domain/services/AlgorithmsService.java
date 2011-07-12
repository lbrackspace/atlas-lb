package org.openstack.atlas.service.domain.services;

import java.util.List;

public interface AlgorithmsService {

    public List<org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject> get();
}
