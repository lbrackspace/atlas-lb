package org.openstack.atlas.util.ca.util;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.RsaConst;

// Nieve chain builder. Experiments suggest its useless for production dont use
@Deprecated
public class X509Chainer {

    static {
        RsaConst.init();
    }
    private Set<X509CertificateHolder> x509Certs;

    public X509Chainer() {
        x509Certs = new HashSet<X509CertificateHolder>();
    }

    // Nieve O(n) based search.
    public List<X509CertificateHolder> getNextIssuer(X509CertificateHolder subjectCert) {
        List<X509CertificateHolder> nextIssuer = new ArrayList<X509CertificateHolder>();
        PublicKey candidateKey;
        for (X509CertificateHolder candidateCrt : x509Certs) {
            try {
                candidateKey = (PublicKey) RSAKeyUtils.getBCRSAPublicKey(candidateCrt.getSubjectPublicKeyInfo());
                X509Certificate xc = CertUtils.getX509Certificate(subjectCert);
                xc.verify(candidateKey);

            } catch (CertificateException | NoSuchAlgorithmException
                    | InvalidKeyException | NoSuchProviderException
                    | SignatureException | RsaException
                    | NotAnX509CertificateException ex) {
                continue;
            }
            // Looks like we found a hit
            nextIssuer.add(candidateCrt);
        }
        return nextIssuer;
    }

    // Nieve pathBuilder that only looks at the key for a matching signing key
    // O(n*l) where l is the length of this chain and n is the number of certs in x509certs
    public List<X509CertificateHolder> buildPath(X509CertificateHolder userCert) {
        List<X509CertificateHolder> pathOut = new ArrayList<X509CertificateHolder>();
        X509Chainer chainer = new X509Chainer();
        chainer.getX509Certs().addAll(this.x509Certs);
        List<X509CertificateHolder> next = chainer.getNextIssuer(userCert);
        if (next.isEmpty()) {
            return pathOut;
        }
        while (next.size() > 0) {
            X509CertificateHolder nextX509 = next.get(0);
            pathOut.add(nextX509);
            chainer.getX509Certs().remove(nextX509); // We don't want to get stuck in a loop
            next = chainer.getNextIssuer(nextX509);
        }
        return pathOut;
    }

    public Set<X509CertificateHolder> getX509Certs() {
        return x509Certs;
    }

    public void setX509Certs(Set<X509CertificateHolder> x509Certs) {
        this.x509Certs = x509Certs;
    }
}
