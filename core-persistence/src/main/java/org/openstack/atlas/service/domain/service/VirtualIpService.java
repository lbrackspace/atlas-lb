package org.openstack.atlas.service.domain.service;


import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

import java.security.NoSuchAlgorithmException;

public interface VirtualIpService {
    LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException;

    void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException;

    void removeAllVipsFromLoadBalancer(LoadBalancer lb);
}
