package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.StmRollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.virtualserver.VirtualServerBasic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateProtocolITest extends VTMTestBase {

    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
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
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTP.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());

        lb.setProtocol(LoadBalancerProtocol.HTTPS);
        vtmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTPS.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
    }

    //These tests need to run in order -- HTTP is the second test
    @Test
    public void bupdateProtocolToHTTP() throws InsufficientRequestException,
            VTMRestClientObjectNotFoundException, VTMRestClientException, StmRollBackException {
        VTMRestClient client = new VTMRestClient();
        String vsName = ZxtmNameBuilder.genVSName(lb);

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTPS.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());

        lb.setProtocol(LoadBalancerProtocol.HTTP);
        vtmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());

        Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(LoadBalancerProtocol.HTTP.name().toLowerCase()),
                client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
    }
}
