package org.openstack.atlas.api.mgmt.resources;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.operations.OperationResponse;

import javax.ws.rs.core.Response;
import java.io.File;

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
        }

        @Test
        public void shouldAlwaysReturn200() throws Exception {
            File file = new File("/etc/openstack/atlas/healthcheckWillBeDeleted.html");

            if (file.createNewFile()) {
                System.out.print("Don't care");
            }

            when(configuration.getString(PublicApiServiceConfigurationKeys.health_check)).thenReturn(file.getAbsolutePath());
            Response resp = resource.getHealthCheck();
            Assert.assertEquals(200, resp.getStatus());
            file.deleteOnExit();
        }
    }
}