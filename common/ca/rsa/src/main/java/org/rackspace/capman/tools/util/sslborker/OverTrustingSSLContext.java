package org.rackspace.capman.tools.util.sslborker;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class OverTrustingSSLContext {

    private static SecureRandom secureRandom;
    private static TrustManager[] trustManagers = new TrustManager[]{new OverTrustingX509TrustManager()};

    static {
        secureRandom = new SecureRandom();
    }

    public static SSLContext newOverTrustingSSLContext(String inst) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance(inst);
        sc.init(null, trustManagers, secureRandom);
        return sc;
    }
}
