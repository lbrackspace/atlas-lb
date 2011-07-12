package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject;
import org.openstack.atlas.service.domain.services.ProtocolsService;

import java.util.List;

public class ProtocolsServiceImpl extends BaseService implements ProtocolsService {

    @Override
    public List<LoadBalancerProtocolObject> get() {
        return loadBalancerRepository.getAllProtocols();
    }
}
