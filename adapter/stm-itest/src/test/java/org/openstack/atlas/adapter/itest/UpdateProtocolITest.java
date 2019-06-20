package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateProtocolITest extends STMTestBase {

    @BeforeClass
    public static void clientInit() {
        stmClient = new StingrayRestClient();
        setupIvars();
        createSimpleLoadBalancer();
    }

    @Before
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    //TODO: verify logic regarding protcol switching. Primarily switching from HTTP to Non-HTTP protocols.
    //These tests need to run in order -- HTTPS is the first test
    @Test
    public void aupdateProtocolToHTTPS() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTP.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());

        lb.setProtocol(LoadBalancerProtocol.HTTPS);
        stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTPS.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
    }

    //These tests need to run in order -- HTTP is the second test
    @Test
    public void bupdateProtocolToHTTP() throws InsufficientRequestException,
            StingrayRestClientObjectNotFoundException, StingrayRestClientException, StmRollBackException {
        StingrayRestClient client = new StingrayRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTPS.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());

        lb.setProtocol(LoadBalancerProtocol.HTTP);
        stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTP.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
    }
}
