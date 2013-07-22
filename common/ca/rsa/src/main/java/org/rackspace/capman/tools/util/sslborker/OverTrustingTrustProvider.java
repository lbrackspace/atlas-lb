package org.rackspace.capman.tools.util.sslborker;

import java.security.Provider;




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


}
