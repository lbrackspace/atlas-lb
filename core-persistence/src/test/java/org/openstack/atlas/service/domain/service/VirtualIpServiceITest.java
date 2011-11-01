package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(Enclosed.class)
public class VirtualIpServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenRemovingAllVipsFromALoadBalancer extends Base {
        @Autowired
        private VirtualIpService virtualIpService;

        @Autowired
        private VirtualIpRepository virtualIpRepository;

        @Autowired
        VirtualIpv6Repository virtualIpv6Repository;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancer = loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
        }

        @Test
        public void shouldRemoveAllVips() throws EntityNotFoundException {
            LoadBalancer savedLb = loadBalancerRepository.getByIdAndAccountId(loadBalancer.getId(), loadBalancer.getAccountId());
            Assert.assertTrue(!savedLb.getLoadBalancerJoinVipSet().isEmpty() || !savedLb.getLoadBalancerJoinVip6Set().isEmpty());

            virtualIpService.removeAllVipsFromLoadBalancer(savedLb);
            
            savedLb = loadBalancerRepository.getById(savedLb.getId());
            Assert.assertTrue(savedLb.getLoadBalancerJoinVipSet().isEmpty() && savedLb.getLoadBalancerJoinVip6Set().isEmpty());
        }
    }
}
