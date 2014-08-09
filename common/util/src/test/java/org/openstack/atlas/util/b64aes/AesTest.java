package org.openstack.atlas.util.b64aes;

import java.io.IOException;
import org.openstack.atlas.util.debug.Debug;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class AesTest {

    private static final SecureRandom rnd;
    private static final String printableStr;

    static {
        printableStr = "0123456789ABCDEFGHIJKLMNOPQRSTUVWYZabcdefghijklmnopqrstubwxyz";
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            sr = new SecureRandom();
        }
        rnd = sr;
    }

    public AesTest() {
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
    public void testEncrypt() {
    }

    @Test
    public void test_bytesToHex(){

    }

    @Test
    public void test_b64encrypt() throws Exception{
        int i = 0;
        String key = Debug.rndString(256, printableStr);
        for(i=0;i<=1024;i++){
            String ptext = Debug.rndString(i, printableStr);
            String ctext = Aes.b64encrypt_str(ptext, key);
            String decodedText = Aes.b64decrypt_str(ctext, key);
            assertEquals(ptext, decodedText);
        }
    }
}
