package org.openstack.atlas.util.crypto;

import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.util.crypto.exception.EncryptException;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class CryptoUtilTest {

    public static final Logger LOGGER = Logger.getLogger(CryptoUtilTest.class);

    public static final String[] UNICODE_NAMES = {"!@#$%^&*()_+{}:\"<>?|./;'[]\\-=`~", // Randomness (ASCII)"
            // +
            "Andrew Young", // English (ASCII)
            "Jos\u00E9 Antonio Calder\u00F3n-Iglesias", // Spanish
            // (ISO-8859-1)
            "Proven\u00E7al", // French (ISO-8859-1)
            "AD\u00C9LA\u00CFDE", // French (ISO-8859-1)
            "\u010Ca\u01D4", // Esperanto (ISO-8859-3). (Also uses
            // character composition)
            "J\u00FCrgen", // German (ISO-8859-1)
            "Gu\u00F0r\u00FAn Eva M\u00EDnervud\u00F3ttir", // Icelandic
            // (ISO-8859-1)
            // Now for some more exotic ones.
            "\u0399\u03B5\u03C1\u03B5\u03BC\u03AF\u03B1\u03C2", // Greek
            // (ISO-8859-7)
            "\u90ED\u674E\u5EFA\u592B", // Chinese (EUC-CN, Big5, GBK,
            // GB18030, etc)
            "\u753A\u7530\u307E\u3053\u3068", // Japanese (EUC-JP,
            // ISO-2022-JP, Shift-JIS,
            // etc)
            "\uC774\uB984 \uC131\uBA85", // Korean (EUC-KR, ISO-2022-KR,
            // etc)
            "Nguy\u1EC5n Ph\u1EA1m", // Vietnamese (VISCII)
            "\u0E2D\u0E07\u0E04\u0E4C\u0E40\u0E25\u0E47\u0E01", // Thai
            // (ISO-8859-11)
            // Ok, and now some RTL for the heck of it.
            "\u0645\u0646\u0635\u0648\u0631 \u062D\u0644\u0627\u062C", // Farsi
            // (Persian)
            // (ISO-8859-6)
            "\u05E6\u05D3\u05E7\u05D9\u05D4\u05D5", // Hebrew
            // (ISO-8859-8)
    };

    public static final String[] ENCRYPTED_NAMES = {
            "79a46b39151228e1dd6e582a34eeb551af7aae5425c91a524f2770b42dfb673f",
            "b9d260dfc4aa08fb8316b5752825e38b",
            "09d8f6e9587f12b219985238bc411179a152e40d98106c94e2c728218bb808c1",
            "007910ecfbc55eb68b04118ae1d8f3a4", "641e3a310db820f0538dc6e8e23486e6",
            "1a14749a9c5e586227853f386d82bfae", "537f574858604599c24eaef8edc7f37d",
            "2af8ea34695c608bbb18ed4da0011a61351b3d2d59f5de290fba0122a278ce37",
            "3434d3b63d75c564f01f7870aaf0905b", "b4d9d866faef5307ace1ddd3ad072059",
            "021a1bd68bea49d88212c19c41bde4b5", "8d820f0f148f452b4ac82ea86bab5425",
            "927ac46ea64e2c63bc4b159512e8d419",
            "0427a000f28d44fb5226cb20a9a3a9e70c7d368c17fd780115367a03cdbdc29c",
            "05e4c27e9fc2bef0b23afd6d2144059b0cfc770b3887c4b3a2c4ed84b3b91c3a",
            "ca50c98286b4ce789d6ba2a9942f1bf3"};

    @Test
    public void AsBytes() throws DecryptException {
        String testHex;
        byte[] actual;
        byte[] expected;
        byte notUsed = -1;

        testHex = "000000";
        expected = new byte[]{0, 0, 0};
        actual = CryptoUtil.asBytes(testHex, notUsed);
        assertByteArrays("3 byte min value", expected, actual);

        testHex = "ffffff";
        expected = new byte[]{-1, -1, -1};
        actual = CryptoUtil.asBytes(testHex, notUsed);
        assertByteArrays("3 byte max value", expected, actual);

        testHex = "101010";
        expected = new byte[]{16, 16, 16};
        actual = CryptoUtil.asBytes(testHex, notUsed);
        assertByteArrays("3 byte 16s place", expected, actual);

        testHex = "010101";
        expected = new byte[]{1, 1, 1};
        actual = CryptoUtil.asBytes(testHex, notUsed);
        assertByteArrays("3 byte 1s place", expected, actual);
    }

    @Test(expected = DecryptException.class)
    public void AsBytesShouldFailWhenGivenBadInput() throws DecryptException {
        String testHex;
        byte notUsed = -1;

        testHex = "bbb";
        CryptoUtil.asBytes(testHex, notUsed);
    }

    @Test
    public void AsHex() {
        byte[] testBytes;
        String actual;
        String expected;

        testBytes = new byte[]{0, 0, 0};
        expected = "000000";
        actual = CryptoUtil.asHex(testBytes);
        assertEquals("3 byte min value", expected, actual);

        testBytes = new byte[]{-1, -1, -1};
        expected = "ffffff";
        actual = CryptoUtil.asHex(testBytes);
        assertEquals("3 byte max value", expected, actual);

        testBytes = new byte[]{16, 16, 16};
        expected = "101010";
        actual = CryptoUtil.asHex(testBytes);
        assertEquals("3 byte 16s place", expected, actual);

        testBytes = new byte[]{1, 1, 1};
        expected = "010101";
        actual = CryptoUtil.asHex(testBytes);
        assertEquals("3 byte 1s place", expected, actual);
    }

    @Test
    public void EncryptedFormat() throws Exception {
        int failed = 0;
        for (int i = 0; i < ENCRYPTED_NAMES.length; i++) {
            String encrypted = ENCRYPTED_NAMES[i];
            try {
                String decrypted = CryptoUtil.decrypt(encrypted);
                if (!UNICODE_NAMES[i].equals(decrypted)) {
                    LOGGER.error(String.format("String changed during decryption.  Original: %s, New: %s",
                            UNICODE_NAMES[i], decrypted));
                    failed++;
                }
            } catch (DecryptException e) {
                LOGGER.error(String.format("Decryption Failed For '%s': %s", encrypted, e.getMessage()), e);
                failed++;
            }
        }
        assertEquals(String.format("%d Failures", failed), 0, failed);
    }

    @Test
    public void Unicode() throws Exception {
        int failed = 0;
        for (int i = 0; i < UNICODE_NAMES.length; i++) {
            String name = UNICODE_NAMES[i];
            try {
                String encrypted = CryptoUtil.encrypt(name);
                if (encrypted == null || !encrypted.equals(ENCRYPTED_NAMES[i])) {
                    LOGGER.error(String.format(
                            "Encrypted Format Has Changed.  Input: %s, Original: %s, New: %s", name,
                            ENCRYPTED_NAMES[i], encrypted));
                    failed++;
                }
                String decrypted = CryptoUtil.decrypt(encrypted);
                if (!name.equals(decrypted)) {
                    LOGGER.error(String.format(
                            "String changed during encryption/decryption.  Original: %s, New: %s", name,
                            decrypted));
                    failed++;
                }
            } catch (EncryptException e) {
                LOGGER.error(String.format("Encryption Failed For '%s': %s", name, e.getMessage()), e);
                failed++;
            } catch (DecryptException e) {
                LOGGER.error(String.format("Decryption Failed For '%s': %s", name, e.getMessage()), e);
                failed++;
            }
        }
        assertEquals(String.format("%d Failures", failed), 0, failed);
    }

    private void assertByteArrays(String message, byte[] expected, byte[] actual) {
        assertEquals("[" + message + "] arrays should be the same size", expected.length, actual.length);
        assertTrue("[" + message + "] should be equal", Arrays.equals(expected, actual));
    }
}
