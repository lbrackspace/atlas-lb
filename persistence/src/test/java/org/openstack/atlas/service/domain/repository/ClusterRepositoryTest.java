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
import org.openstack.atlas.service.domain.entities.ClusterType;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
}
