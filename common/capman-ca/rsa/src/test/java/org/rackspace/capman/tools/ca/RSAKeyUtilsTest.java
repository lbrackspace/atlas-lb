package org.rackspace.capman.tools.ca;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import org.rackspace.capman.tools.ca.RSAKeyUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.junit.Ignore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rackspace.capman.tools.ca.StringUtils;
import static org.junit.Assert.*;
import org.rackspace.capman.tools.ca.exceptions.NoSuchAlgorithmException;
import org.rackspace.capman.tools.ca.exceptions.NotAnRSAKeyException;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.rackspace.capman.tools.ca.exceptions.RsaException;

public class RSAKeyUtilsTest {

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

    @Ignore
    @Test
    public void testRsaGenKey() throws RsaException, UnsupportedEncodingException {
        String msg;
        KeyPair keys = RSAKeyUtils.genKeyPair(1024);
        byte[] pem;
        String pemStr;
        pem = PemUtils.toPem(keys);
        pemStr = new String(pem, "us-ascii");
    }
}
