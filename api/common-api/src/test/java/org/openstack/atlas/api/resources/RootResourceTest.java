package org.openstack.atlas.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
            verify(mockedRequestStateContainer).setHttpHeaders(ArgumentMatchers.<HttpHeaders>any());
            verify(mockedRequestStateContainer).setSecurityContext(ArgumentMatchers.<SecurityContext>any());
            verify(mockedRequestStateContainer).setUriInfo(ArgumentMatchers.<UriInfo>any());
            verify(mockedLoadBalancersResource).setRequestHeaders(ArgumentMatchers.<HttpHeaders>any());
            verify(mockedLoadBalancersResource).setAccountId(ArgumentMatchers.<Integer>any());
        }

        @Test
        public void shouldSetAccountIdForRegionalSourceAddressesResource() {
            RegionalSourceAddressesResource mockedRegionalSourceAddresses = mock(RegionalSourceAddressesResource.class);
            rootResource.setRegionalSourceAddressesResource(mockedRegionalSourceAddresses);
            rootResource.retrieveRegionalSourceAddressesResource();
            verify(mockedRegionalSourceAddresses).setAccountId(ArgumentMatchers.<Integer>any());
        }
    }
}
