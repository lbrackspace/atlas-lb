package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.SslCipherProfile;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.SslCipherProfileRepository;
import org.openstack.atlas.service.domain.services.impl.SslCipherProfileServiceImpl;

import javax.xml.ws.Response;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class SslCipherProfileServiceImplTest {

   public static class whenSavingSslCipherProfile {

       @Mock
       private SslCipherProfileRepository sslCipherProfileRepository;
       @InjectMocks
       private SslCipherProfileServiceImpl sslCipherProfileServiceImpl;
       private Response response;
       private SslCipherProfile sslCipherProfile;
       private SslCipherProfile sslCipherProfileEntity;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           sslCipherProfile = new SslCipherProfile();
           sslCipherProfile.setName("cName1");
           sslCipherProfileEntity = new SslCipherProfile();
       }

       @Rule
       public final ExpectedException expectedException = ExpectedException.none();

       @Test(expected = BadRequestException.class)
       public void shouldThrowBadRequestExceptionWhenProfileNameAlreadyExists() throws BadRequestException {
           sslCipherProfileEntity.setName("cName1");
           doReturn(sslCipherProfileEntity).when(sslCipherProfileRepository).getByName(ArgumentMatchers.anyString());
           try{
               sslCipherProfileServiceImpl.create(this.sslCipherProfile);
           }catch(BadRequestException ex){
               String message = "Bad Request - profile with the same name already exists";
               Assert.assertEquals(message, ex.getMessage());
               throw ex;
           }
       }

       @Test()
       public void shouldSaveProfileWhenProfileNameDoestExist() throws BadRequestException {
           doReturn(null).when(sslCipherProfileRepository).getByName(ArgumentMatchers.anyString());
           sslCipherProfileServiceImpl.create(sslCipherProfile);
           verify(sslCipherProfileRepository, times(1)).create(sslCipherProfile);
       }
   }

   public static class whenUpdatingSslCipherProfile {

       @Mock
       private SslCipherProfileRepository sslCipherProfileRepository;
       @InjectMocks
       private SslCipherProfileServiceImpl sslCipherProfileServiceImpl;
       private Response response;
       private SslCipherProfile sslCipherProfile;
       private SslCipherProfile sslCipherProfileEntity;

       @Before
       public void standUp(){
           MockitoAnnotations.initMocks(this);
           sslCipherProfile = new SslCipherProfile();
           sslCipherProfile.setName("cName1");

           sslCipherProfileEntity = new SslCipherProfile();
           sslCipherProfileEntity.setId(2);
           sslCipherProfileEntity.setCiphers("Ciphers");
           sslCipherProfileEntity.setComments("comment2");
           sslCipherProfileEntity.setName("cName2");

       }

       @Test(expected = BadRequestException.class)
       public void shouldThrowBadRequestExceptionWhenProfileNameAlreadyExists()
               throws BadRequestException, EntityNotFoundException {
           sslCipherProfile.setName("cName2");
           doReturn(sslCipherProfileEntity).when(sslCipherProfileRepository).getByName(ArgumentMatchers.anyString());
           doReturn(sslCipherProfileEntity).when(sslCipherProfileRepository).getById(anyInt());
           sslCipherProfileServiceImpl.update(1, sslCipherProfile);
           verify(sslCipherProfileRepository, times(1)).update(sslCipherProfileEntity);

       }

       @Test(expected = EntityNotFoundException.class)
       public void shouldThrowEntityNotFoundIfEntityDoesntExist() throws BadRequestException, EntityNotFoundException {
           doThrow(EntityNotFoundException.class).when(
                   sslCipherProfileRepository).getById(anyInt());
           sslCipherProfileServiceImpl.update(1, sslCipherProfile);
           verify(sslCipherProfileRepository, times(1)).update(sslCipherProfileEntity);
       }

       @Test()
       public void shouldUpdateCipherProfileSettings() throws BadRequestException, EntityNotFoundException {
           sslCipherProfile.setName("cName22");
           sslCipherProfile.setCiphers("ciphers2");
           doReturn(sslCipherProfileEntity).when(sslCipherProfileRepository).getById(anyInt());
           sslCipherProfileServiceImpl.update(1, sslCipherProfile);
           verify(sslCipherProfileRepository, times(1)).update(sslCipherProfileEntity);
           Assert.assertEquals("2", sslCipherProfileEntity.getId().toString());
           Assert.assertEquals("cName22", sslCipherProfileEntity.getName());
           Assert.assertEquals("comment2", sslCipherProfileEntity.getComments());
           Assert.assertEquals("ciphers2", sslCipherProfileEntity.getCiphers());
       }
   }
}
