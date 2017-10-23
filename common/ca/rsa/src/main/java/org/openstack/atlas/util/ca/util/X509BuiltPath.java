
package org.openstack.atlas.util.ca.util;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.cert.X509CertificateHolder;

public class X509BuiltPath {
    private List<X509CertificateHolder> path = new ArrayList<X509CertificateHolder>(); //
    private X509CertificateHolder root; // The discovered Root Crt

    public X509BuiltPath(List<X509CertificateHolder> path,X509CertificateHolder root){
        this.root = root;
        this.path = path;
    }

    public X509CertificateHolder getRoot() {
        return root;
    }

    public List<X509CertificateHolder> getPath() {
        return path;
    }
}
