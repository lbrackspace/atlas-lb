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
import org.openstack.atlas.service.domain.entities.SslCipherProfile;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.List;

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

    public static class whenRetrievingSslCipherProfile{

        @Mock
        private EntityManager entityManager;
        @Mock
        Query qry;
        @InjectMocks
        private SslCipherProfileRepository sslCipherProfileRepository;
        private SslCipherProfile sslCipherProfile;
        private List<SslCipherProfile> sslCipherProfileList;

        @Before
        public void standUp(){
            MockitoAnnotations.initMocks(this);
            sslCipherProfile = new SslCipherProfile();
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            sslCipherProfile.setComments("ciphers comment");
        }

        @Test
        public void shouldRetrieveAllProfilesSuccessfully() {
            String hqlStr = "SELECT s FROM SslCipherProfile s";
            sslCipherProfileList = new ArrayList<>();
            sslCipherProfileList.add(sslCipherProfile);
            when(entityManager.createQuery(hqlStr)).thenReturn(qry);
            when(qry.getResultList()).thenReturn(sslCipherProfileList);
            List<SslCipherProfile> reSslCipherProfiles = sslCipherProfileRepository.fetchAllProfiles();
            Assert.assertTrue(reSslCipherProfiles.size() > 0);
        }

        @Test
        public void shouldRetrieveProfileByIdSuccessfully() {
            int id = 1;
            when(entityManager.find(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(sslCipherProfile);
            SslCipherProfile reSslCipherProfile = sslCipherProfileRepository.getById(id);
            Assert.assertNotNull(reSslCipherProfile);
        }

        @Test
        public void shouldRetrieveProfileByNameSuccessfully() {
            String pName = "cprofile";
            String hqlStr = "SELECT s FROM SslCipherProfile s where lower(s.name) = :name";
            sslCipherProfileList = new ArrayList<>();
            sslCipherProfileList.add(sslCipherProfile);
            when(entityManager.createQuery(hqlStr)).thenReturn(qry);
            when(qry.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(qry);
            when(qry.getResultList()).thenReturn(sslCipherProfileList);
            SslCipherProfile reSslCipherProfile = sslCipherProfileRepository.getByName(pName);
            Assert.assertTrue(sslCipherProfileList.size() > 0);
        }

        @Test
        public void shouldNotReturnAnythingIfThereAreNoProfiles() {
            String hqlStr = "SELECT s FROM SslCipherProfile s";
            sslCipherProfileList = new ArrayList<>();
            when(entityManager.createQuery(hqlStr)).thenReturn(qry);
            when(qry.getResultList()).thenReturn(sslCipherProfileList);
            List<SslCipherProfile> reSslCipherProfiles = sslCipherProfileRepository.fetchAllProfiles();
            Assert.assertEquals(0, reSslCipherProfiles.size());
        }

        @Test
        public void shouldNotReturnAnythingIfThereIsNoProfileById() {
            int id = 1;
            when(entityManager.find(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(null);
            sslCipherProfile = sslCipherProfileRepository.getById(id);
            Assert.assertEquals(null, sslCipherProfile);
        }

        @Test
        public void shouldNotReturnAnythingIfThereIsNoProfileByName() {
            String name = "pname";
            String hqlStr = "SELECT s FROM SslCipherProfile s where lower(s.name) = :name";
            when(entityManager.createQuery(hqlStr)).thenReturn(qry);
            when(qry.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(qry);
            when(qry.getResultList()).thenReturn(null);
            sslCipherProfile = sslCipherProfileRepository.getByName(name);
            Assert.assertEquals(null, sslCipherProfile);
        }
    }
}
