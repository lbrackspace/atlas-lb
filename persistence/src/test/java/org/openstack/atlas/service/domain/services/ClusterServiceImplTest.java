package org.openstack.atlas.service.domain.services;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.ClusterType;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.exceptions.ClusterNotEmptyException;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.services.impl.ClusterServiceImpl;

import javax.xml.ws.Response;

import java.util.ArrayList;
import java.util.List;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.service.domain.entities.DataCenter;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ClusterServiceImplTest {

   public static class whenRetrievingClusterType {

       @Mock
       private ClusterRepository clusterRepository;
       @InjectMocks
       private ClusterServiceImpl clusterService;
       private ClusterType cType;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           cType = ClusterType.STANDARD;
       }

       @Test
       public void shouldReturnHostWithNullHostId() throws Exception {
           doReturn(cType).when(clusterRepository).getClusterTypeByAccountId(ArgumentMatchers.anyInt());
           cType = clusterService.getClusterTypeByAccountId(1);
           Assert.assertEquals(cType, ClusterType.STANDARD);
       }
   }

   public static class whenDeletingCluster {

       @Mock
       private ClusterRepository clusterRepository;
       @Mock
       private ClusterService clusterServiceMock;
       @InjectMocks
       private ClusterServiceImpl clusterService;
       private Cluster cluster;
       private Response response;
       private Host host;
       private List<Host> hosts;

       @Rule
       public final ExpectedException expectedException = ExpectedException.none();

       @Before
       public void standUp() {
           MockitoAnnotations.initMocks(this);
           cluster = new Cluster();
           cluster.setId(1);
           host = new Host();
           host.setCluster(cluster);
           hosts = new ArrayList<>();
           hosts.add(host);
       }

       @Test
       public void shouldReturn200StatusCodeAfterDeletion() throws ClusterNotEmptyException {
           hosts.clear();
           doReturn(hosts).when(clusterRepository).getHosts(ArgumentMatchers.anyInt());
           clusterService.deleteCluster(cluster);
           verify(clusterRepository, times(1)).delete(cluster);
       }

       @Test
       public void shouldThrowClusterNotEmptyExceptionWhenClusterHasHostAssociated() throws ClusterNotEmptyException {
           doReturn(hosts).when(clusterRepository).getHosts(ArgumentMatchers.anyInt());
           expectedException.expect(ClusterNotEmptyException.class);
           clusterService.deleteCluster(cluster);
       }
   }

   public static class whenUpdatingCluster {


       ClusterServiceImpl clusterService;
       Cluster cluster;

       Cluster dbCluster;

       @Mock
       ClusterRepository clusterRepository;



       @Before
       public void standUp() throws EntityNotFoundException {
           MockitoAnnotations.initMocks(this);
           clusterService = new ClusterServiceImpl();
           clusterService.setClusterRepository(clusterRepository);
           cluster = new Cluster();
           cluster.setName("test");
           cluster.setStatus(ClusterStatus.ACTIVE);
           cluster.setUsername("testUserName");
           cluster.setDescription("testDescription");
           cluster.setDataCenter(DataCenter.DFW);
           cluster.setClusterIpv6Cidr("test");
           dbCluster = new Cluster();

           cluster.setId(1);
           cluster.setClusterType(ClusterType.STANDARD);
           dbCluster.setDataCenter(DataCenter.HKG);
           dbCluster.setClusterIpv6Cidr("test1234");
           dbCluster.setClusterType(ClusterType.INTERNAL);
           dbCluster.setDescription("test1234");
           dbCluster.setPassword("pass");

           when(clusterRepository.getById(anyInt())).thenReturn(dbCluster);

       }

       @Test
       public void shouldSendClusterToRepository() throws Exception {

           clusterService.updateCluster(cluster, 1);
           verify(clusterRepository).update(cluster);
       }

       @Test
       public void shouldSendClusterToRepositoryWithNullDataCenter() throws Exception {
           cluster.setDataCenter(null);
           clusterService.updateCluster(cluster, 1);
           cluster.setDataCenter(DataCenter.HKG);
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullIPv6() throws Exception {
           cluster.setClusterIpv6Cidr(null);
           clusterService.updateCluster(cluster, 1);
           cluster.setClusterIpv6Cidr("test123");
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullClusterType() throws Exception {
           cluster.setClusterType(null);
           clusterService.updateCluster(cluster, 1);
           cluster.setClusterType(ClusterType.INTERNAL);
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullDescription() throws Exception {
           cluster.setDescription(null);
           clusterService.updateCluster(cluster, 1);
           cluster.setDescription("test1234");
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullPass() throws Exception {
           cluster.setPassword(null);
           clusterService.updateCluster(cluster, 1);
           cluster.setPassword("pass");
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullStatus() throws Exception {
           cluster.setStatus(null);
           clusterService.updateCluster(cluster, 1);
           cluster.setStatus(ClusterStatus.INACTIVE);
           verify(clusterRepository).update(cluster);

       }

   }
}
