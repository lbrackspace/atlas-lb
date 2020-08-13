package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.entities.Zone;
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


}
