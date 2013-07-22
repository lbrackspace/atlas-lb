package org.rackspace.capman.tools.util;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.rackspace.capman.tools.ca.exceptions.PrivKeyDecodeException;
import org.rackspace.capman.tools.ca.exceptions.RsaException;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.util.PrivKeyReaderTest;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.rackspace.capman.tools.ca.CertUtils;
import org.rackspace.capman.tools.ca.exceptions.CapManUtilException;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderDecodeException;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderNoSuchExtensionException;

public class X509InspectorTest {

    public static final BigInteger caMod = new BigInteger("92519081613557335824146312114331099693596712334582852764163758222004945339138079274569160547038643263700275407813235096605105941298720472784518757878492358916313599921202611417629889327386843437793899506223684027421621426615502802714869767807708103437254423295282076493912484979373100387883795899165798220483");
    public static final String caSubjId = "a72ad48c3632e4f3f0381b48474abe7126530dd8";
    public static final BigInteger testMod = new BigInteger("92769520113997379614084710552346480125314109559245447551856686347298813937630358936531128096799589632829885895580345157680107273222950724084624729121336709416739678416576244399370216587550882915851315377147000246928233006530724331354867197205185517189113923758771082160421912659909039341373309420290932945177");
    public static final String testCrtSubjKeyId = "607cc6865330a280efdf3d1ca865e79acd745b55";
    public static final String pkcs8CrtPem = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDOzCCAqSgAwIBAgIGATbnee+RMA0GCSqGSIb3DQEBBQUAMHYxEDAOBgNVBAMT\n"
            + "B1Rlc3QgQ0ExGzAZBgNVBAsTElJhY2tzcGFjZSBQbGF0Zm9ybTESMBAGA1UEChMJ\n"
            + "UmFja3NwYWNlMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4YXMx\n"
            + "CzAJBgNVBAYTAlVTMB4XDTEyMDQyNTAzMDk0OVoXDTE2MDIyNDAzMDk0OVowdjEL\n"
            + "MAkGA1UEBhMCVVMxDjAMBgNVBAgTBVRleGFzMRQwEgYDVQQHEwtTYW4gQW50b25p\n"
            + "bzERMA8GA1UEChMIVGVzdCBPcmcxFjAUBgNVBAsTDVRlc3QgT3JnIFVuaXQxFjAU\n"
            + "BgNVBAMTDXd3dy5wa2NzOC5vcmcwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGB\n"
            + "AI3xVCC2rx2KTTyutHLEKpc5/wkU8hkJ1lSqbKoJsuC5fw24Nzw/OmJDSJBHF3Pf\n"
            + "7Z6mVWy+BKXHhiJ8mgkJ9CY97FxrUy/hkPuBwkhcmmPlKO9JOmfP/2MSXLoawsmo\n"
            + "yaQ7jRs2+euoD1bj/9yM2KIWMIGphEtIN99Kr8LZX1WlAgMBAAGjgdMwgdAwDAYD\n"
            + "VR0TAQH/BAIwADCBoAYDVR0jBIGYMIGVgBSnKtSMNjLk8/A4G0hHSr5xJlMN2KF6\n"
            + "pHgwdjEQMA4GA1UEAxMHVGVzdCBDQTEbMBkGA1UECxMSUmFja3NwYWNlIFBsYXRm\n"
            + "b3JtMRIwEAYDVQQKEwlSYWNrc3BhY2UxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4w\n"
            + "DAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVOCAQEwHQYDVR0OBBYEFHNn39xsWJQk\n"
            + "cVREN2P71uI/7O3CMA0GCSqGSIb3DQEBBQUAA4GBAHFYGY+kHS6IwOnq/1oNgqwE\n"
            + "lPa5pYVMmAB8WwqIUzOUMmhwAKeG8p8YYZ6VlvSvuHDjdhgwIy1oA1q21b2Hx7Ka\n"
            + "hjBuRVPLC5kwhlcij7ZLTJ9xSj3M7Bmn8yWJIsr5UD2LEDylFsy4l7VvsXSvTUjm\n"
            + "heVVwQ4IvpsvboMQS4uY\n"
            + "-----END CERTIFICATE-----\n";
    public static final String caCrtPem = "-----BEGIN CERTIFICATE-----\n"
            + "MIIClDCCAf2gAwIBAgIBATANBgkqhkiG9w0BAQUFADB2MRAwDgYDVQQDEwdUZXN0\n"
            + "IENBMRswGQYDVQQLExJSYWNrc3BhY2UgUGxhdGZvcm0xEjAQBgNVBAoTCVJhY2tz\n"
            + "cGFjZTEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYD\n"
            + "VQQGEwJVUzAeFw0xMjA0MjQyMjA2MzhaFw0zOTA5MTAyMjA2MzhaMHYxEDAOBgNV\n"
            + "BAMTB1Rlc3QgQ0ExGzAZBgNVBAsTElJhY2tzcGFjZSBQbGF0Zm9ybTESMBAGA1UE\n"
            + "ChMJUmFja3NwYWNlMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4\n"
            + "YXMxCzAJBgNVBAYTAlVTMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDwGVJ\n"
            + "6fe+iXCVzCcGWc4XcNgoa3R/2YW9fsV8FKfan/6UfeDxYwb7MeDtHg7VFawLLUMr\n"
            + "StOEN2ZKcYsQxsMS65P4lrxrSMSU7F6HtVZMB+XVYe4UeUSVo03MN8t6l9a3/A0b\n"
            + "ga16/SdBqZ91P9BO0IAtxcIyk2m/cYKopDrCwwIDAQABozIwMDAPBgNVHRMBAf8E\n"
            + "BTADAQH/MB0GA1UdDgQWBBSnKtSMNjLk8/A4G0hHSr5xJlMN2DANBgkqhkiG9w0B\n"
            + "AQUFAAOBgQAIvAB4WsQzdg89XSYS5kZy1hC2u834MQG7aUegl93TzesdJI4/Iw6H\n"
            + "E/vlASKYy/hCX8ZKv1c9yXKc2hejJ7IUiKRzk5R8WS0yJ6VDlMNDmsDCpGfaEMj1\n"
            + "M08azGyscfqJooKQc57Q9fn22PVt/vVdOVr9rQEIBKrPMj3tZKu6dw==\n"
            + "-----END CERTIFICATE-----\n";
    public static final String testCrtPem = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDQzCCAqygAwIBAgIGATbmZcRlMA0GCSqGSIb3DQEBBQUAMHYxEDAOBgNVBAMT\n"
            + "B1Rlc3QgQ0ExGzAZBgNVBAsTElJhY2tzcGFjZSBQbGF0Zm9ybTESMBAGA1UEChMJ\n"
            + "UmFja3NwYWNlMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4YXMx\n"
            + "CzAJBgNVBAYTAlVTMB4XDTEyMDQyNDIyMDgxMFoXDTE2MDQyMzIyMDgxMFowezEL\n"
            + "MAkGA1UEBhMCVVMxDjAMBgNVBAgTBVRleGFzMRQwEgYDVQQHEwtTYW4gQW50b25p\n"
            + "bzESMBAGA1UEChMJUmFja3NwYWNlMRswGQYDVQQLExJSYWNrc3BhY2UgUGxhdGZv\n"
            + "cm0xFTATBgNVBAMTDHd3dy50ZXN0Lm9yZzCBnzANBgkqhkiG9w0BAQEFAAOBjQAw\n"
            + "gYkCgYEAhBuxzYbl9FSyiGDkGoIbAOhyUmeJ0QAn8cs10l9ysg5UWDPaRhYybaDI\n"
            + "NrrDRJynrH4Tpgl32HoVzMI99+E9/PGKLvi3HTxI9hSkRNJKaN3PFcYIwlS4Ex50\n"
            + "Ju+8LfzBBmwoYwJCa889oJOmMEOpyV+da48LyMd0a/rX2/4vMRkCAwEAAaOB1jCB\n"
            + "0zAPBgNVHRMBAf8EBTADAQH/MIGgBgNVHSMEgZgwgZWAFKcq1Iw2MuTz8DgbSEdK\n"
            + "vnEmUw3YoXqkeDB2MRAwDgYDVQQDEwdUZXN0IENBMRswGQYDVQQLExJSYWNrc3Bh\n"
            + "Y2UgUGxhdGZvcm0xEjAQBgNVBAoTCVJhY2tzcGFjZTEUMBIGA1UEBxMLU2FuIEFu\n"
            + "dG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVU4IBATAdBgNVHQ4EFgQU\n"
            + "YHzGhlMwooDv3z0cqGXnms10W1UwDQYJKoZIhvcNAQEFBQADgYEASQgzb+VgnENQ\n"
            + "gMPO4Otl9TSEMgaKbPG9zpt/lHrRQPpxiOc/v9Cu4Re3cIRZbI6NV9kAdAOu9I1N\n"
            + "kEnLnXeZT36K/uRHIbgBrzYRAE1ZNnYRcqnUKJjzBC5i+hIYAme1+TGC0D5bP3nK\n"
            + "HHt/1nKUQmnEVy+LhqdDCwDCNmGvPZI=\n"
            + "-----END CERTIFICATE-----\n";
    private static final String caSubj = "CN=TestCa,OU=someOrgUnit,O=SomeOrg"
            + ",L=San Antonio,ST=Texas,C=US";
    private X509Inspector caCrtReader;
    private X509Inspector testCrtReader;
    private X509Inspector pkcs8CrtReader;
    private PrivKeyReader caKeyReader;
    private PrivKeyReader testKeyReader;
    private PrivKeyReader key2048bitReader;

