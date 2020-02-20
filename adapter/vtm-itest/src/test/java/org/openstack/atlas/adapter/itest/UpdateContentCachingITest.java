package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;

public class UpdateContentCachingITest extends VTMTestBase {

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
    public void setContentCaching() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        lb.setContentCaching(Boolean.TRUE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }

    @Test
    public void defaultContentCaching() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertFalse(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }

    @Test
    public void updateContentCaching() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        lb.setContentCaching(Boolean.TRUE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());

        lb.setContentCaching(Boolean.FALSE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertFalse(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }
}
