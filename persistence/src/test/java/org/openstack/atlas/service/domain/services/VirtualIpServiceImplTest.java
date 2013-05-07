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
import org.junit.Assert;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        List<VirtualIp> vips = new ArrayList<VirtualIp>();

        @Before
        public void standUp() {
            virtualIpRepository = mock(VirtualIpRepository.class);
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            virtualIpService = new VirtualIpServiceImpl();
            virtualIpService.setLoadBalancerRepository(loadBalancerRepository);
            virtualIpService.setVirtualIpRepository(virtualIpRepository);

            when(virtualIpRepository.getAll()).thenReturn(vips);
        }

        @Test
        public void shouldMapVipsByLbIdWhenCallingGetAllocatedVipsMappedByLbIdOneVipPerLb() {
            VirtualIp vip1 = new VirtualIp();
            vip1.setAllocated(true);
            vip1.setVipType(VirtualIpType.PUBLIC);
            vip1.setIpVersion(IpVersion.IPV4);
            VirtualIp vip2 = new VirtualIp();
            vip2.setAllocated(true);
            vip2.setVipType(VirtualIpType.SERVICENET);
            vip2.setIpVersion(IpVersion.IPV4);

            Set<LoadBalancerJoinVip> lbJoinVipSet1 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip lbJoinVip1 = new LoadBalancerJoinVip();
            lbJoinVipSet1.add(lbJoinVip1);
            Set<LoadBalancerJoinVip> lbJoinVipSet2 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip lbJoinVip2 = new LoadBalancerJoinVip();
            lbJoinVipSet2.add(lbJoinVip2);

            LoadBalancer lb1 = new LoadBalancer();
            lb1.setId(123);
            LoadBalancer lb2 = new LoadBalancer();
            lb2.setId(124);
            lbJoinVip1.setLoadBalancer(lb1);
            lbJoinVip2.setLoadBalancer(lb2);

            vip1.setLoadBalancerJoinVipSet(lbJoinVipSet1);
            vip2.setLoadBalancerJoinVipSet(lbJoinVipSet2);
            vips.add(vip1);
            vips.add(vip2);

            Map<Integer, List<VirtualIp>> vipMap = virtualIpService.getAllocatedVipsMappedByLbId();
            Assert.assertEquals(2, vipMap.size());
            Assert.assertTrue(vipMap.containsKey(123));
            Assert.assertTrue(vipMap.containsKey(124));
            Assert.assertEquals(1, vipMap.get(123).size());
            Assert.assertEquals(1, vipMap.get(124).size());
            Assert.assertEquals(VirtualIpType.PUBLIC, vipMap.get(123).get(0).getVipType());
            Assert.assertEquals(IpVersion.IPV4, vipMap.get(123).get(0).getIpVersion());
            Assert.assertEquals(VirtualIpType.SERVICENET, vipMap.get(124).get(0).getVipType());
            Assert.assertEquals(IpVersion.IPV4, vipMap.get(124).get(0).getIpVersion());
        }

        @Test
        public void shouldMapVipsByLbIdWhenCallingGetAllocatedVipsMappedByLbIdManyVipsPerLb() {
            VirtualIp vip11 = new VirtualIp();
            vip11.setAllocated(true);
            vip11.setVipType(VirtualIpType.PUBLIC);
            vip11.setIpVersion(IpVersion.IPV4);
            VirtualIp vip12 = new VirtualIp();
            vip12.setAllocated(true);
            vip12.setVipType(VirtualIpType.PUBLIC);
            vip12.setIpVersion(IpVersion.IPV6);
            VirtualIp vip13 = new VirtualIp();
            vip13.setAllocated(true);
            vip13.setVipType(VirtualIpType.PUBLIC);
            vip13.setIpVersion(IpVersion.IPV4);

            VirtualIp vip21 = new VirtualIp();
            vip21.setAllocated(true);
            vip21.setVipType(VirtualIpType.SERVICENET);
            vip21.setIpVersion(IpVersion.IPV4);
            VirtualIp vip22 = new VirtualIp();
            vip22.setAllocated(true);
            vip22.setVipType(VirtualIpType.SERVICENET);
            vip22.setIpVersion(IpVersion.IPV4);

            Set<LoadBalancerJoinVip> lbJoinVipSet1 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip lbJoinVip1 = new LoadBalancerJoinVip();
            lbJoinVipSet1.add(lbJoinVip1);
            Set<LoadBalancerJoinVip> lbJoinVipSet2 = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip lbJoinVip2 = new LoadBalancerJoinVip();
            lbJoinVipSet2.add(lbJoinVip2);

            LoadBalancer lb1 = new LoadBalancer();
            lb1.setId(123);
            LoadBalancer lb2 = new LoadBalancer();
            lb2.setId(124);
            lbJoinVip1.setLoadBalancer(lb1);
            lbJoinVip2.setLoadBalancer(lb2);

            vip11.setLoadBalancerJoinVipSet(lbJoinVipSet1);
            vip12.setLoadBalancerJoinVipSet(lbJoinVipSet1);
            vip13.setLoadBalancerJoinVipSet(lbJoinVipSet1);

            vip21.setLoadBalancerJoinVipSet(lbJoinVipSet2);
            vip22.setLoadBalancerJoinVipSet(lbJoinVipSet2);

            vips.add(vip11);
            vips.add(vip12);
            vips.add(vip13);
            vips.add(vip22);
            vips.add(vip21);

            Map<Integer, List<VirtualIp>> vipMap = virtualIpService.getAllocatedVipsMappedByLbId();
            Assert.assertEquals(2, vipMap.size());
            Assert.assertTrue(vipMap.containsKey(123));
            Assert.assertTrue(vipMap.containsKey(124));
            Assert.assertEquals(3, vipMap.get(123).size());
            Assert.assertEquals(2, vipMap.get(124).size());
            Assert.assertEquals(VirtualIpType.PUBLIC, vipMap.get(123).get(0).getVipType());
            Assert.assertEquals(IpVersion.IPV4, vipMap.get(123).get(0).getIpVersion());
            Assert.assertEquals(VirtualIpType.PUBLIC, vipMap.get(123).get(1).getVipType());
            Assert.assertEquals(IpVersion.IPV6, vipMap.get(123).get(1).getIpVersion());
            Assert.assertEquals(VirtualIpType.PUBLIC, vipMap.get(123).get(2).getVipType());
            Assert.assertEquals(IpVersion.IPV4, vipMap.get(123).get(2).getIpVersion());
            Assert.assertEquals(VirtualIpType.SERVICENET, vipMap.get(124).get(0).getVipType());
            Assert.assertEquals(IpVersion.IPV4, vipMap.get(124).get(0).getIpVersion());
            Assert.assertEquals(VirtualIpType.SERVICENET, vipMap.get(124).get(1).getVipType());
            Assert.assertEquals(IpVersion.IPV4, vipMap.get(124).get(1).getIpVersion());
        }
    }
}
