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
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.ClusterType;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.services.impl.ClusterServiceImpl;

import javax.xml.ws.Response;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@RunWith(Enclosed.class)
public class ClusterServiceImplTest {

   public static class whenRetrievingClusterType{

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

   public static class whenDeletingCluster{

       @Mock
       private ClusterRepository clusterRepository;
       @InjectMocks
       private ClusterServiceImpl clusterService;
       private Cluster cluster;
       private Response response;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           cluster = new Cluster();
           cluster.setId(1);
       }

       @Test
       public void shouldReturn200StatusCodeAfterDeletion(){
           doNothing().when(clusterRepository).delete(cluster);
       }
   }
}
