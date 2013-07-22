package org.rackspace.capman.tools.util.sslborker;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class OverTrustingX509TrustManager implements X509TrustManager {
    // Sure I believe you

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public int nop(int n) { // For Debugging
        return 0 - n;
    }
}
