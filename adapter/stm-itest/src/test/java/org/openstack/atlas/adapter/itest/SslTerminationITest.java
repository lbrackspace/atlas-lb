package org.openstack.atlas.adapter.itest;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.StmConstants;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;
import org.rackspace.stingray.client.bandwidth.Bandwidth;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionConnectionLimiting;
import org.rackspace.stingray.client.util.EnumFactory;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;

public class SslTerminationITest extends STMTestBase {

    private String normalName;
    private String secureName;


    final String testCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIERzCCAy+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJVUzEO\n" +
            "MAwGA1UECBMFVGV4YXMxDjAMBgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3Bh\n" +
            "Y2UgSG9zdGluZzEUMBIGA1UECxMLUmFja0V4cCBDQTQxGDAWBgNVBAMTD2NhNC5y\n" +
            "YWNrZXhwLm9yZzAeFw0xMjAxMTIxNzU3MDZaFw0xNDAxMTAxNzU3MDZaMHkxCzAJ\n" +
            "BgNVBAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEOMAwGA1UEBxMFVGV4YXMxGjAYBgNV\n" +
            "BAoTEVJhY2tTcGFjZSBIb3N0aW5nMRQwEgYDVQQLEwtSYWNrRXhwIENBNTEYMBYG\n" +
            "A1UEAxMPY2E1LnJhY2tleHAub3JnMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
            "CgKCAQEAsVK6npit7Q3NLlVjkpiDj+QuIoYrhHTL5KKzj6CrtQsFYukEL1YEKNlM\n" +
            "/dv8id/PkmdQ0wCNsk8d69CZKgO4hpN6O/b2aUl/vQcrW5lv3fI8x4wLu2Ri92vJ\n" +
            "f04RiZ3Jyc0rgrfGyLyNJcnMIMjnFV7mQyy+7cMGKCDgaLzUGNyR5E/Mi4cERana\n" +
            "xyp1nZI3DjA11Kwums9cx5VzS0Po1RyBsu7Xnpv3Fp2QqCBgdX8uaR5RuSak40/5\n" +
            "Jv2ORv28mi9AFu2AIRj6lrDdaLQGAXnbDk8b0ImEvVOe/QASsgTSmzOtn3q9Yejl\n" +
            "peQ9PFImVr2TymTF6UarGRHCWId1dQIDAQABo4HZMIHWMA8GA1UdEwEB/wQFMAMB\n" +
            "Af8wgaMGA1UdIwSBmzCBmIAUoeopOMWIEeYGtksI+T+ZjXWKc4ahfaR7MHkxCzAJ\n" +
            "BgNVBAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEOMAwGA1UEBxMFVGV4YXMxGjAYBgNV\n" +
            "BAoTEVJhY2tTcGFjZSBIb3N0aW5nMRQwEgYDVQQLEwtSYWNrRXhwIENBMzEYMBYG\n" +
            "A1UEAxMPY2EzLnJhY2tleHAub3JnggECMB0GA1UdDgQWBBSJF0Is0Wn7cVQ2iz/x\n" +
            "W/xdobdNezANBgkqhkiG9w0BAQUFAAOCAQEAHUIe5D3+/j4yca1bxXg0egL0d6ed\n" +
            "Cam/l+E/SHxFJmlLOfkMnDQQy/P31PBNrHPdNw3CwK5hqFGl8oWGLifRmMVlWhBo\n" +
            "wD1wmzm++FQeEthhl7gBkgECxZ+U4+WRiqo9ZiHWDf49nr8gUONF/qnHHkXTOZKo\n" +
            "vB34N2y+nONDvyzky2wzbvU46dW7Wc6Lp2nLTt4amC66V973V31Vlpbzg3C0K7sc\n" +
            "PA2GGTsiW6NF1mLd4fECgXslaQggoAKax7QY2yKrXLN5tmrHHThV3fIvLbSNFJbl\n" +
            "dZsGmy48UFF4pBHdhnE8bCAt8KgK3BJb0XqNrUxxI6Jc/Hcl9AfppFIEGw==\n" +
            "-----END CERTIFICATE-----";


