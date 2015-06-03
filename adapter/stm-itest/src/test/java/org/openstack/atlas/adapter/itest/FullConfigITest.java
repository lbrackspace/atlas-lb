package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.ZeusNodePriorityContainer;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.bandwidth.BandwidthBasic;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.monitor.MonitorHttp;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolNodeWeight;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionAccessRestriction;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;

public class FullConfigITest extends STMTestBase {
    static StingrayRestClient client;

    Node n1;
    Node n2;
    Node n3;

    /**
     * Have to run in order, some tests depend on others for values in STM...
     */

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        client = stmClient;
    }

    @AfterClass
    public static void tearDownClass() {
        teardownEverything();
    }

    @Test
    public void createFullyConfiguredLoadBalancer() {
        try {
            stmAdapter.createLoadBalancer(config, buildHydratedLb());
            Thread.sleep(3000);
            verifyVS(lb);
            verifyPool(lb);
            verifyMonitor(lb);
            verifyProtection(lb);
            verifyVips(lb);
        } catch (Exception e) {
            e.printStackTrace();
            removeLoadBalancer();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateFullyConfiguredLoadBalancer() {
        try {
            buildHydratedLb();
            LoadBalancer nlb = new LoadBalancer();

            VirtualIp v = new VirtualIp();
            v.setIpAddress("10.3.5.6");
            v.setId(12);
            LoadBalancerJoinVip jv = new LoadBalancerJoinVip(80, lb, v);
            lb.getLoadBalancerJoinVipSet().add(jv);
            nlb.setLoadBalancerJoinVipSet(lb.getLoadBalancerJoinVipSet());

            UserPages up = new UserPages();
            up.setErrorpage("iError");
            nlb.setUserPages(up);
            lb.setUserPages(up);

            HealthMonitor mon = new HealthMonitor();
            mon.setType(HealthMonitorType.HTTP);
            mon.setStatusRegex("202");
            mon.setBodyRegex("");
            mon.setAttemptsBeforeDeactivation(4);
            mon.setDelay(3);
            mon.setPath("/");
            mon.setTimeout(2);
            mon.setHostHeader("");
            nlb.setHealthMonitor(mon);
            lb.setHealthMonitor(mon);

            nlb.setSessionPersistence(HTTP_COOKIE);
            lb.setSessionPersistence(HTTP_COOKIE);

            AccessList al = new AccessList();
            al.setIpAddress("10.1.1.76");
            al.setType(AccessListType.ALLOW);
            Set<AccessList> all = new HashSet<AccessList>();
            all.add(al);
            nlb.setAccessLists(all);
            lb.setAccessLists(all);

            lb.getConnectionLimit().setMaxConnectionRate(5);
            lb.getConnectionLimit().setMinConnections(3);
            lb.getConnectionLimit().setRateInterval(31);
            nlb.setConnectionLimit(lb.getConnectionLimit());

            nlb.setHalfClosed(false);
            lb.setHalfClosed(false);

            nlb.setTimeout(35);
            lb.setTimeout(35);

            lb.setConnectionLogging(true);
            lb.setContentCaching(true);

            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(StmTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(StmTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(StmTestConstants.SSL_KEY);

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(StmTestConstants.SSL_CERT);
            zeusCertFile.setPrivate_key(StmTestConstants.SSL_KEY);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(StmTestConstants.SSL_CERT);
            zeusSslTermination.setSslTermination(sslTermination);

            stmAdapter.updateLoadBalancer(config, lb, nlb);

            lb.setSslTermination(zeusSslTermination.getSslTermination());
            stmAdapter.updateSslTermination(config, lb, zeusSslTermination);
            verifySsltermination(lb);

            Thread.sleep(3000);
            verifyVS(lb);
            verifyPool(lb);
            verifyMonitor(lb);
            verifyProtection(lb);
            verifyVips(lb);

        } catch (Exception e) {
            e.printStackTrace();
            removeLoadBalancer();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateFullyConfiguredLoadBalancerWithHealthMonitorRollbacks() throws StingrayRestClientException, IOException, StingrayRestClientObjectNotFoundException, InsufficientRequestException, IPStringConversionException {
        LoadBalancer clb = null;
        try {
            LoadBalancer nlb = new LoadBalancer();
            clb = new LoadBalancer();
            clb.setHealthMonitor(lb.getHealthMonitor());

            HealthMonitor mon = new HealthMonitor();
            mon.setType(HealthMonitorType.HTTP);
            mon.setStatusRegex("207");
            mon.setBodyRegex("[0-9]");
            mon.setAttemptsBeforeDeactivation(1);
            mon.setDelay(6);
            mon.setPath("circle");
            mon.setTimeout(3);
            mon.setHostHeader("header");
            nlb.setHealthMonitor(mon);

            buildHydratedLb();
            lb.setSslTermination(null);
            lb.setHealthMonitor(mon);

            stmAdapter.updateLoadBalancer(config, lb, nlb);
            Assert.fail("Should have failed to update");

        } catch (Exception e) {
            verifyMonitor(clb);
        }
    }

    @Test
    public void updateFullyConfiguredLoadBalancerWithProtectionrollbacks() throws StingrayRestClientException, IOException, StingrayRestClientObjectNotFoundException, InsufficientRequestException, IPStringConversionException {
        LoadBalancer clb = null;
        try {
            LoadBalancer nlb = new LoadBalancer();
            clb = new LoadBalancer();
            clb.setConnectionLimit(lb.getConnectionLimit());

            buildHydratedLb();
            stmAdapter.updateLoadBalancer(config, lb, lb);

            lb.getConnectionLimit().setRateInterval(-2);
            nlb.setConnectionLimit(lb.getConnectionLimit());

            stmAdapter.updateLoadBalancer(config, lb, nlb);
            Assert.fail("Should have failed to update");

        } catch (Exception e) {
            verifyProtection(clb);
        }
    }

    @Test
    public void updateFullyConfiguredSslLoadBalancerWithSslRollbacks() throws StingrayRestClientException, IPStringConversionException, StingrayRestClientObjectNotFoundException, InsufficientRequestException {
        LoadBalancer clb = null;
        try {
            LoadBalancer nlb = new LoadBalancer();
            clb = new LoadBalancer();

            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(StmTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(StmTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(StmTestConstants.SSL_KEY);
            lb.setSslTermination(sslTermination);
            clb.setSslTermination(sslTermination);
            clb.setProtocol(lb.getProtocol());

            buildHydratedLb();
            stmAdapter.updateLoadBalancer(config, lb, lb);

            lb.getSslTermination().setSecurePort(-10);
            nlb.setSslTermination(lb.getSslTermination());

            stmAdapter.updateLoadBalancer(config, lb, nlb);
            Assert.fail("Should have failed to update");
        } catch (Exception e) {
            verifySsltermination(clb);
        }
    }

    @Test
    public void updateFullyConfiguredLoadBalancerWithVirtualIpRollbacks() throws StingrayRestClientException, IPStringConversionException, StingrayRestClientObjectNotFoundException, InsufficientRequestException {
        LoadBalancer clb = null;
        try {
            LoadBalancer nlb = new LoadBalancer();
            clb = new LoadBalancer();

            buildHydratedLb();
            stmAdapter.updateLoadBalancer(config, lb, lb);

            clb.getLoadBalancerJoinVipSet().addAll(lb.getLoadBalancerJoinVipSet());

            lb.getLoadBalancerJoinVipSet().iterator().next().setVirtualIp(new VirtualIp());
            nlb.setLoadBalancerJoinVipSet(lb.getLoadBalancerJoinVipSet());


            stmAdapter.updateLoadBalancer(config, lb, nlb);
            Assert.fail("Should have failed to update");
        } catch (Exception e) {
            verifyVips(clb);
        }
    }

    @Test
    public void updateFullyConfiguredLoadBalancerForRateLimitRollbacks() throws StingrayRestClientException, IPStringConversionException, StingrayRestClientObjectNotFoundException, InsufficientRequestException {
        try {
            buildHydratedLb();
            stmAdapter.updateLoadBalancer(config, lb, lb);
            stmAdapter.updateRateLimit(config, lb, null);
            Assert.fail("Should have failed to update");
        } catch (Exception e) {
            verifyRateLimit(lb);
        }
    }

    @Test
    public void updateFullyConfiguredLoadBalancerForErrorPageRollbacks() {
        try {
            buildHydratedLb();
            stmAdapter.updateLoadBalancer(config, lb, lb);
            stmAdapter.setErrorFile(config, lb, "");
            Assert.fail("Should have failed to update");
        } catch (Exception e) {
            // This check is bad if there are other entries allowed in the user pages. Coded with Error Page only
            Assert.assertNull(lb.getUserPages());
        }
    }

    private LoadBalancer buildHydratedLb() {
        lb = new LoadBalancer();
        lb.setAccountId(TEST_ACCOUNT_ID);
        lb.setId(TEST_LOADBALANCER_ID);
        lb.setPort(LB_PORT);
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        lb.setSessionPersistence(HTTP_COOKIE);
        lb.setTimeout(99);
        lb.setConnectionLogging(true);
        lb.setContentCaching(false);
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        lb.setHalfClosed(true);
        lb.setProtocol(LoadBalancerProtocol.HTTP);

        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(HealthMonitorType.CONNECT);
        monitor.setDelay(10);
        monitor.setTimeout(20);
        monitor.setAttemptsBeforeDeactivation(3);
        lb.setHealthMonitor(monitor);

        ConnectionLimit limit = new ConnectionLimit();
        limit.setMaxConnections(50);
        limit.setRateInterval(10);
        limit.setMaxConnectionRate(10);
        limit.setMinConnections(1);
        lb.setConnectionLimit(limit);

        Set<AccessList> networkItems = new HashSet<AccessList>();
        AccessList item1 = new AccessList();
        AccessList item2 = new AccessList();
        item1.setIpAddress("0.0.0.0/0");
        item2.setIpAddress("127.0.0.1");
        item1.setType(DENY);
        item2.setType(ALLOW);
        networkItems.add(item1);
        networkItems.add(item2);
        lb.setAccessLists(networkItems);

        Set<Node> nodes = new HashSet<Node>();
        n1 = new Node();
        n1.setWeight(1);
        n1.setType(NodeType.SECONDARY);
        n1.setIpAddress("10.3.3.3");
        n1.setCondition(NodeCondition.ENABLED);
        n1.setPort(8080);
        n1.setStatus(NodeStatus.ONLINE);
        nodes.add(n1);

        n2 = new Node();
        n2.setWeight(3);
        n2.setType(NodeType.SECONDARY);
        n2.setIpAddress("10.3.3.5");
        n2.setCondition(NodeCondition.DISABLED);
        n2.setPort(7070);
        n2.setStatus(NodeStatus.OFFLINE);
        nodes.add(n2);

        n3 = new Node();
        n3.setWeight(3);
        n3.setType(NodeType.SECONDARY);
        n3.setIpAddress("10.3.3.9");
        n3.setCondition(NodeCondition.DRAINING);
        n3.setPort(9090);
        n3.setStatus(NodeStatus.OFFLINE);
        nodes.add(n3);

        lb.setNodes(nodes);

        return lb;
    }

    private VirtualServer verifyVS(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException, IOException {
        VirtualServer vs = client.getVirtualServer(loadBalancerName());
        ResourceTranslator translator = new ResourceTranslator();

        Assert.assertNotNull(vs);
        Assert.assertEquals(true, vs.getProperties().getBasic().getEnabled());
        Assert.assertEquals(lb.getPort(), vs.getProperties().getBasic().getPort());
        Assert.assertEquals(poolName(), vs.getProperties().getBasic().getPool());
        Assert.assertEquals(lb.isHalfClosed(), vs.getProperties().getTcp().getProxy_close());
        Assert.assertEquals(lb.isContentCaching(), vs.getProperties().getWeb_cache().getEnabled());

        if (lb.getUserPages() != null) {
            Assert.assertEquals(errorFileName(), vs.getProperties().getConnection_errors().getError_file());
            File ef = client.getExtraFile(errorFileName());
            String content = readFile(ef);
            Assert.assertEquals(lb.getUserPages().getErrorpage(), content);
            ef.delete();
        } else {
            Assert.assertEquals("Default", vs.getProperties().getConnection_errors().getError_file());
        }

        if (lb.getProtocol() == LoadBalancerProtocol.HTTP) {
            Assert.assertTrue(vs.getProperties().getBasic().getAdd_x_forwarded_for());
            Assert.assertTrue(vs.getProperties().getBasic().getAdd_x_forwarded_proto());
        } else {
            Assert.assertFalse(vs.getProperties().getBasic().getAdd_x_forwarded_for());
            Assert.assertFalse(vs.getProperties().getBasic().getAdd_x_forwarded_proto());
        }

        Assert.assertEquals(false, vs.getProperties().getBasic().getListen_on_any());
        Assert.assertEquals(vs.getProperties().getBasic().getListen_on_traffic_ips(), translator.genGroupNameSet(lb));

        Assert.assertEquals(protectionClassName(), vs.getProperties().getBasic().getProtection_class());


        return vs;
    }

    private Pool verifyPool(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Pool pool = client.getPool(loadBalancerName());
        Assert.assertNotNull(pool);
        Assert.assertEquals(1, pool.getProperties().getBasic().getMonitors().size());
        Assert.assertEquals(lb.getAlgorithm().name().toLowerCase(), pool.getProperties().getLoad_balancing().getAlgorithm());
        Assert.assertEquals(2, pool.getProperties().getBasic().getNodes().size()); // List contains ACTIVE and DRAINING nodes
        Assert.assertEquals(1, pool.getProperties().getBasic().getDisabled().size()); // List contains DISABLED nodes
        Assert.assertEquals(1, pool.getProperties().getBasic().getDraining().size()); // List contains DRAINING nodes

        List<PoolNodeWeight> pws = pool.getProperties().getLoad_balancing().getNode_weighting();
        for (PoolNodeWeight pnw : pws) {
            Node lbn = lb.getNodes().iterator().next();
            if (lbn.getIpAddress().equals(pnw.getNode().split(":")[0])) {
                Assert.assertEquals(lbn.getIpAddress() + ":" + lbn.getPort(), pnw.getNode());
                Assert.assertEquals(lbn.getWeight(), pnw.getWeight());
            }
        }

        ZeusNodePriorityContainer znpc = new ZeusNodePriorityContainer(lb.getNodes());
        Assert.assertEquals(znpc.getPriorityValuesSet(), pool.getProperties().getLoad_balancing().getPriority_values());
        Assert.assertEquals(znpc.hasSecondary(), pool.getProperties().getLoad_balancing().getPriority_enabled());

        Assert.assertEquals(lb.getTimeout(), pool.getProperties().getConnection().getMax_reply_time());

        Assert.assertEquals(new HashSet<String>(Arrays.asList(loadBalancerName())), pool.getProperties().getBasic().getMonitors());

        Assert.assertEquals(lb.getSessionPersistence().name(), pool.getProperties().getBasic().getPersistence_class());
        Assert.assertEquals("", pool.getProperties().getBasic().getBandwidth_class());

        return pool;
    }

    private Monitor verifyMonitor(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Monitor monitor = client.getMonitor(loadBalancerName());
        Assert.assertEquals(lb.getHealthMonitor().getType().name(), monitor.getProperties().getBasic().getType().toUpperCase());
        Assert.assertEquals(lb.getHealthMonitor().getTimeout(), monitor.getProperties().getBasic().getTimeout());
        Assert.assertEquals(lb.getHealthMonitor().getAttemptsBeforeDeactivation(), monitor.getProperties().getBasic().getFailures());

        if (lb.getHealthMonitor().getType().equals(HealthMonitorType.HTTP) || lb.getHealthMonitor().equals(HealthMonitorType.HTTPS)) {
            MonitorHttp http = monitor.getProperties().getHttp();
            HealthMonitor hm = lb.getHealthMonitor();
            Assert.assertEquals(hm.getPath(), http.getPath());
            Assert.assertEquals(hm.getStatusRegex(), http.getStatus_regex());
            Assert.assertEquals(hm.getBodyRegex(), http.getBody_regex());
            Assert.assertEquals(hm.getHostHeader(), http.getHost_header());
        }

        return monitor;
    }

    private Protection verifyProtection(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Protection protection = client.getProtection(loadBalancerName());
        if (lb.getAccessLists() != null && !lb.getAccessLists().isEmpty()) {
            ProtectionAccessRestriction pal = protection.getProperties().getAccess_restriction();
            for (AccessList al : lb.getAccessLists()) {
                if (al.getType().equals(AccessListType.ALLOW)) {
                    Assert.assertTrue(pal.getAllowed().contains(al.getIpAddress()));
                } else {
                    Assert.assertTrue(pal.getBanned().contains(al.getIpAddress()));
                }
            }
        }

        if (lb.getConnectionLimit() != null) {
            ProtectionConnectionLimiting cl = protection.getProperties().getConnection_limiting();
            Assert.assertEquals(lb.getConnectionLimit().getMinConnections(), cl.getMin_connections());
            Assert.assertEquals(lb.getConnectionLimit().getMaxConnectionRate(), cl.getMax_connection_rate());
            Assert.assertEquals(lb.getConnectionLimit().getMaxConnections(), cl.getMax_1_connections());
            Assert.assertEquals(lb.getConnectionLimit().getRateInterval(), cl.getRate_timer());
        }

        return protection;
    }

    private void verifyVips(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException, IPStringConversionException {
        TrafficIp t;
        for (LoadBalancerJoinVip jv : lb.getLoadBalancerJoinVipSet()) {
            t = client.getTrafficIp(trafficIpGroupName(jv.getVirtualIp()));
            Assert.assertTrue(t.getProperties().getBasic().getIpaddresses().contains(jv.getVirtualIp().getIpAddress()));
            Assert.assertEquals(true, t.getProperties().getBasic().getEnabled());

            Assert.assertTrue(t.getProperties().getBasic().getMachines().contains(config.getFailoverTrafficManagerNames().iterator().next()));
            Assert.assertTrue(t.getProperties().getBasic().getMachines().contains(config.getTrafficManagerName()));
        }

        for (LoadBalancerJoinVip6 jv : lb.getLoadBalancerJoinVip6Set()) {
            t = client.getTrafficIp(trafficIpGroupName(jv.getVirtualIp()));
            Assert.assertTrue(t.getProperties().getBasic().getIpaddresses().contains(jv.getVirtualIp().getDerivedIpString()));
            Assert.assertEquals(true, t.getProperties().getBasic().getEnabled());

            Assert.assertTrue(t.getProperties().getBasic().getMachines().contains(config.getFailoverTrafficManagerNames().iterator().next()));
            Assert.assertTrue(t.getProperties().getBasic().getMachines().contains(config.getTrafficManagerName()));
        }

    }

    private void verifySsltermination(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException, StingrayRestClientObjectNotFoundException, IPStringConversionException {
        String svsName = secureLoadBalancerName();
        String vsName = loadBalancerName();
        VirtualServer createdSecureVs = client.getVirtualServer(svsName);
        VirtualServer createdVs = client.getVirtualServer(vsName);
        VirtualServerBasic secureBasic = createdSecureVs.getProperties().getBasic();
        Assert.assertEquals(StmTestConstants.LB_SECURE_PORT, (int) secureBasic.getPort());
        Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(secureBasic.getProtocol().toString()));
        Assert.assertEquals(lb.getSslTermination().isEnabled(), secureBasic.getEnabled());
        Assert.assertEquals(vsName, secureBasic.getPool().toString());
        Assert.assertEquals(true, secureBasic.getSsl_decrypt());

        VirtualServerBasic normalBasic = createdVs.getProperties().getBasic();
        Assert.assertEquals(StmTestConstants.LB_PORT, (int) normalBasic.getPort());
        Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(normalBasic.getProtocol().toString()));
        Assert.assertEquals(lb.isSecureOnly(), !normalBasic.getEnabled());
    }

    private void verifyRateLimit(LoadBalancer lb) throws InsufficientRequestException, StingrayRestClientException {
        String vsName = loadBalancerName();
        try {
            Bandwidth bandwidth = client.getBandwidth(vsName);
            BandwidthBasic basic = bandwidth.getProperties().getBasic();
            Assert.assertEquals(basic.getMaximum(), lb.getRateLimit().getMaxRequestsPerSecond());
        } catch(StingrayRestClientObjectNotFoundException notFoundException) {
            Assert.assertNull(lb.getRateLimit());
        }
    }

    private String readFile(File file) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        String content = "";
        while (reader.hasNextLine()) content += reader.nextLine();
        reader.close();
        return content;
    }
}
