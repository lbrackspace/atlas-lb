package org.openstack.atlas.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class RootResourceTest {
    public static class WhenRetrievingResources {
        private RootResource rootResource;

        @Before
        public void setUp() {
            rootResource = new RootResource();
        }

        @Test
        public void shouldSetAccountIdForLoadBalancersResource() {
            LoadBalancersResource mockedLoadBalancersResource = mock(LoadBalancersResource.class);
            RequestStateContainer mockedRequestStateContainer = mock(RequestStateContainer.class);
            rootResource.setOrigContainer(mockedRequestStateContainer);
            rootResource.setLoadBalancersResource(mockedLoadBalancersResource);
            rootResource.retrieveLoadBalancersResource();
            verify(mockedRequestStateContainer).setHttpHeaders(Matchers.<HttpHeaders>any());
            verify(mockedRequestStateContainer).setSecurityContext(Matchers.<SecurityContext>any());
            verify(mockedRequestStateContainer).setUriInfo(Matchers.<UriInfo>any());
            verify(mockedLoadBalancersResource).setRequestHeaders(Matchers.<HttpHeaders>anyObject());
            verify(mockedLoadBalancersResource).setAccountId(anyInt());
        }
    }
}
