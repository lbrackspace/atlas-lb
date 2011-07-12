package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject;

import java.util.List;

public interface ProtocolsService {
    public List<LoadBalancerProtocolObject> get();
}