    final String testKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAsVK6npit7Q3NLlVjkpiDj+QuIoYrhHTL5KKzj6CrtQsFYukE\n" +
            "L1YEKNlM/dv8id/PkmdQ0wCNsk8d69CZKgO4hpN6O/b2aUl/vQcrW5lv3fI8x4wL\n" +
            "u2Ri92vJf04RiZ3Jyc0rgrfGyLyNJcnMIMjnFV7mQyy+7cMGKCDgaLzUGNyR5E/M\n" +
            "i4cERanaxyp1nZI3DjA11Kwums9cx5VzS0Po1RyBsu7Xnpv3Fp2QqCBgdX8uaR5R\n" +
            "uSak40/5Jv2ORv28mi9AFu2AIRj6lrDdaLQGAXnbDk8b0ImEvVOe/QASsgTSmzOt\n" +
            "n3q9YejlpeQ9PFImVr2TymTF6UarGRHCWId1dQIDAQABAoIBACm7jrBEvqpL1T5S\n" +
            "WlzmCBCVY0Y8zYEe+92TbS8gYUj6jwn4TUPWuqPigHw+ifDo+7E5H4yJVM/iTuhw\n" +
            "75szxPnnO51hQh0Fb0rNpSaptepGWIeeLiSsO55/f6y2cuoweI1F/DeHiQE1XwLF\n" +
            "u4T7w2cELq0gms7aV1iaZDZCOqie3Dub7KAL76jwpG3ECQlWzF04TjQ5lZBdM7Fa\n" +
            "z3fbaJ497k5DoPbZMqGi2eR7P8NJAPjIpmaL3vls2vlmWwd/7D10AJUNoILb74jm\n" +
            "648YFo76yKS15jtHFvifSaxEg3gjmth7IuRF4SbL5AjFqhj1qo9yQKLep7pNv9Bx\n" +
            "0eYoqwECgYEA4r3h/4WGuXrnh36zJW860O7+pO3l8rm83wP1oGc8xCK74aBQP5zL\n" +
            "JHaJypeImisZg3OcKL5IBop76LZ/i5oCDozHvTRByFHYnkRU3oh6FDcIvPkDCB7o\n" +
            "qq8y6Q+gbTJlKzpSxoRnj1rkHOweDzNG/7QD/D/g2z5ZejW3xC6H3R8CgYEAyDRe\n" +
            "Qv/ATAn1F0r7LweShjAcqaf5DxmXNDpaw7Wj0OKZxyxYw6aPVm3LnZP1tmGe9UlE\n" +
            "CFRTX5Y98x+9Z+PFtYgW0EdZCVQXKLkGJUhD8SRxyaS5Tlz1hzSHtbxGbDFuecRd\n" +
            "Qv/XmrJapVQrT4TMa5ivw836tjQhVqCrNyCHRusCgYEAk9o793IrkuFI/rqouN1a\n" +
            "HgnqNMQIcQma1lXvomQPZNo9Z3gxO/nTIXjGizva0KUQIv6NMqg5sUI2YF44t2B6\n" +
            "vOAiEwdzadutBC8MpHucF3h3kzpRNsdo8nwCF6Wf9/SnsdN7TIXkPb+IBjAVvdWz\n" +
            "E2RgQOmqh2yVzjIfHac14wMCgYEAkgiA6WYcIlrxB/iNmBRx8KePgMEhjr4f6NzX\n" +
            "8AHCaE+h1AKpDK2lyGl2KI8Qn+Q9SrYShfDcj9DLh1gTlIA0auHFok8oxwErk2zC\n" +
            "6tb3mCH5Thh1go+UGPdcNlgLFkhISVHOpVxxLEoEjKwEm5BGfAV3z9+jjNwhpUq1\n" +
            "GRUFF9kCgYBu/b84bEmflvv0z412hiQuIjDrJWPLUENfJujs6RitU42KV78Momif\n" +
            "/qrCK1exgdMiXET3nXg7Ff2zi5O8QArM3ITaWOczukAXaAeTPKm9o59ubb4PsU9K\n" +
            "A8Lv1syLCAC54udcbBGG2gvv7KVwJZQhmwItdX0ev5oAY3DTbJwstg==\n" +
            "-----END RSA PRIVATE KEY-----";

