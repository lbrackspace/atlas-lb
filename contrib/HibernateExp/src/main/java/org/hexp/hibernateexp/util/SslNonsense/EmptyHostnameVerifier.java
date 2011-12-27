package org.hexp.hibernateexp.util.SslNonsense;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;


public class EmptyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String string, SSLSession ssls) {
            return true; // I trust every one.
        }
    }