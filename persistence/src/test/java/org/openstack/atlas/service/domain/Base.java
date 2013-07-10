package org.openstack.atlas.service.domain;

import org.openstack.atlas.service.domain.repository.AccountLimitRepository;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.AccountLimitService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;

@Service
@ContextConfiguration(locations = {"classpath:loadbalancing-persistence-test.xml", "classpath:loadbalancing-usage-persistence-test.xml"})
public class Base {

    @Autowired
    protected AccountLimitService accountLimitService;
    @Autowired
    protected LoadBalancerService loadBalancerService;

    @Autowired
    protected ClusterRepository clusterRepository;
    @Autowired
    protected HostRepository hostRepository;
    @Autowired
    protected AccountLimitRepository accountLimitRepository;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
}
