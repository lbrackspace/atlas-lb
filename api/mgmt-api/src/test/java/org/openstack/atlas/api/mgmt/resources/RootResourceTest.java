package org.openstack.atlas.api.mgmt.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class RootResourceTest {

    public static class WhenRetrievingResources {

        @Mock
        private RequestStateContainer mockedRequestStateContainer;
        @Mock
        private org.openstack.atlas.api.resources.RootResource mockedPublicApiRootResource;
        private RootResource rootResource;


        @Before
        public void setUp() {
            rootResource = new RootResource();
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void shouldSetHttpHeadersSecurityContextUriInfo() {
           rootResource.setOrigContainer(mockedRequestStateContainer);
           rootResource.retrieveManagementResource();
           verify(mockedRequestStateContainer).setHttpHeaders(ArgumentMatchers.<HttpHeaders>any());
           verify(mockedRequestStateContainer).setSecurityContext(ArgumentMatchers.<SecurityContext>any());
           verify(mockedRequestStateContainer).setUriInfo(ArgumentMatchers.<UriInfo>any());
        }

        @Test
        public void shouldSetHttpHeadersSecurityContextUriInfoPublicApi() {
            rootResource.setOrigContainer(mockedRequestStateContainer);
            rootResource.setPublicApiResource(mockedPublicApiRootResource);
            rootResource.retrievePublicApiResource(12);
            verify(mockedPublicApiRootResource).setRequestHeaders(ArgumentMatchers.<HttpHeaders>any());
            verify(mockedPublicApiRootResource).setAccountId(ArgumentMatchers.anyInt());
            verify(mockedRequestStateContainer).setHttpHeaders(ArgumentMatchers.<HttpHeaders>any());
            verify(mockedRequestStateContainer).setSecurityContext(ArgumentMatchers.<SecurityContext>any());
            verify(mockedRequestStateContainer).setUriInfo(ArgumentMatchers.<UriInfo>any());
        }
    }
}
