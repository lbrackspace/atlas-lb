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
import org.openstack.atlas.service.domain.entities.SslCipherProfile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class SslCipherProfileRepositoryTest {

   public static class whenPersistingAnSslCipherProfile{

       @Mock
       private EntityManager entityManager;
       @InjectMocks
       private SslCipherProfileRepository sslCipherProfileRepository;
       private SslCipherProfile sslCipherProfile;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           sslCipherProfile = new SslCipherProfile();
           sslCipherProfile.setName("cProfile");
           sslCipherProfile.setCiphers("ciphers");
           sslCipherProfile.setComments("ciphers comment");
       }

       @Test
       public void shouldReturnHostWithNullHostId() throws Exception {
           sslCipherProfileRepository.create(sslCipherProfile);
           verify(entityManager, times(1)).persist(sslCipherProfile);
       }
   }
}
