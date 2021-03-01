package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.JobStateService;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class JobResourceTest {

    public static class WhenRetrievingResources {

        static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

        @Mock
        private JobStateService jobStateService;
        @InjectMocks
        private JobResource jobResource;
        private JobState jobState = new JobState();
        private Response response;


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            jobState.setId(1);
            jobResource.setMockitoAuth(true);
            jobResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            doReturn(jobState).when(jobStateService).getById(ArgumentMatchers.anyInt());

        }

        @Test
        public void shouldRetrieveTheHost() {
          response = jobResource.getHost();
          Assert.assertEquals(200, response.getStatus());
        }

    }
}
