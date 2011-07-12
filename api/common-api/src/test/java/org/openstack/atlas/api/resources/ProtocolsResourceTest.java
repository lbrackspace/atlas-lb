package org.openstack.atlas.api.resources;

import java.util.ArrayList;
import java.util.List;

import org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@Ignore
public class ProtocolsResourceTest {

    public static class WhenFetchingResources {

        private LoadBalancerRepository loadBalancerRepository;
        private AsyncService esbService;
        private ProtocolsResource resource;
        private List<LoadBalancerProtocolObject> result;
        private Response resp;

        @Before
        public void setUp() {
            loadBalancerRepository = mock(LoadBalancerRepository.class);
            esbService = mock(AsyncService.class);
            resource = new ProtocolsResource();
            resource.setAsyncService(esbService);
            resource.setLbRepository(loadBalancerRepository);
            result = new ArrayList<LoadBalancerProtocolObject>();
        }

        
        @Test
        public void shouldReturn200() {
            when(loadBalancerRepository.getAllProtocols()).thenReturn(result);
            resp = resource.retrieveLoadBalancingProtocols();
            Assert.assertEquals(200, resp.getStatus());
        }
    }
}
