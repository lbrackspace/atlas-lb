/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.util.b64aes;

import org.junit.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AesTest {

    public AesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws UnsupportedEncodingException {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSixeToChar() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String test = "testt";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, key, iv);
        assertEquals(ctest, dtest);
    }
}
