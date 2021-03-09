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
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.SslCipherProfileService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(Enclosed.class)
public class SslCipherProfilesResourceTest {

    public static class WhenSavingSslCipherProfile {

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
        public void shouldPassValidaionAndSaveCipherProfile() throws BadRequestException, EntityNotFoundException {
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            response = sslCipherProfilesResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(202, response.getStatus());
        }
    }

    public static class WhenRetrievingSslCipherProfile {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private SslCipherProfileService sslCipherProfileService;
        @InjectMocks
        private SslCipherProfilesResource sslCipherProfilesResource;
        private org.openstack.atlas.service.domain.entities.SslCipherProfile sslCipherProfile = new org.openstack.atlas.service.domain.entities.SslCipherProfile();
        private List<org.openstack.atlas.service.domain.entities.SslCipherProfile> sslCipherProfileList;
        private Response response;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            sslCipherProfilesResource.setMockitoAuth(true);
            sslCipherProfile.setId(1);
            sslCipherProfile.setName("cname");
            sslCipherProfile.setCiphers("ciphers");
            sslCipherProfile.setComments("comments");
            sslCipherProfilesResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldRetrieveAllCipherProfiles() throws EntityNotFoundException {
            sslCipherProfileList = new ArrayList<>();
            sslCipherProfileList.add(sslCipherProfile);
           Mockito.doReturn(sslCipherProfileList).when(sslCipherProfileService).fetchAllProfiles();
            response = sslCipherProfilesResource.getAllCipherProfiles();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundExceptionWhileRetrievingAllProfiles(){
            try {
                Mockito.doThrow(EntityNotFoundException.class).when(sslCipherProfileService).fetchAllProfiles();
                response = sslCipherProfilesResource.getAllCipherProfiles();
            }catch(EntityNotFoundException e){
                String msg = "There are no cipher profiles available";
                Assert.assertEquals(msg, e.getMessage());
            }
        }

//        @Test
//        public void shouldRetrieveCipherProfileByName() throws EntityNotFoundException {
//            Mockito.doReturn(sslCipherProfile).when(sslCipherProfileService).getByName(ArgumentMatchers.anyString());
//            response = sslCipherProfilesResource.getCipherProfileByName(sslCipherProfile.getName());
//            Assert.assertEquals(200, response.getStatus());
//        }
//
//        @Test
//        public void shouldThrowEntityNotFoundExceptionWhileRetrievingAProfileByName(){
//            String profileName = "cprofile";
//            try {
//                Mockito.doThrow(EntityNotFoundException.class).when(sslCipherProfileService).getByName(ArgumentMatchers.anyString());
//                response = sslCipherProfilesResource.getCipherProfileByName(profileName);
//            }catch(EntityNotFoundException e){
//                String msg = "here is no cipher profile available with the name" + profileName;
//                Assert.assertEquals(msg, e.getMessage());
//            }
//        }
    }
}
