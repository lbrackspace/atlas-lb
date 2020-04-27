package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

import java.rmi.RemoteException;

public class GlobalCountersITest extends VTMTestBase {


    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    // These tests will only ever return 0 bytes in dev based env. Verify we can make the call and the mapped value is not null
    @Test
    public void getGlobalCountersHostBytesIn() throws VTMRestClientObjectNotFoundException, VTMRestClientException, RollBackException, RemoteException {
        Long s = vtmAdapter.getHostBytesIn(config);
        Assert.assertNotNull(s);
    }

    @Test
    public void getGlobalCountersHostBytesOut() throws VTMRestClientObjectNotFoundException, VTMRestClientException, RollBackException, RemoteException {
        Long s = vtmAdapter.getHostBytesOut(config);
        Assert.assertNotNull(s);
    }

    @Test
    public void getGlobalCountersTotalCurrentConnections() throws VTMRestClientObjectNotFoundException, VTMRestClientException, RollBackException, RemoteException {
        int s = vtmAdapter.getTotalCurrentConnectionsForHost(config);
        Assert.assertNotNull(s);
    }
}
