package org.openstack.atlas.service.domain.entities;


import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by namrata on 30/6/17.
 */
@RunWith(Enclosed.class)
public class SslCipherProfileTest{

    private static Logger logger = Logger.getLogger(SslCipherProfileTest.class.getName());

   @RunWith(Enclosed.class)
   @ContextConfiguration(locations = {"classpath:context.xml"})
   @Transactional
   public static class WhenCreatingLoadBalancer {

       private EntityManagerFactory emFactory;
       private EntityManager em;
       private Connection connection;

       @Before
       public void setUp() throws Exception {
           try {
               Class.forName("org.hsqldb.jdbcDriver");
               connection = DriverManager.getConnection("jdbc:hsqldb:mem:unit-testing-jpa", "sa", "");
           } catch (Exception ex) {
               ex.printStackTrace();
           }
           try {
               emFactory = Persistence.createEntityManagerFactory("loadbalancing");
               em = emFactory.createEntityManager();
           } catch (Exception ex) {
               ex.printStackTrace();
           }
       }

     @After
       public void tearDown() throws Exception {
           if (em != null) {
               em.close();
           }
           if (emFactory != null) {
               emFactory.close();
           }
           try {
               connection.createStatement().execute("SHUTDOWN");
           } catch (Exception ex) {
           }
       }
     @Test
       public void testPersistence() {
           try {
               em.getTransaction().begin();
               logger.trace("transaction started");
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

               SslCipherProfile cipherProfile = new SslCipherProfile();
               cipherProfile.setCiphers("1,2,3,4");
               cipherProfile.setName("testCipher");
               cipherProfile.setComments("This is a test comment");
               em.persist(cipherProfile);
               em.flush();

               SslTermination sslTermination= new SslTermination();
               sslTermination.setPrivatekey(testKey);
               sslTermination.setCertificate(testCert);
               sslTermination.setIntermediateCertificate("\n\n-----BEGIN CERTIFICATE-----\n" +
                       "MIIERzCCAy+gAwIBAgIBAjANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJVUzEO\n" +
                       "MAwGA1UECBMFVGV4YXMxDjAMBgNVBAcTBVRleGFzMRowGAYDVQQKExFSYWNrU3Bh\n" +
                       "dZsGmy48UFF4pBHdhnE8bCAt8KgK3BJb0XqNrUxxI6Jc/Hcl9AfppFIEGw==\n" +
                       "-----END CERTIFICATE-----\n        \n");
               sslTermination.setSecurePort(443);
               sslTermination.setSecureTrafficOnly(false);
               sslTermination.setEnabled(true);
               sslTermination.setTls10Enabled(false);
               sslTermination.setCipherProfile(cipherProfile);
               sslTermination.setCipherList("1,2,3,4");

               em.persist(sslTermination);
               em.getTransaction().commit();
           } catch (Exception ex) {
               ex.printStackTrace();
               em.getTransaction().rollback();
           }
       }
   }
}