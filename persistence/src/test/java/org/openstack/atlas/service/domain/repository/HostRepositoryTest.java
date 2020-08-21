package org.openstack.atlas.service.domain.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.HostStatus;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

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









}
