package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.SslCipherProfileService;

import javax.ws.rs.core.Response;

@RunWith(Enclosed.class)
public class SslCipherProfileResourceTest {

    public static class WhenSavingSslCipherProfile {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private SslCipherProfileService sslCipherProfileService;
        @InjectMocks
        private SslCipherProfileResource sslCipherProfileResource;
        private SslCipherProfile sslCipherProfile = new SslCipherProfile();
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
        public void shouldFailValidaionWhenIdIsPassedInRequest() {
            sslCipherProfile.setId(1);
            response = sslCipherProfileResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldFailValidaionWhenNameIsNotPassedInRequest() {
            response = sslCipherProfileResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldFailValidaionWhenCiphersAreNotPassedInRequest() {
            sslCipherProfile.setName("cProfile");
            response = sslCipherProfileResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldPassValidaionAndSaveCipherProfile() throws BadRequestException, EntityNotFoundException {
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            response = sslCipherProfileResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldThrowBadRequestException() throws BadRequestException {
            sslCipherProfile.setName("cProfile");
            sslCipherProfile.setCiphers("ciphers");
            Mockito.doThrow(BadRequestException.class).when(sslCipherProfileService).create(ArgumentMatchers.any());
            response = sslCipherProfileResource.createCipherProfile(sslCipherProfile);
            Assert.assertEquals(400, response.getStatus());
        }
    }
}
