package org.openstack.atlas.api.mgmt.resources;

import com.sun.org.apache.regexp.internal.RE;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomains;
import org.openstack.atlas.service.domain.services.AllowedDomainsService;

import javax.ws.rs.core.Response;
import java.sql.Array;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class AllowedDomainsResourceTest {

    public static class whenRetrievingAllowedDomains {

        AllowedDomainsResource allowedDomainsResource;

        List<String> ads;
        Set<String> allowedDomains;

        @Mock
        AllowedDomainsService allowedDomainsService;



        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            allowedDomainsResource = new AllowedDomainsResource();
            allowedDomainsResource.setAllowedDomainsService(allowedDomainsService);
            when(allowedDomainsService.getAllowedDomains()).thenReturn(allowedDomains);
        }

        @Test
        public void shouldReturn200withEmptyAllowedDomains() throws Exception {
            Response response = allowedDomainsResource.retireveAllowedDomains("Test");
            Assert.assertEquals(200, response.getStatus());
        }
        @Test
        public void shouldReturn500withAllowedDomainsWithNullRequest() throws Exception {
            Response response = allowedDomainsResource.retireveAllowedDomains(null);
            Assert.assertEquals(500, response.getStatus());

        }



    }

    public static class whenDeletingAllowedDomains {

        AllowedDomainsResource allowedDomainsResource;

        @Mock
        AllowedDomainsService allowedDomainsService;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            allowedDomainsResource = new AllowedDomainsResource();
            allowedDomainsResource.setAllowedDomainsService(allowedDomainsService);
            when(allowedDomainsService.remove("test")).thenReturn(true);

        }

        @Test
        public void shouldReturn200WhenDeletingAllowedDomain() throws Exception {
            Response response = allowedDomainsResource.deleteAllowedDomain("test");
            Assert.assertEquals(200, response.getStatus());
        }
        @Test
        public void shouldReturn410WhenNameDoesNotMatchWhenDeletingAllowedDomains()throws Exception {
            Response response = allowedDomainsResource.deleteAllowedDomain("brokenName");
            Assert.assertEquals(410, response.getStatus());
        }



    }

    public static class whenAddingAllowedDomain{

        AllowedDomainsResource allowedDomainsResource;

        List<String> ads;
        Set<String> allowedDomains;
        AllowedDomain allowedDomain;

        @Mock
        AllowedDomainsService allowedDomainsService;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            allowedDomainsResource = new AllowedDomainsResource();
            allowedDomainsResource.setAllowedDomainsService(allowedDomainsService);
            allowedDomain = new AllowedDomain();
            allowedDomain.setName("testName");
            when(allowedDomainsService.add("testName")).thenReturn(true);

        }

        @Test
        public void shouldReturn200WhenAddingAllowedDomain() throws Exception {
            Response response = allowedDomainsResource.addAllowedDomain(allowedDomain);
            Assert.assertEquals(200, response.getStatus());
        }
        @Test
        public void shouldReturn409WhenNameDoesNotMatchWhenAddingAllowedDomains()throws Exception {
            allowedDomain.setName(null);
            Response response = allowedDomainsResource.addAllowedDomain(allowedDomain);
            Assert.assertEquals(409, response.getStatus());
        }


    }


}
