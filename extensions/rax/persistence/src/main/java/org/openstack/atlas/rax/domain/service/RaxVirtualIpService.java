package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.service.VirtualIpService;

public interface RaxVirtualIpService extends VirtualIpService {

    VirtualIpv6 addIpv6VirtualIpToLoadBalancer(VirtualIpv6 virtualIpv6, LoadBalancer loadBalancer) throws PersistenceServiceException;
}
