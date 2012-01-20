package org.openstack.atlas.util.crypto;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstack.atlas.util.config.LbConfiguration;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.openstack.atlas.util.crypto.exception.EncryptException;

public class CommandLine {

    private static final int PAGESIZE = 4096;

    public static void main(String[] args) throws IOException {
        String prog = getProgName();
        String usage = getUsage(prog);
        String mode;
        String val;
        String result;
        if (args.length < 2) {
            System.out.printf("%s", usage);
            return;
        }

        mode = args[0];
        val = args[1];

        if (mode.toLowerCase().equals("encrypt")) {
            try {
                result = org.openstack.atlas.util.crypto.CryptoUtil.encrypt(val);
                System.out.printf("\n\n\nencrypted =\"%s\"\n", result);
                return;
            } catch (EncryptException ex) {
                System.out.printf("Could not encrypt %s\n\n", val);
                System.out.printf("%s", getExtendedStackTrace(ex));
                return;
            }
        } else if (mode.toLowerCase().equals("decrypt")) {
            try {
                result = org.openstack.atlas.util.crypto.CryptoUtil.decrypt(val);
                System.out.printf("\n\n\ndecrypted=\"%s\"\n", result);
                return;
            } catch (DecryptException ex) {
                System.out.printf("Could not decrypt %s\n\n", val);
                System.out.printf("%s", getExtendedStackTrace(ex));
                return;
            }
        } else {
            System.out.printf("You must specify eith encrypt or decrypt for arg 1\n");
            System.out.printf("\n");
            System.out.printf("%s", usage);
            return;
        }
    }

    public static String getUsage(String prog) {
        StringBuilder sb = new StringBuilder(PAGESIZE);
        sb.append(String.format("Usage is java -jar %s <encrypt|decrypt> <text>\n", prog));
        sb.append(String.format("\n"));
        sb.append(String.format("Encrypt or decrypt the passwd for the cluster based on the key "));
        sb.append(String.format("configured in your \"%s\" file\n\n", LbConfiguration.defaultConfigurationLocation));
        return sb.toString();
    }

    public static String getProgName() {
        int li;
        String sep;
        String path;
        String prog;
        URI uri;
        File file;
        try {
            uri = CommandLine.class.getProtectionDomain().
                    getCodeSource().
                    getLocation().
                    toURI();
            file = new File(uri);
            path = file.getAbsolutePath();
            sep = File.separator;
            li = path.lastIndexOf(sep) + 1;
            prog = path.substring(li, path.length());
        } catch (Exception ex) {
            prog = "prog";
        }
        return prog;
    }

    public static String getExtendedStackTrace(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder(PAGESIZE);
        Exception currEx;
        String msg;

        t = th;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                sb.append(String.format("Exception: %s:%s\n", currEx.getMessage(), currEx.getClass().getName()));
                for (StackTraceElement se : currEx.getStackTrace()) {
                    sb.append(String.format("%s\n", se.toString()));
                }
                sb.append("\n");
                t = t.getCause();
            }
        }
        return sb.toString();
    }
}
