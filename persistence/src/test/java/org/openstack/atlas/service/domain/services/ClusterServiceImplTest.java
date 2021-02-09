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
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.ClusterType;
import org.openstack.atlas.service.domain.entities.DataCenter;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.services.impl.ClusterServiceImpl;

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

           clusterService.updateCluster(cluster);
           verify(clusterRepository).update(cluster);
       }

       @Test
       public void shouldSendClusterToRepositoryWithNullDataCenter() throws Exception {
           cluster.setDataCenter(null);
           clusterService.updateCluster(cluster);
           cluster.setDataCenter(DataCenter.HKG);
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullIPv6() throws Exception {
           cluster.setClusterIpv6Cidr(null);
           clusterService.updateCluster(cluster);
           cluster.setClusterIpv6Cidr("test123");
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullClusterType() throws Exception {
           cluster.setClusterType(null);
           clusterService.updateCluster(cluster);
           cluster.setClusterType(ClusterType.INTERNAL);
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullDescription() throws Exception {
           cluster.setDescription(null);
           clusterService.updateCluster(cluster);
           cluster.setDescription("test1234");
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullPass() throws Exception {
           cluster.setPassword(null);
           clusterService.updateCluster(cluster);
           cluster.setPassword("pass");
           verify(clusterRepository).update(cluster);

       }

       @Test
       public void shouldSendClusterToRepositoryWithNullStatus() throws Exception {
           cluster.setStatus(null);
           clusterService.updateCluster(cluster);
           cluster.setStatus(ClusterStatus.INACTIVE);
           verify(clusterRepository).update(cluster);

       }





   }
}
