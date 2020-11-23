package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.adapter.helpers.VTMNameBuilder;
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
            VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = VTMNameBuilder.genVSName(lb);

        lb.setContentCaching(Boolean.TRUE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }

    @Test
    public void defaultContentCaching() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = VTMNameBuilder.genVSName(lb);

        vtmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertFalse(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }

    @Test
    public void updateContentCaching() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, VTMRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = VTMNameBuilder.genVSName(lb);

        lb.setContentCaching(Boolean.TRUE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());

        lb.setContentCaching(Boolean.FALSE);
        vtmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertFalse(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }
}
