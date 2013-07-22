package org.rackspace.capman.tools.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.rackspace.capman.tools.ca.primitives.RsaConst;

// Nieve chain builder. Experiments suggest its useless for production dont use
@Deprecated
public class X509Chainer {
    static {
        RsaConst.init();
    }
    private Set<X509Certificate> x509Certs;

    public X509Chainer() {
        x509Certs = new HashSet<X509Certificate>();
    }

    // Nieve O(n) based search.
    public List<X509Certificate> getNextIssuer(X509Certificate subjectCert) {
        List<X509Certificate> nextIssuer = new ArrayList<X509Certificate>();
        for (X509Certificate candidateCrt : x509Certs) {
            PublicKey candidateKey = (PublicKey) candidateCrt.getPublicKey();
            try {
                subjectCert.verify(candidateKey);
            } catch (CertificateException ex) {
                continue;
            } catch (NoSuchAlgorithmException ex) {
                continue;
            } catch (InvalidKeyException ex) {
                continue;
            } catch (NoSuchProviderException ex) {
                continue;
            } catch (SignatureException ex) {
                continue;
            }
            // Looks like we found a hit
            nextIssuer.add(candidateCrt);
        }
        return nextIssuer;
    }

    // Nieve pathBuilder that only looks at the key for a matching signing key
    // O(n*l) where l is the length of this chain and n is the number of certs in x509certs
    public List<X509Certificate> buildPath(X509Certificate userCert) {
        List<X509Certificate> pathOut = new ArrayList<X509Certificate>();
        X509Chainer chainer = new X509Chainer();
        chainer.getX509Certs().addAll(this.x509Certs);
        List<X509Certificate> next = chainer.getNextIssuer(userCert);
        if (next.isEmpty()) {
            return pathOut;
        }
        while (next.size() > 0) {
            X509Certificate nextX509 = next.get(0);
            pathOut.add(nextX509);
            chainer.getX509Certs().remove(nextX509); // We don't want to get stuck in a loop
            next = chainer.getNextIssuer(nextX509);
        }
        return pathOut;
    }

    public Set<X509Certificate> getX509Certs() {
        return x509Certs;
    }

    public void setX509Certs(Set<X509Certificate> x509Certs) {
        this.x509Certs = x509Certs;
    }
}
