package org.openstack.atlas.util.ca.util;

import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.PrivKeyDecodeException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.RsaConst;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.CapManUtilException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderDecodeException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderNoSuchExtensionException;
import org.openstack.atlas.util.ca.primitives.Debug;

public class X509InspectorTest {

    public static final BigInteger caMod = new BigInteger("92519081613557335824146312114331099693596712334582852764163758222004945339138079274569160547038643263700275407813235096605105941298720472784518757878492358916313599921202611417629889327386843437793899506223684027421621426615502802714869767807708103437254423295282076493912484979373100387883795899165798220483");
    public static final String caSubjId = "a72ad48c3632e4f3f0381b48474abe7126530dd8";
    public static final BigInteger testMod = new BigInteger("92769520113997379614084710552346480125314109559245447551856686347298813937630358936531128096799589632829885895580345157680107273222950724084624729121336709416739678416576244399370216587550882915851315377147000246928233006530724331354867197205185517189113923758771082160421912659909039341373309420290932945177");
    public static final String testCrtSubjKeyId = "607cc6865330a280efdf3d1ca865e79acd745b55";

    public static final String key2048bit = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEpQIBAAKCAQEAo5ZrBNCUIeKU8gfptbq9CpkUeZwRasJmij4lGJBaRrTeOpeX\n"
            + "z6lMEqiN49+UvE3NF6LuMCx/sb+/gAU+BGS/pAauJ/TbIESHF4dDzfUMr5nOk0Ae\n"
            + "slfST3QX4wAgVDkuuxxHP0K+jL85qaYBGsLL4HETKMDZKXvOcCORSq0ZCy7D1sP0\n"
            + "wCHomtX7k1RE7EgWsKDOumj+7SH/9jnBMKrHNqtj3xBZt18tugSyfeZVwQgGOW+m\n"
            + "f7A96tLmw1XQgoAxQqTTZkjFIMjM5JEFOeX8Bia0CzixTMGN2BjnuafkoUxslaF4\n"
            + "APmRu6n4xGlDuDuKpAFHEYg2nyPkezpsBX5s7QIDAQABAoIBAQCWoBG6RTOgX7k1\n"
            + "ggO3yVH3SCyKLSH8YzN3ZvFRRNla8X8OBDdMhl39cCX2BBA3sot9kBAxW0fYqu3x\n"
            + "OuJ3uSycI2qIb/S0KWUaTPop0dD0f3KuMwQQwrxrXEICSkN6SYy1zLvti89YWVsG\n"
            + "0kuCEIsZBgTWKXvLrqvOpXFKiUfd+qBKJjPqV+7Thna5ffoznnE+fTPaKozKwknm\n"
            + "tdMd6pG7vdhht8/vEf6s0qCNRkStpZhbs0OjGCziqp6Zs0IzrmUdDP4QLiNlt1Fh\n"
            + "HLSkN9fuD3z6PanQ3DLcqeZTZEaZ6dSiJljS7VeUfYQbR8jhydqNzL8wbzIhH8l+\n"
            + "ZzL7KS0hAoGBANRcfXQpi/YAcdBeo2s/ugI2PxTlg/r2kaNFa03iJKYXvOHWS9eF\n"
            + "kNtEW5LfvJYWWQNz99e71pFksYqTqeEerJvB37xrluyIPjyOo2oxuRvQNCeTX5jR\n"
            + "XY4NFVwTWVaPYCC1zTK6y4jiOL+We44Kip6sgLS9kGZAA+gQnLRX1EmHAoGBAMU0\n"
            + "IWGq7sKqujGnhQUKfqay/4f6cAh0WEaGP5guqk9AMYT9IDQ98qVpMxh9uP7yJiTx\n"
            + "Mx5TDj7BBCIqpAt2OAFhMqovAqxKGOgiJU8BVacRF6ow8FOTXBwPcrdXHeDas+4n\n"
            + "DdFvjxa4+aeUK2SGNW8kPImEEOrxu3LEwSTIDSLrAoGBAKaiQKrC4xlQdf5cFH1W\n"
            + "jv2nVU5vXmWxzsu/8Bg4CCvwWn0Xa4GdQ/JaLEUOnOtkc8p62BKHSTHjQlEL13RX\n"
            + "XngF5Cr0fYPy0GsyPdZZV/gUIqifQpcmSfPqHkWWxTZf4L0qCu7wlj89y+vCCAeI\n"
            + "DAfAMmogiUtClg4l4uC8Pk7HAoGBAKkoBlpY3WVuPTjKkXe5gNpNQJPLZr5Zzj7w\n"
            + "eSx5Gu3QCqog1rb5TGJG0uV3MnC+Faoqm8avR9DckEcefIi4Z2IHlgYVPR28kZDN\n"
            + "eWNDqc0dBEegowWNqb0II0bRG3f9IcpvBZNZNkwvbzcoCfC4jq0/UA5Fkp11rWzN\n"
            + "CUAbuejxAoGAC+A33jMXrI8i+a/rHCtnyfG+mqbMV8+XOrXVH6jpVns8WmTGovPK\n"
            + "fQHzylx4AGlmn3X+zvahzEJZUvaClsQVaEJh0jykl4/48swkv3LNWrQVxPJGehk2\n"
            + "NYdiC1EDRLo2B+3mFOu8UbVz6e0q3KiWkLBxxDbMwzn7diCJdap6rDo=\n"
            + "-----END RSA PRIVATE KEY-----\n";

