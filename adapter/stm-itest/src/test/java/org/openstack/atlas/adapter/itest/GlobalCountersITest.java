package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.rmi.RemoteException;

public class GlobalCountersITest extends STMTestBase {


    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
//        createSimpleLoadBalancer();
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
    public void getHostBytesIn() throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException, RollBackException, RemoteException {
        Long s = stmAdapter.getHostBytesIn(config);
        Assert.assertNotNull(s);
    }

    @Test
    public void getGlobalCountersHostBytesOut() throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException, RollBackException, RemoteException {
        Long s = stmAdapter.getHostBytesOut(config);
        Assert.assertNotNull(s);
    }

    @Test
    public void getGlobalCountersTotalCurrentConnections() throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException, RollBackException, RemoteException {
        int s = stmAdapter.getTotalCurrentConnectionsForHost(config);
        Assert.assertNotNull(s);
    }
}
