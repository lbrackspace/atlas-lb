/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.util.ca.zeus;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.junit.*;

import static org.junit.Assert.*;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509ChainEntry;
import org.openstack.atlas.util.ca.util.X509PathBuilder;

public class ZeusUtilsTest {

    private static KeyPair userKey;
    private static X509CertificateObject userCrt;
    private static Set<X509CertificateObject> imdCrts;
    private static X509CertificateObject rootCA;
    private static int keySize = 512; // Keeping the key small for testing
    private static List<X509ChainEntry> chainEntries;
    // These are for testing pre defined keys and certs
    private static String workingRootCa;
    private static String workingUserKey;
    private static String workingUserCrt;
    private static String workingUserChain;

    public ZeusUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws RsaException, NotAnX509CertificateException {
        List<X509CertificateObject> orderImds = new ArrayList<X509CertificateObject>();
        long now = System.currentTimeMillis();
        long lastYear = now - (long) 1000 * 24 * 60 * 60 * 365;
        long nextYear = now + (long) 1000 * 24 * 60 * 60 * 365;
        Date notBefore = new Date(lastYear);
        Date notAfter = new Date(nextYear);
        String wtf = String.format("%s\n%s", StaticHelpers.getDateString(notBefore), StaticHelpers.getDateString(notAfter));
        List<String> subjNames = new ArrayList<String>();
        // Root SubjName
        subjNames.add("CN=RootCA");

        // Add the middle subjs
        for (int i = 1; i <= 7; i++) {
            String fmt = "CN=Intermedite Cert %s";
            String subjName = String.format(fmt, i);
            subjNames.add(subjName);
        }

        // Lastly add the end user subj
        String subjName = "CN=www.rackexp.org";
        subjNames.add(subjName);
        chainEntries = X509PathBuilder.newChain(subjNames, keySize, notBefore, notAfter);
        int lastIdx = chainEntries.size() - 1;
        rootCA = chainEntries.get(0).getX509obj();
        userCrt = chainEntries.get(lastIdx).getX509obj();
        userKey = chainEntries.get(lastIdx).getKey();

        imdCrts = new HashSet<X509CertificateObject>();
        for (int i = 1; i < lastIdx; i++) {
            imdCrts.add(chainEntries.get(i).getX509obj());
            orderImds.add(chainEntries.get(i).getX509obj());
        }

        workingRootCa = PemUtils.toPemString(rootCA);
        workingUserCrt = PemUtils.toPemString(userCrt);
        workingUserKey = PemUtils.toPemString(userKey);
        Collections.reverse(orderImds);
        StringBuilder sb = new StringBuilder();
        for (X509CertificateObject imd : orderImds) {
            sb = sb.append(PemUtils.toPemString(imd)).append("\n");
        }
        workingUserChain = sb.toString();
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
    public void testZeusCertFile() throws X509PathBuildException, PemException {
        StringBuilder wtf = new StringBuilder(4096);
        StringBuilder sb = new StringBuilder(4096);
        Set<X509CertificateObject> roots = new HashSet<X509CertificateObject>();
        String rootCaStr = PemUtils.toPemString(rootCA);
        roots.add(rootCA);
        String userKeyStr = PemUtils.toPemString(userKey);
        String userCrtStr = PemUtils.toPemString(userCrt);
        List<X509CertificateObject> imdCrtsReversed = new ArrayList(imdCrts);
        Collections.reverse(imdCrtsReversed);
        for (X509CertificateObject x509obj : imdCrtsReversed) {
            sb.append(PemUtils.toPemString(x509obj));
        }
        String imdsString = sb.toString();
        ZeusUtils zu = new ZeusUtils(roots);
        ZeusCrtFile zcf = zu.buildZeusCrtFile(userKeyStr, userCrtStr, imdsString, false);
        for (ErrorEntry errors : zcf.getErrors()) {
            Throwable ex = errors.getException();
            if (ex != null) {
                wtf.append(StringUtils.getEST(ex));
            }
        }

        assertTrue(zcf.getErrors().isEmpty());
        List<PemBlock> parsedImds = PemUtils.parseMultiPem(imdsString);
        assertTrue(parsedImds.size() == 7);
        for (PemBlock block : parsedImds) {
            assertTrue(block.getDecodedObject() instanceof X509CertificateObject);
        }
    }

    @Test
    public void testLbaasValidation() throws PemException {
        Set<X509CertificateObject> roots = loadX509Set((X509CertificateObject) PemUtils.fromPemString(workingRootCa));
        Set<X509CertificateObject> blanks = loadX509Set();
        boolean expectNoErrors = false;
        boolean expectErrors = true;
        boolean expectFatalErrors = true;
        boolean expectNoFatalErrors = false;
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, workingUserChain, expectNoFatalErrors, expectNoErrors);
        assertZCFLbaasErrors(blanks, workingUserKey, workingUserCrt, workingUserChain, expectNoFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, "", expectNoFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, workingUserCrt, null, expectNoFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, "", "", expectFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, "", null, expectFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, workingUserKey, null, null, expectFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, "", "", "", expectFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, "", null, null, expectFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, null, null, null, expectFatalErrors, expectErrors);
        assertZCFLbaasErrors(roots, "", "", workingUserChain, expectFatalErrors, expectErrors);
    }

