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
    public void testGCMEncryptAndDecryptValidData() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, key, iv);
        assertEquals(ctest, dtest);
    }

    @Test(expected = AEADBadTagException.class)
    public void testGCMDecryptWithInvalidKey() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String invalidKey = "nZr4u7x!A%D*F-Jb";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, invalidKey, iv);
        assertEquals(ctest, dtest);
    }

    @Test(expected = AEADBadTagException.class)
    public void testGCMDecryptIncorrectIV() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String incorrectIV = "testt";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        String dtest = Aes.b64decryptGCM_str(etest, key, incorrectIV);
        assertEquals(ctest, dtest);
    }

    @Test
    public void testEncryptOutputDoesNotEqualInput() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String ctest = "encryptMe";
        String iv = "testiv";
        String etest = Aes.b64encryptGCM(ctest.getBytes(), key, iv);
        Assert.assertNotEquals(ctest, etest);
    }

    @Test
    public void testCBCEncryptAndDecryptValidData() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String ctest = "encryptMe";
        String etest = Aes.b64encrypt(ctest.getBytes(), key);
        String dtest = Aes.b64decrypt_str(etest, key);
        assertEquals(ctest, dtest);
    }

    @Test(expected = BadPaddingException.class)
    public void testCBCDecryptWithInvalidKey() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String invalidKey = "nZr4u7x!A%D*F-Jb";
        String ctest = "encryptMe";
        String etest = Aes.b64encrypt(ctest.getBytes(), key);
        String dtest = Aes.b64decrypt_str(etest, invalidKey);
        assertEquals(ctest, dtest);
    }

    @Test
    public void testCBCEncryptOutputDoesNotEqualInput() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException {
        String key = "nZr4u7x!A%D*F-Ja";
        String ctest = "encryptMe";
        String etest = Aes.b64encrypt(ctest.getBytes(), key);
        Assert.assertNotEquals(ctest, etest);
    }

}
