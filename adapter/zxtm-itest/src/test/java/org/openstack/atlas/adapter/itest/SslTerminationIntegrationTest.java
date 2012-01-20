package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.Certificate;
import com.zxtm.service.client.VirtualServerBasicInfo;
import com.zxtm.service.client.VirtualServerProtocol;
import com.zxtm.service.client.VirtualServerRule;
import org.apache.axis.types.UnsignedInt;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.service.domain.entities.RateLimit;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;

import java.util.Calendar;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.HTTPS;

public class SslTerminationIntegrationTest extends ZeusTestBase {
    //TODO: robustoize it...

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

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        setupSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        removeSimpleLoadBalancer();
    }

    @Test
    public void testSSlTerminationOperations() {
        setSslTermination();
        updateSslTermination();
        deleteSslTermination();
    }

    private void setSslTermination() {
        String sVs = null;

        try {
            sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(443);
            sslTermination.setCertificate(testCert);
            sslTermination.setPrivatekey(testKey);

            ZeusCertFile zeusCertFile = new ZeusCertFile();
            zeusCertFile.setPublic_cert(testCert);
            zeusCertFile.setPrivate_key(testKey);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testCert);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());

            zxtmAdapter.updateSslTermination(config, lb, zeusSslTermination);

            //Check to see if VS was created
            String[] virtualServers = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
            boolean doesExist = false;
            for (String vsName : virtualServers) {
                if (vsName.equals(sVs)) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist);

            String[] certificate = getServiceStubs().getVirtualServerBinding().getSSLCertificate(new String[]{sVs});
            Assert.assertEquals(sVs, certificate[0]);

            final VirtualServerBasicInfo[] serverBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{sVs});
            Assert.assertEquals(sslTermination.getSecurePort(), serverBasicInfos[0].getPort());
            Assert.assertEquals(true, lb.getProtocol().toString().equalsIgnoreCase(serverBasicInfos[0].getProtocol().toString()));
            Assert.assertEquals(ZxtmNameBuilder.genVSName(lb), serverBasicInfos[0].getDefault_pool());

            boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{ZxtmNameBuilder.genVSName(lb)});
            Assert.assertEquals(true, vsEnabled[0]);

            boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
            Assert.assertEquals(sslTermination.isEnabled(), vsNonSecureEnabled[0]);

            String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
            Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void updateSslTermination() {
        String sVs = null;

        try {
            sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(true);
            sslTermination.setEnabled(false);
            sslTermination.setSecurePort(500);
            sslTermination.setCertificate(testCert);
            sslTermination.setPrivatekey(testKey);

            ZeusCertFile zeusCertFile = new ZeusCertFile();
            zeusCertFile.setPublic_cert(testCert);
            zeusCertFile.setPrivate_key(testKey);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(testCert);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());

            zxtmAdapter.updateSslTermination(config, lb, zeusSslTermination);

            //Check to see if VS is still here
            String[] virtualServers = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
            boolean doesExist = false;
            for (String vsName : virtualServers) {
                if (vsName.equals(sVs)) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist);

            String[] certificate = getServiceStubs().getVirtualServerBinding().getSSLCertificate(new String[]{sVs});
            Assert.assertEquals(sVs, certificate[0]);

            final VirtualServerBasicInfo[] serverBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{sVs});
            Assert.assertEquals(sslTermination.getSecurePort(), serverBasicInfos[0].getPort());
            Assert.assertEquals(true, lb.getProtocol().toString().equalsIgnoreCase(serverBasicInfos[0].getProtocol().toString()));
            Assert.assertEquals(ZxtmNameBuilder.genVSName(lb), serverBasicInfos[0].getDefault_pool());

            boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{ZxtmNameBuilder.genVSName(lb)});
            Assert.assertEquals(false, vsEnabled[0]);

             boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
            Assert.assertEquals(sslTermination.isEnabled(), vsNonSecureEnabled[0]);

            String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
            Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private void deleteSslTermination() {
        String sVs = null;

        try {
            sVs = ZxtmNameBuilder.genSslVSName(lb.getId(), lb.getAccountId());
        } catch (InsufficientRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            zxtmAdapter.removeSslTermination(config, lb);
            //Check to see if VS is gone
            String[] virtualServers = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
            boolean doesExist = false;
            for (String vsName : virtualServers) {
                if (vsName.equals(sVs)) {
                    doesExist = true;
                    break;
                }
            }
            Assert.assertFalse(doesExist);

            //Check to see if original VS is still here
            String[] virtualServers2 = getServiceStubs().getVirtualServerBinding().getVirtualServerNames();
            boolean doesExist1 = false;
            for (String vsName : virtualServers) {
                if (vsName.equals(ZxtmNameBuilder.genVSName(lb))) {
                    doesExist1 = true;
                    break;
                }
            }
            Assert.assertTrue(doesExist1);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
