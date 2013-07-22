package org.rackspace.capman.tools.util.sslborker;

import java.security.Security;
import javax.net.ssl.HttpsURLConnection;

// Usefull for testing when you need to connect to untrusted endpoints
// Read endpoints that didn't pay for a valid cert.
// Do not run bork() in production. Staging maby but not production.
public class SecurityBorker {


    private static int borkedCallCount = 0;
    private static boolean isBorked = false;

    public static void bork() {
        if (!isBorked) {
            Security.addProvider(new OverTrustingTrustProvider());
            Security.setProperty("ssl.TrustManagerFactory.algorithm", "TrustAllCertificates");
        }
        HttpsURLConnection.setDefaultHostnameVerifier(new TrustAllHostsVerifier());
        borkedCallCount++;
    }

    public static boolean getIsBorked() {
        return isBorked;
    }

    public static int getBorkCallCount() {
        return borkedCallCount;
    }
}
