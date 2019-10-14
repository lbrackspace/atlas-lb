package org.openstack.atlas.util.ca;

import java.math.BigInteger;
import java.security.KeyPair;

import junit.framework.AssertionFailedError;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.Debug;

public class PemUtilsTest {

    private byte[] BEG_PRV;
    private byte[] END_PRV;
    private byte[] BEG_CSR;
    private byte[] END_CSR;
    private byte[] BEG_CRT;
    private byte[] END_CRT;
    private byte[] BEG_RSA;
    private byte[] END_RSA;
    private static final String TEST_CSR = ""
            + "-----BEGIN CERTIFICATE REQUEST-----\n"
            + "MIIB4zCCAUwCAQAwgYAxFDASBgNVBAMTC3d3dy5leHAub3JnMRwwGgYDVQQLExND\n"
            + "bG91ZCBMb2FkQmFsYW5jaW5nMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEU\n"
            + "MBIGA1UEBxMLU2FuIEFudG9uaW8xCzAJBgNVBAgTAlRYMQswCQYDVQQGEwJVUzCB\n"
            + "nzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA7lQP+mYbuVBcRenId8pMTDONor1L\n"
            + "lzV2fU0Co9C0HNQ68KJXxPhBqsvPeQfroleiGdeVTPACIW2KDcP0waIPSotPXkG5\n"
            + "RdP8LEadDa89nHUW3o++q8n8k0hQ9lG9+JIdwdxjKYMVJ7GBknLNIlkztQCttACS\n"
            + "x8QeeRd+xcvbAvsCAwEAAaAiMCAGCSqGSIb3DQEJDjETMBEwDwYDVR0TAQH/BAUw\n"
            + "AwEB/zANBgkqhkiG9w0BAQsFAAOBgQBOI5RjRnZiG595nWhSsQAa4uO2f8PGV2uI\n"
            + "xB74pTraq4RpPDaMyUrus29Nzkk8e2X1VWQt3EXWUW/mue9JguEIeEQJEYRgDapc\n"
            + "LThlpGkXxE6CtFgnGRftz8b6hFHzO03SsKrojUhv2IUASu2v83uCVjFO/mC92GPh\n"
            + "nB30EIkwGw==\n"
            + "-----END CERTIFICATE REQUEST-----\n"
            + "";

    private static final String TEST_RSA_PKCS1 = ""
            + "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICXAIBAAKBgQDuVA/6Zhu5UFxF6ch3ykxMM42ivUuXNXZ9TQKj0LQc1DrwolfE\n"
            + "+EGqy895B+uiV6IZ15VM8AIhbYoNw/TBog9Ki09eQblF0/wsRp0Nrz2cdRbej76r\n"
            + "yfyTSFD2Ub34kh3B3GMpgxUnsYGScs0iWTO1AK20AJLHxB55F37Fy9sC+wIDAQAB\n"
            + "AoGAa19oC3HxT54K1FytOnrjwPkA+K673ZXymiUV6WPfoZVkMIGTdWQ7gY1tHaQZ\n"
            + "3vwIRghAdXc0HodRST5diNdQd3COX1hbUJgGO7epKqCvQMBXGWe+Qm10wmY+hFK5\n"
            + "M88ZURsfy4TxdWYzTo+PRBvlTy/LGaLyUkuX3M60EjwQ0KECQQD7P1+IpuCewjJL\n"
            + "Pw2jyKrxZ0DRueEjO9hL2h24gEgMG06ZrAtmD7J4rUcuOn/NO1oy6OaYRkkP5FP1\n"
            + "pP5Wo1e5AkEA8tYhT6tviAVZkIGmaYfm0ZlNDIXSZ7WU5b1zyWH6maEh+y1kgo9H\n"
            + "RbCeIZ2Ua+Cs4oXlO6oSV9FD7J/nvrciUwJAGIUq0a9XpKbXObjBoAZpiH3HObCm\n"
            + "ZEXm6iAzXlpGrcfMd/rucdt+U4C1vbE/38u3FdjdCGdzofVqrEKZ/+KZIQJBANBr\n"
            + "2tFfae2L8fBmZqci3og9FHVQEmOy5OY+MbfBoW3kb57+ucqZIOn+iep+LlpczuXp\n"
            + "V/NaqD25PZxFLolw80MCQGARMg+TztphgpAQKJJlrn5HDDMURx/R4p20ks86f+lv\n"
            + "wXlmf737+vluQMsiNwIKlyMl+EnplVX/q2sE2x0aeys=\n"
            + "-----END RSA PRIVATE KEY-----\n"
            + "";

