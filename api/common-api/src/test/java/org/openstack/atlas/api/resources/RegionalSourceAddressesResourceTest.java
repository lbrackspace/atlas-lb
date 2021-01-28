package org.openstack.atlas.api.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.RegionalSourceAddresses;
import org.openstack.atlas.service.domain.entities.ClusterType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.ClusterService;
import org.openstack.atlas.service.domain.services.HostService;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class RegionalSourceAddressesResourceTest {

    public static class WhenRetrievingResources {
        @Mock
        private ClusterService clusterService;
        @Mock
        private HostService hostService;
        private RegionalSourceAddressesResource regionalSourceAddressesResource;
        private RegionalSourceAddresses regionalSourceAddresses;
        private Response response;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            regionalSourceAddressesResource = new RegionalSourceAddressesResource();
            regionalSourceAddressesResource.setAccountId(123);
            regionalSourceAddressesResource.setClusterService(clusterService);
            regionalSourceAddressesResource.setHostService(hostService);
            regionalSourceAddresses = new RegionalSourceAddresses();

        }

        @Test
        public void shouldReturnRegionalSourceAddressesWith200() {
            when(clusterService.getClusterTypeByAccountId(ArgumentMatchers.anyInt())).thenReturn(ClusterType.STANDARD);
            doReturn(regionalSourceAddresses).when(hostService).getRegionalSourceAddresses(ArgumentMatchers.eq(ClusterType.STANDARD));
            response = regionalSourceAddressesResource.retrieveRegionalSourceAddresses();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowEntityNotFoundExceptionWithStatusCode404() {
            when(clusterService.getClusterTypeByAccountId(ArgumentMatchers.anyInt())).thenReturn(null);
            doThrow(EntityNotFoundException.class).when(hostService).getRegionalSourceAddresses(ArgumentMatchers.eq(null));
            response = regionalSourceAddressesResource.retrieveRegionalSourceAddresses();
            Assert.assertEquals(404, response.getStatus());
        }

    }
}
