package org.openstack.atlas.service.domain.repository;

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
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class HostRepositoryTest {

   public static class whenGettingDefaultActiveHost{

       @Mock
       Query qry;
       @Mock
       EntityManager entityManager;
       @InjectMocks
       HostRepository hostRepository;

       Host host;
       List<Host> hosts;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);

           host = new Host();
           hosts = new ArrayList<>();

           host.setId(1);
           host.setHostStatus(HostStatus.ACTIVE_TARGET);
           hosts.add(host);

           when(qry.setParameter("hostStatus",HostStatus.ACTIVE_TARGET)).thenReturn(qry);
           when(qry.setParameter("clusterId", 1)).thenReturn(qry);
           when(qry.setParameter("hostId", 2)).thenReturn(qry);
           when(entityManager.createQuery(anyString())).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);

       }

       @Test
       public void shouldReturnHostWithNullHostId() throws Exception {
           Host defaultHost = hostRepository.getDefaultActiveHost(1, null);
           Assert.assertEquals(host, defaultHost);
       }

       @Test
       public void shouldReturnHostWithHostId() throws Exception {
           Host defaultHost = hostRepository.getDefaultActiveHost(1, 2);
           Assert.assertEquals(host, defaultHost);
       }
   }


   public static class whenRetrievingHostRestEndpoint {

       @Mock
       Query qry;
       @Mock
       EntityManager entityManager;
       @InjectMocks
       HostRepository hostRepository;

       Host host;
       List<Host> hosts;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);

           host = new Host();
           hosts = new ArrayList<>();

           host.setId(1);
           host.setHostStatus(HostStatus.REST_API_ENDPOINT);
           hosts.add(host);

           String hqlStr = "from Host h where h.restEndpointActive  = 1 "
                   + "and h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER', 'SOAP_API_ENDPOINT', 'REST_API_ENDPOINT') "
                   + "and h.cluster.id = :clusterId "
                   + "order by h.hostStatus desc, h.id asc";
           when(entityManager.createQuery(hqlStr)).thenReturn(qry);
           when(qry.setParameter("clusterId", 1)).thenReturn(qry);
           when(qry.setMaxResults(1)).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);

       }

       @Test
       public void shouldReturnRestEndpointHost() throws Exception {
           Host reHost = hostRepository.getRestEndPointHost(1);
           Assert.assertEquals(host, reHost);
       }

       @Test
       public void shouldReturnNullWhenNoHostMeetsCriteria() throws Exception {
           when(qry.getResultList()).thenReturn(new ArrayList<>());
           Host reHost = hostRepository.getRestEndPointHost(1);
           Assert.assertNull(reHost);
       }
   }

   //CLB-1199
   public static class WhenRetrievingHosts{

       @Mock
       Query qry;
       @Mock
       EntityManager entityManager;
       @InjectMocks
       HostRepository hostRepository;

       Host host;
       Cluster cluster;
       List<Host> hosts;

       @Before
       public void standUp(){

           MockitoAnnotations.initMocks(this);

           host = new Host();
           cluster = new Cluster();
           cluster.setClusterType(ClusterType.INTERNAL);
           cluster.setStatus(ClusterStatus.ACTIVE);
           hosts = new ArrayList<>();

           host.setId(1);
           host.setCluster(cluster);
           hosts.add(host);
       }

       @Test
       public void shouldReturnListOfHostsWithSpecifiedClusterId(){
           String hqlStr = "from Host h where h.cluster.id = :clusterId AND h.hostStatus not in (:hostStatus, :hostStatus1)";
           when(entityManager.createQuery(hqlStr)).thenReturn(qry);
           when(qry.setParameter("clusterId", 1)).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);
           List<Host> hostList = hostRepository.getAllActiveHostsByClusterId(1);
           Assert.assertTrue(hostList.size() > 0);
       }

       @Test
       public void shouldReturnNoListOfHostsWhenHostIsInactive(){
           hosts.remove(0);
           String hqlStr = "from Host h where h.cluster.id = :clusterId AND h.hostStatus not in (:hostStatus, :hostStatus1)";
           when(entityManager.createQuery(hqlStr)).thenReturn(qry);
           when(qry.setParameter("hostStatus", HostStatus.OFFLINE)).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);
           List<Host> hostList = hostRepository.getAllActiveHostsByClusterId(1);
           Assert.assertEquals(0, hostList.size());
       }

       @Test
       public void shouldReturnEmptyListOfHostsWhenSpecifiedClusterIdDoestExist(){
           hosts.remove(0);
           String hqlStr = "from Host h where h.cluster.id = :clusterId AND h.hostStatus not in (:hostStatus, :hostStatus1)";
           when(entityManager.createQuery(hqlStr)).thenReturn(qry);
           when(qry.setParameter("clusterId", 1)).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);
           List<Host> hostList = hostRepository.getAllActiveHostsByClusterId(1);
           Assert.assertTrue(hostList.size() == 0);
       }

       @Test
       public void shouldReturnListOfActiveHostsWithSpecifiedClusterType(){
           String hqlStr = "from Host h where h.hostStatus not in (:hostStatus, :hostStatus1) AND h.cluster.clusterType = :clusterType";
           when(entityManager.createQuery(hqlStr)).thenReturn(qry);
           when(qry.setParameter("hostStatus", HostStatus.ACTIVE)).thenReturn(qry);
           when(qry.setParameter("clusterType", ClusterType.INTERNAL)).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);
           List<Host> hostList = hostRepository.getAllActiveHostsByClusterType(ClusterType.INTERNAL);
           Assert.assertTrue(hostList.size() > 0);
       }

       @Test
       public void shouldReturnEmptyListOfHostsWhenSpecifiedClusterTypeDoestMatch(){
           hosts.remove(0);
           String hqlStr = "from Host h where h.hostStatus not in (:hostStatus, :hostStatus1) AND h.cluster.clusterType = :clusterType";
           when(entityManager.createQuery(hqlStr)).thenReturn(qry);
           when(qry.setParameter("hostStatus", HostStatus.ACTIVE)).thenReturn(qry);
           when(qry.setParameter("clusterType", ClusterType.STANDARD)).thenReturn(qry);
           when(qry.getResultList()).thenReturn(hosts);
           List<Host> hostList = hostRepository.getAllActiveHostsByClusterType(ClusterType.STANDARD);
           Assert.assertTrue(hostList.size() == 0);
       }

   }


    //CLB-1016
    public static class WhenUpdatingHostEndpoint{

        @Mock
        EntityManager entityManager;
        @Mock
        CriteriaBuilder criteriaBuilder;
        @Mock
        CriteriaQuery<Host> criteriaQuery;
        @Mock
        Root<Host> root;
        @Mock
        Predicate predicate;
        @Mock
        TypedQuery typedQuery;

        @InjectMocks
        HostRepository hostRepository;

        Host host;
        Cluster cluster;

        @Before
        public void standUp(){

            MockitoAnnotations.initMocks(this);
            host = new Host();
            cluster =new Cluster();

            cluster.setClusterType(ClusterType.INTERNAL);
            cluster.setId(1);
            cluster.setName("dev");

            host.setId(1);
            host.setHostStatus(HostStatus.ACTIVE);
            host.setCluster(cluster);
            host.setName("172.24.1.1");
            host.setManagementIp("172.24.1.1");
            host.setRestEndpointActive(true);

            doReturn(criteriaBuilder).when(entityManager).getCriteriaBuilder();
            doReturn(criteriaQuery).when(criteriaBuilder).createQuery(ArgumentMatchers.any());
            doReturn(root).when(criteriaQuery).from(Host.class);
            doReturn(predicate).when(criteriaBuilder).equal(ArgumentMatchers.any(), ArgumentMatchers.any());
            doReturn(criteriaQuery).when(criteriaQuery).select(root);
            doReturn(criteriaQuery).when(criteriaQuery).where(predicate);


        }

        @Test
        public void shouldReturn200WhenRestEndPointActiveSuccessfullyUpdated() throws EntityNotFoundException {
            doReturn(typedQuery).when(entityManager).createQuery(ArgumentMatchers.eq(criteriaQuery));
            doReturn(typedQuery).when(typedQuery).setLockMode(ArgumentMatchers.any());
            doReturn(typedQuery).when(typedQuery).setMaxResults(ArgumentMatchers.anyInt());
            doReturn(host).when(typedQuery).getSingleResult();
            Host h = hostRepository.updateEndpoints(host);
            verify(entityManager, times(1)).merge(host);
            Assert.assertEquals(true, h.getRestEndpointActive());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenHostFetchFailed() throws EntityNotFoundException {
            doThrow(EntityNotFoundException.class).when(entityManager).createQuery(ArgumentMatchers.eq(criteriaQuery));
            hostRepository.updateEndpoints(host);
        }
    }
}
