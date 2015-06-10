package org.openstack.atlas.adapter.helpers;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.bandwidth.BandwidthBasic;
import org.rackspace.stingray.client.bandwidth.BandwidthProperties;
import org.rackspace.stingray.client.exception.ClientException;
import org.rackspace.stingray.client.monitor.Monitor;
import org.rackspace.stingray.client.monitor.MonitorBasic;
import org.rackspace.stingray.client.monitor.MonitorHttp;
import org.rackspace.stingray.client.monitor.MonitorProperties;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolNodeWeight;
import org.rackspace.stingray.client.pool.PoolProperties;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionAccessRestriction;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIpBasic;
import org.rackspace.stingray.client.virtualserver.*;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class ResourceTranslatorTest extends STMTestBase {


    public static class WhenTranslatingAVirtualServer {

        private String vsName;
        private Boolean isHalfClosed;
        private Boolean isConnectionLogging;
        private Boolean isContentCaching;
        private String logFormat;
        private VirtualServerTcp expectedTcp;
        private VirtualServerConnectionError expectedError;
        private ArrayList<String> rules;
        private ResourceTranslator translator;
        private String errorFile;
        private AccessList accessListAllowed;
        private String ipAddressAllowed;
        private Set<AccessList> lists;
        @Mock
        private LoadBalancerEndpointConfiguration config;


        public void initializeVars(String logFormat, LoadBalancerProtocol protocol) throws InsufficientRequestException {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = ZxtmNameBuilder.genVSName(lb);
            this.isConnectionLogging = true;
            isHalfClosed = true;
            isContentCaching = true;
            expectedTcp = new VirtualServerTcp();
            expectedError = new VirtualServerConnectionError();
            errorFile = "Default";
            expectedError.setError_file(errorFile);
            rules = new ArrayList<String>();

            if (lb.getProtocol() == LoadBalancerProtocol.HTTP) {
                rules.add(StmConstants.XFPORT);
//                rules.add(StmConstants.XFP);
            }
//            else {
//                rules = new ArrayList<String>();
//                if (lb.getRateLimit() != null) rules.add(StmConstants.RATE_LIMIT_NON_HTTP);
//            }
            translator = new ResourceTranslator();

            ConnectionLimit connectionLimit = new ConnectionLimit();
            Set<AccessList> accessListSet = new HashSet<AccessList>();
            accessListSet.add(new AccessList());
            this.logFormat = logFormat;


            lb.setConnectionLogging(isConnectionLogging);
            lb.setHalfClosed(isHalfClosed);
            lb.setContentCaching(isContentCaching);
            lb.setProtocol(protocol);
            lb.setConnectionLimit(connectionLimit);
            lb.setAccessLists(accessListSet);
        }

        public void initializeVars(String logFormat, LoadBalancerProtocol protocol, String errorFile) throws InsufficientRequestException {

            initializeVars(logFormat, protocol);
            this.errorFile = errorFile;
            UserPages userPages = new UserPages();
            userPages.setErrorpage(this.errorFile);
            lb.setUserPages(userPages);
            expectedError.setError_file(this.errorFile);
            lb.setConnectionLimit(null);

            ipAddressAllowed = "10.1.1.1";
            accessListAllowed = new AccessList();
            accessListAllowed.setIpAddress(ipAddressAllowed);
            accessListAllowed.setType(AccessListType.ALLOW);

            lists = new HashSet<AccessList>();
            lists.add(accessListAllowed);
            lb.setAccessLists(lists);


        }

        public void pathOne() {
            lists = new HashSet<AccessList>();
            lb.setAccessLists(lists);
            lb.setConnectionLimit(null);
            isConnectionLogging = false;
            lb.setConnectionLogging(isConnectionLogging);


        }

        public void pathTwo() {
            lists.add(accessListAllowed);
            lb.setConnectionLimit(null);

        }

        public void pathThree() {
            lists.add(accessListAllowed);
            ConnectionLimit connectionLimit = new ConnectionLimit();
            lb.setConnectionLimit(connectionLimit);


        }

        public void pathFour() {
            lb.setConnectionLimit(null);
            lb.setAccessLists(null);
        }


        @Test
        public void stringToObjTest() throws InsufficientRequestException, IOException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            String refString = "{\"properties\":" +
                    "{\"basic\":{\"disabled\":[\"127.0.0.2:80\"],\"draining\":[],\"monitors\":[],\"nodes\"" +
                    ":[\"127.0.0.1:80\"],\"passive_monitoring\":false},\"connection\":{},\"load_balancing\":{\"algorithm\":" +
                    "\"weighted_least_connections\",\"node_weighting\":[{\"node\":\"127.0.0.1:80\"},{\"node\":\"127.0.0.2:80\"}]," +
                    "\"priority_enabled\":false,\"priority_values\":[\"127.0.0.1:80:2\"]}}}";
            Pool createdPool = translator.stringToObject(refString, Pool.class);
            System.out.println("Pool String AFter Mapping: " + createdPool.toString());
            String poolString = translator.objectToString(createdPool, Pool.class);
            Assert.assertEquals(refString, poolString);

        }

        @Ignore
        @Test
        public void experimental() throws InsufficientRequestException, IOException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            String refString = "{\"error_id\":\"resource.validation_error\",\"error_text\":\"The resource provided is " +
                    "invalid\",\"error_info\": {\"load_balancing\":{\"node_weighting\":{\"127.0.0.1:80\":{\"weight\":" +
                    "{\"error_id\":\"table.no_default\",\"error_text\":\"Table field 'weight' is not set and has no " +
                    "default value\"}},\"127.0.0.2:80\":{\"weight\":{\"error_id\":\"table.no_default\",\"error_text\":\"" +
                    "Table field 'weight' is not set and has no default value\"}}}}}}";
            ClientException exception = translator.stringToObject(refString, ClientException.class);

        }

        @Test
        public void shouldCreateValidVirtualServer() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            expectedTcp.setProxy_close(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWeb_cache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(ZxtmConversionUtils.mapProtocol(lb.getProtocol()).getValue(), createdBasic.getProtocol());
            Assert.assertEquals(lb.getPort(), createdBasic.getPort());
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtection_class());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListen_on_any());
            if (lb.isContentCaching() == true)
                rules.add(StmConstants.CONTENT_CACHING);
            Assert.assertTrue(rules.size() == createdBasic.getRequest_rules().size());