    private static final String TEST_X509 = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIIDYzCCAsygAwIBAgIGAV8keuL0MA0GCSqGSIb3DQEBCwUAMIGBMRUwEwYDVQQD\n"
            + "EwxDQSBBdXRob3JpdHkxHDAaBgNVBAsTE0Nsb3VkIExvYWRCYWxhbmNpbmcxGjAY\n"
            + "BgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEL\n"
            + "MAkGA1UECBMCVFgxCzAJBgNVBAYTAlVTMB4XDTE3MTAxNjA5MTkxNFoXDTI1MTAx\n"
            + "NDA5MTkxNFowgYAxFDASBgNVBAMTC3d3dy5leHAub3JnMRwwGgYDVQQLExNDbG91\n"
            + "ZCBMb2FkQmFsYW5jaW5nMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIG\n"
            + "A1UEBxMLU2FuIEFudG9uaW8xCzAJBgNVBAgTAlRYMQswCQYDVQQGEwJVUzCBnzAN\n"
            + "BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA7lQP+mYbuVBcRenId8pMTDONor1LlzV2\n"
            + "fU0Co9C0HNQ68KJXxPhBqsvPeQfroleiGdeVTPACIW2KDcP0waIPSotPXkG5RdP8\n"
            + "LEadDa89nHUW3o++q8n8k0hQ9lG9+JIdwdxjKYMVJ7GBknLNIlkztQCttACSx8Qe\n"
            + "eRd+xcvbAvsCAwEAAaOB5DCB4TAPBgNVHRMBAf8EBTADAQH/MIGuBgNVHSMEgaYw\n"
            + "gaOAFCBVfzYdodSg9J9PCocRn/L24kDPoYGHpIGEMIGBMRUwEwYDVQQDEwxDQSBB\n"
            + "dXRob3JpdHkxHDAaBgNVBAsTE0Nsb3VkIExvYWRCYWxhbmNpbmcxGjAYBgNVBAoT\n"
            + "EVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzELMAkGA1UE\n"
            + "CBMCVFgxCzAJBgNVBAYTAlVTggEBMB0GA1UdDgQWBBSoNGhqAlv5T6TT1B59ExzM\n"
            + "+SJaUDANBgkqhkiG9w0BAQsFAAOBgQDVXrDoXo3LHuXkmyOB+grDskGsa13FwM3B\n"
            + "X0DkNI6/aAmfMXYRtU7oSvoZyEycDZ8oEbPQnUgpU0WAofiaxC42yBYF4FBAhFVJ\n"
            + "Q4p376khhyI5XaDDIcmxWwHsywLozwwpdxxh/kb/J7sutp6lwX+cpnBlwr/MxEug\n"
            + "Gzaqqi1aAA==\n"
            + "-----END CERTIFICATE-----\n"
            + "";

    private static final String TEST_RSA_PKCS8 = ""
            + "-----BEGIN PRIVATE KEY-----\n"
            + "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAO5UD/pmG7lQXEXp\n"
            + "yHfKTEwzjaK9S5c1dn1NAqPQtBzUOvCiV8T4QarLz3kH66JXohnXlUzwAiFtig3D\n"
            + "9MGiD0qLT15BuUXT/CxGnQ2vPZx1Ft6PvqvJ/JNIUPZRvfiSHcHcYymDFSexgZJy\n"
            + "zSJZM7UArbQAksfEHnkXfsXL2wL7AgMBAAECgYBrX2gLcfFPngrUXK06euPA+QD4\n"
            + "rrvdlfKaJRXpY9+hlWQwgZN1ZDuBjW0dpBne/AhGCEB1dzQeh1FJPl2I11B3cI5f\n"
            + "WFtQmAY7t6kqoK9AwFcZZ75CbXTCZj6EUrkzzxlRGx/LhPF1ZjNOj49EG+VPL8sZ\n"
            + "ovJSS5fczrQSPBDQoQJBAPs/X4im4J7CMks/DaPIqvFnQNG54SM72EvaHbiASAwb\n"
            + "TpmsC2YPsnitRy46f807WjLo5phGSQ/kU/Wk/lajV7kCQQDy1iFPq2+IBVmQgaZp\n"
            + "h+bRmU0MhdJntZTlvXPJYfqZoSH7LWSCj0dFsJ4hnZRr4KziheU7qhJX0UPsn+e+\n"
            + "tyJTAkAYhSrRr1ekptc5uMGgBmmIfcc5sKZkRebqIDNeWkatx8x3+u5x235TgLW9\n"
            + "sT/fy7cV2N0IZ3Oh9WqsQpn/4pkhAkEA0Gva0V9p7Yvx8GZmpyLeiD0UdVASY7Lk\n"
            + "5j4xt8GhbeRvnv65ypkg6f6J6n4uWlzO5elX81qoPbk9nEUuiXDzQwJAYBEyD5PO\n"
            + "2mGCkBAokmWufkcMMxRHH9HinbSSzzp/6W/BeWZ/vfv6+W5AyyI3AgqXIyX4SemV\n"
            + "Vf+rawTbHRp7Kw==\n"
            + "-----END PRIVATE KEY-----\n"
            + "";

