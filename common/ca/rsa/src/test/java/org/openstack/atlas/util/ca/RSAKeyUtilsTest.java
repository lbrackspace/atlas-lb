package org.openstack.atlas.util.ca;

//import org.openstack.atlas.util.ca.PemUtils;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyPair;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.junit.Ignore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.util.ca.exceptions.NotAnRSAKeyException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.Debug;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.openstack.atlas.util.ca.primitives.ByteLineReader;
import sun.security.rsa.RSAPublicKeyImpl;

public class RSAKeyUtilsTest {

    public static final int KEY_SIZE = 512;

    public RSAKeyUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPKCS8() throws RsaException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        KeyPair kp = RSAKeyUtils.genKeyPair(KEY_SIZE);
        PemObject pkcs8 = RSAKeyUtils.toPKCS8(kp);
        String pemStr = PemUtils.toPemString(pkcs8);
        ByteLineReader blr = new ByteLineReader(pemStr.getBytes());
        String firstLine = new String(blr.readLine(true));
        String begRsa = new String(ByteLineReader.chopLine(PemUtils.BEG_RSA));
        Assert.assertEquals(begRsa, firstLine);
        PEMKeyPair pkp = RSAKeyUtils.getPemKeyPair(kp);
        Assert.assertTrue(pkp instanceof PEMKeyPair);
        kp = RSAKeyUtils.getKeyPair(pkp);
        Assert.assertTrue(kp instanceof KeyPair);
    }

    @Test
    public void testPublicKeyConverter() throws RsaException, PEMException {
        KeyPair kp = RSAKeyUtils.genKeyPair(1024);
        PKCS10CertificationRequest csr = CsrUtils.newCsr("CN=Test", kp, true);
        SubjectPublicKeyInfo pubKeyInfo = csr.getSubjectPublicKeyInfo();
        Object obj = RSAKeyUtils.getBCRSAPublicKey(pubKeyInfo);
        Assert.assertTrue(obj instanceof BCRSAPublicKey);
    }
    
    @Test
    public void testKeyGenerator() throws RsaException {
        int i;
        KeyPair kp = RSAKeyUtils.genKeyPair(KEY_SIZE);
        BCRSAPrivateCrtKey privKey = RSAKeyUtils.getBCRSAPrivteKey(kp);
        BigInteger p = privKey.getPrimeP();
        BigInteger q = privKey.getPrimeQ();
        BigInteger n = privKey.getModulus();
        BigInteger t = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // t = (p-1)*(q-1)
        BigInteger e = privKey.getPublicExponent();
        BigInteger d = privKey.getPrivateExponent();
        Assert.assertEquals(n, p.multiply(q)); // n == p * q
        BigInteger cycle = BigInteger.TEN.modPow(e, n).modPow(d, n);
        Assert.assertEquals(BigInteger.TEN, cycle);
    }
}
