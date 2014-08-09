package org.openstack.atlas.util.b64aes;

import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.debug.StringBuilderWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AesStatic {

    private static String getUsage(String prog) {
        StringBuilderWriter sbw = new StringBuilderWriter();
        sbw.printf("Usage is %s <key> <encrypt|decrypt> <ptext|ctext>\n", prog);
        sbw.printf("\n");
        sbw.printf("Encrypts or decrypts the above ctext or ptext\n");
        return sbw.toString();
    }

    public static void main(String... args) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeySpecException, BadPaddingException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, UnsupportedEncodingException, PaddingException {
        String prog = Debug.getProgName(AesStatic.class);
        if (args.length < 3) {
            System.out.printf("%s", getUsage(prog));
            return;
        }

        String key = args[0];
        String method = args[1];
        String itext = args[2];
        String otext;
        String htext;
        if (method.toLowerCase().equals("encrypt")) {
            otext = Aes.b64encrypt_str(itext, key);
            htext = Aes.bytesToHex(Aes.encrypt(itext.getBytes("UTF-8"), Aes.sha256(key)));
            System.out.printf("%s\n", otext);
            return;
        } else if (method.toLowerCase().equals("decrypt")) {
            otext = Aes.b64decrypt_str(itext, key);
            System.out.printf("%s\n", otext);
            return;
        } else {
            System.out.printf("Unknown method: %s please use encrypt or decrypt", method);
            return;
        }
    }
}
