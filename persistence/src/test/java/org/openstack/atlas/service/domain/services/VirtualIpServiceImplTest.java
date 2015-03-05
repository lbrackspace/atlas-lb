package org.openstack.atlas.service.domain.services;

import org.junit.Test;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.TrafficType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpLoadBalancerDetails;
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

@RunWith(Enclosed.class)
public class VirtualIpServiceImplTest {

    public static class WhenRetrievingLoadbalancerDetailsByIp {
        VirtualIpRepository virtualIpRepository;
        LoadBalancerRepository loadBalancerRepository;
        VirtualIpServiceImpl virtualIpService;

        String ipAddress = "10.2.3.4";
        Integer accountId = 1234;
        Integer lbId = 5678;
        Integer vipId = 23945;
        VirtualIp vip;
        List<LoadBalancer> lbList = new ArrayList<LoadBalancer>();

        @Before
        public void standUp() {
            virtualIpRepository = mock(VirtualIpRepository.class);
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            virtualIpService = new VirtualIpServiceImpl();
            virtualIpService.setLoadBalancerRepository(loadBalancerRepository);
            virtualIpService.setVirtualIpRepository(virtualIpRepository);
            vip = new VirtualIp();
            vip.setIpAddress(ipAddress);
            vip.setId(vipId);

            when(virtualIpRepository.getLoadBalancersByVipAddress(ipAddress)).thenReturn(lbList);
        }

        private LoadBalancer getNewLb() {
            LoadBalancer newLb = new LoadBalancer();
            newLb.setAccountId(accountId);
            newLb.setId(lbId);
            newLb.setHttpsRedirect(false);
            newLb.setStatus(LoadBalancerStatus.ACTIVE);

            LoadBalancerJoinVip lbJoinVip = new LoadBalancerJoinVip();
            lbJoinVip.setLoadBalancer(newLb);
            lbJoinVip.setVirtualIp(vip);

            Set<LoadBalancerJoinVip> lbJoinVipSet = new HashSet<LoadBalancerJoinVip>();
            lbJoinVipSet.add(lbJoinVip);

            newLb.setLoadBalancerJoinVipSet(lbJoinVipSet);

            return newLb;
        }

        private HashMap<Integer, String> getActualProtocols(VirtualIpLoadBalancerDetails lbDetails) {
            HashMap<Integer, String> actualProtocols = new HashMap<Integer, String>();
            for (TrafficType p : lbDetails.getProtocols()) {
                actualProtocols.put(p.getPort(), p.getProtocol());
            }
            return actualProtocols;
        }

