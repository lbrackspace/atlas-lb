package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.RegionalSourceAddresses;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.impl.HostServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@RunWith(Enclosed.class)
public class HostServiceImplTest {

    public static class createHost {

        @InjectMocks
        HostService hostService = new HostServiceImpl();

        @Mock
        ClusterRepository clusterRepository;

        @Mock
        HostRepository hostRepository;


        Cluster cluster = new Cluster();

        Host host = new Host();

        List<org.openstack.atlas.service.domain.entities.Host> allHosts = new ArrayList<>();

        @Before
        public void standUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            cluster.setId(1);
            host.setName("testHost");
            host.setTrafficManagerName("zues01.aasdfad.blah");
            host.setZone(Zone.B);
            host.setCluster(cluster);
            host.setManagementIp("12.34.56.78");

            when(clusterRepository.getById(anyInt())).thenReturn(cluster);
            when(hostRepository.getAllHosts()).thenReturn(allHosts);

        }

        @Test
        public void hostStatusShouldSetToBurnIn() throws Exception {
            hostService.create(host);
            Assert.assertEquals(host.getCluster(),cluster);
            Assert.assertEquals(HostStatus.BURN_IN, host.getHostStatus());
            verify(hostRepository, times(1)).save(host);
        }

        @Test
        public void hostStatusShouldSetToActive() throws Exception {
            host.setHostStatus(HostStatus.ACTIVE);
            hostService.create(host);
            Assert.assertEquals(host.getCluster(),cluster);
            Assert.assertEquals(HostStatus.ACTIVE, host.getHostStatus());
            verify(hostRepository, times(1)).save(host);
        }
    }

    public static class GetRegionalSourceAddress{

        @InjectMocks
        HostService hostService = new HostServiceImpl();

        @Mock
        HostRepository hostRepository;

        Cluster cluster = new Cluster();

        Host host = new Host();

        List<org.openstack.atlas.service.domain.entities.Host> hosts = new ArrayList<>();

        @Before
        public void standUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            cluster.setId(1);
            host.setName("testHost");
            cluster.setClusterType(ClusterType.INTERNAL);
            host.setCluster(cluster);
            host.setIpv4Public("ipv4p");
            host.setIpv4Servicenet("ipv4s");
            host.setIpv6Public("ipv6p");
            host.setIpv6Servicenet("ipv6s");
            hosts.add(host);

        }

        @Test
        public void ShouldReturnAListOfHostByClusterType(){
            RegionalSourceAddresses regionalSourceAddresses;
            ClusterType ctype = ClusterType.INTERNAL;
            when(hostRepository.getAllHostsByClusterType(ArgumentMatchers.eq(ClusterType.INTERNAL))).thenReturn(hosts);
            regionalSourceAddresses = hostService.getRegionalSourceAddresses(ctype);
            Assert.assertTrue(regionalSourceAddresses.getIpv4Servicenets().get(0).equals("ipv4s"));
            Assert.assertTrue(regionalSourceAddresses.getIpv6Servicenets().get(0).equals("ipv6s"));
            Assert.assertTrue(regionalSourceAddresses.getIpv4Publicnets().get(0).equals("ipv4p"));
            Assert.assertTrue(regionalSourceAddresses.getIpv6Publicnets().get(0).equals("ipv6p"));


        }

    }


}
