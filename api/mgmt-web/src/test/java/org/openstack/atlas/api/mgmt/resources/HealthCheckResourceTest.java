package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.operations.OperationResponse;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(Enclosed.class)
public class HealthCheckResourceTest {

    public static class whenCheckingHealth {
        private HealthCheckResource resource;
        private OperationResponse response;
        private Configuration configuration;

        @Before
        public void setUp() {
            resource = new HealthCheckResource();
            resource.setMockitoAuth(true);
            response = new OperationResponse();
            response.setExecutedOkay(true);
            configuration = mock(Configuration.class);
            resource.setConfiguration(configuration);
        }

        @Test
        public void shouldAlwaysReturn200() throws Exception {
            when(configuration.getString(Matchers.<PublicApiServiceConfigurationKeys>any())).thenReturn("test");
            Response resp = resource.getHealthCheck();
            Assert.assertEquals(200, resp.getStatus());
        }
    }
}