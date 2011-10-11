package org.openstack.atlas.service.domain.service;

import org.junit.Before;
import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class Base {
    public static final String contextConfig = "classpath:db-services-test.xml";

    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

    @Autowired
    protected AtlasTypeHelper atlasTypeHelper;

    @Autowired
    protected LoadBalancerService loadBalancerService;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    protected LoadBalancer loadBalancer;

    @Before
    public void setUpMinimalLoadBalancer() {
        loadBalancer = StubFactory.createMinimalDomainLoadBalancer();
    }
}
