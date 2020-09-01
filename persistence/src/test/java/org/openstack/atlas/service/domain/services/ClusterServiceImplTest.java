package org.openstack.atlas.service.domain.services;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPBlocksOverLapException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPCidrBlockOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ClusterNotEmptyException;
import org.openstack.atlas.service.domain.repository.ClusterRepository;
import org.openstack.atlas.service.domain.services.impl.ClusterServiceImpl;

import javax.xml.ws.Response;

import java.util.ArrayList;
import java.util.List;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
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
       public void standUp() throws EntityNotFoundException {
           MockitoAnnotations.initMocks(this);
           cluster = new Cluster();
           cluster.setId(1);
           host = new Host();
           host.setCluster(cluster);
           hosts = new ArrayList<>();
           hosts.add(host);

           when(clusterRepository.getById(anyInt())).thenReturn(cluster);
       }

       @Test
       public void shouldReturn200StatusCodeAfterDeletion() throws ClusterNotEmptyException, EntityNotFoundException {
           hosts.clear();
           doReturn(hosts).when(clusterRepository).getHosts(ArgumentMatchers.anyInt());
           clusterService.deleteCluster(cluster);
           verify(clusterRepository, times(1)).delete(cluster);
       }

       @Test
       public void shouldThrowClusterNotEmptyExceptionWhenClusterHasHostAssociated() throws ClusterNotEmptyException, EntityNotFoundException {
           doReturn(hosts).when(clusterRepository).getHosts(ArgumentMatchers.anyInt());
           expectedException.expect(ClusterNotEmptyException.class);
           clusterService.deleteCluster(cluster);
       }

       @Test
       public void shouldThrowEntityNotFoundExceptionIfClusterDoesntExist() throws ClusterNotEmptyException, EntityNotFoundException {
           doThrow(EntityNotFoundException.class).when(clusterRepository).getById(ArgumentMatchers.anyInt());
           expectedException.expect(EntityNotFoundException.class);
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

    public static class whenCreatingCluster {

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

           List<Cluster> clusters = new ArrayList<>();
           dbCluster = new Cluster();
           dbCluster.setId(1);
           dbCluster.setName("dev1");
           dbCluster.setDescription("test");
           dbCluster.setDataCenter(DataCenter.DFW);
           dbCluster.setClusterIpv6Cidr("2001:4801:79f1:1::/64");
           dbCluster.setPassword("e2fed4da98a840a40788acb64940469d");
           dbCluster.setUsername("admin");
           dbCluster.setStatus(ClusterStatus.ACTIVE);
           clusters.add(dbCluster);

           cluster = new Cluster();
           cluster.setName("dev2");
           cluster.setDescription("test2");
           cluster.setDataCenter(DataCenter.DFW);
           cluster.setClusterIpv6Cidr("2001:4801:79f1:1::/64");
           cluster.setPassword("e2fed4da98a840a40788acb64940469d");
           cluster.setUsername("admin");
           cluster.setStatus(ClusterStatus.ACTIVE);

           when(clusterRepository.getAll()).thenReturn(clusters);
       }

       @Test
       public void shouldCreateAValidCluster() throws Exception {
           clusterService.create(cluster);
           verify(clusterRepository, times(1)).save(cluster);
           verify(clusterRepository, times(1)).getAll();
       }

       @Test(expected = BadRequestException.class)
       public void shouldValidateAgainstDuplicateNames() throws BadRequestException {
           cluster.setName("dev1");
           clusterService.create(cluster);
           verify(clusterRepository, times(1)).save(cluster);
           verify(clusterRepository, times(1)).getAll();

       }

        @Test(expected = BadRequestException.class)
        public void shouldValidateAgainstInvalidPassword() throws BadRequestException {
            cluster.setPassword("password");
            clusterService.create(cluster);
            verify(clusterRepository, times(1)).save(cluster);
            verify(clusterRepository, times(1)).getAll();
        }

        @Test(expected = BadRequestException.class)
        public void shouldValidateAgainstNullPassword() throws BadRequestException {
            cluster.setPassword(null);
            clusterService.create(cluster);
            verify(clusterRepository, times(1)).save(cluster);
            verify(clusterRepository, times(1)).getAll();
        }
    }

    public static class whenAddingVirtualIpBlocks {
       Cluster cluster;
       IPv4Ranges iPv4Ranges;
       List<VirtualIp> vips = new ArrayList<>();
       VirtualIp vip1 = new VirtualIp();

       @Mock
       ClusterRepository clusterRepository;
       @Mock
       VirtualIpService virtualIpService;
       @InjectMocks
       ClusterServiceImpl clusterService = new ClusterServiceImpl();

        @Captor
        private ArgumentCaptor<ArrayList<VirtualIp>> captor;

       @Before
       public void standUp() throws EntityNotFoundException,
               IPOctetOutOfRangeException, IPStringConversionException,
               IPCidrBlockOutOfRangeException, IPBlocksOverLapException {
           MockitoAnnotations.initMocks(this);

           vip1.setId(1);
           vip1.setIpAddress("192.168.1.1");
           vips.add(vip1);

           cluster = new Cluster();
           cluster.setName("dev2");
           cluster.setDescription("test2");
           cluster.setDataCenter(DataCenter.DFW);
           cluster.setClusterIpv6Cidr("2001:4801:79f1:1::/64");
           cluster.setPassword("e2fed4da98a840a40788acb64940469d");
           cluster.setUsername("admin");
           cluster.setStatus(ClusterStatus.ACTIVE);
           cluster.setId(1);

           iPv4Ranges = new IPv4Ranges();
           iPv4Ranges.add(IPv4ToolSet.ipv4BlockToRange("192.168.0.1/30"));

           when(virtualIpService.getVipsByClusterId(cluster.getId())).thenReturn(vips);
       }

       @Test
       public void shouldAddVips() throws Exception {
           clusterService.addVirtualIpBlocks(iPv4Ranges, VirtualIpType.PUBLIC, cluster.getId());

           verify(clusterRepository, times(1)).getClusterById(cluster.getId());
           verify(virtualIpService, times(1)).getVipsByClusterId(cluster.getId());
           verify(virtualIpService, times(1)).batchPersist(captor.capture());
           List<VirtualIp> cappedVips = captor.getValue();
           Assert.assertEquals(2, cappedVips.size());
           Assert.assertEquals("192.168.0.1", cappedVips.get(0).getIpAddress());
           Assert.assertEquals("192.168.0.2", cappedVips.get(1).getIpAddress());
       }

       @Test
       public void shouldAddVipsAndSkipDupes() throws Exception {
           vips = new ArrayList<>();
           vip1.setId(1);
           vip1.setIpAddress("192.168.0.1");
           vips.add(vip1);
           when(virtualIpService.getVipsByClusterId(cluster.getId())).thenReturn(vips);

           clusterService.addVirtualIpBlocks(iPv4Ranges, VirtualIpType.PUBLIC, cluster.getId());

           verify(clusterRepository, times(1)).getClusterById(cluster.getId());
           verify(virtualIpService, times(1)).getVipsByClusterId(cluster.getId());
           verify(virtualIpService, times(1)).batchPersist(captor.capture());
           List<VirtualIp> cappedVips = captor.getValue();
           Assert.assertEquals(1, cappedVips.size());
           Assert.assertEquals("192.168.0.2", cappedVips.get(0).getIpAddress());
       }

       @Test
       public void shouldSkipAllAndNotPersist() throws Exception {
           vips = new ArrayList<>();
           vip1.setId(1);
           vip1.setIpAddress("192.168.0.1");
           VirtualIp vip2 = new VirtualIp();
           vip2.setId(2);
           vip2.setIpAddress("192.168.0.2");
           vips.add(vip1);
           vips.add(vip2);
           when(virtualIpService.getVipsByClusterId(cluster.getId())).thenReturn(vips);

           clusterService.addVirtualIpBlocks(iPv4Ranges, VirtualIpType.PUBLIC, cluster.getId());

           verify(clusterRepository, times(1)).getClusterById(cluster.getId());
           verify(virtualIpService, times(1)).getVipsByClusterId(cluster.getId());
           verify(virtualIpService, times(0)).batchPersist(captor.capture());
       }
    }
}
