package org.openstack.atlas.adapter.itest;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;
import org.rackspace.stingray.client.protection.ProtectionProperties;
import org.rackspace.stingray.client.util.EnumFactory;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;

import java.io.File;
import java.util.*;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;

public class SslTerminationITest extends STMTestBase {

    private String normalName;
    private String secureName;

    @Before
    public void setupClass() throws Exception {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
        normalName = ZxtmNameBuilder.genVSName(lb);
        secureName = ZxtmNameBuilder.genSslVSName(lb);
    }

    @Test
    public void testSSlTerminationOperations() {
        setSslTermination();
        updateSslTermination();
        deleteSslTermination();
    }

    @Test
    public void testSSlTerminationOperationsWhenUpdatingLBAttributes() throws Exception {
        setSslTermination();
        updateLoadBalancerAttributes();
    }

    @Test
    public void testAddingRateLimitWithSslTermination() throws Exception {
        setRateLimitBeforeSsl();
        deleteRateLimit();
        setSslTermination();
        setRateLimit();
    }

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testAddingOnlyAccessListWithSslTermination() throws Exception {
        verifyAccessListWithoutSsl();
        verifyDeleteAccessListWithoutConnectionThrottling();
    }

    @Test
    public void testAddingAccessListAndConnectionThrottlingWithSslTermination() throws Exception {
        lb.setConnectionLimit(new ConnectionLimit());
        stmAdapter.updateConnectionThrottle(config, lb);
        verifyAccessListWithoutSsl();
        verifyDeleteAccessListWithConnectionThrottling();
        setSslTermination();
        verifyAccessListWithSsl();
    }

    @Test
    public void testConnectionThrottleWhenCreatingSslTermination() throws Exception {
        verifyConnectionThrottle();
    }

    @Test
    public void testErrorPageWhenCreatingSslTermination() throws Exception {
        verifyDeleteErrorPage();
        verifyErrorPage();
    }

    @Test
    public void shouldPassIfCertificateIsRemovedWithSecureVSStillThere() throws Exception {
        setSslTermination();
        stmClient.deleteKeypair(secureName);
        updateSslTermination();
    }

    @Test
    public void verifyHostHeaderRewriteIsNever() {
        verifyHostHeaderRewrite();
    }

    private void setSslTermination() {
        boolean isSslTermEnabled = true;
        boolean allowSecureTrafficOnly = false;
        setSslTermination(isSslTermEnabled, allowSecureTrafficOnly);
    }

    private void updateSslTermination() {
        boolean isSslTermEnabled = true;
        boolean allowSecureTrafficOnly = true;
        setSslTermination(isSslTermEnabled, allowSecureTrafficOnly);
    }

