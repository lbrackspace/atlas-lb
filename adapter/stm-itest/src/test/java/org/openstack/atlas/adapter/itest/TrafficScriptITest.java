package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.adapter.helpers.TrafficScriptHelper;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.io.File;
import java.io.IOException;

public class TrafficScriptITest extends STMTestBase {


    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
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
    public void testAddRateLimitScript() throws StingrayRestClientObjectNotFoundException, StingrayRestClientException, IOException {
        StingrayRestClient client = new StingrayRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(StmConstants.RATE_LIMIT_HTTP);       //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.RATE_LIMIT_HTTP);  //Or this will fail, if the above line succeeds
        } catch (StingrayRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                                      //So we'll get to here one way or another
        }

        try {
            client.deleteTrafficscript(StmConstants.RATE_LIMIT_NON_HTTP);      //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.RATE_LIMIT_NON_HTTP); //Or this will fail, if the above line succeeds
        } catch (StingrayRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                                         //So we'll get to here one way or another
        }

        TrafficScriptHelper.addRateLimitScriptsIfNeeded(client);
        script = client.getTraffiscript(StmConstants.RATE_LIMIT_HTTP);
        Assert.assertNotNull(script);
        script = client.getTraffiscript(StmConstants.RATE_LIMIT_NON_HTTP);
        Assert.assertNotNull(script);
    }

    @Test
    public void testAddXForwardedForScript() throws StingrayRestClientObjectNotFoundException, StingrayRestClientException, IOException {
        StingrayRestClient client = new StingrayRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(StmConstants.XFF);           //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.XFF);      //Or this will fail, if the above line succeeds
        } catch (StingrayRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                              //So we'll get to here one way or another
        }

        TrafficScriptHelper.addXForwardedForScriptIfNeeded(client);
        script = client.getTraffiscript(StmConstants.XFF);
        Assert.assertNotNull(script);
    }

    @Test
    public void testAddXForwardedProtoScript() throws StingrayRestClientObjectNotFoundException, StingrayRestClientException, IOException {
        StingrayRestClient client = new StingrayRestClient();

        File script = null;
        try {
            client.deleteTrafficscript(StmConstants.XFP);           //Either this will fail, meaning there is no script already
            script = client.getTraffiscript(StmConstants.XFP);      //Or this will fail, if the above line succeeds
        } catch (StingrayRestClientObjectNotFoundException objectNotFoundException) {
            Assert.assertNull(script);                              //So we'll get to here one way or another
        }

        TrafficScriptHelper.addXForwardedProtoScriptIfNeeded(client);
        script = client.getTraffiscript(StmConstants.XFP);
        Assert.assertNotNull(script);
    }

}