    public static final String testKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICXgIBAAKBgQCEG7HNhuX0VLKIYOQaghsA6HJSZ4nRACfxyzXSX3KyDlRYM9pG\n"
            + "FjJtoMg2usNEnKesfhOmCXfYehXMwj334T388You+LcdPEj2FKRE0kpo3c8VxgjC\n"
            + "VLgTHnQm77wt/MEGbChjAkJrzz2gk6YwQ6nJX51rjwvIx3Rr+tfb/i8xGQIDAQAB\n"
            + "AoGAcyxx5u0krc7pl1xhgXrMcA43HQCHdl7cdEDlu3LbW8CCaCNMuK3BaTIzWwOY\n"
            + "Gck5pXiFSMwYX/KP7uOpguIsV4gBrJhy3l6wQmhH5QGshqIBEDtbI21gTisv1fhx\n"
            + "DYceEqwp9WapRf719cyMMMAQ+8Nm1YDudUxeDnUvWfcoxcECQQDFjSdeVMxexFgE\n"
            + "0vKlPw0oP4+6sMvvaObEOFfW/e73LM0ome2XXSixQyA30z8y/1145rD/sNK4KiaQ\n"
            + "l9Ea0DOfAkEAqzHFmqxW860LffT2XyXYhpHzdQiJdm0WXUDkx/2FPXGyljqBGZtL\n"
            + "ZWYDUpE3HyOYCrCYZsMGbYusodGS/uMgRwJBAJLIocLWcQ/NBbV34/DiW21XZP0L\n"
            + "Vkwp/qU3VBUbks43jKypSr8X6h9jx/GS1beXxKULi+JASSGruAHhu+4XWvMCQQCA\n"
            + "+p7GSdG5BUcDPuvgA8N+n7etFSF79/RBjgLQKlGYWXETfkCF6lqDqrgWHRJKg6ap\n"
            + "ZyNrSMQvBGyr/hmhr71BAkEArMvC4eVA9MRXfpPclDgSpSGUpiklbaeZB/jbpi4Q\n"
            + "V0ozFm1Kecdu1Le+mp5Mad3qktlMB7Euifsj8QlT2KN5AQ==\n"
            + "-----END RSA PRIVATE KEY-----\n";