    private static final String CA_X509 = ""
            + "-----BEGIN CERTIFICATE-----\n"
            + "MIICrDCCAhWgAwIBAgIBATANBgkqhkiG9w0BAQsFADCBgTEVMBMGA1UEAxMMQ0Eg\n"
            + "QXV0aG9yaXR5MRwwGgYDVQQLExNDbG91ZCBMb2FkQmFsYW5jaW5nMRowGAYDVQQK\n"
            + "ExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xCzAJBgNV\n"
            + "BAgTAlRYMQswCQYDVQQGEwJVUzAeFw0xNzEwMTYwOTE4MTVaFw0yNTEwMTQwOTE4\n"
            + "MTVaMIGBMRUwEwYDVQQDEwxDQSBBdXRob3JpdHkxHDAaBgNVBAsTE0Nsb3VkIExv\n"
            + "YWRCYWxhbmNpbmcxGjAYBgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQH\n"
            + "EwtTYW4gQW50b25pbzELMAkGA1UECBMCVFgxCzAJBgNVBAYTAlVTMIGfMA0GCSqG\n"
            + "SIb3DQEBAQUAA4GNADCBiQKBgQDWSGcRVVmTKSY/OQ7S1pgBSYvOt+Rp8hwl1TsO\n"
            + "GspQapbRlzoauerydv+yauQPbE+gNOivry+fH4QCwKhrfsCyU+PY5rJv2qZhMQZ1\n"
            + "Wyaad7VhcmMnvk5Mq2EN6Df88EADqod2kS2H4sW0NRohKcd/+SO8w2Feuqdvjk9P\n"
            + "54h8IwIDAQABozIwMDAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBQgVX82HaHU\n"
            + "oPSfTwqHEZ/y9uJAzzANBgkqhkiG9w0BAQsFAAOBgQAgKOtfXOq6m/+XQW2cXLez\n"
            + "XYDP/PQLU2ijoBXAoO15qnGW/wi1w76nOyw+i7vqXC8JUlBOZqRKbD58bAR/h5Iu\n"
            + "oa4zjTdbiu6zdUUtNyTJA+SD5gu2TmZCuhaK1Ucx/TEXiTW7yLkEpGgwHQnmweB8\n"
            + "siRU0LBOuceuiAAOwR8ZIg==\n"
            + "-----END CERTIFICATE-----\n"
            + "";

    private static final String CA_KEY = ""
            + "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIICXQIBAAKBgQDWSGcRVVmTKSY/OQ7S1pgBSYvOt+Rp8hwl1TsOGspQapbRlzoa\n"
            + "uerydv+yauQPbE+gNOivry+fH4QCwKhrfsCyU+PY5rJv2qZhMQZ1Wyaad7VhcmMn\n"
            + "vk5Mq2EN6Df88EADqod2kS2H4sW0NRohKcd/+SO8w2Feuqdvjk9P54h8IwIDAQAB\n"
            + "AoGBAMxFaxOIBIMYCCDe+LWe4nrfrIpcnT8uEQ0zzz8r5M9yPOICQt61eza2oBxq\n"
            + "b4wQWXvE1EiePUx4k7S4ChWnWHMn1dxduAvndL564WJ14o00WeBjuRufkey/Qnfp\n"
            + "lknSl6jBpiGsTP8mMxpR3PbsS0HePeKwrHXIa7+307ddlVapAkEA+IVNu+mpjjUT\n"
            + "D4Ku5gF/7MnNJIu+MyZUP+ykPwBL7j0PwD1jieOqHQpcrdko2V28hMEGcsYX1sEx\n"
            + "/HEJ9qadtQJBANy7UUPeSvhLgColGhsO7DxStbKGz2/Ri7iOFnyljj+BxfaHiqx1\n"
            + "IJiLzZXm1Lu2krUCriAgc9E2Q0JRqoEEmXcCQAvbmGzp5jwxzhoEW0IAOlFbA53F\n"
            + "ySp5nrV6HhAffH/+i9zZUQGISlqeXvOwk+FjNpaC+0LIruSBTVhFMHuDUa0CQG5V\n"
            + "d7acDA929fabGySBXhZ9JXO6rT/wl43GAHmH68XU4ZZshCXWWlfGMhqHCrlV9pli\n"
            + "YJWQgzQFkrM4zSi2mtsCQQDGhrITCSiLViAA1O8bFJpECUYKZmPZr8qoW/nYjxG6\n"
            + "XTM/wzOGfKASLdH1qqiBpvdq3scm94v9zfrAm+F8qPxw\n"
            + "-----END RSA PRIVATE KEY-----\n"
            + "";

