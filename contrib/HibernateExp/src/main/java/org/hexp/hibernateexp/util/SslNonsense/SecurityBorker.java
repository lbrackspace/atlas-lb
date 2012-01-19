package org.hexp.hibernateexp.util.SslNonsense;

import java.security.Security;

public class SecurityBorker {
    static{
        Security.addProvider(new OverTrustingTrustProvider());
        Security.setProperty("ssl.TrustManagerFactory.algorithm","TrustAllCertificates");
    }

    public static void bork(){
        // Unless you access a method the class static initializer won't be called
    }
}