    public static final String caKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICXQIBAAKBgQCDwGVJ6fe+iXCVzCcGWc4XcNgoa3R/2YW9fsV8FKfan/6UfeDx\n"
            + "Ywb7MeDtHg7VFawLLUMrStOEN2ZKcYsQxsMS65P4lrxrSMSU7F6HtVZMB+XVYe4U\n"
            + "eUSVo03MN8t6l9a3/A0bga16/SdBqZ91P9BO0IAtxcIyk2m/cYKopDrCwwIDAQAB\n"
            + "AoGAW1RlYmVzvXsctlp8uuRJ/unUjcBfU7kAAqn8T9Upvl2mZl0UL4CL+FlNKFHr\n"
            + "yj5psp2/sCUAluioWfZ3hjuiQV6R8ZnZW712YhXnov6Ph/r4NcMGnHJYer0HM1zq\n"
            + "VFlyPWQCkS34u+aQhPpKAdddBrLmjSv1DgxRMaWs6RYtMrECQQC+Nl/7afabGRKw\n"
            + "QihMzjSv1ZBsue3IH7uqNlQf0nW0u+YiDGheSxikzZulBK/lO9XxMGVtYhrwq/TG\n"
            + "lPAnU65/AkEAsVHURZAGzTydFv0dnMm8g/tknqt+OKlZBgHuBe5Zt+RV3jIIkt5V\n"
            + "UiNUS0YTUuZwa4+SJP5nN6J+58AHBeORvQJBAIka2o53L6lWJlFkLnZGQFXp44Nr\n"
            + "dYjFzth+9p5FblCLC/PI68Xj7WyFQ8ZrnXnnamvCjamNiIun9vTY0E4YlHMCQQCP\n"
            + "ri7C7yGTzDm+FvuXwB/xEhNGPs/YOeDY7VdhlvE8ANlTYldwKpgYJmh3ViDyW6dc\n"
            + "gMl7EGmyuwj54K/QJcZBAkBuUPuLnWSfMYw6fyguxH7oqzmSt8mMZBlbu7ocrBC1\n"
            + "pdzb+eyYlzihca/Wa0nBtn43wqUq415AAJKfnGUzqtSd\n"
            + "-----END RSA PRIVATE KEY-----\n";

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
    private BCRSAPrivateKey caKey;
    private BCRSAPrivateKey testKey;
    private BCRSAPrivateKey key2048bitKey;

    public X509InspectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws X509ReaderDecodeException, NotAnX509CertificateException, PrivKeyDecodeException, PemException {
        RsaConst.init();
        caCrtReader = X509Inspector.newX509Inspector(caCrtPem);
        caKey = (BCRSAPrivateKey) ((KeyPair) PemUtils.fromPemString(caKeyPem)).getPrivate();
        testCrtReader = X509Inspector.newX509Inspector(testCrtPem);
        testKey = (BCRSAPrivateKey) ((KeyPair) PemUtils.fromPemString(testKeyPem)).getPrivate();

        pkcs8CrtReader = X509Inspector.newX509Inspector(pkcs8CrtPem);
        key2048bitKey = (BCRSAPrivateKey) ((KeyPair) PemUtils.fromPemString(key2048bit)).getPrivate();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void caCrtShouldModMatchCaKey() throws RsaException {
        BigInteger crtMod = caCrtReader.getPubModulus();
        BigInteger keyMod = caKey.getModulus();
        Assert.assertEquals(keyMod, crtMod);
        Assert.assertEquals(keyMod, caMod);
    }

    @Test
    public void keyShouldModMatchWithTestCrt() throws RsaException {
        BigInteger crtMod = testCrtReader.getPubModulus();
        BigInteger keyMod = testKey.getModulus();
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
        Assert.assertEquals(caSubjId, caCrtReader.getSubjKeyId());
        Assert.assertEquals(testCrtSubjKeyId, testCrtReader.getSubjKeyId());
        Assert.assertEquals(testCrtReader.getAuthKeyId(), caCrtReader.getSubjKeyId());
        Debug.nop();
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
        KeyPair kp = RSAKeyUtils.genKeyPair(1024);
        X509CertificateHolder x509 = CertUtils.quickSelfSign(kp, caSubj, notBefore, notAfter);
        X509Inspector xr;
        xr = X509Inspector.newX509Inspector(x509);
        return xr;
    }
}
