package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.LoadBalancer;

public interface SuspensionService {

    public void createSuspension(LoadBalancer queueLb) throws Exception;

    public void deleteSuspension(LoadBalancer queueLb) throws Exception;
}
