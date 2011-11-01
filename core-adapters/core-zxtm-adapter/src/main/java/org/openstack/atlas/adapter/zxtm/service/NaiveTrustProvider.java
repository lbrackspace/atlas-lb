package org.openstack.atlas.adapter.zxtm.service;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;

/* The following code disables certificate checking.
 * Use the Security.addProvider and Security.setProperty
 * calls to enable it */
public final class NaiveTrustProvider extends Provider {
	
	 private static final long serialVersionUID = -656249890541787247L;
    public static final String TRUST_PROVIDER_ALG = "TrustAllCertificates";

    public NaiveTrustProvider() {
        super("NaiveTrustProvider", 1.0, "Trust self-signed certificates");
        put("TrustManagerFactory." + TRUST_PROVIDER_ALG,
                MyTrustManagerFactory.class.getName());
    }

    protected static class MyTrustManagerFactory extends TrustManagerFactorySpi {
        public MyTrustManagerFactory() {
        }

        @Override
        protected void engineInit(KeyStore keystore) {
            nop();
        }

        @Override
        protected void engineInit(ManagerFactoryParameters mgrparams) {
            nop();
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return new TrustManager[]{new MyX509TrustManager()};
        }

        private void nop(){
        }

    }

    protected static class MyX509TrustManager implements X509TrustManager {

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

        private void nop(){
        }
    }
}
