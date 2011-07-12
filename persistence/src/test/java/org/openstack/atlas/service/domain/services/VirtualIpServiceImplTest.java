package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

@Ignore
@RunWith(Enclosed.class)
public class VirtualIpServiceImplTest {

    public static class WhenConfiguringVipsForNewLb {
        Integer accountId = 1234;
        VirtualIpRepository virtualIpRepository;
        LoadBalancerRepository loadBalancerRepository;
        VirtualIpServiceImpl virtualIpService;
        LoadBalancer lb;
        LoadBalancerJoinVip lbjv;
        Set<LoadBalancerJoinVip> loadBalancerJoinVips;
        VirtualIp vip;

        @Before
        public void standUp() {
            virtualIpRepository = mock(VirtualIpRepository.class);
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            virtualIpService = new VirtualIpServiceImpl();
            virtualIpService.setLoadBalancerRepository(loadBalancerRepository);
            virtualIpService.setVirtualIpRepository(virtualIpRepository);
        }

        @Before
        public void standUpObjects() {
            lb = new LoadBalancer();
            lb.setAccountId(898989);
            lb.setId(12);
            lb.setStatus(LoadBalancerStatus.ACTIVE);
            lbjv = new LoadBalancerJoinVip();
            loadBalancerJoinVips = new HashSet<LoadBalancerJoinVip>();
            vip = new VirtualIp();
        }
    }
}