    @Before
    public void setupClass() throws Exception {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
        normalName = ZxtmNameBuilder.genVSName(lb);
        secureName = ZxtmNameBuilder.genSslVSName(lb);
    }

    @After
    public void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    public void removeSimpleLoadBalancer() {
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


    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testWhenAddingRateLimitWithSslTermination() throws Exception {
        setRateLimitBeforeSsl();
        deleteRateLimit();
        setSslTermination();
        setRateLimit();
    }


    @Test
    public void testWhenAddingAccessListWith() throws Exception {
        verifyAccessListWithoutSsl();
        verifyDeleteAccessList();
        setSslTermination();
        verifyAccessListWithSsl();
    }


    @Test
    public void testErrorPageWhenCreatingSslTermination() throws Exception {
        verifyDeleteErrorPage();
        verifyErrorPage();
    }


    @Test
    public void testConnectionThrottleWhenCreatingSslTermination() throws Exception {
        verifyConnectionThrottle();
    }


    @Test
    public void shouldPassIfCertificateIsRemovedWithSecureVSStillThere() throws Exception {
        setSslTermination();
        updateSslTermination();
        deleteCertificate();
    }


    @Test
    public void verifyHostHeaderRewriteIsNever() {
        verifyHostHeaderRewrite();
    }


    private void setSslTermination(int port, boolean isSslTermEnabled, boolean allowSecureTrafficOnly) {
        try {
            boolean isVsEnabled = true;
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(allowSecureTrafficOnly);
            sslTermination.setEnabled(isSslTermEnabled);
            sslTermination.setSecurePort(port);
            sslTermination.setCertificate(testCert);
            sslTermination.setPrivatekey(testKey);

            ZeusCertFile zeusCertFile = new ZeusCertFile();
            zeusCertFile.setPublic_cert(testCert);
            zeusCertFile.setPrivate_key(testKey);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testCert);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());
            VirtualServer createdSecureVs = null;
            VirtualServer createdNormalVs = null;
            try {
                stmAdapter.updateSslTermination(config, lb, zeusSslTermination);
                createdSecureVs = stmClient.getVirtualServer(secureName);
                createdNormalVs = stmClient.getVirtualServer(normalName);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            Assert.assertNotNull(createdSecureVs);
            Assert.assertNotNull(createdNormalVs);
            VirtualServerBasic secureBasic = createdSecureVs.getProperties().getBasic();
            Assert.assertEquals(port, (int) secureBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(secureBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, secureBasic.getEnabled());
            Assert.assertEquals(secureName, secureBasic.getPool().toString());
            Assert.assertEquals(isSslTermEnabled, secureBasic.getSsl_decrypt());

            VirtualServerBasic normalBasic = createdNormalVs.getProperties().getBasic();
            Assert.assertEquals(port, (int) normalBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(normalBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, normalBasic.getEnabled());
            Assert.assertEquals(secureName, normalBasic.getPool().toString());



            Assert.assertEquals(testCert, createdSecureVs.getProperties().getSsl().getServer_cert_default());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void setSslTermination() {
        int port = 443;
        boolean isSslTermEnabled = true;
        boolean allowSecureTrafficOnly = false;
        setSslTermination(port, isSslTermEnabled, allowSecureTrafficOnly);
    }

    private void updateSslTermination() {
        int port = 500;
        boolean isSslTermEnabled = false;
        boolean allowSecureTrafficOnly = true;
        setSslTermination(port, isSslTermEnabled, allowSecureTrafficOnly);
    }

    private void deleteSslTermination() {
        try {
            VirtualServer createdSecureVs = stmClient.getVirtualServer(secureName);
            VirtualServer createdNormalVs = stmClient.getVirtualServer(normalName);
            stmAdapter.removeSslTermination(config, lb);
            Assert.assertFalse(stmClient.getVirtualServers().contains(createdSecureVs));
            Assert.assertTrue(stmClient.getVirtualServers().contains(createdNormalVs));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }


    }

    private void updateLoadBalancerAttributes() {

        try {
            //Should us updateSslTermination
            int securePort = 8080;
            int normalPort = 443;
            boolean isConnectionLogging = true;
            String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
            String normalVsName = ZxtmNameBuilder.genVSName(lb);
            stmAdapter.updateSslTermination(config, lb, new ZeusSslTermination());
            VirtualServer createdSecureVs = stmClient.getVirtualServer(secureVsName);
            Assert.assertEquals(securePort, (int) createdSecureVs.getProperties().getBasic().getPort());
            VirtualServer createdNormalVs = stmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(normalPort, (int) createdNormalVs.getProperties().getBasic().getPort());

            lb.setConnectionLogging(isConnectionLogging);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            createdSecureVs = stmClient.getVirtualServer(secureVsName);
            createdNormalVs = stmClient.getVirtualServer(normalVsName);
            Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
            Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());

            isConnectionLogging = false;

            Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
            Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }


    private void verifyHostHeaderRewrite() {
        try {
            int port = 443;
            boolean allowSecureTrafficOnly = false;
            boolean isSslTermEnabled = true;
            setSslTermination(port, isSslTermEnabled, allowSecureTrafficOnly);
            VirtualServer createdVs = null;

            createdVs = stmClient.getVirtualServer(ZxtmNameBuilder.genSslVSName(lb));


            Assert.assertEquals(EnumFactory.Accept_from.NEVER.toString(),
                    createdVs.getProperties().getHttp().getLocation_rewrite().toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void verifyErrorPage() {
        try {
            String errorContent = "HI";
            lb.getUserPages().setErrorpage(errorContent);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            errorPageHelper(errorContent);
            lb.getUserPages().setErrorpage(null);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            errorPageHelper("Default");
            lb.getUserPages().setErrorpage(errorContent);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            errorPageHelper(errorContent);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }

    private void verifyDeleteErrorPage() {
        String errorContent = "HI";
        try {
            lb.getUserPages().setErrorpage(errorContent);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            errorPageHelper(errorContent);
            lb.getUserPages().setErrorpage(null);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            errorPageHelper("Default");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }


    }

    private void errorPageHelper(String expectedContent) {
        try {
            String secureErrorFileName = stmClient.getVirtualServer(secureName).getProperties().getConnection_errors().getError_file();
            String normalErrorFileName = stmClient.getVirtualServer(normalName).getProperties().getConnection_errors().getError_file();
            Assert.assertEquals(secureName + "_error.html", secureErrorFileName);
            Assert.assertEquals(normalName + "_error.html", normalErrorFileName);

            File secureFile = stmClient.getExtraFile(secureErrorFileName);
            File normalFile = stmClient.getExtraFile(normalErrorFileName);
            BufferedReader secureReader = new BufferedReader(new FileReader(secureFile));
            BufferedReader normalReader = new BufferedReader(new FileReader(normalFile));
            Assert.assertEquals(secureReader.readLine(), expectedContent);
            Assert.assertEquals(normalReader.readLine(), expectedContent);

            secureReader.close();
            normalReader.close();
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
            int expectedMax10 = 0;

            throttle.setMaxConnectionRate(maxConnectionRate);
            throttle.setMaxConnections(maxConnections);
            throttle.setMinConnections(minConnections);
            throttle.setRateInterval(rateInterval);

            lb.setConnectionLimit(throttle);


            setSslTermination();
            connectionThrottleHelper(secureName, maxConnectionRate, maxConnections, minConnections, rateInterval, expectedMax10);
            connectionThrottleHelper(normalName, maxConnectionRate, maxConnections, minConnections, rateInterval, expectedMax10);


            stmAdapter.deleteProtection(config, lb);


            int deletedRate = 0;
            connectionThrottleHelper(normalName, deletedRate, deletedRate, deletedRate, deletedRate, deletedRate);
            connectionThrottleHelper(secureName, deletedRate, deletedRate, deletedRate, deletedRate, deletedRate);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }

    }

    private void connectionThrottleHelper(String vsName, int maxConnectionRate, int maxConnections,
                                          int minConnections, int rateInterval, int expectedMax10) {
        try {
            stmAdapter.updateProtection(config, lb);


            Protection protection = null;
            protection = stmClient.getProtection(vsName);

            Assert.assertNotNull(protection);
            ProtectionConnectionLimiting createdThrottle = protection.getProperties().getConnection_limiting();
            Assert.assertEquals(maxConnectionRate, (int) createdThrottle.getMax_connection_rate());
            Assert.assertEquals(expectedMax10, (int) createdThrottle.getMax_10_connections());
            Assert.assertEquals(maxConnections, (int) createdThrottle.getMax_1_connections());
            Assert.assertEquals(rateInterval, (int) createdThrottle.getRate_timer());
            Assert.assertEquals(minConnections, (int) createdThrottle.getMin_connections());
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
            stmAdapter.setRateLimit(config, lb, rateLimit);


            Bandwidth createdNormalBandwidth = stmClient.getBandwidth(normalName);
            Assert.assertNotNull(createdNormalBandwidth);
            Assert.assertEquals(maxRequestsPerSecond, (int) createdNormalBandwidth.getProperties().getBasic().getMaximum());
            Assert.assertEquals(ticketComment, createdNormalBandwidth.getProperties().getBasic().getNote());


            Bandwidth createdSecureBandwidth = stmClient.getBandwidth(secureName);
            Assert.assertNotNull(createdSecureBandwidth);
            Assert.assertEquals(maxRequestsPerSecond, (int) createdSecureBandwidth.getProperties().getBasic().getMaximum());
            Assert.assertEquals(ticketComment, createdSecureBandwidth.getProperties().getBasic().getNote());
            VirtualServer createdServer = stmClient.getVirtualServer(normalName);
            Assert.assertTrue(createdServer.getProperties().getBasic().getResponse_rules().contains(StmConstants.XFF.toString()) ||
                    createdServer.getProperties().getBasic().getResponse_rules().contains(StmConstants.XFP.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }


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
            Protection secureProtection = stmClient.getProtection(secureName);

            Assert.assertTrue(normalProtection.getProperties().getAccess_restriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(normalProtection.getProperties().getAccess_restriction().getAllowed().contains(ipAddressTwo));


            Assert.assertTrue(secureProtection.getProperties().getAccess_restriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(secureProtection.getProperties().getAccess_restriction().getAllowed().contains(ipAddressTwo));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            removeSimpleLoadBalancer();
        }
    }


    private void verifyDeleteAccessList() {
        try {
            verifyAccessListWithSsl();
            stmAdapter.deleteAccessList(config, lb);
            Protection normalProtection = stmClient.getProtection(normalName);
            Protection secureProtection = stmClient.getProtection(secureName);
            Assert.assertTrue(normalProtection.getProperties().getAccess_restriction().getAllowed().isEmpty());
            Assert.assertTrue(normalProtection.getProperties().getAccess_restriction().getBanned().isEmpty());

            Assert.assertTrue(secureProtection.getProperties().getAccess_restriction().getBanned().isEmpty());
            Assert.assertTrue(secureProtection.getProperties().getAccess_restriction().getAllowed().isEmpty());
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
            Assert.assertTrue(normalProtection.getProperties().getAccess_restriction().getBanned().contains(ipAddressOne));
            Assert.assertTrue(normalProtection.getProperties().getAccess_restriction().getAllowed().contains(ipAddressTwo));
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
        stmAdapter.deleteRateLimit(config, lb);
        Bandwidth createdNormalBandwidth = stmClient.getBandwidth(normalName);
        Bandwidth createdSecureBandwidth = stmClient.getBandwidth(secureName);
    }

    private void deleteCertificate() {

    }


}
