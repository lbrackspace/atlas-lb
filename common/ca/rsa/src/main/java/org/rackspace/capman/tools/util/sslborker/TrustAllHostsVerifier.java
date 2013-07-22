package org.rackspace.capman.tools.util.sslborker;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


public class TrustAllHostsVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String string, SSLSession ssls) {
            return true; // I trust all hosts
        }
    }