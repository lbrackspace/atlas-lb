/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.util.b64aes;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import org.openstack.atlas.util.debug.Debug;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author crc
 */
public class Base64Test {

    private static final int BIGBLOCKSIZE = 1024 * 1024 * 64;
    private InputStream is;
    private OutputStream os;
    private static double secsPerMilli = 1.0 / 1000.0;

    public Base64Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws UnsupportedEncodingException {
        is = new ByteArrayInputStream("WHF".getBytes("us-ascii"));
        os = new ByteArrayOutputStream();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSixeToChar() {
        StringBuilder sb = new StringBuilder(65);

        for (int i = 0; i < 64; i++) {
            sb.append((char) Base64.sixToChar[i]);
        }
        System.out.printf("map=%s\n", sb.toString());
    }

    // Encode Tests
    @Test
    public void testEncodeShouldHave2PaddingSymbols() throws UnsupportedEncodingException {
        assertEncodeMatches("1234", "MTIzNA==");
        assertEncodeMatches("1234567", "MTIzNDU2Nw==");
        assertEncodeMatches("123456789A", "MTIzNDU2Nzg5QQ==");
    }

    @Test
    public void testEncodeShouldHave1PaddingSymbols() throws UnsupportedEncodingException {
        assertEncodeMatches("12345", "MTIzNDU=");
        assertEncodeMatches("12345678", "MTIzNDU2Nzg=");
        assertEncodeMatches("123456789AB", "MTIzNDU2Nzg5QUI=");
    }

    @Test
    public void testEncodeShouldHaveNoPadding() throws UnsupportedEncodingException {
        assertEncodeMatches("123", "MTIz");
        assertEncodeMatches("123456", "MTIzNDU2");
        assertEncodeMatches("123456789", "MTIzNDU2Nzg5");
    }

    @Test
    public void testEncodeAll8bitCharacters() throws UnsupportedEncodingException {

        String expectedStr = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIj"
                + "JCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZH"
                + "SElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWpr"
                + "bG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6P"
                + "kJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKz"
                + "tLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX"
                + "2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7"
                + "/P3+/w==";

        byte[] inBytes = new byte[256];
        byte[] outB64;
        String outB64Str;
        for (int i = 0; i < 256; i++) {
            inBytes[i] = (byte) i;
        }
        outB64 = Base64.encode(inBytes, inBytes.length);
        outB64Str = new String(outB64, "us-ascii");
        assertEquals(expectedStr, outB64Str);
    }

    // Decode Tests
    @Test
    public void testDecodeShouldHave2PaddingSymbols() throws UnsupportedEncodingException, PaddingException {
        assertDecodeMatches("1234", "MTIzNA==");
        assertDecodeMatches("1234567", "MTIzNDU2Nw==");
        assertDecodeMatches("123456789A", "MTIzNDU2Nzg5QQ==");
        assertDecodeMatches("1234", "M T I z N A = = ");
        assertDecodeMatches("1234567", "M T I z N D U 2 N w = =");
        assertDecodeMatches("123456789A", "M T I z N D U 2 N z  g 5 Q Q = =");
    }

    @Test
    public void testDecodeShouldHave1PaddingSymbols() throws UnsupportedEncodingException, PaddingException {
        assertDecodeMatches("12345", "MTIzNDU=");
        assertDecodeMatches("12345678", "MTIzNDU2Nzg=");
        assertDecodeMatches("123456789AB", "MTIzNDU2Nzg5QUI=");
        assertDecodeMatches("12345", "M T I z N D U = ");
        assertDecodeMatches("12345678", "M T I  zN D U 2  N z g =");
        assertDecodeMatches("123456789AB", "M  T\nI z N  D  U  2 N z g 5 Q U I =  ");
    }

    @Test
    public void testDecodeShouldHaveNoPadding() throws UnsupportedEncodingException, PaddingException {
        assertDecodeMatches("123", "MTIz");
        assertDecodeMatches("123456", "MTIzNDU2");
        assertDecodeMatches("123456789", "MTIzNDU2Nzg5");
        assertDecodeMatches("123", "M T I z  ");
        assertDecodeMatches("123456", "M  T I zN D U 2");
        assertDecodeMatches("123456789", "M T I z ND U2  N  zg5");
    }

    @Test
    public void testDecodeAll8bitCharacters() throws UnsupportedEncodingException, PaddingException {

        String inStr = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIj"
                + "JCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZH"
                + "SElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWpr"
                + "bG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6P"
                + "kJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKz"
                + "tLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX"
                + "2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7"
                + "/P3+/w==";

        byte[] bytesOut;
        byte[] bytesIn;
        byte[] expectedBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            expectedBytes[i] = (byte) i;
        }
        bytesIn = inStr.getBytes("us-ascii");
        bytesOut = Base64.decode(bytesIn, bytesIn.length);
        assertArrayEquals(expectedBytes, bytesOut);
    }

