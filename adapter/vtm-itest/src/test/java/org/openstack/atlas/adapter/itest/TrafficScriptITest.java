package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.openstack.atlas.adapter.vtm.VTMConstants;
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
    public void testAddRateLimitScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException, IOException {
        VTMRestClient client = new VTMRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(StmConstants.RATE_LIMIT_HTTP);       //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.RATE_LIMIT_HTTP);  //Or this will fail, if the above line succeeds
        } catch (VTMRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                                      //So we'll get to here one way or another
        }

        try {
            client.deleteTrafficscript(StmConstants.RATE_LIMIT_NON_HTTP);      //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.RATE_LIMIT_NON_HTTP); //Or this will fail, if the above line succeeds
        } catch (VTMRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                                         //So we'll get to here one way or another
        }

        TrafficScriptHelper.addRateLimitScriptsIfNeeded(client);
        script = client.getTraffiscript(StmConstants.RATE_LIMIT_HTTP);
        Assert.assertNotNull(script);
        script = client.getTraffiscript(StmConstants.RATE_LIMIT_NON_HTTP);
        Assert.assertNotNull(script);
    }

    @Test
    public void testAddXForwardedForScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException, IOException {
        VTMRestClient client = new VTMRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(StmConstants.XFF);           //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.XFF);      //Or this will fail, if the above line succeeds
        } catch (VTMRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                              //So we'll get to here one way or another
        }

        TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
        script = client.getTraffiscript(StmConstants.XFF);
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
        script = client.getTraffiscript(StmConstants.XFP);
        Assert.assertNotNull(script);
    }

}
