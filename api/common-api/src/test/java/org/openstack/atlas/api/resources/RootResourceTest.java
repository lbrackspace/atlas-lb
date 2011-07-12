package org.openstack.atlas.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import javax.ws.rs.core.HttpHeaders;

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
            rootResource.setLoadBalancersResource(mockedLoadBalancersResource);
            rootResource.retrieveLoadBalancersResource();
            verify(mockedLoadBalancersResource).setRequestHeaders(Matchers.<HttpHeaders>anyObject());
            verify(mockedLoadBalancersResource).setAccountId(anyInt());
        }
    }
}
