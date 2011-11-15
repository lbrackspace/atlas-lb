package org.openstack.atlas.api.auth;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// Needed to prevent cheap chain certs from breaking. when useing https
// protocol. Beware useing this class globally ignores HTTPS cert validation all
// together.
public class HttpsCertIgnore {

    private static final TrustManager[] trustAllCerts;
    private static final SSLContext sc;
    private static final HostnameVerifier hv;
    private static final Log LOG = LogFactory.getLog(HttpsCertIgnore.class);
    private static final Exception initException;

    static {
        Exception tinitException;
        SSLContext tsc;
        tinitException = null;
        trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // Trust always
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // Trust always
                        }
                    }
                };
        hv = new HostnameVerifier() {

            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };
        try {
            tsc = SSLContext.getInstance("SSL");
            tsc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(tsc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (KeyManagementException ex) {

            tsc = null;
            tinitException = ex;
        } catch (NoSuchAlgorithmException ex) {
            tsc = null;
            tinitException = ex;
        }
        sc = tsc;
        initException = tinitException;
    }

    public static TrustManager[] getTrustAllCerts() {
        return trustAllCerts;
    }

    public static SSLContext getSc() {
        return sc;
    }

    public static HostnameVerifier getHv() {
        return hv;
    }

    public static Log getLOG() {
        return LOG;
    }

    public static Exception getInitException() {
        return initException;
    }
}
