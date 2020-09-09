/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.util.b64aes;

import org.junit.*;
import org.omg.CORBA.DynAnyPackage.Invalid;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

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

    @Test
    public void testSixeToCharAssertNotEquals() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String test = "testt";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, key, iv);
        assertNotEquals(test, dtest);
    }

    @Test(expected = AEADBadTagException.class)
    public void testSixeToCharWithInvalidKeyDecrypt() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String invalidKey = "nZr4u7x!A%D*F-Jb";
        String test = "testt";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, invalidKey, iv);
        assertEquals(ctest, dtest);
    }

    @Test(expected = AEADBadTagException.class)
    public void testSixeToCharIncorrectIV() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String incorrectIV = "testt";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, key, incorrectIV);
        assertEquals(ctest, dtest);
    }



}
