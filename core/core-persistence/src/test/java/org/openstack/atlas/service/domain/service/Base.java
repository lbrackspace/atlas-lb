package org.openstack.atlas.service.domain.service;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ContextConfiguration(locations = {"classpath:db-services-test.xml"})
@Transactional
@Service
public class Base {

    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

    @Autowired
    protected AtlasTypeHelper atlasTypeHelper;

    @Autowired
    protected LoadBalancerService loadBalancerService;

    @Autowired
    protected NodeService nodeService;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected NodeRepository nodeRepository;

    protected LoadBalancer loadBalancer;

    @Before
    public void setUpMinimalLoadBalancer() {
        loadBalancer = StubFactory.createMinimalDomainLoadBalancer();
    }
}
