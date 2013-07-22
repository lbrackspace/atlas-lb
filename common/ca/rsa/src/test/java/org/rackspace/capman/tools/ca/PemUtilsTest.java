package org.rackspace.capman.tools.ca;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PemUtilsTest {

    private byte[] BEG_PRV;
    private byte[] END_PRV;
    private byte[] BEG_CSR;
    private byte[] END_CSR;
    private byte[] BEG_CRT;
    private byte[] END_CRT;
    private byte[] BEG_RSA;
    private byte[] END_RSA;

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
}
