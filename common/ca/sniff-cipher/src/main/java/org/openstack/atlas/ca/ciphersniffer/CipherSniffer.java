package org.openstack.atlas.ca.ciphersniffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.util.ca.util.ConnectUtils;
import org.openstack.atlas.util.debug.Debug;

public class CipherSniffer {

    public static void usage(String prog) {
        System.out.printf("usage is %s <host> <port>\n", prog);
        System.out.printf("\n");
        System.out.printf("");
        return;
    }

    public static void main(String[] argv) {
        String prog = Debug.getProgName(CipherSniffer.class);
        if (argv.length < 2) {
            usage(prog);
            return;
        }
        String host = argv[0];
        int port;
        try {
            port = Integer.parseInt(argv[1]);

        } catch (NumberFormatException ex) {
            System.out.printf("Error could not parse \"%s\" into an integer\n", argv[1]);
            return;
        }
        try {
            List<String> allowedCiphers = ConnectUtils.getSupportedCiphers();
            List<String> acceptedCiphers = ConnectUtils.getServerCiphers(host, port);
            for (String allowedCipher : allowedCiphers) {
                System.out.printf("%s\n", allowedCipher);
            }
            System.out.printf("\n");
            System.out.printf("Server Ciphers prefered\n");
            for (String acceptedCipher : acceptedCiphers) {
                System.out.printf("%s\n", acceptedCipher);
            }
            System.out.printf("Supported ciphers:\n");

        } catch (Exception ex) {
            System.out.printf("Error retriving server cipher list\n%s\n", Debug.getExtendedStackTrace(ex));
            return;
        }
    }
}