    public X509InspectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws X509ReaderDecodeException, NotAnX509CertificateException, PrivKeyDecodeException {
        RsaConst.init();
        caCrtReader = X509Inspector.newX509Inspector(caCrtPem);
        caKeyReader = PrivKeyReader.newPrivKeyReader(PrivKeyReaderTest.caKeyPem);

        testCrtReader = X509Inspector.newX509Inspector(testCrtPem);
        testKeyReader = PrivKeyReader.newPrivKeyReader(PrivKeyReaderTest.testKeyPem);

        pkcs8CrtReader = X509Inspector.newX509Inspector(pkcs8CrtPem);
        key2048bitReader = PrivKeyReader.newPrivKeyReader(PrivKeyReaderTest.key2048bit);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void caCrtShouldModMatchCaKey() {
        BigInteger crtMod = caCrtReader.getPubModulus();
        BigInteger keyMod = caKeyReader.getN();
        Assert.assertEquals(keyMod, crtMod);
        Assert.assertEquals(keyMod, caMod);
    }

    @Test
    public void keyShouldModMatchWithTestCrt() {
        BigInteger crtMod = testCrtReader.getPubModulus();
        BigInteger keyMod = testKeyReader.getN();
        Assert.assertEquals(crtMod, keyMod);
        Assert.assertEquals(crtMod, testMod);
    }

    @Test
    public void shouldGetCorrectCN() throws X509ReaderDecodeException {
        Assert.assertEquals("Test CA", caCrtReader.getSubjectCN());
        Assert.assertEquals("Test CA", caCrtReader.getIssuerCN()); // This caCrt was self signed. LOL

        Assert.assertEquals("www.test.org", testCrtReader.getSubjectCN());
        Assert.assertEquals("Test CA", caCrtReader.getIssuerCN());
        Assert.assertEquals("www.pkcs8.org", pkcs8CrtReader.getSubjectCN());
    }

    @Test
    public void shouldGetCorrectSerials() {
        Assert.assertEquals(BigInteger.ONE, caCrtReader.getSerial());
        Assert.assertEquals("136e665c465", testCrtReader.getSerial().toString(16));
    }

    @Test
    public void shouldGetCorrectKeyIds() throws X509ReaderNoSuchExtensionException, X509ReaderDecodeException {
        Assert.assertEquals(caCrtReader.getSubjKeyId(), caSubjId);
        Assert.assertEquals(testCrtSubjKeyId, testCrtReader.getSubjKeyId());
        Assert.assertEquals(testCrtReader.getAuthKeyId(), caCrtReader.getSubjKeyId()); // They should match since CA signed testCrt;
    }

    @Test
    public void shouldGetNullIfKeyIdDoesNotExist() {
        Assert.assertNull(caCrtReader.getAuthKeyId());
    }

    @Test
    public void shouldRecognizePrematureCert() throws CapManUtilException, InvalidKeySpecException {
        Assert.assertTrue(qXR(bef(2040, 1, 1), aft(2048, 1, 1)).isPremature(null));
        Assert.assertFalse(qXR(bef(2012, 5, 21), aft(2048, 1, 1)).isPremature(null));
        Assert.assertFalse(qXR(bef(2005, 1, 1), aft(2006, 1, 1)).isPremature(null));
        Assert.assertTrue(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isPremature(now(2011, 12, 31)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isPremature(now(2012, 1, 1)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isPremature(now(2012, 5, 28)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isPremature(now(2013, 1, 1)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isPremature(now(2013, 6, 28)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isPremature(now(2014, 6, 28)));
    }

    @Test
    public void shouldRecognizeExpiredCert() throws CapManUtilException, InvalidKeySpecException {
        Assert.assertFalse(qXR(bef(2040, 1, 1), aft(2048, 1, 1)).isExpired(null));
        Assert.assertFalse(qXR(bef(2012, 5, 21), aft(2048, 1, 1)).isExpired(null));
        Assert.assertTrue(qXR(bef(2005, 1, 1), aft(2006, 1, 1)).isExpired(null));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isExpired(now(2011, 12, 31)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isExpired(now(2012, 1, 1)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isExpired(now(2012, 5, 28)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isExpired(now(2013, 1, 1)));
        Assert.assertTrue(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isExpired(now(2013, 6, 28)));
        Assert.assertTrue(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isExpired(now(2014, 6, 28)));
    }

    @Test
    public void shouldRecognizeInRangeCerts() throws CapManUtilException, InvalidKeySpecException {
        Assert.assertFalse(qXR(bef(2040, 1, 1), aft(2048, 1, 1)).isDateValid(null));
        Assert.assertTrue(qXR(bef(2012, 5, 21), aft(2048, 1, 1)).isDateValid(null));
        Assert.assertFalse(qXR(bef(2005, 1, 1), aft(2006, 1, 1)).isDateValid(null));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isDateValid(now(2011, 12, 31)));
        Assert.assertTrue(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isDateValid(now(2012, 1, 1)));
        Assert.assertTrue(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isDateValid(now(2012, 5, 28)));
        Assert.assertTrue(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isDateValid(now(2013, 1, 1)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isDateValid(now(2013, 6, 28)));
        Assert.assertFalse(qXR(bef(2012, 1, 1), aft(2013, 1, 1)).isDateValid(now(2014, 6, 28)));
    }

    public void testRootCa() {
        System.out.printf("%s\n\n%s\n\n%s\n\n", caCrtPem, caCrtPem, caCrtPem);
        System.out.printf("%s\n\n%s\n\n%s\n\n", caCrtPem, caCrtPem, caCrtPem);
        System.out.printf("%s\n\n%s\n\n%s\n\n", caCrtPem, caCrtPem, caCrtPem);
        System.out.printf("%s\n\n%s\n\n%s\n\n", caCrtPem, caCrtPem, caCrtPem);
    }

    private static Date now(int... tup) {
        return StaticHelpers.dateFromTuple(tup);
    }

    private static Date bef(int... tup) {
        return StaticHelpers.dateFromTuple(tup);
    }

    private static Date aft(int... tup) {
        return qDT(tup);
    }

    private static Date qDT(int... tup) {
        return StaticHelpers.dateFromTuple(tup);
    }

    private X509Inspector qXR(Date notBefore, Date notAfter) throws CapManUtilException, InvalidKeySpecException {
        KeyPair kp = key2048bitReader.toKeyPair();
        X509Certificate x509 = CertUtils.quickSelfSign(kp, caSubj, notBefore, notAfter);
        X509Inspector xr;
        try {
            xr = X509Inspector.newX509Inspector(x509);
        } catch (CertificateEncodingException ex) {
            throw new CapManUtilException(ex);
        } catch (CertificateParsingException ex) {
            throw new CapManUtilException(ex);

        } catch (NotAnX509CertificateException ex) {
            throw new CapManUtilException(ex);
        }
        return xr;
    }
}
