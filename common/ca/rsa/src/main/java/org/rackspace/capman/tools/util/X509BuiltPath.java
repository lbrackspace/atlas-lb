
package org.rackspace.capman.tools.util;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class X509BuiltPath<E extends X509Certificate> {
    private List<E> path = new ArrayList<E>(); //
    private E root; // The discovered Root Crt

    public X509BuiltPath(List<E> path,E root){
        this.root = root;
        this.path = path;
    }

    public E getRoot() {
        return root;
    }

    public List<E> getPath() {
        return path;
    }
}