    private static final String ECDSA_KEY = "" +
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQg3CW4HRg8tOnEcZdFUXnq\n" +
            "jxWj8MRTEvc4ddGGakRkNSShRANCAAQluVw/0hI7JyKCM1E/0lhQlGF6sdEA0pbA\n" +
            "ltFhfkM63NFzv1PMknXIfgu3xtbP6hpFXx+PtoNhBZ8TX+rcLo66\n" +
            "-----END PRIVATE KEY-----\n";

    public PemUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        BEG_PRV = StringUtils.asciiBytes("-----BEGIN RSA PRIVATE KEY-----");
        END_PRV = StringUtils.asciiBytes("-----END RSA PRIVATE KEY-----");
        BEG_CSR = StringUtils.asciiBytes("-----BEGIN CERTIFICATE REQUEST-----");
        END_CSR = StringUtils.asciiBytes("-----END CERTIFICATE REQUEST-----");
        BEG_CRT = StringUtils.asciiBytes("-----BEGIN CERTIFICATE-----");
        END_CRT = StringUtils.asciiBytes("-----END CERTIFICATE-----");
        BEG_RSA = StringUtils.asciiBytes("-----BEGIN PRIVATE KEY-----");
        END_RSA = StringUtils.asciiBytes("-----END PRIVATE KEY-----");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDecodeString() throws PemException{
        Object obj;
        
        obj = PemUtils.fromPemString(TEST_CSR);
        Assert.assertTrue(obj instanceof PKCS10CertificationRequest);
        obj = PemUtils.fromPemString(TEST_RSA_PKCS1);
        Assert.assertTrue(obj instanceof KeyPair);
        obj = PemUtils.fromPemString(TEST_RSA_PKCS8);
        
        obj = PemUtils.fromPemString(TEST_X509);
        System.out.printf(String.format("%s", obj.getClass().getCanonicalName()));
    }
    
    @Test
    public void testIsBegPemBlock() {
        assertTrue(PemUtils.isBegPemBlock(BEG_PRV));
        assertTrue(PemUtils.isBegPemBlock(BEG_CSR));
        assertTrue(PemUtils.isBegPemBlock(BEG_CRT));
        assertTrue(PemUtils.isBegPemBlock(BEG_RSA));
        assertFalse(PemUtils.isBegPemBlock(END_PRV));
        assertFalse(PemUtils.isBegPemBlock(END_CSR));
        assertFalse(PemUtils.isBegPemBlock(END_CRT));
        assertFalse(PemUtils.isBegPemBlock(END_RSA));
    }

    @Test
    public void testIsEndPemBlock() {
        assertFalse(PemUtils.isEndPemBlock(BEG_PRV));
        assertFalse(PemUtils.isEndPemBlock(BEG_CSR));
        assertFalse(PemUtils.isEndPemBlock(BEG_CRT));
        assertFalse(PemUtils.isEndPemBlock(BEG_RSA));
        assertTrue(PemUtils.isEndPemBlock(END_PRV));
        assertTrue(PemUtils.isEndPemBlock(END_CSR));
        assertTrue(PemUtils.isEndPemBlock(END_CRT));
        assertTrue(PemUtils.isEndPemBlock(END_RSA));
    }

    @Test
    public void testRsaGenKey() throws RsaException {
        String msg;
        KeyPair keys = RSAKeyUtils.genKeyPair(1024);
        String pemStr;
        Object obj;
        Assert.assertFalse(keys == null);
        BigInteger mod = RSAKeyUtils.getModulus(keys);
        Assert.assertEquals(1, mod.compareTo(BigInteger.ONE));
        String pubKeyStr = RSAKeyUtils.objToString(keys.getPublic());
        Assert.assertTrue(pubKeyStr != null);
        pemStr = PemUtils.toPemString(keys);
        obj = PemUtils.fromPemString(pemStr);
        Assert.assertTrue(obj instanceof KeyPair);
        pemStr = PemUtils.toPemString(keys.getPublic());
        obj = PemUtils.fromPemString(pemStr);
        pemStr = PemUtils.toPemString(keys.getPrivate());
        obj = PemUtils.fromPemString(pemStr);
        Debug.nop();
    }

    @Test()
    public void testFromPembytesWithECDSA() throws RsaException {
        try {
            PemUtils.fromPemString(ECDSA_KEY);
            fail("Expecting PemException for ECDSA key");
        } catch (PemException ex) {
            Assert.assertEquals("ECDSA is an invalid algorithm at this time.", ex.getCause().getMessage());
        }
    }

    @Test
    public void testPKCS10Read() {

    }
}
