/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.util.ca;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.Debug;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;

public class CertUtilsTest {

    public static final String test_subj = "CN=www.exp.org, OU=Cloud LoadBalancing, O=Rackspace Hosting, L=San Antonio, ST=TX, C=US";
    public static final String ca_subj = "CN=CA, OU=Cloud LoadBalancing, O=Rackspace Hosting, L=San Antonio, ST=TX, C=US";
    public static final int KEY_SIZE = 512;
    public static final Date notBefore;
    public static final Date notAfter;

    static {
        notBefore = new Date(System.currentTimeMillis());
        notAfter = new Date(notBefore.getTime() + 60L * 60L * 364L * 4L * 1000L * 24L);
    }

    public CertUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateX509CertAllTheWay() throws RsaException, NotAnX509CertificateException {

        // Generate a self Signed ca x509
        // generate ca key
        KeyPair caKey = RSAKeyUtils.genKeyPair(KEY_SIZE);
        // Generate ca csr
        PKCS10CertificationRequest caCsr = CsrUtils.newCsr(ca_subj, caKey, true);
        // Self sign the ca with its own key
        X509CertificateHolder caCrt = CertUtils.selfSignCsrCA(caCsr, caKey, notBefore, notAfter);
        X509Certificate caCrtXC = CertUtils.getX509Certificate(caCrt);
        try {
            caCrtXC.checkValidity();
        } catch (CertificateExpiredException ex) {
            Assert.fail("Error cert not signed");
        } catch (CertificateNotYetValidException ex) {
            Assert.fail("Error cert not valid yet");
        }
        X500Name subjName = CertUtils.getSubjectNameFromCert(caCrtXC);
        String cn = IETFUtils.valueToString(subjName.getRDNs(BCStyle.CN)[0].getFirst().getValue());
        Assert.assertEquals("CA", cn);
        PublicKey crtPubKey = caCrtXC.getPublicKey();
        PublicKey caPubKey = caKey.getPublic();
        boolean pubKeysEqual = caPubKey.equals(crtPubKey);
        Assert.assertTrue(pubKeysEqual); // The public keys should match
        // Generte test certificate
        // Generate test key
        KeyPair testKey = RSAKeyUtils.genKeyPair(KEY_SIZE);

        // Generate the test csr
        PKCS10CertificationRequest testCsr = CsrUtils.newCsr(test_subj, caKey, false);

        // Sign the testCsr with the CA ceert and key
        X509CertificateHolder testCrt = CertUtils.signCSR(testCsr, caKey, caCrt, KEY_SIZE, BigInteger.ZERO);

        //Verify if the signature worked
        List<ErrorEntry> errors = CertUtils.verifyIssuerAndSubjectCert(testCrt, testCrt);
        Assert.assertFalse(ErrorEntry.hasFatal(errors));

        Debug.nop();
    }

    @Test
    public void testX509Casting() {
        X509Certificate xc;
        X509CertificateObject xo;
        X509CertificateHolder xh;
        try {
            KeyPair kp = RSAKeyUtils.genKeyPair(KEY_SIZE);
            xh = CertUtils.quickSelfSign(kp, test_subj, -1, 365);
            xc = CertUtils.getX509Certificate(xh);
            xo = CertUtils.getX509CertificateObject(xc);
            xh = CertUtils.getX509CertificateHolder(xc);
            xh = CertUtils.getX509CertificateHolder(xo);
            xc = CertUtils.getX509Certificate(xh);
            xo = CertUtils.getX509CertificateObject(xh);
        } catch (Exception ex) {
            String msg = Debug.getExtendedStackTrace(ex);
            Assert.fail("x509 converters throw exception");
        }
        Debug.nop();
    }
}
