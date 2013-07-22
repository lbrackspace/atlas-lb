package org.rackspace.capman.tools.util;


import java.security.KeyPair;
import java.security.PrivateKey;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.rackspace.capman.tools.ca.RSAKeyUtils;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import org.rackspace.capman.tools.ca.primitives.RsaConst;

// Used for the ChainBuilder
public class X509ChainEntry {

    static {
        RsaConst.init();
    }
    private KeyPair key;
    private PKCS10CertificationRequest csr;
    private X509CertificateObject x509obj;

    public X509ChainEntry() {
    }

    public X509ChainEntry(KeyPair key, PKCS10CertificationRequest csr, X509CertificateObject x509obj) {
        this.key = key;
        this.csr = csr;
        this.x509obj = x509obj;
    }

    public KeyPair getKey() {
        return key;
    }

    public void setKey(KeyPair key) {
        this.key = key;
    }

    public PKCS10CertificationRequest getCsr() {
        return csr;
    }

    public void setCsr(PKCS10CertificationRequest csr) {
        this.csr = csr;
    }

    public X509CertificateObject getX509obj() {
        return x509obj;
    }

    public void setX509obj(X509CertificateObject x509obj) {
        this.x509obj = x509obj;
    }

    public String shortEntry() {
        X509Inspector xi;
        try {
            xi = new X509Inspector(x509obj);
        } catch (NotAnX509CertificateException ex) {
            return "null";
        }
        String subjName = xi.getSubjectName();
        String issuerName = xi.getIssuerName();
        String shortCrtKey = RSAKeyUtils.shortKey(x509obj.getPublicKey());
        String shortPrivKey = RSAKeyUtils.shortKey(key.getPrivate());
        PrivateKey privKey = key.getPrivate();
        String fmt = "{issuer=\"%s\" subj=\"%s\" privKey=\"%s\" crtKey=\"%s\"}";
        String msg = String.format(fmt,issuerName,subjName,shortPrivKey,shortCrtKey);
        return msg;
    }
}
