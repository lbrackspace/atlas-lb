package org.openstack.atlas.api.auth.integration.helpers;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public final class CustomHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}
