package org.openstack.atlas.service.domain.service;


import org.openstack.atlas.common.ip.exception.IPStringConversionException;
import org.openstack.atlas.service.domain.entity.Cluster;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

public interface VirtualIpService {
    LoadBalancer assignVIpsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException;

    void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException;
}
