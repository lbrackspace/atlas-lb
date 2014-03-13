package org.openstack.atlas.util.ca.primitives;

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.util.X509Inspector;

public class RootIntermediateContainer<E extends X509Certificate> {

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.rootCAs != null ? this.rootCAs.hashCode() : 0);
        hash = 97 * hash + (this.intermediates != null ? this.intermediates.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RootIntermediateContainer<E> other = (RootIntermediateContainer<E>) obj;
        if (this.rootCAs != other.rootCAs && (this.rootCAs == null || !this.rootCAs.equals(other.rootCAs))) {
            return false;
        }
        if (this.intermediates != other.intermediates && (this.intermediates == null || !this.intermediates.equals(other.intermediates))) {
            return false;
        }
        return true;
    }
    private Set<E> rootCAs;
    private Set<E> intermediates;

    public RootIntermediateContainer() {
        rootCAs = new HashSet<E>();
        intermediates = new HashSet<E>();
    }

    public RootIntermediateContainer(Set<E> rootCAs, Set<E> intermediates) {
        this.rootCAs = new HashSet<E>(rootCAs);
        this.intermediates = new HashSet<E>(intermediates);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ROOTS:\n");
        for (X509Certificate x509 : rootCAs) {
            sb.append(CertUtils.certToStr(x509));
        }
        sb.append("Intermediates:\n");
        for (X509Certificate x509 : intermediates) {
            sb.append(CertUtils.certToStr(x509));
        }
        sb.append("total Roots: ").append(rootCAs.size()).append("\n").
                append("total Intermediates: ").append(intermediates.size()).append("\n");
        sb.append("hashCode: ").append(hashCode()).append("\n");
        return sb.toString();
    }

    public Set<E> getRootCAs() {
        return rootCAs;
    }

    public void setRootCAs(Set<E> rootCAs) {
        this.rootCAs = rootCAs;
    }

    public Set<E> getIntermediates() {
        return intermediates;
    }

    public void setIntermediates(Set<E> intermediates) {
        this.intermediates = intermediates;
    }

    public String showClassLoaders() {
        StringBuilder sb = new StringBuilder();
        for (E rootCa : rootCAs) {
            sb.append(Debug.classLoaderInfo(rootCa.getClass())).append("\n");
        }
        for (E imd : intermediates) {
            sb.append(Debug.classLoaderInfo(imd.getClass())).append("\n");
        }
        return sb.toString();
    }
}
