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

import static org.mockito.Mockito.*;

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
       public void standUp(){
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
}
