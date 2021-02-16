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
import org.openstack.atlas.service.domain.entities.DataCenter;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ClusterRepositoryTest {

   public static class whenRetrievingClusterType{

       @Mock
       Query qry;
       @Mock
       EntityManager entityManager;
       @InjectMocks
       ClusterRepository clusterRepository;
       private List<ClusterType> cTypeList;


       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           cTypeList = new ArrayList<ClusterType>();
           cTypeList.add(ClusterType.STANDARD);
           when(entityManager.createQuery(anyString())).thenReturn(qry);
           when(qry.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(qry);
           when(qry.getResultList()).thenReturn(cTypeList);

       }

       @Test
       public void shouldReturnHostWithNullHostId() throws Exception {
           ClusterType cType = clusterRepository.getClusterTypeByAccountId(1);
           Assert.assertEquals(cType, ClusterType.STANDARD);
       }
   }

   public static class whenPersistingACluster {

       @Mock
       EntityManager entityManager;
       @InjectMocks
       ClusterRepository clusterRepository;

       Cluster cluster;


       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           cluster = new Cluster();
           cluster.setName("dev2");
           cluster.setDescription("test2");
           cluster.setDataCenter(DataCenter.DFW);
           cluster.setClusterIpv6Cidr("2001:4801:79f1:1::/64");
           cluster.setPassword("e2fed4da98a840a40788acb64940469d");
           cluster.setUsername("admin");
           cluster.setStatus(ClusterStatus.ACTIVE);
       }

       @Test
       public void shouldPersistACluster() {
           clusterRepository.save(cluster);
           verify(entityManager, times(1)).persist(cluster);
       }
   }
}
