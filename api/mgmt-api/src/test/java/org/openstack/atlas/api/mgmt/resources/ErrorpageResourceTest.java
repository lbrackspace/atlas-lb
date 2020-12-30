package org.openstack.atlas.api.mgmt.resources;

import javassist.tools.rmi.ObjectNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class ErrorpageResourceTest {

    public static class whenRetrievingErrorpage {

        @Mock
        LoadBalancerService loadBalancerService;

        ErrorpageResource errorpageResource;

        Errorpage errorpage;

        @Before
        public void setUp() throws EntityNotFoundException, ObjectNotFoundException {
            MockitoAnnotations.initMocks(this);
            errorpageResource = new ErrorpageResource();
            errorpage = new Errorpage();
            errorpageResource.setLoadBalancerService(loadBalancerService);
            when(loadBalancerService.getDefaultErrorPage()).thenReturn("testErrorPage");

        }

        @Test
        public void shouldReturn200WhenRetrievingErrorpage() throws Exception {

            Response response = errorpageResource.retrieveErrorpage();
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(Errorpage.class, response.getEntity().getClass());
        }

        @Test
        public void shouldReturnA404WhenEntityNotFoundIsThrown() throws Exception{
            doThrow(EntityNotFoundException.class).when(loadBalancerService).getDefaultErrorPage();
            Response response = errorpageResource.retrieveErrorpage();
            Assert.assertEquals(404, response.getStatus());
        }

    }
    public static class whenSettingDefaultErrorPage {



            @Mock
            LoadBalancerService loadBalancerService;
        @Mock
            ManagementAsyncService managementAsyncService;

            ErrorpageResource errorpageResource;

            Errorpage errorpage;

            @Before
            public void setUp() {

                MockitoAnnotations.initMocks(this);

                errorpageResource = new ErrorpageResource();
                errorpage = new Errorpage();
                errorpageResource.setLoadBalancerService(loadBalancerService);
                errorpageResource.setManagementAsyncService(managementAsyncService);
                errorpage.setContent("testContent");

            }

            @Test
            public void shouldReturn202WhenSettingErrorPage() throws Exception {

                Response response = errorpageResource.setDefaultErrorPage(errorpage);
                Assert.assertEquals(202, response.getStatus());

            }

        @Test
            public void shouldReturn400WhenSettingErrorPageWithEmptyContent(){
                errorpage.setContent(null);
                Response response = errorpageResource.setDefaultErrorPage(errorpage);
                Assert.assertEquals(400, response.getStatus());

            }

            @Test
            public void shouldReturn400WhenErrorPageIsOverSizeLimit() throws Exception {
                StringBuilder sbContent = new StringBuilder();
                sbContent.setLength(1024*64+1);
                String content = new String(sbContent);
                errorpage.setContent(content);
                Response response = errorpageResource.setDefaultErrorPage(errorpage);
                Assert.assertEquals(400, response.getStatus());
            }


    }



}