    private void setSslTermination(boolean isSslTermEnabled, boolean allowSecureTrafficOnly) {
        try {
            boolean isVsEnabled = true;
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(allowSecureTrafficOnly);
            sslTermination.setEnabled(isSslTermEnabled);
            sslTermination.setSecurePort(StmTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(StmTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(StmTestConstants.SSL_KEY);

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(StmTestConstants.SSL_CERT);
            zeusCertFile.setPrivate_key(StmTestConstants.SSL_KEY);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(StmTestConstants.SSL_CERT);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());
            VirtualServer createdSecureVs = null;
            VirtualServer createdNormalVs = null;
            try {
                stmAdapter.updateSslTermination(config, lb, zeusSslTermination);
                createdSecureVs = stmClient.getVirtualServer(secureName);
                createdNormalVs = stmClient.getVirtualServer(normalName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Assert.assertNotNull(createdSecureVs);
            Assert.assertNotNull(createdNormalVs);

            VirtualServerBasic secureBasic = createdSecureVs.getProperties().getBasic();
            Assert.assertEquals(StmTestConstants.LB_SECURE_PORT, (int) secureBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(secureBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, secureBasic.getEnabled());
            Assert.assertEquals(normalName, secureBasic.getPool().toString());
            Assert.assertEquals(isSslTermEnabled, secureBasic.getSslDecrypt());

            VirtualServerBasic normalBasic = createdNormalVs.getProperties().getBasic();
            Assert.assertEquals(StmTestConstants.LB_PORT, (int) normalBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(normalBasic.getProtocol().toString()));
            if (allowSecureTrafficOnly) {
                Assert.assertEquals(!isVsEnabled, normalBasic.getEnabled());
            } else {
                Assert.assertEquals(isVsEnabled, normalBasic.getEnabled());
            }
            Assert.assertEquals(normalName, normalBasic.getPool().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void deleteSslTermination() {
        try {
            String vsName = ZxtmNameBuilder.genVSName(lb);
            String vsSslName = ZxtmNameBuilder.genSslVSName(lb);
            VirtualServer createdNormalVs = stmClient.getVirtualServer(normalName);
            stmAdapter.removeSslTermination(config, lb);
            List<String> names = new ArrayList<String>();
            for (Child child : stmClient.getVirtualServers()) {
                names.add(child.getName());
            }
            Assert.assertFalse(names.contains(vsSslName));
            Assert.assertTrue(names.contains(vsName));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void updateLoadBalancerAttributes() {

        try {
            //Should us updateSslTermination
            int securePort = StmTestConstants.LB_SECURE_PORT;
            int normalPort = StmTestConstants.LB_PORT;
            boolean isConnectionLogging = true;
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String normalVsName = ZxtmNameBuilder.genVSName(lb);
            stmAdapter.updateSslTermination(config, lb, new ZeusSslTermination());
            VirtualServer createdSecureVs = stmClient.getVirtualServer(secureVsName);
            Assert.assertEquals(securePort, (int) createdSecureVs.getProperties().getBasic().getPort());
            VirtualServer createdNormalVs = stmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(normalPort, (int) createdNormalVs.getProperties().getBasic().getPort());

            LoadBalancer nlb = new LoadBalancer();
            lb.setConnectionLogging(isConnectionLogging);
            nlb.setConnectionLogging(isConnectionLogging);
            stmAdapter.updateLoadBalancer(config, lb, nlb);
            createdSecureVs = stmClient.getVirtualServer(secureVsName);
            createdNormalVs = stmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
            Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());

            isConnectionLogging = false;
            lb.setConnectionLogging(isConnectionLogging);
            nlb.setConnectionLogging(isConnectionLogging);
            stmAdapter.updateLoadBalancer(config, lb, nlb);
            createdSecureVs = stmClient.getVirtualServer(secureVsName);
            createdNormalVs = stmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
            Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void setRateLimit() {
        try {
            int maxRequestsPerSecond = 1000;
            String ticketComment = "HI";
            RateLimit rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);
            Ticket ticket = new Ticket();
            ticket.setComment(ticketComment);
            rateLimit.setTicket(ticket);
            stmAdapter.setRateLimit(config, lb, rateLimit);


            Bandwidth createdNormalBandwidth = stmClient.getBandwidth(normalName);
            Assert.assertNotNull(createdNormalBandwidth);
            Assert.assertEquals(maxRequestsPerSecond, (int) createdNormalBandwidth.getProperties().getBasic().getMaximum());
            Assert.assertEquals(ticketComment, createdNormalBandwidth.getProperties().getBasic().getNote());

            VirtualServer createdServer = stmClient.getVirtualServer(normalName);
            Assert.assertTrue(createdServer.getProperties().getBasic().getAddXForwardedFor());
            Assert.assertTrue(createdServer.getProperties().getBasic().getAddXForwardedProto());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }


    }

    private void setRateLimitBeforeSsl() {
        try {
            int maxRequestsPerSecond = 1000;
            String ticketComment = "HI";
            RateLimit rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);
            Ticket ticket = new Ticket();
            ticket.setComment(ticketComment);
            rateLimit.setTicket(ticket);
            stmAdapter.setRateLimit(config, lb, rateLimit);

            Bandwidth createdNormalBandwidth = stmClient.getBandwidth(normalName);
            Assert.assertNotNull(createdNormalBandwidth);
            Assert.assertEquals(maxRequestsPerSecond, (int) createdNormalBandwidth.getProperties().getBasic().getMaximum());
            Assert.assertEquals(ticketComment, createdNormalBandwidth.getProperties().getBasic().getNote());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void deleteRateLimit() throws Exception {
        Boolean notFound = false;
        stmAdapter.deleteRateLimit(config, lb);
        try {
            stmClient.getBandwidth(normalName);
        } catch (StingrayRestClientObjectNotFoundException notFoundException) {
            notFound = true;
        }
        Assert.assertTrue(notFound);
    }

    private void connectionThrottleHelper(String vsName, int maxConnectionRate, int maxConnections,
                                          int minConnections, int rateInterval, int expectedMax10) {
        try {
            stmAdapter.updateConnectionThrottle(config, lb);


            Protection protection = stmClient.getProtection(vsName);

            Assert.assertNotNull(protection);
            ProtectionConnectionLimiting createdThrottle = protection.getProperties().getConnectionLimiting();
            Assert.assertEquals(maxConnectionRate, (int) createdThrottle.getMaxConnectionRate());
            Assert.assertEquals(expectedMax10, (int) createdThrottle.getMax10Connections());
            Assert.assertEquals(maxConnections, (int) createdThrottle.getMax1Connections());
            Assert.assertEquals(rateInterval, (int) createdThrottle.getRateTimer());
            Assert.assertEquals(minConnections, (int) createdThrottle.getMinConnections());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void errorPageHelper(String expectedContent) {
        try {
            String normalErrorFileName = stmClient.getVirtualServer(normalName).getProperties().getConnectionErrors().getErrorFile();
//            Assert.assertEquals(normalName + "_error.html", normalErrorFileName);
            File normalFile = stmClient.getExtraFile(normalErrorFileName);
            Scanner reader = new Scanner(normalFile);
            String content = "";
            while (reader.hasNextLine()) content += reader.nextLine();
            reader.close();
            Assert.assertEquals(content, expectedContent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyHostHeaderRewrite() {
        try {
            boolean allowSecureTrafficOnly = false;
            boolean isSslTermEnabled = true;
            setSslTermination(isSslTermEnabled, allowSecureTrafficOnly);
            VirtualServer createdVs = null;
            createdVs = stmClient.getVirtualServer(ZxtmNameBuilder.genSslVSName(lb));


            Assert.assertEquals(EnumFactory.AcceptFrom.NEVER.toString(),
                    createdVs.getProperties().getHttp().getLocationRewrite().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyErrorPage() {
        try {
            String errorContent = "HI";
            String errorFileNormalName = normalName + "_error.html";

            LoadBalancer nlb = new LoadBalancer();
            UserPages up = new UserPages();
            up.setErrorpage(errorContent);
            nlb.setUserPages(up);

            lb.getUserPages().setErrorpage(errorFileNormalName);
            nlb.getUserPages().setErrorpage(errorFileNormalName);
            stmAdapter.updateLoadBalancer(config, lb, nlb);
            stmAdapter.setErrorFile(config, lb, errorContent);
            errorPageHelper(errorContent);
            lb.getUserPages().setErrorpage(null);
            nlb.getUserPages().setErrorpage(null);

            stmAdapter.updateLoadBalancer(config, lb, nlb);
            stmAdapter.setErrorFile(config, lb, "Default");
            //TODO: wont have a file for default, assert just that the name is default
//            errorPageHelper("Default");
            lb.getUserPages().setErrorpage("Default");
            nlb.getUserPages().setErrorpage("Default");
            stmAdapter.updateLoadBalancer(config, lb, nlb);
            stmAdapter.setErrorFile(config, lb, errorContent);
            errorPageHelper(errorContent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyDeleteErrorPage() {
        String errorContent = "HI";
        String errorFileNormalName = normalName + "_error.html";
        try {
            UserPages userPages = new UserPages();
            userPages.setLoadbalancer(lb);
            userPages.setErrorpage(errorContent);

            LoadBalancer nlb = new LoadBalancer();
            lb.setUserPages(userPages);
            nlb.setUserPages(userPages);
            stmAdapter.updateLoadBalancer(config, lb, nlb);
            stmAdapter.setErrorFile(config, lb, errorContent);
            errorPageHelper(errorContent);
            lb.getUserPages().setErrorpage(null);
            nlb.getUserPages().setErrorpage(null);
            stmAdapter.updateLoadBalancer(config, lb, nlb);
            stmAdapter.setErrorFile(config, lb, "Default");
//            errorPageHelper("Default");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyConnectionThrottle() {
        try {
            ConnectionLimit throttle = new ConnectionLimit();
            int maxConnectionRate = 10;
            int maxConnections = 20;
            int minConnections = 40;
            int rateInterval = 44;
            int expectedMax10 = maxConnections * 10;

            throttle.setMaxConnectionRate(maxConnectionRate);
            throttle.setMaxConnections(maxConnections);
            throttle.setMinConnections(minConnections);
            throttle.setRateInterval(rateInterval);

            lb.setConnectionLimit(throttle);

            setSslTermination();
            connectionThrottleHelper(normalName, maxConnectionRate, maxConnections, minConnections, rateInterval, expectedMax10);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyDeleteAccessListWithoutConnectionThrottling() throws Exception {
        verifyAccessListWithSsl();
        List<Integer> deletionList = new ArrayList<Integer>();
        for (AccessList item : lb.getAccessLists()) {
            deletionList.add(item.getId());
        };
        stmAdapter.deleteAccessList(config, lb, deletionList);
        stmClient.getProtection(normalName);
    }

    private void verifyAccessListWithSsl() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            String ipAddressOne = "0.0.0.0/0";
            String ipAddressTwo = "127.0.0.1";
            item1.setIpAddress(ipAddressOne);
            item2.setIpAddress(ipAddressTwo);
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            stmAdapter.updateAccessList(config, lb);

            Protection normalProtection = stmClient.getProtection(normalName);
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getAllowed().contains(ipAddressTwo));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyDeleteAccessListWithConnectionThrottling() {
        try {
            verifyAccessListWithSsl();
            List<Integer> deletionList = new ArrayList<Integer>();
            for (AccessList item : lb.getAccessLists()) {
                deletionList.add(item.getId());
            }
            stmAdapter.deleteAccessList(config, lb, deletionList);
            Protection protection = stmClient.getProtection(normalName);
            ProtectionProperties properties = protection.getProperties();
            Assert.assertTrue(properties.getAccessRestriction().getAllowed().isEmpty());
            Assert.assertTrue(properties.getAccessRestriction().getBanned().isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyAccessListWithoutSsl() {
        try {
            Set<AccessList> networkItems = new HashSet<AccessList>();
            AccessList item1 = new AccessList();
            AccessList item2 = new AccessList();
            String ipAddressOne = "0.0.0.0/0";
            String ipAddressTwo = "127.0.0.1";
            item1.setIpAddress(ipAddressOne);
            item2.setIpAddress(ipAddressTwo);
            item1.setType(DENY);
            item2.setType(ALLOW);
            networkItems.add(item1);
            networkItems.add(item2);

            lb.setAccessLists(networkItems);
            stmAdapter.updateAccessList(config, lb);
            Protection normalProtection = stmClient.getProtection(normalName);
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(normalProtection.getProperties().getAccessRestriction().getAllowed().contains(ipAddressTwo));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    @After
    public void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    public static void removeSimpleLoadBalancer() {
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            String output = "Failure to delete load balancer:\n";
            for (StackTraceElement line : e.getStackTrace()) {
                output += line.toString() + "\n";
            }
            System.err.println(output);
        }
    }
}
