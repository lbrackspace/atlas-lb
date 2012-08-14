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

    private static final int PAGESIZE = 4096;

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

        String method = args[0];
        String itext = args[1];
        String key = args[2];
        String otext;
        if (method.toLowerCase().equals("encrypt")) {
            otext = Aes.b64encrypt(itext.getBytes(), key);
            System.out.printf("%s\n", otext);
            return;
        } else if (method.toLowerCase().equals("decrypt")) {
            otext = new String(Aes.b64decrypt(itext, key));
            System.out.printf("%s\n", otext);
            return;
        } else {
            System.out.printf("Unknown method: %s please use encrypt or decrypt", method);
            return;
        }
    }

    private static String getUsage(String prog) {
        StringBuilderWriter sbw = new StringBuilderWriter(PAGESIZE);
        sbw.printf("Usage is %s <encrypt|decrypt> <ptext|ctext> <key>\n", prog);
        sbw.printf("\n");
        sbw.printf("Encrypts or decrypts the above ctext or ptext\n");
        return sbw.toString();
    }
}
