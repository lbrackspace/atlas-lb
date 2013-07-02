package org.openstack.atlas.adapter.itest;


import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;

import java.io.File;
import java.rmi.RemoteException;

public class SslTerminationITest extends STMTestBase {


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
    public void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @After
    public void tearDownClass() {
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RollBackException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Ignore
    @Test
    public void testSslTerminationOperations() {


    }

    @Test
    public void testSslTerminationOperationsWhenUpdatingLBAttributes() throws Exception {


    }

    @Test
    public void testWhenAddingRateLimitWithSslTermination() throws Exception {

    }

    @Test
    public void testWhenAddingAccessListWith() throws Exception {

    }

    @Test
    public void testErrorPageWhenCreatingSslTermination() throws Exception {

    }

    @Test
    public void testConnectionThrottleWhenCreatingSslTermination() throws Exception {

    }

    @Test
    public void shouldPassifCertificateIsRemovedWithSecureVSStillThere() throws Exception {

    }

    @Test
    public void verifyHostHeaderRewriteIsNever() throws Exception {

    }

    private void setSslTermination(int port, boolean isSslTermEnabled, boolean allowSecureTrafficOnly) {
        String secureVs = null;
        String normalVs = null;
        boolean isVsEnabled = true;
        try {
            secureVs = ZxtmNameBuilder.genSslVSName(lb);
            normalVs = ZxtmNameBuilder.genVSName(lb);
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

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
            createdSecureVs = stmClient.getVirtualServer(secureVs);
            createdNormalVs = stmClient.getVirtualServer(normalVs);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Assert.assertNotNull(createdSecureVs);
        Assert.assertNotNull(createdNormalVs);
        VirtualServerBasic createdBasic = createdSecureVs.getProperties().getBasic();
        Assert.assertEquals(port, (int) createdSecureVs.getProperties().getBasic().getPort());
        Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(createdBasic.getProtocol().toString()));
        Assert.assertEquals(isVsEnabled, createdBasic.getEnabled());
        Assert.assertEquals(secureVs, createdBasic.getPool().toString());
        Assert.assertEquals(isSslTermEnabled, createdBasic.getSsl_decrypt());
        Assert.assertEquals(testCert, createdSecureVs.getProperties().getSsl().getServer_cert_default());


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
        String secureVs = null;
        String normalVs = null;
        try {
            secureVs = ZxtmNameBuilder.genSslVSName(lb);
            normalVs = ZxtmNameBuilder.genVSName(lb);
            VirtualServer createdSecureVs = stmClient.getVirtualServer(secureVs);
            VirtualServer createdNormalVs = stmClient.getVirtualServer(normalVs);
            stmAdapter.removeSslTermination(config, lb);
            Assert.assertFalse(stmClient.getVirtualServers().contains(createdSecureVs));
            Assert.assertTrue(stmClient.getVirtualServers().contains(normalVs));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private void updateLoadBalancerAttributes() throws Exception {

        int securePort = 8080;
        int normalPort = 443;
        boolean isConnectionLogging = true;
        String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
        String normalVsName = ZxtmNameBuilder.genVSName(lb);
        stmAdapter.updatePort(config, lb.getId(), lb.getAccountId(), securePort);
        VirtualServer createdSecureVs = stmClient.getVirtualServer(secureVsName);
        Assert.assertEquals(securePort, (int) createdSecureVs.getProperties().getBasic().getPort());
        VirtualServer createdNormalVs = stmClient.getVirtualServer(normalVsName);
        Assert.assertEquals(normalPort, (int) createdNormalVs.getProperties().getBasic().getPort());

        lb.setConnectionLogging(isConnectionLogging);
        stmAdapter.updateConnectionLogging(config, lb);
        createdSecureVs = stmClient.getVirtualServer(secureVsName);
        createdNormalVs = stmClient.getVirtualServer(normalVsName);
        Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
        Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());

        isConnectionLogging = false;

        Assert.assertEquals(isConnectionLogging, createdSecureVs.getProperties().getLog().getEnabled());
        Assert.assertEquals(isConnectionLogging, createdNormalVs.getProperties().getLog().getEnabled());


    }


    private void verifyHostHeaderRewrite() {
        int port = 443;
        boolean allowSecureTrafficOnly = false;
        boolean isSslTermEnabled = true;
        setSslTermination(port, isSslTermEnabled, allowSecureTrafficOnly);
        VirtualServer createdVs = null;
        try {
            createdVs = stmClient.getVirtualServer(ZxtmNameBuilder.genSslVSName(lb));
        } catch (StingrayRestClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (StingrayRestClientObjectNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //TODO that val should be in an enum somewhere...
        Assert.assertEquals("never", createdVs.getProperties().getHttp().getLocation_rewrite());


    }

    private void verifyErrorPage() throws Exception {
        String errorContent = "<html><body>ErrorFileContents</body></html>";
        String secureVsName = ZxtmNameBuilder.genSslVSName(lb);
        String errorFileName = stmClient.getVirtualServer(secureVsName).getProperties().getConnection_errors().getError_file();
        stmAdapter.setErrorFile(config, lb, errorContent);
        File file = stmClient.getExtraFile(errorFileName);
//        Assert.assertEquals(loadBalancerName() + "_error.html", );


    }


}
