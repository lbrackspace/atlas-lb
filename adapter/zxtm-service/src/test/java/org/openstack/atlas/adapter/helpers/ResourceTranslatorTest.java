package org.openstack.atlas.adapter.helpers;


import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.stm.STMTestBase;
import org.openstack.atlas.adapter.stm.StmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
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
            createSimpleLoadBalancer();
            vsName = "test_name";
            this.isConnectionLogging = true;
            isHalfClosed = true;
            isContentCaching = true;
            expectedTcp = new VirtualServerTcp();
            expectedError = new VirtualServerConnectionError();
            errorFile = "Default";
            expectedError.setError_file(errorFile);

            rules = java.util.Arrays.asList(StmAdapterImpl.XFF, StmAdapterImpl.XFP);
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

        String expectedGroupName6;
        String expectedGroupName4;
        String failoverHost;
        String ipAddress4;
        String ipAddress6;
        String expectedVip6Ip;

        public void standUp() throws IPStringConversionException {

            setupIvars();
            createSimpleLoadBalancer();
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
            expectedVip6Ip =  ip6.getDerivedIpString();
            lb.setLoadBalancerJoinVip6Set(vip6s);


            //found in /etc/openstack/atlas/
            failoverHost = "development.lbaas.rackspace.net";

            //traffic group name Lb ID _ VIP ID
            expectedGroupName6 = Integer.toString(TEST_ACCOUNT_ID) + "_" + Integer.toString(ip6.getId());
            expectedGroupName4 = Integer.toString(TEST_ACCOUNT_ID) + "_" + Integer.toString(ip4.getId());

        }

        @Test
        public void shouldCreateValidTrafficIpGroup() throws IPStringConversionException, InsufficientRequestException {
            standUp();
            ResourceTranslator translator = new ResourceTranslator();
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
}