        @Test
        public void testSimpleLB() {
            Integer port = 80;

            LoadBalancer lb = getNewLb();
            lb.setPort(port);
            lb.setProtocol(LoadBalancerProtocol.HTTP);

            lbList.add(lb);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbDetails.getLoadBalancerId(), lbId);
            Assert.assertEquals(lbDetails.getAccountId(), accountId);
            Assert.assertEquals(lbDetails.getVirtualIpId(), vipId);

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(80, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testHttpsRedirectLB() {
            Integer port = 443;

            LoadBalancer lb = getNewLb();
            lb.setPort(port);
            lb.setProtocol(LoadBalancerProtocol.HTTPS);
            lb.setHttpsRedirect(true);

            lbList.add(lb);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbId, lbDetails.getLoadBalancerId());
            Assert.assertEquals(accountId, lbDetails.getAccountId());
            Assert.assertEquals(vipId, lbDetails.getVirtualIpId());

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(80, "TCP");
            expectedProtocols.put(443, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testSSLTermMixedLB() {
            Integer port = 80;
            Integer securePort = 443;

            LoadBalancer lb = getNewLb();
            lb.setPort(port);
            lb.setProtocol(LoadBalancerProtocol.HTTP);

            SslTermination sslTermination = new SslTermination();
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(securePort);
            sslTermination.setSecureTrafficOnly(false);
            lb.setSslTermination(sslTermination);

            lbList.add(lb);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbId, lbDetails.getLoadBalancerId());
            Assert.assertEquals(accountId, lbDetails.getAccountId());
            Assert.assertEquals(vipId, lbDetails.getVirtualIpId());

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(80, "TCP");
            expectedProtocols.put(443, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testSSLTermSecureOnlyLB() {
            Integer port = 80;
            Integer securePort = 443;

            LoadBalancer lb = getNewLb();
            lb.setPort(port);
            lb.setProtocol(LoadBalancerProtocol.HTTP);

            SslTermination sslTermination = new SslTermination();
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(securePort);
            sslTermination.setSecureTrafficOnly(true);
            lb.setSslTermination(sslTermination);

            lbList.add(lb);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbId, lbDetails.getLoadBalancerId());
            Assert.assertEquals(accountId, lbDetails.getAccountId());
            Assert.assertEquals(vipId, lbDetails.getVirtualIpId());

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(443, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testDisabledSSLTermLB() {
            Integer port = 80;
            Integer securePort = 443;

            LoadBalancer lb = getNewLb();
            lb.setPort(port);
            lb.setProtocol(LoadBalancerProtocol.HTTP);

            SslTermination sslTermination = new SslTermination();
            sslTermination.setEnabled(false);
            sslTermination.setSecurePort(securePort);
            lb.setSslTermination(sslTermination);

            lbList.add(lb);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbId, lbDetails.getLoadBalancerId());
            Assert.assertEquals(accountId, lbDetails.getAccountId());
            Assert.assertEquals(vipId, lbDetails.getVirtualIpId());

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(80, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testSSLTermWithHttpsRedirectLB() {
            Integer port = 80;
            Integer securePort = 443;

            LoadBalancer lb = getNewLb();
            lb.setPort(port);
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            lb.setHttpsRedirect(true);

            SslTermination sslTermination = new SslTermination();
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(securePort);
            sslTermination.setSecureTrafficOnly(true);
            lb.setSslTermination(sslTermination);

            lbList.add(lb);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbId, lbDetails.getLoadBalancerId());
            Assert.assertEquals(accountId, lbDetails.getAccountId());
            Assert.assertEquals(vipId, lbDetails.getVirtualIpId());

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(80, "TCP");
            expectedProtocols.put(443, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testSharedVipLB() {
            Integer port1 = 21;
            Integer port2 = 53;

            LoadBalancer lb1 = getNewLb();
            lb1.setPort(port1);
            lb1.setProtocol(LoadBalancerProtocol.FTP);
            LoadBalancer lb2 = getNewLb();
            lb2.setPort(port2);
            lb2.setProtocol(LoadBalancerProtocol.DNS_TCP);

            lbList.add(lb1);
            lbList.add(lb2);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbDetails.getLoadBalancerId(), lbId);
            Assert.assertEquals(lbDetails.getAccountId(), accountId);
            Assert.assertEquals(lbDetails.getVirtualIpId(), vipId);

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(21, "TCP");
            expectedProtocols.put(53, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void testSharedVipWithHttpsRedirectLB() {
            Integer port1 = 80;
            Integer port2 = 53;

            LoadBalancer lb1 = getNewLb();
            lb1.setPort(port1);
            lb1.setProtocol(LoadBalancerProtocol.HTTP);
            lb1.setHttpsRedirect(true);
            LoadBalancer lb2 = getNewLb();
            lb2.setPort(port2);
            lb2.setProtocol(LoadBalancerProtocol.DNS_TCP);

            lbList.add(lb1);
            lbList.add(lb2);

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbDetails.getLoadBalancerId(), lbId);
            Assert.assertEquals(lbDetails.getAccountId(), accountId);
            Assert.assertEquals(lbDetails.getVirtualIpId(), vipId);

            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();
            expectedProtocols.put(80, "TCP");
            expectedProtocols.put(443, "TCP");
            expectedProtocols.put(53, "TCP");

            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        private class ProtocolTriplet {
            private LoadBalancerProtocol protocol;
            private Integer port;
            private String type;

            public ProtocolTriplet(LoadBalancerProtocol protocol, Integer port, String type) {
                this.protocol = protocol;
                this.port = port;
                this.type = type;
            }

            public LoadBalancerProtocol getProtocol() {
                return this.protocol;
            }

            public Integer getPort() {
                return this.port;
            }

            public String getType() {
                return this.type;
            }
        }

        @Test
        public void testAllProtocolsForCorrectTypesLoop() {
            ArrayList<ProtocolTriplet> protocols = new ArrayList<ProtocolTriplet>();
            HashMap<Integer, String> expectedProtocols = new HashMap<Integer, String>();

            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.HTTP, 1, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.FTP, 2, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.TCP, 3, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.TCP_CLIENT_FIRST, 4, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.TCP_STREAM, 5, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.IMAPv2, 6, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.IMAPv3, 7, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.IMAPv4, 8, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.POP3, 9, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.SMTP, 10, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.LDAP, 11, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.HTTPS, 12, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.IMAPS, 13, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.POP3S, 14, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.LDAPS, 15, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.DNS_UDP, 16, "UDP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.DNS_TCP, 17, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.UDP_STREAM, 18, "UDP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.UDP, 19, "UDP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.MYSQL, 20, "TCP"));
            protocols.add(new ProtocolTriplet(LoadBalancerProtocol.SFTP, 21, "TCP"));

            for (ProtocolTriplet pt : protocols) {
                LoadBalancer lb = getNewLb();
                lb.setPort(pt.getPort());
                lb.setProtocol(pt.getProtocol());
                lbList.add(lb);
                expectedProtocols.put(pt.getPort(), pt.getType());
            }

            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertEquals(lbId, lbDetails.getLoadBalancerId());
            Assert.assertEquals(accountId, lbDetails.getAccountId());
            Assert.assertEquals(vipId, lbDetails.getVirtualIpId());
            Assert.assertEquals(expectedProtocols, getActualProtocols(lbDetails));
        }

        @Test
        public void shouldReturnEmptyResultWhenNoLBsMatchIp() {
            VirtualIpLoadBalancerDetails lbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);

            Assert.assertNotNull(lbDetails);
            Assert.assertNull(lbDetails.getAccountId());
            Assert.assertNull(lbDetails.getLoadBalancerId());
            Assert.assertNull(lbDetails.getVirtualIpId());
            Assert.assertNull(lbDetails.getProtocols());
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
