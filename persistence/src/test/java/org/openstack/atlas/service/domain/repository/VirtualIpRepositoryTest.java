package org.openstack.atlas.service.domain.repository;

import org.hibernate.jpa.internal.EntityManagerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.service.domain.entities.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class VirtualIpRepositoryTest {

   public static class whenBatchInsertingVips{

       @Mock
       Query qry;
       @Mock
       EntityManager entityManager;
       @InjectMocks
       VirtualIpRepository virtualIpRepository;

       List<VirtualIp> vipList;
       VirtualIp vip1;
       Cluster cluster;


       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           vipList = new ArrayList<>();
           vip1 = new VirtualIp();
           cluster = new Cluster();
           cluster.setId(1);
           vip1.setId(1);
           vip1.setIpAddress("192.168.0.1");
           vip1.setCluster(cluster);
           vip1.setVipType(VirtualIpType.PUBLIC);
           vip1.setAllocated(false);
           vipList.add(vip1);

           when(entityManager.createNativeQuery(any())).thenReturn(qry);
       }

       @Test
       public void shouldPersistASingleVip() {
           virtualIpRepository.batchPersist(vipList);
           ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
           verify(entityManager, times(1)).createNativeQuery(argument.capture());
           Assert.assertEquals("INSERT INTO virtual_ip_ipv4" +
                   " (ip_address, type,cluster_id, is_allocated) " +
                   "VALUES('192.168.0.1','PUBLIC',1,0)", argument.getValue());       }

       @Test
       public void shouldPersistATwoBatches() throws Exception {
           vipList.clear();
           // 40 vips will give us two batches
           for (int i = 1; i <= 40; i++) {
               VirtualIp vip = new VirtualIp();
               vip.setId(i);
               vip.setIpAddress("192.168.1." + i);
               vip.setCluster(cluster);
               vip.setVipType(VirtualIpType.PUBLIC);
               vip.setAllocated(false);
               vipList.add(vip);
           }
           virtualIpRepository.batchPersist(vipList);
           verify(entityManager, times(2)).createNativeQuery(any());
       }

       @Test
       public void shouldPersistATwoBatchesOnePartiallyFull() {
           vipList.clear();
           // 30 vips will give us two batches
           for (int i = 1; i <= 30; i++) {
               VirtualIp vip = new VirtualIp();
               vip.setId(i);
               vip.setIpAddress("192.168.1." + i);
               vip.setCluster(cluster);
               vip.setVipType(VirtualIpType.PUBLIC);
               vip.setAllocated(false);
               vipList.add(vip);
           }
           virtualIpRepository.batchPersist(vipList);
           verify(entityManager, times(2)).createNativeQuery(any());
       }

       @Test
       public void shouldPersistASingBatchesPartiallyFull() {
           vipList.clear();
           // 10 vips will give us one batch
           for (int i = 1; i <= 6; i++) {
               VirtualIp vip = new VirtualIp();
               vip.setId(i);
               vip.setIpAddress("192.168.1." + i);
               vip.setCluster(cluster);
               vip.setVipType(VirtualIpType.PUBLIC);
               vip.setAllocated(false);
               vipList.add(vip);
           }
           virtualIpRepository.batchPersist(vipList);
           ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
           verify(entityManager, times(1)).createNativeQuery(argument.capture());
           Assert.assertEquals("INSERT INTO virtual_ip_ipv4" +
                   " (ip_address, type,cluster_id, is_allocated) " +
                   "VALUES('192.168.1.1','PUBLIC',1,0),('192.168.1.2','PUBLIC',1,0)," +
                   "('192.168.1.3','PUBLIC',1,0),('192.168.1.4','PUBLIC',1,0)," +
                   "('192.168.1.5','PUBLIC',1,0),('192.168.1.6','PUBLIC',1,0)", argument.getValue());
       }

       @Test
       public void verifyBatchInsertQueryGenerationForMultiVips() {
           vipList.clear();
           for (int i = 1; i <= 2; i++) {
               VirtualIp vip = new VirtualIp();
               vip.setId(i);
               vip.setIpAddress("192.168.1." + i);
               vip.setCluster(cluster);
               vip.setVipType(VirtualIpType.PUBLIC);
               vip.setAllocated(false);
               vipList.add(vip);
           }

           String query = virtualIpRepository.generateBatchInsertQuery(vipList);
           Assert.assertEquals("INSERT INTO virtual_ip_ipv4" +
                   " (ip_address, type,cluster_id, is_allocated) " +
                   "VALUES('192.168.1.1','PUBLIC',1,0),('192.168.1.2','PUBLIC',1,0)", query);

       }

       @Test
       public void verifyBatchInsertQueryGenerationForSingleVip() {
           String query = virtualIpRepository.generateBatchInsertQuery(vipList);
           Assert.assertEquals("INSERT INTO virtual_ip_ipv4" +
                   " (ip_address, type,cluster_id, is_allocated) " +
                   "VALUES('192.168.0.1','PUBLIC',1,0)", query);

       }
   }

   public static class whenMigratingVipsToCluster {

       VirtualIpRepository virtualIpRepository;

       @Mock
       EntityManagerImpl entityManager;
       @Mock
       Query qry;
       List<String> addresses;
       List<String> batchAddresses;


       @Before
       public void setUp() {
           MockitoAnnotations.initMocks(this);
           virtualIpRepository = new VirtualIpRepository();
           virtualIpRepository.setEntityManager(entityManager);
           addresses = new ArrayList<>();
           addresses.add("192.25.0.1");

           when(entityManager.createNativeQuery(any())).thenReturn(qry);
       }
       @Test
       public void shouldMigrate40Times() {
           addresses.clear();
           for (int i = 1; i <= 40; i++) {
               addresses.add("192.25.0." + i);
           }
           virtualIpRepository.migrateVIPsByCidrBlock(1, addresses);
           verify(entityManager, times(40)).createNativeQuery(any());
       }
       @Test
       public void shouldInsertQueryForOneAddress() {
           String qry = virtualIpRepository.generateBatchInsertQueryForMigration("192.25.0.1", 1);
           Assert.assertEquals("UPDATE virtual_ip_ipv4 set cluster_id = 1 where ip_address = '192.25.0.1' AND is_allocated = 0", qry);
       }
       @Test
       public void shouldMigrate1Time() {
           virtualIpRepository.migrateVIPsByCidrBlock(1, addresses);
           verify(entityManager, times(1)).createNativeQuery(any());
       }


    }
}
