package org.openstack.atlas.api.resources;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.operations.OperationResponse;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class SslTerminationResourceTest {

    public static class createSsl {

        private AsyncService esbService;
        private SslTerminationResource sslTermResource;
        private OperationResponse operationResponse;
        private SslTermination sslTermination;

        @Before
        public void setUp() {
            sslTermResource = new SslTerminationResource();
            esbService = mock(AsyncService.class);
            sslTermResource.setAsyncService(esbService);
            sslTermResource.setId(12);
            sslTermResource.setAccountId(31337);
            sslTermResource.setLoadBalancerId(32);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Ignore
        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);
            Response resp = sslTermResource.createSsl(null);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Ignore
        @Test
        public void shouldReturn200WhenEsbIsNormal() throws Exception {
            Response resp = sslTermResource.createSsl(null);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Ignore
        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {
            Response resp = sslTermResource.createSsl(null);
            Assert.assertEquals(500, resp.getStatus());
        }

//        @Test
//        public void shouldAcceptJsonRequest() throws JSONException {
//            JSONObject obj = new JSONObject();
//            JSONObject codes = new JSONObject();
//            //Forms the name value pairs for the barcodes and quantity
//
//            //Headers in the JSON array
//            obj.put("certificate", "-----BEGIN CERTIFICATE-----\n" +
//                    "MIIEWjCCA0KgAwIBAgIGATTTGu/tMA0GCSqGSIb3DQEBBQUAMHkxCzAJBgNVBAYT\n" +
//                    "AlVTMQ4wDAYDVQQIEwVUZXhhczEOMAwGA1UEBxMFVGV4YXMxGjAYBgNVBAoTEVJh\n" +
//                    "Y2tTcGFjZSBIb3N0aW5nMRQwEgYDVQQLEwtSYWNrRXhwIENBNTEYMBYGA1UEAxMP\n" +
//                    "Y2E1LnJhY2tleHAub3JnMB4XDTEyMDExMjE4MDgwNVoXDTM5MDUzMDE4MDgwNVow\n" +
//                    "gZcxCzAJBgNVBAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEUMBIGA1UEBxMLU2FuIEFu\n" +
//                    "dG9uaW8xEDAOBgNVBAoTB1JhY2tFeHAxEDAOBgNVBAsTB1JhY2tEZXYxPjA8BgNV\n" +
//                    "BAMMNW15c2l0ZS5jb20vZW1haWxBZGRyZXNzPXBoaWxsaXAudG9vaGlsbEByYWNr\n" +
//                    "c3BhY2UuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqSXePu8q\n" +
//                    "LmniU7jNxoWq3SLkR8txMsl1gFYftpq7NIFaGfzVf4ZswYdEYDVWWRepQjS0TvsB\n" +
//                    "0d5+usEUy/pcdZAlQLnn+540iLkvxKPVMzojUbG6yOAmjC/xAZuExJHtfCrRHUQ4\n" +
//                    "WQCwqyqANfP81y1inAb0zJGbtWUreV+nv8Ue77qX77fOuqI6zOHinGZU7l25XGLc\n" +
//                    "VUphgt8UtHZBzz2ahoftZ97DhUyQiSJQCaHXJd3QeIHAq9qc7hu+usiYZWz34A0l\n" +
//                    "w/gAl+RYcdvVc8kIwWxpiSieqqBPOwNzN5B0+9uu5sDzMGMFnnSWcNKIPumX0rke\n" +
//                    "3xFUl3UD6GJwvwIDAQABo4HIMIHFMIGjBgNVHSMEgZswgZiAFIkXQizRaftxVDaL\n" +
//                    "P/Fb/F2ht017oX2kezB5MQswCQYDVQQGEwJVUzEOMAwGA1UECBMFVGV4YXMxDjAM\n" +
//                    "BgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3BhY2UgSG9zdGluZzEUMBIGA1UE\n" +
//                    "CxMLUmFja0V4cCBDQTQxGDAWBgNVBAMTD2NhNC5yYWNrZXhwLm9yZ4IBAjAdBgNV\n" +
//                    "HQ4EFgQUQUXHjce1JhjJDA4nhYcbebMrIGYwDQYJKoZIhvcNAQEFBQADggEBACLe\n" +
//                    "vxcDSx91uQoc1uancb+vfkaNpvfAxOkUtrdRSHGXxvUkf/EJpIyG/M0jt5CLmEpE\n" +
//                    "UedeCFlRN+Qnsqt589ZemWWJwth/Jbu0wQodfSo1cP0J2GFZDyTd5cWgm0IxD8A/\n" +
//                    "ZRGzNnTx3xskv6/lOh7so9ULppEbOsZTNqQ4ahbxbiaR2iDTQGF3XKSHha8O93RB\n" +
//                    "YlnFahKZ2j0CpYvg0lJjfN0Lvj7Sm6GBA74n2OrGuB14H27wklD+PtIEFniyxKbq\n" +
//                    "5TDO0l4yDgkR7PsckmZqK22GP9c3fQkmXodtpV1wRjcSAxxVWYm+S24XvMFERs3j\n" +
//                    "yXEf+VJ0H+voAvxgbAk=\n" +
//                    "-----END CERTIFICATE-----");
//            obj.put("enabled", true);
//            obj.put("secureTrafficOnly", false);
//            obj.put("privatekey", "-----BEGIN RSA PRIVATE KEY-----\n" +
//                    "MIIEpAIBAAKCAQEAqSXePu8qLmniU7jNxoWq3SLkR8txMsl1gFYftpq7NIFaGfzV\n" +
//                    "f4ZswYdEYDVWWRepQjS0TvsB0d5+usEUy/pcdZAlQLnn+540iLkvxKPVMzojUbG6\n" +
//                    "yOAmjC/xAZuExJHtfCrRHUQ4WQCwqyqANfP81y1inAb0zJGbtWUreV+nv8Ue77qX\n" +
//                    "77fOuqI6zOHinGZU7l25XGLcVUphgt8UtHZBzz2ahoftZ97DhUyQiSJQCaHXJd3Q\n" +
//                    "eIHAq9qc7hu+usiYZWz34A0lw/gAl+RYcdvVc8kIwWxpiSieqqBPOwNzN5B0+9uu\n" +
//                    "5sDzMGMFnnSWcNKIPumX0rke3xFUl3UD6GJwvwIDAQABAoIBABQ7alT+yH3avm6j\n" +
//                    "OUHYtTJUPRf1VqnrfPmH061E3sWN/1gCbQse6h1P77bOSnDHqsA3i6Wy0mnnAiOW\n" +
//                    "esVXQf3x6vLOCdiH+OKtu+/6ZMMG3jikWKI0ZYf5KAu4LW5RwiVK/c5RXagPtBIV\n" +
//                    "OFa7w299h0EAeAGMHSLaYhPXhDokyJa6yDkAQL3n+9L3V8kNWeCELfrqXnXF4X0K\n" +
//                    "CJp622tS/fW6kzppJyLJ4GPkK9HNMpu02/n2Z7swWypfF+7set+9/aNTooDYWzCu\n" +
//                    "dbnRgqEIG1IP8+t6HG6x9VujJVJLIW/WLITnQ/WTRXOQHBGhazgmwe1GPdxsQgXu\n" +
//                    "/wIcsIkCgYEA8Si0q+QhmJyoAm8vTHjo6+DD06YYTvSODLJOpOqr1ncGGDJ/evBw\n" +
//                    "x+9QsK3veXMbAK5G7Xss32IuXbBfjqQ89+/q/YT4BnS3T0OQa2WlR8tURNphCDr5\n" +
//                    "B3yD212kJTTehC+p7BI9zhnWXD9kImh4vm4XcOsC9iqOSCZkGfvRPRsCgYEAs46t\n" +
//                    "Y85v2Pk235r1BPbgKwqYR+jElH4VWKu+EguUeQ4BlS47KktlLhvHtwrTv/UZ+lPx\n" +
//                    "8gSJTgyy7iEmzcGwPf1/MI5xg+DPgGhbr2G8EvrThmdHy+rPF2YSp1iBmJ4xq/1r\n" +
//                    "6XYKvf6ST3iujxTPU5xPEDUSLsH2ejJD/ddqSS0CgYEAkIdxyDa//8ObWWIjObSY\n" +
//                    "+4zIMBcyKFeernNKeMH/3FeW+neBOT/Sh7CgblK/28ylWUIZVghlOzePTC0BB+7c\n" +
//                    "b0eFUQ0YzF204rc+XW8coCt2xJEQaCtXxinUqGq1jmriFNyv/MBt9BA+DSkcrRZp\n" +
//                    "js9SEyV1r+yPOyRvB7eIjhMCgYEAkd5yG+fkU1c6bfNb4/mPaUgFKD4AHUZEnzF+\n" +
//                    "ivhfWOy4+nGBXT285/VnjNs95O8AeK3jmyJ2TTLh1bSW6obUX7flsRO3QlTLHd0p\n" +
//                    "xtPWT3D3kHOtDwslzDN/KfYr6klxvvB0z0e3OFxsjiVTYiecuqb8UAVdTSED1Ier\n" +
//                    "Vre+v80CgYB86OqcAlR3diNaIwHgwK5kP2NAH1DaSwZXoobYpdkjsUQfJN5jwJbD\n" +
//                    "4/6HVydoc5xe0z8B+O1VUzC+QA0gdXgHbmLZBIUeQU8sE4hGELoe/eWULXGwI91M\n" +
//                    "FyEWg03jZj8FkFh2954zwU6BOcbeL+9GrTdTPu1vuHoTitmNEye4iw==\n" +
//                    "-----END RSA PRIVATE KEY-----");
//            obj.put("securePort", 443);
//            System.out.print(obj);
//
//            //httppost
//            try {
//                DefaultHttpClient httpclient = new DefaultHttpClient();
//                HttpResponse response;
//                HttpPut httppost = new HttpPut("http://127.0.0.1:8080/man/406271/loadbalancers/12/ssltermination");
//                StringEntity se = new StringEntity(obj.toString());
//                httppost.setEntity(se);
//                System.out.println(se);
//                httppost.setHeader("Accept", "application/json");
//                httppost.setHeader("Content-type", "application/json");
//                httppost.setHeader("bypass-auth", "true");
//
//                response = httpclient.execute(httppost);
//                System.out.println(response);
//                Assert.assertEquals(202, response.getStatusLine().getStatusCode());
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.out.print("Cannot establish connection!");
//            }
//        }
    }
}

