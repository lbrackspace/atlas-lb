package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.GroupService;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.doThrow;

@RunWith(Enclosed.class)
public class GroupResourceTest {

    public static class WhenSavingResources {

        @Mock
        private GroupService groupService;
        @InjectMocks
        private GroupResource groupResource;
        private Response response;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void shouldSaveTheGroup() {
          response = groupResource.createAccountGroupRateLimit(ArgumentMatchers.anyInt());
          Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldThrowBadRequestException() throws BadRequestException, EntityNotFoundException {
            doThrow(BadRequestException.class).when(groupService).insertAccountGroup(ArgumentMatchers.any());
            response = groupResource.createAccountGroupRateLimit(ArgumentMatchers.anyInt());
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Bad request.", ((BadRequest)response.getEntity()).getMessage());
        }

    }

    public static class WhenUpdatingResources {

        @Mock
        private GroupService groupService;
        @InjectMocks
        private GroupResource groupResource;
        int id = 1;
        private Response response;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            groupResource.setMockitoAuth(true);
        }

        @Test
        public void shouldUpdateTheGroupWithDefault() {
            response = groupResource.updateGroup("Y");
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldUpdateTheGroupWithNonDefault() {
            response = groupResource.updateGroup("N");
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundWhileUpdatingTheGroup() throws EntityNotFoundException {
            doThrow(EntityNotFoundException.class).when(groupService).updateGroup(ArgumentMatchers.any());
            response = groupResource.updateGroup("Y");
            Assert.assertEquals(404, response.getStatus());
        }
    }

    public static class WhenDeletingResources {

        @Mock
        private GroupService groupService;
        @InjectMocks
        private GroupResource groupResource;
        int id = 1;
        private Response response;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void shouldDeleteTheGroup() {
            response = groupResource.deleteGroup();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundWhileDeletingTheGroup() throws EntityNotFoundException, BadRequestException {
            doThrow(EntityNotFoundException.class).when(groupService).deleteGroup(ArgumentMatchers.any());
            response = groupResource.deleteGroup();
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldThrowBadRequestWhileDeletingTheGroup() throws EntityNotFoundException, BadRequestException {
            doThrow(BadRequestException.class).when(groupService).deleteGroup(ArgumentMatchers.any());
            response = groupResource.deleteGroup();
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Bad request.", ((BadRequest)response.getEntity()).getMessage());
        }
    }
}