    @Test
    public void testGiantRandomBlock() throws PaddingException {
        Debug.nop();
        int i;
        byte[] inBytes = Debug.rndBytes(BIGBLOCKSIZE);
        byte[] b64Bytes = Base64.encode(inBytes, BIGBLOCKSIZE);
        byte[] decodedBytes = Base64.decode(b64Bytes, b64Bytes.length);
        int nBytesDecoded = decodedBytes.length;
        assertEquals(inBytes.length, nBytesDecoded);
        for (i = 0; i < nBytesDecoded; i++) {
            if (inBytes[i] != decodedBytes[i]) {
                fail("Error b64decoded failed to block mismatch");
            }
        }
    }

    @Test
    public void testLargeBlock() throws PaddingException {
        assertBigBlockMatches(1024);
        assertBigBlockMatches(1024 * 16);
        assertBigBlockMatches(1024 * 64);
        double startTime = getTimeOfDaySecs();
        assertBigBlockMatches(1024 * 1024);
        double stopTime = getTimeOfDaySecs();
        System.out.printf("Took %f seconds to encode decode 1 Meg of B64 data\n", stopTime - startTime);
    }

    private byte[] loopBytes(int nbytes) {
        byte[] bytes = new byte[nbytes];
        for (int i = 0; i < nbytes; i++) {
            bytes[i] = (byte) (i % 256);
        }
        return bytes;
    }

    private byte[] encodeBytes(byte[] bytesIn) {
        return Base64.encode(bytesIn, bytesIn.length);
    }

    private byte[] decodeBytes(byte[] bytesIn) throws PaddingException {
        return Base64.decode(bytesIn, bytesIn.length);
    }

    private void assertBigBlockMatches(int nbytes) throws PaddingException {
        byte[] expectedBytes = loopBytes(nbytes);
        byte[] encodedBytes = encodeBytes(expectedBytes);
        byte[] decodedBytes = decodeBytes(encodedBytes);
        assertArrayEquals(expectedBytes, decodedBytes);
    }

    private static void assertDecodeMatches(String outStr, String inStr) throws UnsupportedEncodingException, PaddingException {
        byte[] inBytes = inStr.getBytes("us-ascii");
        byte[] outBytes;
        outBytes = Base64.decode(inBytes, inBytes.length);
        assertEquals(outStr, new String(outBytes, "us-ascii"));
    }

    private static void assertEncodeMatches(String inStr, String outStr) throws UnsupportedEncodingException {
        byte[] bytesIn = inStr.getBytes("us-ascii");
        byte[] bytesOut;
        bytesOut = Base64.encode(bytesIn, bytesIn.length);
        assertEquals(outStr, new String(bytesOut, "us-ascii"));
    }

    double getTimeOfDaySecs() {
        return ((double) System.currentTimeMillis()) * secsPerMilli;
    }
}