//            Assert.assertTrue(rules.containsAll(createdBasic.getRequest_rules()));

//            Assert.assertEquals(expectedError, createdProperties.getConnection_errors());
        }

        @Test
        public void checkAlternateValidPaths() throws InsufficientRequestException {
            initializeVars("%v %t %h %A:%p %n %B %b %T", LoadBalancerProtocol.FTP, "HI");
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerLog log = createdProperties.getLog();
            Assert.assertNotNull(createdServer.getProperties().getLog());
            Assert.assertEquals(logFormat, log.getFormat());
//            Assert.assertEquals(expectedError, createdProperties.getConnection_errors());
            Assert.assertEquals(vsName, createdBasic.getProtection_class());
            pathOne();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals(null, createdServer.getProperties().getBasic().getProtection_class());
            pathTwo();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals(vsName, createdServer.getProperties().getBasic().getProtection_class());
            pathThree();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals(vsName, createdServer.getProperties().getBasic().getProtection_class());
            pathFour();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            //TODO:  Intern will handle this bug
            Assert.assertEquals(null, createdServer.getProperties().getBasic().getProtection_class());

        }
    }

    public static class whenTranslatingATrafficIpGroup {

        private String expectedGroupName6;
        private String expectedGroupName4;
        private String failoverHost;
        private String ipAddress4;
        private String ipAddress6;
        private String expectedVip6Ip;

        private ResourceTranslator translator;
        @Mock
        private LoadBalancerEndpointConfiguration config;

        @Before
        public void standUp() throws IPStringConversionException {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            int acctId = 1234567890;
            int ipv4Id = 1111;
            int ipv6Id = 2222;
            int ipv6Octet = 1;
            ipAddress4 = "000.000.000.000";
            ipAddress6 = "fe80::a00:27ff:fe05:d0d5/64";

            Set<LoadBalancerJoinVip> vip4s = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip vip4 = new LoadBalancerJoinVip();
            VirtualIp ip4 = new VirtualIp();
            ip4.setId(ipv4Id);
            ip4.setIpAddress(ipAddress4);
            vip4.setVirtualIp(ip4);
            vip4s.add(vip4);
            lb.setLoadBalancerJoinVipSet(vip4s);

            Set<LoadBalancerJoinVip6> vip6s = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 vip6 = new LoadBalancerJoinVip6();
            VirtualIpv6 ip6 = new VirtualIpv6();
            ip6.setAccountId(acctId);
            ip6.setId(ipv6Id);
            ip6.setVipOctets(ipv6Octet);
            vip6.setVirtualIp(ip6);
            Cluster cluster = new Cluster();
            cluster.setClusterIpv6Cidr(ipAddress6);
            ip6.setCluster(cluster);
            vip6s.add(vip6);
            expectedVip6Ip = ip6.getDerivedIpString();
            lb.setLoadBalancerJoinVip6Set(vip6s);

            //found in /etc/openstack/atlas/
            failoverHost = "development.lbaas.rackspace.net";

            //traffic group name Lb ID _ VIP ID
            expectedGroupName6 = Integer.toString(TEST_ACCOUNT_ID) + "_" + Integer.toString(ip6.getId());
            expectedGroupName4 = Integer.toString(TEST_ACCOUNT_ID) + "_" + Integer.toString(ip4.getId());
            List<String> failoverHosts = new ArrayList<String>();
            failoverHosts.add(failoverHost);
            when(config.getFailoverTrafficManagerNames()).thenReturn(failoverHosts);
        }

        @Test
        public void shouldCreateValidTrafficIpGroup() throws IPStringConversionException, InsufficientRequestException {
            translator = new ResourceTranslator();
            Map<String, TrafficIp> mappedGroups = translator.translateTrafficIpGroupsResource(config, lb, true);

            Assert.assertTrue(mappedGroups.containsKey(expectedGroupName4));
            Assert.assertTrue(mappedGroups.containsKey(expectedGroupName6));

            TrafficIp retrievedTrafficIp4 = mappedGroups.get(expectedGroupName4);
            TrafficIp retrievedTrafficIp6 = mappedGroups.get(expectedGroupName6);

            TrafficIpBasic basic4 = retrievedTrafficIp4.getProperties().getBasic();
            TrafficIpBasic basic6 = retrievedTrafficIp6.getProperties().getBasic();

            Assert.assertTrue(basic4.getIpaddresses().contains(ipAddress4));
            Assert.assertTrue(basic4.getMachines().contains(failoverHost));
            Assert.assertTrue(basic4.getEnabled());

            Assert.assertTrue(basic6.getMachines().contains(failoverHost));
            Assert.assertTrue(basic6.getIpaddresses().contains(expectedVip6Ip));
            Assert.assertTrue(basic6.getEnabled());
        }


    }

    public static class whenTranslatingAPool {

        private HealthMonitor healthMonitor;
        private int numAttemptsCheck;
        private String vsName;
        private int expectedTimeout;
        private Node nodeEnabled;
        private Node nodeDraining;
        private Node nodeDisabled;
        private String nodeEnabledAddress;
        private String nodeDrainingAddress;
        private String nodeDisabledAddress;
        private int nodePort;
        private int nodeEnabledWeight;
        private int nodeDrainingWeight;
        private int nodeDisabledWeight;
        private PoolNodeWeight poolNodeEnabledWeight;
        private PoolNodeWeight poolNodeDrainingWeight;
        private PoolNodeWeight poolNodeDisabledWeight;
        private ZeusNodePriorityContainer container;
        private String nodeEnabledName;
        private String nodeDisabledName;
        private String nodeDrainingName;


        public void standUp(LoadBalancerAlgorithm algorithm) {
            setupIvars();
            vsName = "qwertyuiop";
            numAttemptsCheck = 90;
            expectedTimeout = 132;
            nodeEnabledAddress = "10.1.1.1";
            nodeDrainingAddress = "10.1.1.2";
            nodeDisabledAddress = "10.1.1.3";
            nodeEnabledWeight = 1;
            nodeDrainingWeight = 2;
            nodeDisabledWeight = 3;
            nodePort = 1107;
            healthMonitor = new HealthMonitor();
            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
            lb.setAlgorithm(algorithm);
            lb.setHealthMonitor(healthMonitor);
            lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
            lb.setTimeout(expectedTimeout);
            Set<Node> nodes = new HashSet<Node>();
            nodeEnabled = new Node();
            nodeDraining = new Node();
            nodeDisabled = new Node();
            nodeEnabled.setCondition(NodeCondition.ENABLED);
            nodeDraining.setCondition(NodeCondition.DRAINING);
            nodeDisabled.setCondition(NodeCondition.DISABLED);
            nodeEnabled.setIpAddress(nodeEnabledAddress);
            nodeDraining.setIpAddress(nodeDrainingAddress);
            nodeDisabled.setIpAddress(nodeDisabledAddress);
            nodeEnabled.setWeight(nodeEnabledWeight);
            nodeDraining.setWeight(nodeDrainingWeight);
            nodeDisabled.setWeight(nodeDisabledWeight);
            nodeEnabled.setPort(nodePort);
            nodeDraining.setPort(nodePort);
            nodeDisabled.setPort(nodePort);
            nodes.add(nodeEnabled);
            nodes.add(nodeDraining);
            nodes.add(nodeDisabled);

            poolNodeEnabledWeight = new PoolNodeWeight();
            poolNodeDrainingWeight = new PoolNodeWeight();
            poolNodeDisabledWeight = new PoolNodeWeight();


            poolNodeEnabledWeight.setWeight(nodeEnabledWeight);
            poolNodeDrainingWeight.setWeight(nodeDrainingWeight);
            poolNodeDisabledWeight.setWeight(nodeDisabledWeight);

            nodeEnabledName = nodeEnabledAddress + ":" + nodePort;
            nodeDisabledName = nodeDisabledAddress + ":" + nodePort;
            nodeDrainingName = nodeDrainingAddress + ":" + nodePort;

            poolNodeEnabledWeight.setNode(nodeEnabledName);
            poolNodeDrainingWeight.setNode(nodeDrainingName);
            poolNodeDisabledWeight.setNode(nodeDisabledName);


            lb.setNodes(nodes);
            container = new ZeusNodePriorityContainer(lb.getNodes());


        }

        public void tearDown() {
            lb.setHealthMonitor(null);
            lb.setSessionPersistence(null);
            lb.setAlgorithm(LoadBalancerAlgorithm.LEAST_CONNECTIONS);

        }


        @Test
        public void shouldCreateAValidPool() throws InsufficientRequestException {
            ResourceTranslator translator = new ResourceTranslator();
            standUp(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
            Pool createdPool = translator.translatePoolResource(vsName, lb, lb);
            Assert.assertNotNull(createdPool);
            PoolProperties createdProperties = createdPool.getProperties();
            Assert.assertNotNull(createdProperties);
            PoolBasic createdBasic = createdPool.getProperties().getBasic();
            Assert.assertTrue(createdBasic.getMonitors().contains(vsName));
            Assert.assertFalse(createdBasic.getPassive_monitoring());
            Assert.assertEquals(SessionPersistence.HTTP_COOKIE.toString(), createdBasic.getPersistence_class());
            Assert.assertTrue(createdBasic.getNodes().contains(nodeEnabledName));
            Assert.assertTrue(createdBasic.getDraining().contains(nodeDrainingName));
            Assert.assertTrue(createdBasic.getDisabled().contains(nodeDisabledName));
            Assert.assertEquals(expectedTimeout, (int) createdProperties.getConnection().getMax_reply_time());
            Assert.assertEquals(container.getPriorityValuesSet(), createdProperties.getLoad_balancing().getPriority_values());
            Assert.assertEquals(container.hasSecondary(), createdProperties.getLoad_balancing().getPriority_enabled());
            Assert.assertEquals(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN.toString().toLowerCase(), createdProperties.getLoad_balancing().getAlgorithm());
            List<PoolNodeWeight> weights = createdProperties.getLoad_balancing().getNode_weighting();
            Assert.assertNotNull(weights);
            Assert.assertTrue(weights.contains(poolNodeEnabledWeight));
            Assert.assertTrue(weights.contains(poolNodeDrainingWeight));
            Assert.assertTrue(weights.contains(poolNodeDisabledWeight));

        }

        @Test
        public void testAlternatePaths() throws InsufficientRequestException {
            ResourceTranslator translator = new ResourceTranslator();
            standUp(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS);
            Pool createdPool = translator.translatePoolResource(vsName, lb, lb);
            PoolProperties createdProperties = createdPool.getProperties();
            Assert.assertNotNull(createdProperties);
            Assert.assertEquals(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS.toString().toLowerCase(), createdProperties.getLoad_balancing().getAlgorithm());
            List<PoolNodeWeight> weights = createdProperties.getLoad_balancing().getNode_weighting();
            Assert.assertNotNull(weights);
            Assert.assertTrue(weights.contains(poolNodeEnabledWeight));
            Assert.assertTrue(weights.contains(poolNodeDrainingWeight));
            Assert.assertTrue(weights.contains(poolNodeDisabledWeight));
            standUp(LoadBalancerAlgorithm.RANDOM);
            createdPool = translator.translatePoolResource(vsName, lb, lb);
            Assert.assertTrue(createdPool.getProperties().getLoad_balancing().getNode_weighting().size() == 0);
            tearDown();
            createdPool = translator.translatePoolResource(vsName, lb, lb);
            createdProperties = createdPool.getProperties();
            PoolBasic createdBasic = createdProperties.getBasic();
            Assert.assertEquals("", createdBasic.getPersistence_class());
            Assert.assertTrue(createdBasic.getMonitors().size() == 0);

        }


    }

    public static class whenTranslatingAHealthMonitor {

        private HealthMonitor healthMonitor;
        private HealthMonitorType monitorType;
        private Integer numAttemptsCheck;
        private Integer delay;
        private Integer timeout;
        private String hostHeader;
        private String path;
        private String bodyRegex;
        private String statusRegex;
        private ResourceTranslator translator;

        @Before
        public void standUp() {
            setupIvars();
//            numAttemptsCheck = 90;
//            delay = 30;
//            timeout = 20;
//            hostHeader = "host123";
//            path = "path123";
//            bodyRegex = "br123";
//            statusRegex = "sr123";
//            useSsl = true; //This is set automatically on the LoadBalancer object when type is HTTPS
//            healthMonitor = new HealthMonitor();
//            healthMonitor.setType(monitorType);
//            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
//            healthMonitor.setDelay(delay);
//            healthMonitor.setTimeout(timeout);
//            healthMonitor.setHostHeader(hostHeader);
//            healthMonitor.setPath(path);
//            healthMonitor.setBodyRegex(bodyRegex);
//            healthMonitor.setStatusRegex(statusRegex);
//            lb.setHealthMonitor(healthMonitor);
        }

        @Test
        public void shouldCreateAValidHealthMonitor() throws InsufficientRequestException {
            translator = new ResourceTranslator();
            Monitor createdMonitor;
            MonitorProperties createdProperties;
            MonitorBasic createdBasic;
            MonitorHttp createdHttp;

            // Test MonitorType HTTPS
            monitorType = HealthMonitorType.HTTPS;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdBasic.getType(), HealthMonitorType.HTTP.toString().toLowerCase()); //The REST API does not use HTTPS as a type
            Assert.assertTrue(createdBasic.getUse_ssl());

            // Test MonitorType HTTP
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdBasic.getType(), monitorType.toString().toLowerCase());
            Assert.assertFalse(createdBasic.getUse_ssl());

            // Test MonitorType CONNECT
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getType(), monitorType.toString().toLowerCase());

            // Test Number of Attempts (type doesn't matter)
            numAttemptsCheck = 20;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getFailures(), numAttemptsCheck);

            numAttemptsCheck = 30;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getFailures(), numAttemptsCheck);

            // Test Delay (type doesn't matter)
            delay = 20;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setDelay(delay);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getDelay(), delay);

            delay = 30;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setDelay(delay);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getDelay(), delay);

            // Test Timeout (type doesn't matter)
            timeout = 20;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setTimeout(timeout);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getTimeout(), timeout);

            timeout = 30;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setTimeout(timeout);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getTimeout(), timeout);

            // Test Host Header (must be HTTP or HTTPS)
            hostHeader = "host123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setHostHeader(hostHeader);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getHost_header(), hostHeader);

            hostHeader = "host456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setHostHeader(hostHeader);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getHost_header(), hostHeader);

            // Test Path (must be HTTP or HTTPS)
            path = "path123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setPath(path);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getPath(), path);

            path = "path456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setPath(path);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getPath(), path);

            // Test Body Regex (must be HTTP or HTTPS)
            bodyRegex = "br123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setBodyRegex(bodyRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getBody_regex(), bodyRegex);

            bodyRegex = "br456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setBodyRegex(bodyRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getBody_regex(), bodyRegex);

            // Test Status Regex (must be HTTP or HTTPS)
            statusRegex = "sr123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setStatusRegex(statusRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getStatus_regex(), statusRegex);

            statusRegex = "sr456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setStatusRegex(statusRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getStatus_regex(), statusRegex);
        }

    }

    public static class whenTranslatingABandwidthResource {

        private RateLimit rateLimit;
        public Integer maxRequestsPerSecond;
        private String comment;
        private ResourceTranslator translator;

        @Before
        public void standUp() {
            setupIvars();
            comment = "This is a comment.";
            Ticket ticket = new Ticket();
            ticket.setComment(comment);
            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);
            rateLimit.setTicket(ticket);

            lb.setRateLimit(rateLimit);
        }

        @Test
        public void shouldCreateAValidBandwidth() throws InsufficientRequestException {
            translator = new ResourceTranslator();
            Bandwidth createdBandwidth = translator.translateBandwidthResource(lb);
            BandwidthProperties createdProperties = createdBandwidth.getProperties();
            BandwidthBasic createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdBandwidth);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getMaximum(), maxRequestsPerSecond);
            Assert.assertEquals(createdBasic.getNote(), comment);
        }
    }

    public static class whenTranslatingAProtection {

        String vsName;
        int maxConnections;
        int maxRateInterval;
        int minConnections;
        int rateTiming;
        String ipAddressAllowed;
        String ipAddressBanned;
        ConnectionLimit connectionLimit;
        AccessList accessListAllowed;
        AccessList accessListBanned;


        @Before
        public void standUp() {
            setupIvars();


            vsName = "HI";

            maxConnections = 90;
            minConnections = 35;
            rateTiming = 78;
            maxRateInterval = 50;

            ipAddressAllowed = "10.1.1.4";
            ipAddressBanned = "10.1.1.5";

            connectionLimit = new ConnectionLimit();
            connectionLimit.setMaxConnections(maxConnections);
            connectionLimit.setMinConnections(minConnections);
            connectionLimit.setRateInterval(rateTiming);
            connectionLimit.setMaxConnectionRate(maxRateInterval);

            accessListAllowed = new AccessList();
            accessListAllowed.setIpAddress(ipAddressAllowed);
            accessListAllowed.setType(AccessListType.ALLOW);

            accessListBanned = new AccessList();
            accessListBanned.setIpAddress(ipAddressBanned);
            accessListBanned.setType(AccessListType.DENY);

            Set<AccessList> accessListSet = new HashSet<AccessList>();
            accessListSet.add(accessListAllowed);
            accessListSet.add(accessListBanned);

            lb.setConnectionLimit(connectionLimit);
            lb.setAccessLists(accessListSet);


        }

        @Test
        public void shouldCreateAValidProtection() {
            ResourceTranslator translator = new ResourceTranslator();
            Protection createdProtection = translator.translateProtectionResource(lb);
            ProtectionConnectionLimiting createdLimiting = createdProtection.getProperties().getConnection_limiting();
            Assert.assertNotNull(createdLimiting);
            Assert.assertEquals(maxConnections, (int) createdLimiting.getMax_1_connections());
            /* Due to a Zeus bug, this all has to be ignored and set to static values */
            // Assert.assertEquals(maxRateInterval, (int) createdLimiting.getMax_connection_rate());
            // Assert.assertEquals(minConnections, (int) createdLimiting.getMin_connections());
            // Assert.assertEquals(rateTiming, (int) createdLimiting.getRate_timer());
            Assert.assertFalse(createdProtection.getProperties().getBasic().getPer_process_connection_count());
            Assert.assertEquals(0, (int) createdLimiting.getMax_connection_rate());
            Assert.assertEquals(0, (int) createdLimiting.getMin_connections());
            Assert.assertEquals(1, (int) createdLimiting.getRate_timer());
            ProtectionAccessRestriction createdRestriction = createdProtection.getProperties().getAccess_restriction();
            Assert.assertNotNull(createdRestriction);
            Assert.assertTrue(createdRestriction.getAllowed().contains(accessListAllowed.getIpAddress()));
            Assert.assertTrue(createdRestriction.getBanned().contains(accessListBanned.getIpAddress()));
        }
    }


    public static class whenGenGroupNameSet {

        private ResourceTranslator translator;


        @Test
        public void shouldGenGroupNameSet() throws InsufficientRequestException {
            translator = new ResourceTranslator();
            Set<String> groupNameSet = translator.genGroupNameSet(lb);
            Assert.assertFalse(groupNameSet.isEmpty());
            //Should probably verify if they are generated correctly..
        }
    }

    public static class whenTranslatingAPersistenceResource {

        private String vsName;
        private PersistenceType persistenceType;
        private ResourceTranslator translator;

        @Before
        public void standUp() {
            setupIvars();
            vsName = "asdfgh";
            persistenceType = PersistenceType.HTTP_COOKIE;
            org.openstack.atlas.service.domain.entities.SessionPersistence
                    persistence = SessionPersistence.fromDataType(persistenceType);
            lb.setSessionPersistence(persistence);
        }

        @Test
        public void test() {
            //gotta initialize test or tests break
        }
    }
}
