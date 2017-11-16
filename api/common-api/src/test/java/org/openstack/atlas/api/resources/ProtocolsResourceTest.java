package org.openstack.atlas.api.resources;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.ProtocolsService;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@Ignore
public class ProtocolsResourceTest {

    public static class WhenFetchingResources {

        @Mock
        LoadBalancerRepository loadBalancerRepository;
        @Mock
        AsyncService asyncService;
        @Mock
        ProtocolsService protocolsService;

        private List<LoadBalancerProtocolObject> result;
        private Response resp;

        @InjectMocks
        private ProtocolsResource resource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            resource = new ProtocolsResource();
            resource.setProtocolsService(protocolsService);
            resource.setAsyncService(asyncService);
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
