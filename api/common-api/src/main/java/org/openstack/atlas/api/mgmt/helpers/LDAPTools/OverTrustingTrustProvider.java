package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/* The following code disables certificate checking.
 * Use the Security.addProvider and Security.setProperty
 * calls to enable it */
public final class OverTrustingTrustProvider extends Provider {

    private static final long serialVersionUID = -656249890541787247L;

    public OverTrustingTrustProvider() {
        super("OverTrustingTrustProvider", 1.0, "Trust self-signed certificates");
        put("TrustManagerFactory.TrustAllCertificates",
                MyTrustManagerFactory.class.getName());
    }

    protected static class MyTrustManagerFactory extends TrustManagerFactorySpi {

        public MyTrustManagerFactory() {
        }

        @Override
        protected void engineInit(KeyStore keystore) {
        }

        @Override
        protected void engineInit(ManagerFactoryParameters mgrparams) {
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return new TrustManager[]{new MyX509TrustManager()};
        }
    }

    protected static class MyX509TrustManager implements X509TrustManager {
        // Sure I believe you

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            nop();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            nop();
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void nop(){ // For Debugging
            int i = -1;
            i ^= i;
        }
    }
}
