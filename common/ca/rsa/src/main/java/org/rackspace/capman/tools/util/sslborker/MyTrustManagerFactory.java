package org.rackspace.capman.tools.util.sslborker;

import java.security.KeyStore;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class MyTrustManagerFactory extends TrustManagerFactorySpi {

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
        return new TrustManager[]{new OverTrustingX509TrustManager()};
    }
}
