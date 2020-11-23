package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;


public class VirtualServerStatsITest extends VTMTestBase {


    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @After
    public void destroy() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }


    @Test
    public void getVirtualServerStats() throws InsufficientRequestException, VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException {
        //Needs to have traffic ran through it to return usage. CAFE verifies usage...
        Stats s = vtmAdapter.getVirtualServerStats(config, lb);
        Assert.assertNotNull(s);
    }
}
