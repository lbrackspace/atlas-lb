package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.helpers.VTMConstants;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.io.File;
import java.io.IOException;

public class TrafficScriptITest extends VTMTestBase {


    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    @Test
    public void testAddXForwardedForScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException, IOException {
        VTMRestClient client = new VTMRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(VTMConstants.XFF);           //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(VTMConstants.XFF);      //Or this will fail, if the above line succeeds
        } catch (VTMRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                              //So we'll get to here one way or another
        }

        TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
        script = client.getTraffiscript(VTMConstants.XFF);
        Assert.assertNotNull(script);
    }

    @Test
    public void testAddXForwardedProtoScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException, IOException {
        VTMRestClient client = new VTMRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(VTMConstants.XFP);           //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(VTMConstants.XFP);      //Or this will fail, if the above line succeeds
        } catch (VTMRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                              //So we'll get to here one way or another
        }

        TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
        script = client.getTraffiscript(VTMConstants.XFP);
        Assert.assertNotNull(script);
    }

}
