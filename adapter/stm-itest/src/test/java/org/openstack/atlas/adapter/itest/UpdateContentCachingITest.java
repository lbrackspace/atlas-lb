package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

public class UpdateContentCachingITest extends STMTestBase {

    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
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
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        lb.setContentCaching(Boolean.TRUE);
        stmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }

    @Test
    public void defaultContentCaching() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        stmAdapter.updateLoadBalancer(config, lb, lb);

        Assert.assertFalse(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }

    @Test
    public void updateContentCaching() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        lb.setContentCaching(Boolean.TRUE);
        stmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());

        lb.setContentCaching(Boolean.FALSE);
        stmAdapter.updateLoadBalancer(config, lb, lb);
        Assert.assertFalse(client.getVirtualServer(vsName).getProperties().getWebCache().getEnabled());
    }
}