    private void assertZCFLbaasErrors(Set<X509CertificateObject> roots, String key, String crt, String imd, boolean expectFatalErrors, boolean expectErrors) {
        ZeusUtils zu = new ZeusUtils(roots);
        ZeusCrtFile zcf = zu.buildZeusCrtFile(key, crt, imd, true);
        boolean hasErrors = zcf.hasErrors();
        boolean hasFatalErrors = zcf.hasFatalErrors();
        boolean testFailed = false;
        StringBuilder assertFailSb = new StringBuilder();
        if (expectErrors && !hasErrors) {
            assertFailSb.append(String.format("Expected errors but buildZeusCrtFile returned no Errors. "));
            testFailed = true;
        }
        if (!expectErrors && hasErrors) {
            assertFailSb.append(String.format("Wasn't expected errors but buildZeusCrtFile returned %s.", StringUtils.joinString(zcf.getErrors(), ",")));
            testFailed = true;
        }
        if (expectFatalErrors && !hasFatalErrors) {
            assertFailSb.append(String.format("Expected fatal Errors but buildZeusCrtFile returned no Fatal Errors. "));
            testFailed = true;
        }
        if (!expectFatalErrors && hasFatalErrors) {
            assertFailSb.append(String.format("wasn't expecting Fatal Errors but buildZeusCrtFile returned %s. ", StringUtils.joinString(zcf.getFatalErrors(), ",")));
        }

        if (testFailed) {
            fail(assertFailSb.toString());
        }
    }

    private Set<X509CertificateObject> loadX509Set(X509CertificateObject... x509objs) {
        Set<X509CertificateObject> x509set = new HashSet<X509CertificateObject>();
        for (int i = 0; i < x509objs.length; i++) {
            X509CertificateObject x509obj = x509objs[i];
            if (x509obj == null) {
                continue; // Cause I'm paranoid
            }
            x509set.add(x509obj);
        }
        return x509set;
    }

    @Test
    public void shouldValidateWellFormedKey() {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseKey(workingUserKey, errors);

        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldInvalidateMalformedKey() {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseKey("foobar", errors);

        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(ErrorType.UNREADABLE_KEY, errors.get(0).getErrorType());
    }

    @Test
    public void shouldValidateWellFormedCertificate() {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseCert(workingUserCrt, errors);

        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldInvalidateMalformedCertificate() {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseCert("foobar", errors);

        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(ErrorType.UNREADABLE_CERT, errors.get(0).getErrorType());
    }

    @Test
    public void shouldValidateWellFormedIntermediateCertificates() {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseIntermediateCerts(workingUserChain, errors);

        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldInvalidateMalformedIntermediateCertificates() {
        String foobar = "-----BEGIN CERTIFICATE-----\n"
                + "foobar\n"
                + "-----END CERTIFICATE-----\n";

        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseIntermediateCerts(foobar, errors);

        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(ErrorType.UNREADABLE_CERT, errors.get(0).getErrorType());
    }
}
