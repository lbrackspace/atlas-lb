package org.rackspace.stingray.client.manager.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.rackspace.stingray.client.util.ClientConstants;

import java.io.IOException;
import java.net.URISyntaxException;


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