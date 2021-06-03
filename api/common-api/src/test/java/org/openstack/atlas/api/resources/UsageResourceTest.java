package org.openstack.atlas.api.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.GeneralFault;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ItemNotFound;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.impl.UsageServiceImpl;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class UsageResourceTest {

    public static class whenRetrievingUsage {

        UsageResource usageResource;
        List<Usage> cusage;
        @Mock
        Usage usage;

        @Mock
        UsageServiceImpl usageService;

        @Before
        public void setUp() throws DeletedStatusException, EntityNotFoundException {

            MockitoAnnotations.initMocks(this);
            usageResource = new UsageResource();
            cusage = new ArrayList<>();
            cusage.add(usage);
            usageResource.setUsageService(usageService);
            when(usageService.getUsageByAccountIdandLbId(anyInt(), anyInt(), any(), any(), anyInt(), anyInt())).thenReturn(cusage);

        }

        @Test
        public void shouldReturn200whenRetrievingSingleUsage() {
            Response response = usageResource.retrieveUsage(null, null, 0, 1);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn400whenRetrievingUsageWithBadDate() {
            Response response = usageResource.retrieveUsage("05/01/2021", "06/01/2021", 0, 1);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("Date parameters must follow ISO-8601 format", ((GeneralFault) response.getEntity()).getMessage());
        }

        @Test
        public void shouldReturn200whenRetrievingUsageWithNullOffsetAndLimit() {
            Response response = usageResource.retrieveUsage(null, null, null, null);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn200whenRetrievingUsageValidDate() {
            Response response = usageResource.retrieveUsage("2010-12-21T12:32:07-06:00", "2010-12-21T12:32:07-06:00", 0, 1);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn200whenRetrievingMultipleUsage() {
            cusage.add(usage);
            Response response = usageResource.retrieveUsage("2010-12-21T12:32:07-06:00", "2010-12-21T12:32:07-06:00", 0, 2);
            Assert.assertEquals(200, response.getStatus());
        }

    }


    public static class whenRetrievingCurrentUsage {
        List<Usage> cusage;
        @Mock
        Usage usage;
        @Mock
        PaginationHelper paginationHelper;

        @Mock
        UsageServiceImpl usageService;
        @InjectMocks
        UsageResource usageResource;

        @Before
        public void setUp() throws DeletedStatusException, EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            usageResource.setAccountId(1234);
            usageResource.setLoadBalancerId(1);
            cusage = new ArrayList<>();
            cusage.add(usage);
            when(usageService.getUsageByAccountIdandLbId(anyInt(), anyInt(), any(), any(), anyInt(), anyInt())).thenReturn(cusage);
        }

        @Test
        public void shouldReturn200WithLimitTwo() throws Exception {
            Response response = usageResource.retrieveCurrentUsage(0,2);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn200WithZeroLimitAndOffset() {
            Response response = usageResource.retrieveCurrentUsage(0,0);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn404() throws Exception {
            when(usageService.getUsageByAccountIdandLbId(anyInt(), anyInt(), any(), any(), anyInt(), anyInt())).thenThrow(EntityNotFoundException.class);
            Response response = usageResource.retrieveCurrentUsage(0,2);
            Assert.assertEquals(404, response.getStatus());
            Assert.assertEquals("Object not Found", ((ItemNotFound) response.getEntity()).getMessage());

        }

        @Test
        public void shouldReturn200WithLimitIsZero() {
            Response response = usageResource.retrieveCurrentUsage(0,0);
            Assert.assertEquals(200, response.getStatus());
        }


    }

}
