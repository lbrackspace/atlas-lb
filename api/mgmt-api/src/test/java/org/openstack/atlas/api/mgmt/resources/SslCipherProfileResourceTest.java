package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ItemNotFound;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.SslCipherProfileService;

import javax.ws.rs.core.Response;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class SslCipherProfileResourceTest {

    public static class WhenRetrievingSslCipherProfile {


        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private SslCipherProfileService sslCipherProfileService;
        @InjectMocks
        private SslCipherProfileResource sslCipherProfileResource;
        private org.openstack.atlas.service.domain.entities.SslCipherProfile sslCipherProfile = new org.openstack.atlas.service.domain.entities.SslCipherProfile();
        private List<org.openstack.atlas.service.domain.entities.SslCipherProfile> sslCipherProfileList;
        private Response response;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            sslCipherProfileResource.setMockitoAuth(true);
            sslCipherProfileResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldRetrieveCipherProfileById() throws EntityNotFoundException {
            sslCipherProfile.setId(1);
            sslCipherProfile.setName("cname");
            sslCipherProfile.setCiphers("ciphers");
            sslCipherProfile.setComments("comments");
            Mockito.doReturn(sslCipherProfile).when(sslCipherProfileService).getById(ArgumentMatchers.anyInt());
            response = sslCipherProfileResource.getById();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundExceptionWhileRetrievingAProfileById(){
            int id = 1;
            try {
                Mockito.doThrow(EntityNotFoundException.class).when(sslCipherProfileService).getById(id);
                response = sslCipherProfileResource.getById();
            }catch(EntityNotFoundException e){
                String msg = "There is no cipher profile available with ID " + id;
                Assert.assertEquals(msg, e.getMessage());
            }
        }
    }

    public static class WhenCreatingSslCipherProfile {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private SslCipherProfileService sslCipherProfileService;
        @InjectMocks
        private SslCipherProfilesResource sslCipherProfilesResource;
        private SslCipherProfile sslCipherProfile = new SslCipherProfile();
        private Response response;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            sslCipherProfilesResource.setMockitoAuth(true);
            sslCipherProfilesResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldFailValidaionWhenIdIsPassedInRequest() {
            sslCipherProfile.setId(1);
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            response = sslCipherProfilesResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must not include ID for this request",
                    ((BadRequest)response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldFailValidaionWhenNameIsNotPassedInRequest() {
            response = sslCipherProfilesResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide a valid cipher profile name",
                    ((BadRequest)response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldFailValidaionWhenCiphersAreNotPassedInRequest() {
            sslCipherProfile.setName("cProfile");
            response = sslCipherProfilesResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must provide ciphers text",
                    ((BadRequest)response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldPassValidaionAndSaveCipherProfile() {
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            response = sslCipherProfilesResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(202, response.getStatus());
            Assert.assertEquals(sslCipherProfile.getCiphers(), ((SslCipherProfile) response.getEntity()).getCiphers());
            Assert.assertEquals(sslCipherProfile.getComments(), ((SslCipherProfile) response.getEntity()).getComments());
            Assert.assertEquals(sslCipherProfile.getName(), ((SslCipherProfile) response.getEntity()).getName());
        }
    }

    public static class WhenUpdatingSslCipherProfile {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private SslCipherProfileService sslCipherProfileService;
        @InjectMocks
        private SslCipherProfileResource sslCipherProfileResource;
        private SslCipherProfile sslCipherProfile = new SslCipherProfile();
        private org.openstack.atlas.service.domain.entities.SslCipherProfile domainProfile = new org.openstack.atlas.service.domain.entities.SslCipherProfile();
        private Response response;


        @Before
        public void setUp() throws BadRequestException, EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            sslCipherProfileResource.setMockitoAuth(true);
            sslCipherProfileResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            domainProfile.setId(1);
            domainProfile.setCiphers("cipherString");
            domainProfile.setComments("aComment");
            domainProfile.setName("profileName");
            when(sslCipherProfileService.update(any(), any())).thenReturn(domainProfile);

        }

        @Test
        public void shouldFailValidaionWhenIdIsPassedInRequest() {
            sslCipherProfile.setId(1);
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            response = sslCipherProfileResource.updateCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Must not include ID for this request",
                    ((BadRequest)response.getEntity()).getValidationErrors().getMessages().get(0));
        }

        @Test
        public void shouldReturnEntityNotFoundForNonExistentID() throws BadRequestException, EntityNotFoundException {
            doThrow(EntityNotFoundException.class).when(sslCipherProfileService).update(any(), any());
            response = sslCipherProfileResource.updateCipherProfile(sslCipherProfile);
            Assert.assertEquals(404, response.getStatus());
            Assert.assertEquals("Object not Found",
                    ((ItemNotFound) response.getEntity()).getMessage());
        }

        @Test
        public void shouldPassValidaionAndSaveCipherProfile() {
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            response = sslCipherProfileResource.updateCipherProfile(sslCipherProfile);
            Assert.assertEquals(202, response.getStatus());
        }
    }

    public static class whenDeletingSslCipherProfile {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private SslCipherProfileService sslCipherProfileService;
        @InjectMocks
        private SslCipherProfileResource sslCipherProfileResource;


        @Before
        public void setUp() {

            MockitoAnnotations.initMocks(this);
            sslCipherProfileResource = new SslCipherProfileResource();
            sslCipherProfileResource.setMockitoAuth(true);
            sslCipherProfileResource.setSslCipherProfileService(sslCipherProfileService);
        }

        @Test
        public void shouldReturn200UponSuccessfulDeletion() {

            Response response = sslCipherProfileResource.deleteSslCipherProfile();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldReturn404WhenCipherProfileNotFound() throws EntityNotFoundException {
            doThrow(EntityNotFoundException.class).when(sslCipherProfileService).deleteSslCipherProfile(any(org.openstack.atlas.service.domain.entities.SslCipherProfile.class));
            Response response = sslCipherProfileResource.deleteSslCipherProfile();
            Assert.assertEquals(404, response.getStatus());
        }
    }

}
