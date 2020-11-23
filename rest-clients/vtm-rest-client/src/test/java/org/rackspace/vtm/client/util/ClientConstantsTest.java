package org.rackspace.vtm.client.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rackspace.vtm.client.counters.VirtualServerStatsProperties;
import org.rackspace.vtm.client.counters.VirtualServerStatsStatistics;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.manager.VTMRequestManager;
import org.rackspace.vtm.client.manager.impl.VTMRequestManagerImpl;
import org.rackspace.vtm.client.pool.Pool;
import org.rackspace.vtm.client.pool.PoolHttp;
import org.rackspace.vtm.client.pool.PoolProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;

@RunWith(Enclosed.class)
public class ClientConstantsTest {

    public static class WhenRetrievingConstants {

        @Before
        public void standUp() throws URISyntaxException, IOException {

        }


        @Test
        public void shouldReturnConstantValue() {
            // Verify constant class functions as expected
            Assert.assertEquals(200, ClientConstants.ACCEPTED);
        }

        @Test
        public void shouldReturnConstantVirtualServerPath() {
            Assert.assertEquals("virtual_servers/", ClientConstants.V_SERVER_PATH);
        }

    }

}