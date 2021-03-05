package org.openstack.atlas.service.domain.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.SslCipherProfile;

import javax.persistence.EntityManager;

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
       public void shouldPersistTheProfileSuccessfully() throws Exception {
           sslCipherProfileRepository.create(sslCipherProfile);
           verify(entityManager, times(1)).persist(sslCipherProfile);
       }
   }

   public static class whenUpdatingSslCipherProfile{

       @Mock
       private EntityManager entityManager;
       @InjectMocks
       private SslCipherProfileRepository sslCipherProfileRepository;
       private SslCipherProfile sslCipherProfile;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           sslCipherProfile = new SslCipherProfile();
           sslCipherProfile.setId(2);
           sslCipherProfile.setName("cProfile");
           sslCipherProfile.setCiphers("ciphers");
           sslCipherProfile.setComments("ciphers comment");
       }

       @Test
       public void shouldPersistTheProfileSuccessfully() throws Exception {
           when(entityManager.merge(sslCipherProfile)).thenReturn(sslCipherProfile);
           SslCipherProfile retProfile = sslCipherProfileRepository.update(sslCipherProfile);
           verify(entityManager, times(1)).merge(sslCipherProfile);
           verify(entityManager, times(1)).flush();
           Assert.assertEquals(sslCipherProfile, retProfile);
       }
   }

   public static class whenDeletingCipherProfile {

       @Mock
       private EntityManager entityManager;
       @InjectMocks
       private SslCipherProfileRepository sslCipherProfileRepository;
       private SslCipherProfile sslCipherProfile;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           sslCipherProfile = new SslCipherProfile();
           sslCipherProfile.setId(2);
           sslCipherProfile.setName("cProfile");
           sslCipherProfile.setCiphers("ciphers");
           sslCipherProfile.setComments("ciphers comment");
       }

       @Test
       public void shouldDeleteSslProfile() throws Exception {

           sslCipherProfileRepository.delete(sslCipherProfile);
           verify(entityManager).remove(sslCipherProfile);

       }

   }

}
