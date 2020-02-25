package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.protection.Protection;
import org.rackspace.vtm.client.protection.ProtectionConnectionRate;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ReassignHostITest extends VTMTestBase {

    private String vsName;

    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void standUp() throws InterruptedException, InsufficientRequestException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();

        createSimpleLoadBalancer();

        vsName = ZxtmNameBuilder.genVSName(lb);
    }

    @After
    public void resetAfter() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDown() {
        teardownEverything();
    }


    @Test
    public void verifyHostDefaults() throws Exception {
        TrafficIp tg = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip1));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().isEmpty());
        // Master and slave are same machine for dev env
        Assert.assertTrue(tg.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertTrue(tg.getProperties().getBasic().getSlaves().contains("172.24.1.2"));
    }

    @Test
    public void verifySimpleChangeHostSuccess() throws Exception {
        TrafficIp tg = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip1));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().isEmpty());
        // Master and slave are same machine for dev env
        Assert.assertTrue(tg.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertTrue(tg.getProperties().getBasic().getSlaves().contains("172.24.1.2"));
        ArrayList<LoadBalancer> lbs = new ArrayList<>();
        lbs.add(lb);
        vtmAdapter.changeHostForLoadBalancers(config, updateEndpointConfiguration(), lbs,1);
        tg = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip1));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().isEmpty());
        Assert.assertTrue(tg.getProperties().getBasic().getMachines().contains("172.24.1.1"));
        Assert.assertTrue(tg.getProperties().getBasic().getSlaves().contains("172.24.1.1"));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertFalse(tg.getProperties().getBasic().getSlaves().contains("172.24.1.2"));
    }

    @Test
    public void verifyMultipleSimpleChangeHostSuccess() throws Exception {
        LoadBalancer lb2 = lb;
        lb2.setId(455);
        lb2.setAccountId(7777);
        VirtualIp vip2 = vip1;
        vip2.setId(12);
        lb2.getLoadBalancerJoinVipSet().clear();
        LoadBalancerJoinVip lbjv = new LoadBalancerJoinVip();
        lbjv.setLoadBalancer(lb2);
        lbjv.setPort(30);
        lbjv.setVirtualIp(vip2);
        lb2.getLoadBalancerJoinVipSet().add(lbjv);
        ArrayList<LoadBalancer> lbs = new ArrayList<>();
        lbs.add(lb);
        lbs.add(lb2);

        vtmAdapter.updateLoadBalancer(config, lb2, lb2);

        TrafficIp tg = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip1));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().isEmpty());
        TrafficIp tg2 = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb2, vip2));

        // Master and slave are same machine for dev env
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().isEmpty());
        Assert.assertFalse(tg2.getProperties().getBasic().getMachines().isEmpty());
        Assert.assertTrue(tg.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertTrue(tg.getProperties().getBasic().getSlaves().contains("172.24.1.2"));
        Assert.assertTrue(tg2.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertTrue(tg2.getProperties().getBasic().getSlaves().contains("172.24.1.2"));

        vtmAdapter.changeHostForLoadBalancers(config, updateEndpointConfiguration(), lbs,1);
        tg = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb, vip1));
        tg2 = vtmClient.getTrafficIp(ZxtmNameBuilder.generateTrafficIpGroupName(lb2, vip2));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().isEmpty());
        Assert.assertTrue(tg.getProperties().getBasic().getMachines().contains("172.24.1.1"));
        Assert.assertTrue(tg.getProperties().getBasic().getSlaves().contains("172.24.1.1"));
        Assert.assertFalse(tg.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertFalse(tg.getProperties().getBasic().getSlaves().contains("172.24.1.2"));

        Assert.assertFalse(tg2.getProperties().getBasic().getMachines().isEmpty());
        Assert.assertTrue(tg2.getProperties().getBasic().getMachines().contains("172.24.1.1"));
        Assert.assertTrue(tg2.getProperties().getBasic().getSlaves().contains("172.24.1.1"));
        Assert.assertFalse(tg2.getProperties().getBasic().getMachines().contains("172.24.1.2"));
        Assert.assertFalse(tg2.getProperties().getBasic().getSlaves().contains("172.24.1.2"));

        vtmAdapter.deleteLoadBalancer(config, lb2);
    }

    private LoadBalancerEndpointConfiguration updateEndpointConfiguration() throws MalformedURLException, DecryptException {
        List<String> targetFailoverHosts = new ArrayList<String>();
        targetFailoverHosts.add("172.24.1.1");
        Host soapEndpointHost = new Host();
        soapEndpointHost.setEndpoint(VTM_ENDPOINT_URI);
        soapEndpointHost.setRestEndpoint(VTM_ENDPOINT_URI);
        Host trafficManagerHost = new Host();
        trafficManagerHost.setEndpoint(VTM_ENDPOINT_URI);
        trafficManagerHost.setRestEndpoint(VTM_ENDPOINT_URI);
        trafficManagerHost.setTrafficManagerName("172.24.1.1");
        List<Host> failoverHosts = new ArrayList<Host>();
        failoverHosts.add(soapEndpointHost);
        LoadBalancerEndpointConfiguration conf = new LoadBalancerEndpointConfiguration(soapEndpointHost, VTM_USERNAME, CryptoUtil.decrypt(VTM_PASSWORD), trafficManagerHost, targetFailoverHosts, failoverHosts);
        conf.setLogFileLocation(DEFAULT_LOG_FILE_LOCATION);
        return conf;
    }
}
