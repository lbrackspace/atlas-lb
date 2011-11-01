package org.openstack.atlas.service.domain.service;

import org.junit.After;
import org.junit.Before;
import org.openstack.atlas.datamodel.AtlasTypeHelper;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.NodeRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@ContextConfiguration(locations = {"classpath:db-services-test.xml"})
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

    @After
    public void deleteAllLoadBalancers() throws PersistenceServiceException {
        List<LoadBalancer> loadBalancersToDelete = loadBalancerRepository.getByAccountId(loadBalancer.getAccountId());

        for (LoadBalancer lbToDelete : loadBalancersToDelete) {
            loadBalancerService.delete(lbToDelete);
        }
    }
}
