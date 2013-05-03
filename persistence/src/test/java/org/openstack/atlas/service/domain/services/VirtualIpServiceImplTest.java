package org.openstack.atlas.service.domain.services;

import org.junit.Test;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Assert;

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

    public static class WhenRetrievingVirtualIps {
        VirtualIpRepository virtualIpRepository;
        LoadBalancerRepository loadBalancerRepository;
        VirtualIpServiceImpl virtualIpService;

        @Before
        public void standUp() {
            virtualIpRepository = mock(VirtualIpRepository.class);
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            virtualIpService = new VirtualIpServiceImpl();
            virtualIpService.setLoadBalancerRepository(loadBalancerRepository);
            virtualIpService.setVirtualIpRepository(virtualIpRepository);

            List<VirtualIp> vips = new ArrayList<VirtualIp>();
            VirtualIp vip1 = new VirtualIp();
            vip1.setAllocated(true);
            vip1.setVipType(VirtualIpType.PUBLIC);
            vip1.setAllocated(true);
            vip1.setVipType(VirtualIpType.SERVICENET);

            Set<LoadBalancerJoinVip> lbJoinVipSet = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip lbJoinVip1 = new LoadBalancerJoinVip();
            lbJoinVipSet.add(lbJoinVip1);

            LoadBalancer lb1 = new LoadBalancer();
            lb1.setId(123);
            LoadBalancer lb2 = new LoadBalancer();
            lb1.setId(124);
            lbJoinVip1.setLoadBalancer(lb1);

            vip1.setLoadBalancerJoinVipSet(lbJoinVipSet);
            vips.add(vip1);
            when(virtualIpRepository.getAll()).thenReturn(vips);
        }

        @Test
        public void test() {
            Map<Integer, List<VirtualIp>> vipMap = virtualIpService.getAllVipsMappedByLbId();
            Assert.assertTrue(vipMap.containsKey(123));
            Assert.assertEquals(1, vipMap.get(123).size());
            Assert.assertEquals(VirtualIpType.PUBLIC, vipMap.get(123).get(0).getVipType());
        }
    }
}
