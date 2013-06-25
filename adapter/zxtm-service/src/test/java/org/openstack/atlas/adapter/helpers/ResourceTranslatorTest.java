package org.openstack.atlas.adapter.helpers;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.stm.STMTestBase;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        private List<String> rules;
        private ResourceTranslator translator;
        private String errorFile;


        public void initializeVars(String logFormat, LoadBalancerProtocol protocol) {
            setupIvars();
//            createSimpleLoadBalancer();
            vsName = "test_name";
            this.isConnectionLogging = true;
            isHalfClosed = true;
            isContentCaching = true;
            expectedTcp = new VirtualServerTcp();
            expectedError = new VirtualServerConnectionError();
            errorFile = "Default";
            expectedError.setError_file(errorFile);

            rules = java.util.Arrays.asList(StmConstants.XFF, StmConstants.XFP);
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

        public void initializeVars(String logFormat, LoadBalancerProtocol protocol, String errorFile) {

            initializeVars(logFormat, protocol);
            this.errorFile = errorFile;
            UserPages userPages = new UserPages();
            userPages.setErrorpage(this.errorFile);
            lb.setUserPages(userPages);
            expectedError.setError_file(this.errorFile);


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
            Assert.assertEquals(lb.getProtocol().name(), createdBasic.getProtocol());
            Assert.assertEquals(lb.getPort(), createdBasic.getPort());
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtection_class());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListen_on_any());
            Assert.assertEquals(rules, createdBasic.getRequest_rules());
            Assert.assertEquals(expectedError, createdProperties.getConnection_errors());
        }

        @Test
        public void checkAlternateValidPaths() throws InsufficientRequestException {
            initializeVars("%v %t %h %A:%p %n %B %b %T", LoadBalancerProtocol.FTP, "HI");
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerLog log = createdProperties.getLog();
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertEquals(expectedError, createdProperties.getConnection_errors());


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

        @Before
        public void standUp() throws IPStringConversionException {

            setupIvars();
//            createSimpleLoadBalancer();
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

        }

        @Test
        public void shouldCreateValidTrafficIpGroup() throws IPStringConversionException, InsufficientRequestException {
            translator = new ResourceTranslator();
            Map<String, TrafficIp> mappedGroups = translator.translateTrafficIpGroupsResource(config, lb);

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

        @Before
        public void standUp() {
            setupIvars();
//            createSimpleLoadBalancer();
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
            lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
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

            poolNodeEnabledWeight.setNode(nodeEnabledAddress);
            poolNodeDrainingWeight.setNode(nodeDrainingAddress);
            poolNodeDisabledWeight.setNode(nodeDisabledAddress);

            poolNodeEnabledWeight.setWeight(nodeEnabledWeight);
            poolNodeDrainingWeight.setWeight(nodeDrainingWeight);
            poolNodeDisabledWeight.setWeight(nodeDisabledWeight);


            lb.setNodes(nodes);
            container = new ZeusNodePriorityContainer(lb.getNodes());




        }

        @Test
        public void shouldCreateAValidPool() throws InsufficientRequestException {
            ResourceTranslator translator = new ResourceTranslator();
            Pool createdPool = translator.translatePoolResource(vsName, lb);
            Assert.assertNotNull(createdPool);
            PoolProperties createdProperties = createdPool.getProperties();
            Assert.assertNotNull(createdProperties);
            PoolBasic createdBasic = createdPool.getProperties().getBasic();
            Assert.assertTrue(createdBasic.getMonitors().contains(vsName));
            Assert.assertFalse(createdBasic.getPassive_monitoring());
            Assert.assertEquals(SessionPersistence.HTTP_COOKIE.toString(), createdBasic.getPersistence_class());
            Assert.assertTrue(createdBasic.getNodes().contains(nodeEnabledAddress));
            Assert.assertTrue(createdBasic.getDraining().contains(nodeDrainingAddress));
            Assert.assertTrue(createdBasic.getDisabled().contains(nodeDisabledAddress));
            Assert.assertEquals(expectedTimeout, (int)createdProperties.getConnection().getMax_reply_time());
            Assert.assertEquals(container.getPriorityValuesSet(), createdProperties.getLoad_balancing().getPriority_values());
            Assert.assertEquals(container.hasSecondary(), createdProperties.getLoad_balancing().getPriority_enabled());
            Assert.assertEquals(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN.toString().toLowerCase(), createdProperties.getLoad_balancing().getAlgorithm());
            List<PoolNodeWeight> weights = createdProperties.getLoad_balancing().getNode_weighting();
            Assert.assertNotNull(weights);
            Assert.assertTrue(weights.contains(poolNodeEnabledWeight));
            Assert.assertTrue(weights.contains(poolNodeDrainingWeight));
            Assert.assertTrue(weights.contains(poolNodeDisabledWeight));




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

            ipAddressAllowed = "10.1.1.4";
            ipAddressBanned = "10.1.1.5";

            connectionLimit = new ConnectionLimit();
            connectionLimit.setMaxConnections(maxConnections);
            connectionLimit.setMinConnections(minConnections);
            connectionLimit.setRateInterval(rateTiming);

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
            Protection createdProtection = translator.translateProtectionResource(vsName, lb);
            ProtectionConnectionLimiting createdLimiting = createdProtection.getProperties().getConnection_limiting();
            Assert.assertNotNull(createdLimiting);
            Assert.assertEquals(maxConnections, (int) createdLimiting.getMax_1_connections());
            Assert.assertEquals(rateTiming, (int) createdLimiting.getMax_connection_rate());
            Assert.assertEquals(minConnections, (int) createdLimiting.getMin_connections());
            Assert.assertEquals(rateTiming, (int) createdLimiting.getRate_timer());
            ProtectionAccessRestriction createdRestriction = createdProtection.getProperties().getAccess_restriction();
            Assert.assertNotNull(createdRestriction);
            Assert.assertTrue(createdRestriction.getAllowed().contains(accessListAllowed.getIpAddress()));
            Assert.assertTrue(createdRestriction.getBanned().contains(accessListBanned.getIpAddress()));
        }












    }
}
